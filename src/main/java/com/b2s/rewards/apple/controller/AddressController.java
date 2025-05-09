package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CartAddressService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.common.constant.Constant;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * All cart address related calls reside in this controller
 *
 * @author rperumal
 */
@RestController
@RequestMapping(value="/address", produces = "application/json;charset=UTF-8")
@ResponseBody
public class AddressController {

    @Autowired
    CartAddressService addressService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AddressController.class);


    @RequestMapping(value = "/getStates", method = RequestMethod.GET)
    public ResponseEntity getStates(
        HttpServletRequest servletRequest)
        throws Exception {
        final User user = appSessionInfo.currentUser();
        logger.info("Successfully retrieved States ...  ");
        return new ResponseEntity(Constant.getStatesByCode(user.getCountry(), user.getState(), user.getLocale()), HttpStatus.OK);
    }

    @RequestMapping(value = "/cities", method = RequestMethod.GET)
    public ResponseEntity getCities(
        HttpServletRequest servletRequest)
        throws Exception {

        final User user = appSessionInfo.currentUser();
        logger.info("Successfully retrieved Cities ...  ");
        return new ResponseEntity(Constant.getCitiesByCountryLocale(user.getCountry(), user.getLocale()), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCartAddress", method = RequestMethod.GET)
    public ResponseEntity<Address> getCartAddress(
        HttpServletRequest servletRequest)
        throws Exception {

        final User user = appSessionInfo.currentUser();
        //Address can never be NULL, as this will be accessed only after getCart
        final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        logger.info("Successfully retrieved Shipping Address...  ");
        return new ResponseEntity(addressService.getAddress(getCartFromSession(servletRequest), user, program), HttpStatus.OK);
    }

    /**
     * Update cart Address and contact information
     * Assumption: Only one cart per user. Multi-cart is deferred for next release
     * @param servletRequest
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/modifyCartAddress", method = RequestMethod.POST)
    public ResponseEntity<Object> modifyCartAddress(
        final HttpServletRequest servletRequest, final HttpServletResponse response)
        throws Exception {
        // Set cache control headers
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        try {
            Gson gson = new Gson();
            Map<String, String> errorMessage = new HashMap<String, String>();
            final User user = appSessionInfo.currentUser();
            //validate JSON format
            final LinkedTreeMap newShippingInformation =
                gson.fromJson(IOUtils.toString(servletRequest.getInputStream(), Charset.defaultCharset()), LinkedTreeMap.class);
            final Map<String, Object> newShippingAddress =
                (Map<String, Object>) newShippingInformation.get(CartItemOption.SHIPPING_ADDRESS.getValue());

            final Map<String, Object> cleanShippingAddress = newShippingAddress.entrySet()
                .stream()
                .collect(HashMap::new, (map, v) -> map.put(v.getKey(),
                    getCleanShippingAddress(v.getValue())), HashMap::putAll);

            Cart sessionCart =
                (Cart) servletRequest.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
            sessionCart.setAddressError(true);
            Program program =
                (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            //For Phase 1: Shipping country  and user country country has to be the same. In other words,
            // international shipping is not allowed.
            Address newAddress = new Address();
            BeanUtils.populate(newAddress, Collections.unmodifiableMap(cleanShippingAddress));
            if (!StringUtils.isEmpty(getCountry(user)) && !StringUtils.isEmpty(newAddress.getCountry()) &&
                !getCountry(user).equals(newAddress.getCountry())) {
                errorMessage.put("country",
                    "International shipping is not allowed. User Country and ShipTo country did not match");
                sessionCart.getShippingAddress().setErrorMessage(errorMessage);
                logger.error(
                    "Modify address did not go through. International shipping is not allowed. User Country and " +
                        "ShipTo country did not match");
                return ResponseEntity.badRequest().body(sessionCart.getShippingAddress());
            }
            final double preAddToCartTotal = CartCalculationUtil.getDisplayCartTotalAmount(sessionCart);
            sessionCart = addressService.updateShippingInformation(sessionCart, cleanShippingAddress, user, program);
            if (sessionCart.isAddressError()) {
                logger.error("Address Validation did not go through. Invalid address {}",newAddress );
                AppleUtil.encodeAddressFields(sessionCart.getNewShippingAddress());
                return ResponseEntity.badRequest().body(sessionCart.getNewShippingAddress());
            }
            user.setShipTo(sessionCart.getShippingAddress());
            updateShippingAddressCartTotalModifiedFlag(sessionCart, program, preAddToCartTotal);

            //update cart to session
            servletRequest.getSession().setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, sessionCart);
            logger.info("Successfully modified Shipping Address...  " );

            boolean deceasedUserCheck =
                BooleanUtils.toBoolean((Boolean) program.getConfig().get(CommonConstants.DECEASED_USER_CHECK));

            if (deceasedUserCheck
                    && user.isDeceased()
                    && Objects.nonNull(sessionCart.getShippingAddress())) {
                user.setFirstName(sessionCart.getShippingAddress().getFirstName());
                user.setLastName(sessionCart.getShippingAddress().getLastName());
            }

            return getShippingAddressBasedOnWarningAndErrors(sessionCart);


        } catch ( UnknownHostException uhex) {
            logger.error("Exception while validating Address. Melissa Service may be down.....", uhex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Exception while validating Address. Melissa Service may be down....");
        } catch (ServiceException se) {
            logger.error("Modify address did not go through... " , se);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se.getMessage());
        } catch (final Exception se) {
            logger.error("Exception in updating contact information in cart... " , se);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR).body(
                (!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " :
                    "Exception in updating contact information in cart... "));
        }
    }

    private ResponseEntity<Object> getShippingAddressBasedOnWarningAndErrors(final Cart sessionCart) {
        if (sessionCart.getNewShippingAddress().getWarningMessage().isEmpty() &&
            sessionCart.getNewShippingAddress().getErrorMessage().isEmpty()) {
            if (null != sessionCart.getNewShippingAddress().getIgnoreSuggestedAddress() &&
                sessionCart.getNewShippingAddress().getIgnoreSuggestedAddress().equalsIgnoreCase("true")) {
                sessionCart.getShippingAddress().getErrorMessage().clear();
                sessionCart.getShippingAddress().getWarningMessage().clear();
            }
            return ResponseEntity.ok(sessionCart.getShippingAddress());
        } else if (!sessionCart.getNewShippingAddress().getWarningMessage().isEmpty() &&
            sessionCart.getIsAddressChanged().equalsIgnoreCase(
            CommonConstants.YES_VALUE)) {
            sessionCart.getNewShippingAddress().setAddressModified(CommonConstants.YES_VALUE);
            return ResponseEntity.ok(sessionCart.getNewShippingAddress());
        } else {
            AppleUtil.encodeAddressFields(sessionCart.getNewShippingAddress());
            return ResponseEntity.ok(sessionCart.getNewShippingAddress());
        }
    }

    private void updateShippingAddressCartTotalModifiedFlag(final Cart sessionCart, final Program program, final double preAddToCartTotal) {
        final double currentCartTotal = CartCalculationUtil.getDisplayCartTotalAmount(sessionCart);
        final Object disableCartTotalModifiedPopUp =
            program.getConfig().get(CommonConstants.DISABLE_CART_TOTAL_MODIFIED_POP_UP);

        if(disableCartTotalModifiedPopUp==null || !(boolean)disableCartTotalModifiedPopUp){
            sessionCart.getShippingAddress()
                .setCartTotalModified(CartCalculationUtil.isCartTotalModified(preAddToCartTotal, currentCartTotal));
        }
    }

    //get cart information from current session
    private Cart getCartFromSession(HttpServletRequest servletRequest) {
        //get cart from current session
        return (Cart) servletRequest.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
    }

    private Object getCleanShippingAddress(final Object value) {
        if (value instanceof String) {
            return XSSRequestWrapper.cleanXSS(value.toString());
        }
        return value;
    }

    private String getCountry(final User user) {
        if (CollectionUtils.isNotEmpty(user.getAddresses())) {
            return user.getAddresses().get(0).getCountry();
        }
        return user.getCountry();
    }
}