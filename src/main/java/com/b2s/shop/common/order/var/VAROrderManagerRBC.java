package com.b2s.shop.common.order.var;

import com.b2s.apple.services.PricingModelService;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.RedemptionError;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.PaymentOption;
import com.b2s.rewards.apple.model.PricingModel;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.util.USER_MSG;
import com.google.gson.Gson;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.Integer.valueOf;

@Component("varOrderManagerRBC")
public class VAROrderManagerRBC extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerRBC.class);

    @Autowired
    private PricingModelService pricingModelService;

    private static final String VAR_ID = "RBC";
    private static final String DEFAULT_STATE = "AB";
    private static final String PREVIOUS_QUEBEC_STATE = "PQ";
    private static final String CURRENT_QUEBEC_STATE = "QC";
    private static final Map<String, String> ASSERTION_USER_FIELDS_MAP = new HashMap<String, String>();
    private static final String PROGRAM_ATTR_NAME = "programId";
    private static final String REFERER_REGEX = "referer=";

    static {
        ASSERTION_USER_FIELDS_MAP.put("firstName", "firstName");
        ASSERTION_USER_FIELDS_MAP.put("lastName", "lastName");
        ASSERTION_USER_FIELDS_MAP.put("email", "email");
        ASSERTION_USER_FIELDS_MAP.put("addr1", "addr1");
        ASSERTION_USER_FIELDS_MAP.put("addr2", "addr2");
        ASSERTION_USER_FIELDS_MAP.put("city", "city");
        ASSERTION_USER_FIELDS_MAP.put("zip", "zip");
        ASSERTION_USER_FIELDS_MAP.put("state", "state");
        ASSERTION_USER_FIELDS_MAP.put("country", "country");
        ASSERTION_USER_FIELDS_MAP.put("varId", "varId");
        ASSERTION_USER_FIELDS_MAP.put("phone", "phone");
        ASSERTION_USER_FIELDS_MAP.put("proxyUserId", "proxyUserId");
        ASSERTION_USER_FIELDS_MAP.put("csid", "csid");
    }


    @Override
    public User selectUser(final HttpServletRequest httpServletRequest) throws B2RException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final User user;
        CommonConstants.LoginType loginType = null;
        if (anonymousLogin(request)) {
            user = populateAnonymousLoginInfo(request);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        } else if (request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) == null) {
            user = selectLocalUser(request, new User());
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            loginType = CommonConstants.LoginType.SAML;
            user = new User();

            logSamlInformation(request, user);
            switchAddressFields(user);

            //Set the value to empty string if phone is not 9999999999 format
            resetPhoneNumber(user);
            //
            if(!StringUtils.isBlank(user.getState()) && PREVIOUS_QUEBEC_STATE.equalsIgnoreCase(user.getState())){
                user.setState(CURRENT_QUEBEC_STATE);
            }

            setUserId(request, user);

            user.setProgramId((String)request.getAttribute(PROGRAM_ATTR_NAME));
        }
        user.setLoginType(loginType.getValue());
        initializeLocaleDependents(request, user);

        //Select program information from database
        Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        if (Objects.isNull(program)) {
            program = getProgram(user);
            request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        }

        // Set the Bag Menu URLs from Program Config
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);
        setKeepAliveRequestUrl(request);
        retrieveUserAdditionalInfoWithProgramId(user);
        setCatalogIdPromoOffer(user, program);

        // Add pricing models
        List<PricingModel> pricingModels = pricingModelService.getPricingModels(program.getVarId(), program.getProgramId(), CommonConstants.PRICE_TYPE_USER_COST);
        program.setPricingModels(pricingModels);

        getUserPointBalance(request, user, program);
        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);
        prepareUserAddress(user);
        return user;
    }

    private void setUserId(final XSSRequestWrapper request, final User user) {
        if(request.getUserPrincipal() == null || StringUtils.isBlank(request.getUserPrincipal().getName())) {
            LOGGER.error("User id(request.getUserPrincipal().getName()) is blank.");
            throw new IllegalArgumentException("User id(request.getUserPrincipal().getName()) is blank.");
        } else {
            user.setUserId(request.getUserPrincipal().getName());
        }
    }

    private User populateAnonymousLoginInfo(final XSSRequestWrapper request) {
        final User user;
        user = updateUser(request, new User(), CommonConstants.COUNTRY_CODE_CA, true);

        // for anonymous order detail request, discount code is not applicable
        if (StringUtils.isBlank(request.getParameter("uid"))) {
            // apply discount code from URL. If not exist redirect to error page.
            applyDiscountFromUrl(request, user);
            if (CollectionUtils.isEmpty(user.getDiscounts()) || user.getDiscounts().size() == 0) {
                request.setAttribute(CommonConstants.LOCALE, user.getLocale().toString());
                throw new IllegalArgumentException(CommonConstants.INVALID_DISCOUNT_CODE);
            }
        }
        return user;
    }

    private static void switchAddressFields(User user) {
        //RBC has INVALID address data, they requested B2S to switch the Addr2 to Addr1 if Addr1 field is empty
        if(StringUtils.isBlank(user.getAddr1())){
            user.setAddr1(user.getAddr2());
            user.setAddr2("");
        }
    }

    private static void logSamlInformation(HttpServletRequest request, User user) {
        LOGGER.info("Printing SAML request attributes and values......START");
        for (final Map.Entry<String, String> entry : ASSERTION_USER_FIELDS_MAP.entrySet()) {
            try {
                if (PropertyUtils.isWriteable(user, entry.getValue())) {
                    PropertyUtils.setProperty(user, entry.getValue(), request.getAttribute(entry.getKey()));
                    LOGGER.info("{} - {}",entry.getKey(), request.getAttribute(entry.getKey()));
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Access not allowed error while Setting the values on the user object", e);
            } catch (InvocationTargetException e) {
                LOGGER.error("Could not invoke setter error while Setting the values on the user object", e);
            } catch (NoSuchMethodException e) {
                LOGGER.error("No set method error while Setting the values on the user object", e);
            }
        }
        LOGGER.info("programId - {}", request.getAttribute(PROGRAM_ATTR_NAME));
        LOGGER.info("browseOnly - {}", request.getAttribute("browseOnly"));
        LOGGER.info("locale - {}", request.getAttribute("locale"));
        LOGGER.info("proxy User Id - {}", request.getAttribute(CommonConstants.PROXY_USER_ID));
        LOGGER.info("Printing SAML request attributes and values......END");
    }

    private void resetPhoneNumber(User user) {
        //Set the value to empty string if phone is not 9999999999 format
        if(StringUtils.isBlank(user.getPhone()) || !StringUtils.isNumeric(user.getPhone()) || user.getPhone().length()!=10){
            user.setPhone("");
        }
    }

    private void getUserPointBalance(XSSRequestWrapper request, User user, Program program) {
        if(Objects.nonNull(request.getParameter(CommonConstants.RELAY_STATE))) {
            try {
                final int pointsBalance = getUserPointBalance(user, program, request);
                LOGGER.info("Points balance from VIS for user with userId: {}, first name: {} and last name: {} is {}", user.getUserId(), user.getFirstName(), user.getLastName(), pointsBalance);
                user.setPoints(pointsBalance);
            } catch(Exception e) {
                LOGGER.error("Error while getting user balance for user id: {}, having program id: {} and var id: {}.", user.getUserId(), user.getProgramId(), user.getVarId());
                throw new IllegalArgumentException("User balance call failed with message: "+e.getMessage());
            }
        }
    }

    private void setCatalogIdPromoOffer(User user, Program program) {
        if (MapUtils.isNotEmpty(program.getConfig())) {
            Boolean promotionalOffer = (Boolean) program.getConfig().get(CommonConstants.PROMOTIONAL_OFFER);
            if (promotionalOffer != null && promotionalOffer) {
                if (user.getAdditionalInfo() == null) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                user.getAdditionalInfo().put(CommonConstants.PROMOTIONAL_OFFER, String.valueOf(promotionalOffer));
            }
        }
    }

    private void setKeepAliveRequestUrl(XSSRequestWrapper request) {
        if (Objects.nonNull(request.getParameterValues(CommonConstants.RELAY_STATE))) {
            final String relayState = request.getParameterValues(CommonConstants.RELAY_STATE)[0];
            if (relayState.contains(REFERER_REGEX)) {
                final String referer = relayState.split("\\?")[1].split(REFERER_REGEX)[1];
                if (referer != null && request.getAttribute(CommonConstants.LOCALE) != null) {
                    final URI initialReferer = URI.create(referer);
                    final String initialPath = initialReferer.getPath();
                    final String contextPath = initialPath.substring(1).split("/")[0];
                    final String keepAlivePath = "/" + String.join("/", contextPath, "rbc-keepalive");
                    final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
                    final URI keepAliveURI;
                    try {
                        keepAliveURI = new URI(initialReferer.getScheme(), initialReferer.getHost(), keepAlivePath, null);
                        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE, keepAliveURI.toString());
                    } catch (final URISyntaxException e) {
                        LOGGER.error("Unable to build keep alive base url {}", e);
                    }

                    final String keystoneBaseUrl = CommonConstants.getRequestAttribute(request, ExternalUrlConstants.KEYSTONE_BASE_URL);
                    LOGGER.info("RBC SAML keystoneBaseUrl ---- {}", keystoneBaseUrl);
                    setKeystoneUrls(externalUrls, keystoneBaseUrl);

                    request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
                }
            }
        }
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            final RedemptionResponse redemptionResponse = getRedemptionResponse(order, user, program);

            //assign/update order with the redemption response
            if (redemptionResponse == null) {
                return false;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            //adjust user points
            getBalanceUserPoints(order, user);
            message = getMessage(redemptionResponse);

        } catch (final Exception ex) {
            placeOrderRBCExceptionHandler(order, user, ex);
        }

        return message.isSuccess();
    }

    private void placeOrderRBCExceptionHandler(Order order, User user, Exception ex) {
        LOGGER.error("Error updating user Var details in VarIntegrationService...Exception trace:  ", ex.getMessage(), ex);
        message = new Message();

        //noinspection deprecation
        final String tst = messageService.getMessage(user.getVarId(), user.getProgramId(), -1, USER_MSG.ORDER_IN_PROCESS);
        if (Objects.nonNull(order)) {
            if (StringUtils.isBlank(order.getVarOrderId())) {
                order.setVarOrderId(tst);
            }
            message.setContentText("Error in VIS OrderRedemption process for Order: " + order.getOrderId());
        }else {
            message.setContentText("Error in VIS OrderRedemption process " );
        }
        if(StringUtils.isNotBlank(ex.getMessage())) {
            redemptionErrorHandler(ex);

        }
        message.setSuccess(false);
        if (order != null) {
            message.setVAROrderId(order.getVarOrderId());
            messageService.insertMessageException(user, this.getClass().getName(), "ERROR: Sending Order: " + order.getOrderId(), null);
        }
        message.setCode(OrderCodeStatus.FAIL.getValue());
    }

    private void redemptionErrorHandler(Exception ex) {
        RedemptionError redemptionError = new Gson().fromJson(ex.getMessage(), RedemptionError.class);
        if(redemptionError != null && StringUtils.isNotBlank(redemptionError.getMessage())) {
            String declineReasonCodePrefix = "decline reason code: ";
            int idx = redemptionError.getMessage().indexOf(declineReasonCodePrefix);
            if(idx != -1) {
                String declineReasonCode = redemptionError.getMessage().substring(idx + declineReasonCodePrefix.length());
                if(CommonConstants.RBC_PROMO_ALREADY_USED_DECLINE_REASON_CODE.equals(declineReasonCode)) {
                    message.setPromotionUseExceeded(true);
                }
            }
        }
    }

    /**
     * This method is used to get User points. It has logic to avoid user balance is low issue by returning payment max limit of var program payment option.
     * Do not use this method to populate user points in User object.
     * @param user current user
     * @param program
     * @return
     * @throws B2RException
     */

    @Override
    public int getUserPoints(final User user, final Program program) throws B2RException {
        int userPoints = 0;
        if(program != null && CollectionUtils.isNotEmpty(program.getPayments())) {
            boolean rewardsProgram = program.getPayments().stream().anyMatch(paymentOption -> CommonConstants.PaymentOption.POINTS.toString().equals(paymentOption.getPaymentOption()));
            if(rewardsProgram) {
                if(program.getIsLocal()) {
                    userPoints = varIntegrationServiceLocalImpl.getLocalUserPoints(user);
                } else {
                    final Optional<String> sid = Optional.ofNullable(user.getSid());
                    userPoints = varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
                }
            } else {
                final Optional<PaymentOption> cashPaymentOption = program.getPayments().stream()
                    .filter(paymentOption -> CommonConstants.PaymentOption.CASH.name().equals(paymentOption.getPaymentOption()))
                    .findFirst();
                userPoints = cashPaymentOption.map(PaymentOption::getPaymentMaxLimit).map(Double::intValue).orElse(0);
            }
        }
        return userPoints;
    }


    /**
     * Do use this method to populate user points to set in user object.
     * @param user
     * @param program
     * @return
     * @throws B2RException
     */
    private int getUserPointBalance(final User user, final Program program, final HttpServletRequest request) throws B2RException {
        int userPoints = 0;
        if(program != null && CollectionUtils.isNotEmpty(program.getPayments())) {
            boolean rewardsProgram = program.getPayments().stream().anyMatch(paymentOption -> CommonConstants.PaymentOption.POINTS.toString().equals(paymentOption.getPaymentOption()));
            if (rewardsProgram) {
                if (program.getIsLocal()) {
                    if (CommonConstants.SAML.equalsIgnoreCase(user.getLoginType()) &&
                        Objects.nonNull(request.getAttribute(CommonConstants.POINTS_BALANCE))) {
                        return valueOf(CommonConstants.getRequestAttribute(request, CommonConstants.POINTS_BALANCE));
                    } else {
                        userPoints = varIntegrationServiceLocalImpl.getLocalUserPoints(user);
                    }
                } else {
                    final Optional<String> sid = Optional.ofNullable(user.getSid());
                    userPoints = varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
                }
            }
        }
        return userPoints;
    }

    /**
     * This method simply serves to extract some locale setting logic that get pushed into various objects.
     * @param request
     * @param user
     */
    private void initializeLocaleDependents(HttpServletRequest request, User user) {
        final String country = CommonConstants.COUNTRY_CODE_CA;

        //SPECIAL HANDLING TO FORCE THE USER COUNTRY TO THE CATCOUNTRY THAT COMES IN THE SAML URL PARAMETER
        if(!country.equalsIgnoreCase(user.getCountry())){
            user.setAddr1(null);
            user.setAddr2(null);
            user.setCity(null);
            user.setZip(null);
            user.setState(DEFAULT_STATE);
            user.setCountry(country);
        }
        user.setLocale(getLocale(request, CommonConstants.LOCALE_EN_CA));
    }

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

}