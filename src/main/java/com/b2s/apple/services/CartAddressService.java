package com.b2s.apple.services;

import com.b2r.util.address.MelissaDataRules;
import com.b2r.util.address.StreetAddress;
import com.b2r.util.address.StreetAddressRule;
import com.b2r.util.address.melissadata.GlobalAddress;
import com.b2r.util.address.melissadata.GlobalAddressResponse;
import com.b2r.util.address.melissadata.GlobalAddressValidator;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.rewards.apple.validator.AddressValidatorIF;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.var.UserChase;
import com.b2s.shop.util.VarProgramConfigHelper;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.*;

/**
 * Created by rperumal on 6/28/2015.
 */
@Service
public class CartAddressService {

    private static final Logger logger = LoggerFactory.getLogger(CartAddressService.class);
    public static final String SELECTED_ADDRESS_ID = "selectedAddressId";
    @Autowired
    protected MessageSource messageSource;

    @Autowired
    @Qualifier("LocalPricingServiceV2")
    private LocalPricingServiceV2 pricingServiceV2;

    @Autowired
    private GlobalAddressValidator globalAddressValidator;

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    @Value("${melissadata.global.av.customerid}")
    private String melissaDataGlobalAVCustomerId;

    private boolean checkAddressEqual(Address address, Address userAddress) {
        return isStringsEqualIgnoreCase(userAddress.getAddress1(), address.getAddress1()) &&
            isStringsEqualIgnoreCase(userAddress.getAddress2(), address.getAddress2()) &&
            isStringsEqualIgnoreCase(userAddress.getAddress3(), address.getAddress3()) &&
            isStringsEqualIgnoreCase(userAddress.getCity(), address.getCity()) &&
            isStringsEqualIgnoreCase(userAddress.getState(), address.getState()) &&
            isStringsEqualIgnoreCase(userAddress.getCountry(), address.getCountry()) &&
            isStringsEqualIgnoreCase(userAddress.getZip4(), address.getZip4()) &&
            isStringsEqualIgnoreCase(userAddress.getZip5(), address.getZip5());
    }

    /**
     * Updates cart address information and saves it ot session
     *
     * @param updateShippingInformation  This has address information to be updated to cart
     * @throws ServiceException
     */
    public Cart updateShippingInformation(Cart sessionCart, Map<String, Object> updateShippingInformation, User user, Program program) throws Exception {
        try {
            //NOTE: Address and Email changes are mutually exclusive
            //Address validation
            if (MapUtils.isNotEmpty(updateShippingInformation)) {
                final Address newShippingAddress = getNewShippingAddress(updateShippingInformation, sessionCart, user, program);
                //limit recipient Name length
                AddressMapper.limitRecipientNameSize(newShippingAddress);
                if (newShippingAddress != null) {
                    final AddressValidatorIF addressValidator =
                        AddressMapper.getValidatorForCountry(newShippingAddress.getCountry());

                    final boolean hasValidationError =
                        addressValidator.hasValidationError(newShippingAddress, messageSource, user);
                    if (hasValidationError) {
                        logger.info("Country Specific validation failed for the given address: {}", newShippingAddress);
                    }
                    sessionCart.setAddressError(hasValidationError);

                    //validate if restricted shipping address fields are getting updated
                    validateRestrictedShippingAddress(sessionCart, user, program, newShippingAddress, addressValidator);

                    executeMelissaRules(sessionCart, user, program, newShippingAddress);
                }
                validateEmailChange(sessionCart, user, newShippingAddress);
                validateAddressChange(sessionCart, user, program, newShippingAddress);

                if (!sessionCart.isAddressError() && newShippingAddress.getErrorMessage() != null
                        && newShippingAddress.getErrorMessage().isEmpty()) {
                    logger.info("New P$ Request flow(updateShippingInformation) starts");
                    pricingServiceV2.calculateCartPrice(sessionCart, user, program);
                    logger.info("Service call with new P$ is completed");
                } else if (newShippingAddress.equals(sessionCart.getShippingAddress())) {
                    sessionCart.setShippingAddress(newShippingAddress);
                }
                sessionCart.setNewShippingAddress(newShippingAddress);
            }
            AppleUtil.encodeAddressFields(sessionCart.getShippingAddress());
        }catch ( UnknownHostException uhex) {
            logger.error("Exception while validating Address with Melissa Service....", uhex);
            throw new UnknownHostException("Exception while validating Address with Melissa Service....");
        } catch(ServiceException se) {
            logger.error("Failed to Modify Shipping Address...", se.getMessage(), se);
            throw new ServiceException(ServiceExceptionEnums.ITEM_NO_LONGER_AVAILABLE_EXCEPTION, se);
        }catch ( Exception ex) {
            logger.error("Exception while updating ShippingAddress to Cart", ex);
            throw ex;
        }
        return sessionCart;
    }

    private void executeMelissaRules(Cart sessionCart, User user, Program program, Address newShippingAddress) throws Exception {
        boolean hasMelissaAddressError;
        if (!sessionCart.isAddressError()) {
            if (!program.isSkipAddressValidation()) {
                //transform to street address
                hasMelissaAddressError = validateMelissaRules(user, program, newShippingAddress);
                logger.debug("Has Melissa Address Error : {​​}​​", hasMelissaAddressError);
            }
            populateUserAdditionalInfo(sessionCart, user, program, newShippingAddress);
        }
    }

    private void populateUserAdditionalInfo(Cart sessionCart, User user, Program program, Address newShippingAddress) {
        if(newShippingAddress.getErrorMessage().isEmpty()) {
            user.setShipTo(sessionCart.getShippingAddress());

            /**
             * Ranjith - 6/30/2017
             * Collection.emptyMap returns an immutable map. put operation will throw unsupportedoperationexception.
             * So changing to new HashMap.
             */
            final Map<String,String> additionalInfo = new HashMap<>();
            if(MapUtils.isNotEmpty(user.getAdditionalInfo())) {
                additionalInfo.putAll(user.getAdditionalInfo());
            }
            additionalInfo.put(CommonConstants.IGNORE_SUGGESTED_ADDRESS, newShippingAddress.getIgnoreSuggestedAddress());
            user.setAdditionalInfo(additionalInfo);
            setNewShippingAddressToCart(newShippingAddress, sessionCart, program);
            sessionCart.getShippingAddress().setFirstName(AppleUtil.decodeSpecialChar(sessionCart.getShippingAddress().getFirstName()));
            sessionCart.getShippingAddress().setLastName(AppleUtil.decodeSpecialChar(sessionCart.getShippingAddress().getLastName()));
            if (CollectionUtils.isNotEmpty(user.getAddresses())) {
                sessionCart.setShippingAddress(newShippingAddress);
            }
        }
    }

    private void validateAddressChange(Cart sessionCart, User user, Program program, Address newShippingAddress) {
        if (checkAddressEqual(newShippingAddress, AddressMapper.getAddress(user,program))) {
            sessionCart.setIsAddressChanged(CommonConstants.NO_VALUE);
        } else {
            sessionCart.setIsAddressChanged(CommonConstants.YES_VALUE);
        }
    }

    private void validateEmailChange(Cart sessionCart, User user, Address newShippingAddress) {
        if (newShippingAddress.getEmail().equalsIgnoreCase(user.getEmail())) {
            sessionCart.setIsEmailChanged(CommonConstants.NO_VALUE);
        } else {
            sessionCart.setIsEmailChanged(CommonConstants.YES_VALUE);
        }
    }

    private void validateRestrictedShippingAddress(Cart sessionCart, User user, Program program,
        Address newShippingAddress, AddressValidatorIF addressValidator) {
        if (CollectionUtils.isEmpty(user.getAddresses()) && !sessionCart.isAddressError()) {
            addressValidator.replaceSpecialCharWithEmpty(sessionCart.getShippingAddress(), user.getLocale().toString());

            final boolean isInvalidAddressUpdate =
                addressValidator.isInvalidAddressUpdate(sessionCart.getShippingAddress(), newShippingAddress, program);
            if (isInvalidAddressUpdate) {
                logger.error("Address Locked: Failed to update Invalid Address: {}", newShippingAddress);
            }
            sessionCart.setAddressError(isInvalidAddressUpdate);
        }
    }

    public Address setNewShippingAddressToCart(final Address newShippingAddress, final Cart cart, final Program program){

        boolean addressLocked = getProgramConfigValueAsBoolean(program, "MercAddressLocked");
        boolean contactInfoLocked = getProgramConfigValueAsBoolean(program, "ContactInfoLocked");
        boolean shipToNameLocked = getProgramConfigValueAsBoolean(program, "ShipToNameLocked");
        boolean businessNameLocked = getProgramConfigValueAsBoolean(program, "businessNameLocked");
        List<String> contactInfoLockOverrides = getProgramConfigValueAsList(program, "ContactInfoLockOverrides");
        List<String> mercAddressLockOverrides = getProgramConfigValueAsList(program, "MercAddressLockOverrides");

        // if ShipToNameLocked is not set or false, update first name, last name
        if(!shipToNameLocked) {
            cart.getShippingAddress().setFirstName(AppleUtil.encodeSpecialChar( newShippingAddress.getFirstName()));
            cart.getShippingAddress().setLastName(AppleUtil.encodeSpecialChar( newShippingAddress.getLastName()));
        }

        if(!businessNameLocked) {
            cart.getShippingAddress().setBusinessName(AppleUtil.encodeSpecialChar( newShippingAddress.getBusinessName()));
        }

        validateContactInfoLocked(newShippingAddress, cart, contactInfoLocked, contactInfoLockOverrides);

        return validateMercAddressLocked(newShippingAddress, cart, addressLocked, mercAddressLockOverrides);
    }

    private Address validateMercAddressLocked(Address newShippingAddress, Cart cart, boolean addressLocked, List<String> mercAddressLockOverrides) {
        // if MercAddressLocked is not set or false, update address directly in cart -> shipping address, otherwise use a new Address object, cloned from cart -> shipping address
        Address address = cart.getShippingAddress();
        try {
            if (addressLocked) {
                if (mercAddressLockOverrides.isEmpty()) {
                    address = (Address) BeanUtils.cloneBean(cart.getShippingAddress());
                } else {
                    address = processAddressOverrides(mercAddressLockOverrides, cart.getShippingAddress(), newShippingAddress);
                }
            }
            else{
                populateAddressFields(address, newShippingAddress);
            }

        } catch(Exception e) {
            logger.error("Error while populating address fields in cart", e);
        }
        return address;
    }

    private void validateContactInfoLocked(Address newShippingAddress, Cart cart, boolean contactInfoLocked, List<String> contactInfoLockOverrides) {
        // if ContactInfoLocked is not set or false, update contact information
        if(!contactInfoLocked) {
            cart.getShippingAddress().setEmail(newShippingAddress.getEmail());
            cart.getShippingAddress().setPhoneNumber(newShippingAddress.getPhoneNumber());
        } else if(!contactInfoLockOverrides.isEmpty()) {
            if(contactInfoLockOverrides.contains("email")) {
                cart.getShippingAddress().setEmail( newShippingAddress.getEmail());
            }
            if(contactInfoLockOverrides.contains("phoneNumber")) {
                cart.getShippingAddress().setPhoneNumber( newShippingAddress.getPhoneNumber());
            }
        }
    }

    public Address getNewShippingAddress(final Map<String, Object> updateShippingInformation, final Cart cart, final User user, final Program program) {
        Address address = cart.getShippingAddress();
        try {
            address = (Address) BeanUtils.cloneBean(cart.getShippingAddress());
            address.setErrorMessage(new HashMap<>());
            address.setWarningMessage(new HashMap<>());
        } catch (IllegalAccessException |InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Error while populating address fields in cart", e);
        }
        populateAddressFields(address, updateShippingInformation,user);
        return address;
    }

    private Address processAddressOverrides(final List<String> mercAddressLockOverrides,final Address address,final Address newShippingAddress){
        if(mercAddressLockOverrides.contains("address1")) {
            address.setAddress1(AppleUtil.encodeSpecialChar(newShippingAddress.getAddress1()));
        }
        if(mercAddressLockOverrides.contains("address2")) {
            address.setAddress2(AppleUtil.encodeSpecialChar(newShippingAddress.getAddress2()));
        }
        if(mercAddressLockOverrides.contains("address3")) {
            address.setAddress3(AppleUtil.encodeSpecialChar(newShippingAddress.getAddress3()));
        }
        if(mercAddressLockOverrides.contains("subCity")) {
            address.setSubCity(AppleUtil.encodeSpecialChar(newShippingAddress.getSubCity()));
        }
        if(mercAddressLockOverrides.contains("city")) {
            address.setCity(AppleUtil.encodeSpecialChar(newShippingAddress.getCity()));
        }
        if(mercAddressLockOverrides.contains("businessName")) {
            address.setBusinessName(AppleUtil.encodeSpecialChar( newShippingAddress.getBusinessName()));
        }
        if(mercAddressLockOverrides.contains("state")) {
            address.setState(AppleUtil.encodeSpecialChar(newShippingAddress.getState()));
        }
        if(mercAddressLockOverrides.contains("zip5")) {
            address.setZip5(AppleUtil.encodeSpecialChar( newShippingAddress.getZip5()));
        }
        if(mercAddressLockOverrides.contains("zip4")) {
            address.setZip4(AppleUtil.encodeSpecialChar( newShippingAddress.getZip4()));
        }
        if(mercAddressLockOverrides.contains("country")) {
            address.setCountry(AppleUtil.encodeSpecialChar( newShippingAddress.getCountry()));
        }

        return address;
    }

    private void populateAddressFields(final Address address, final Map<String, Object> addressFields, final User user) {
        address.setFirstName(AppleUtil.encodeSpecialChar((String) addressFields.get("firstName")));
        address.setLastName(AppleUtil.encodeSpecialChar((String) addressFields.get("lastName")));
        address.setBusinessName((String) addressFields.get("businessName"));
        final String address3 = (String) addressFields.get("address3");
        address.setPhoneNumber((String) addressFields.get("phoneNumber"));
        address.setEmail((String) addressFields.get("email"));
        if (address3 != null) {
            address.setAddress3(address3);
        } else {
            address.setAddress3("");
        }
        if (user instanceof UserChase) {
            final long selectedAddressId = ((Double) addressFields.get(SELECTED_ADDRESS_ID)).longValue();
            final com.b2s.common.services.model.Address addressResponse =
                    user.getAddresses()
                            .stream()
                            .filter(ob -> ob.getAddressId() == selectedAddressId)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Address not found for ID : " + selectedAddressId));
            address.setSelectedAddressId((selectedAddressId));
            address.setAddress1(addressResponse.getAddress1());
            address.setAddress2(addressResponse.getAddress2());
            address.setCity(addressResponse.getCity());
            address.setZip5(addressResponse.getZip5());
            address.setZip4(addressResponse.getZip4());
            address.setState(addressResponse.getState());
            address.setCountry(addressResponse.getCountry());
        } else {
            if (addressFields.get(SELECTED_ADDRESS_ID) != null) {
                address.setSelectedAddressId(((Double) addressFields.get(SELECTED_ADDRESS_ID)).longValue());
            }
            address.setAddress1((String) addressFields.get("address1"));
            address.setAddress2((String) addressFields.get("address2"));
            address.setCity((String) addressFields.get("city"));
            address.setSubCity((String) addressFields.get("subCity"));
            address.setZip5((String) addressFields.get("zip5"));
            address.setZip4((String) addressFields.get("zip4"));
            address.setState((String) addressFields.get("state"));
            address.setCountry((String) addressFields.get("country"));
        }
        address.setFirstName(AppleUtil.decodeSpecialChar(address.getFirstName()));
        address.setLastName(AppleUtil.decodeSpecialChar(address.getLastName()));
        address.setIgnoreSuggestedAddress(addressFields.get("ignoreSuggestedAddress") != null ? addressFields.get("ignoreSuggestedAddress").toString() : "false");
        address.getWarningMessage().clear();
        address.getErrorMessage().clear();
    }

    private void populateAddressFields(final Address address, final Address newShippingAddress) {

        address.setAddress1( newShippingAddress.getAddress1());
        address.setAddress2( newShippingAddress.getAddress2());
        final String address3 =  newShippingAddress.getAddress3();
        if(address3 != null) {
            address.setAddress3(address3);
        } else {
            address.setAddress3("");
        }

        address.setSelectedAddressId((newShippingAddress.getSelectedAddressId()));

        address.setCity( newShippingAddress.getCity());
        address.setSubCity( newShippingAddress.getSubCity());
        address.setZip5( newShippingAddress.getZip5());
        address.setZip4( newShippingAddress.getZip4());
        address.setState( newShippingAddress.getState());
        address.setCountry( newShippingAddress.getCountry());
        address.setIgnoreSuggestedAddress(newShippingAddress.getIgnoreSuggestedAddress() != null ? newShippingAddress.getIgnoreSuggestedAddress() : "false");
        address.getWarningMessage().clear();
        address.getErrorMessage().clear();
    }

    /**
     * Validate Address thru Melissa Service.  Skip this validation based on VarProgramConfig settings on 'MercAddressLocked' flag
     *
     * @param user
     * @param newShippingAddress
     * @return
     * @throws Exception
     */
    private boolean validateMelissaRules(final User user, final Program program, final Address newShippingAddress)
        throws Exception {
        boolean hasAddressError = true;
        Map<String, String> melissaErrorMessageMap = new HashMap<>();
        Map<String, String> melissaWarmingMessageMap = new HashMap<>();
        final boolean skipMelissaAddressValidation =
            AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.MERC_ADDRESS_LOCKED);
        if(skipMelissaAddressValidation || "true".equalsIgnoreCase(newShippingAddress.getIgnoreSuggestedAddress())){
            return true;
        }

        //Melissa address validation
        List<StreetAddressRule> rules = getMelissaRules(user, newShippingAddress);
        //globalAddressValidator returns null when Address could not be verified by Melissa. Below rule codes are considered as invalid response by Melissa
        //SE00, SE01, GE01, GE02, GE03, GE04, GE05, GE06, GE07
        if (rules == null ) {
            addMessageForInvalidResponse(melissaErrorMessageMap,user);
        }
        else{
            for (final StreetAddressRule rule : rules) {
                final String ruleDescription = AppContext.getApplicationContext().getMessage("label.street_address_codes." + rule.getCode(), null, "", user.getLocale());
                if (Optional.ofNullable(ruleDescription).isPresent() && !ruleDescription.isEmpty()) {
                    rule.setDescription(ruleDescription);
                }
            }
            hasAddressError = validateStreetAddressRules(hasAddressError,
                    melissaErrorMessageMap, melissaWarmingMessageMap, rules);

        }

        //Merge all errors & warning
        newShippingAddress.getErrorMessage().putAll(melissaErrorMessageMap);
        newShippingAddress.getWarningMessage().putAll(melissaWarmingMessageMap);

        return hasAddressError;
    }

    private List<StreetAddressRule> getMelissaRules(User user, Address newShippingAddress) throws Exception {
        List<StreetAddressRule> rules = null;
        if (CommonConstants.COUNTRY_CODE_US.equals(newShippingAddress.getCountry()) || CommonConstants.COUNTRY_CODE_CA.equals(newShippingAddress.getCountry())) {
            final StreetAddress streetAddress = new StreetAddress();
            AddressMapper.transformAddress(newShippingAddress, streetAddress);
            rules = validateStreetAddress(streetAddress, user);

            //Copy recomended/changed address sent by Melissa validation
            AddressMapper.transformStreetAddress(streetAddress, newShippingAddress);
        } else {
            final GlobalAddress globalAddress = new GlobalAddress();
            globalAddress.setCustomerId(melissaDataGlobalAVCustomerId);
            globalAddress.setOptions(CommonConstants.MELISSA_DATA_DEFAULT_OPTIONS);
            AddressMapper.transformAddress(newShippingAddress, globalAddress);
            String globalAddressToJson = reflectionToStringJsonStyle(globalAddress);
            logger.info("Address sent to Melisa Validation: {}", globalAddressToJson);
            final GlobalAddressResponse addressResponse = globalAddressValidator.validate(globalAddress);
            if(Objects.nonNull(addressResponse.getAddress())) {
                globalAddressToJson = reflectionToStringJsonStyle(addressResponse.getAddress());
                logger.info("Address returned by Melisa Validation: {}", globalAddressToJson);
            }
            rules = addressResponse.getAddressRules();
            //Copy recomended/changed address sent by Melissa validation
            AddressMapper.transformGlobalAddress(addressResponse.getAddress(), newShippingAddress);
        }
        return rules;
    }

    private boolean validateStreetAddressRules(boolean hasAddressError, Map<String, String> melissaErrorMessageMap, Map<String, String> melissaWarmingMessageMap, List<StreetAddressRule> rules) {
        /** Address handling Workflow for phase 1:
         *  AS01 and no other Error code: Success
         *  AS01 and error code: Treat as Warnings
         *  No  AS01 code: Treat all as error
         */
        for (final StreetAddressRule rule : rules) {
            if (rule != null ) { // we should never run into this scenario, but leaving it as is per core implementation
                //TODO : refactor this AS01 line
                if (MelissaDataRules.AS01.equals(rule.getCode())) {
                    hasAddressError = false;
                } else {
                    addMessage(melissaErrorMessageMap, melissaWarmingMessageMap, rule);

                }
            }
        }
        return hasAddressError;
    }

    private void addMessage(Map<String, String> melissaErrorMessageMap, Map<String, String> melissaWarmingMessageMap, StreetAddressRule rule) {
        if (StringUtils.startsWith(rule.getCode(), "AE")) {
            if ((StringUtils.equals(rule.getCode(), "AE10") ) || (StringUtils.equals(rule.getCode(), "AE11") ) ) {
                // Overwrite Melissa's generic message
                rule.setDescription("Street number is either missing or invalid");
            }
            addMessage(melissaErrorMessageMap, rule);

        } else if(ArrayUtils.contains(CommonConstants.MELISSA_DATA_AV_ERROR_CODES, rule.getCode())) {
            addMessage(melissaErrorMessageMap, rule);
        } else {  //add all other AC, AS code messages here, sent by Melissa
            addMessage(melissaWarmingMessageMap, rule);
        }
    }


    /**
     *
     * @param sessionCart
     * @param user
     * @return
     * @throws ServiceException
     */
    public Address getAddress(final Cart sessionCart,final User user,final Program program) throws ServiceException {
        try {
            if (sessionCart != null && sessionCart.getShippingAddress() != null) {
                //return address from session cart
                return sessionCart.getShippingAddress();
            }
            else {
                //return user address
                return AddressMapper.getAddress(user, program);
            }
        }
        catch ( Exception ex) {
            logger.error("Exception while getting ShippingAddress from Cart", ex);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    /**
     * Do base validation
     * @param streetAddress
     * @param user
     * @return
     * @throws Exception
     */
    private List<StreetAddressRule> validateStreetAddress(final StreetAddress streetAddress, final User user) throws Exception {
        boolean verifyAddress = true;
        //set street address rule
        streetAddress.setRules(Collections.emptyList());

        final List<Integer> supplierIds = new ArrayList<>();
        final List<String> merchantIds = new ArrayList<>();
        supplierIds.add(CommonConstants.APPLE_SUPPLIER_ID);
        merchantIds.add(CommonConstants.APPLE_MERCHANT_ID);

        final Map<String, Object> addressValidationArgs = new HashMap<>();
        addressValidationArgs.put("SupplierIds", supplierIds);
        addressValidationArgs.put("MerchantIds", merchantIds);
        addressValidationArgs.put("User", user);

        //TODO: revisit this 'verifyaddress'
        if (("APO".equalsIgnoreCase(streetAddress.getCity()) || "FPO".equalsIgnoreCase(streetAddress.getCity())) &&
                (streetAddress.getZip() !=null && streetAddress.getZip().length() >=9 && streetAddress.getZip().length() <=10) ){
            verifyAddress=false;
        }

        //Melissa address validation
        return streetAddress.validate(addressValidationArgs, verifyAddress, true);
    }

    /**
     * Add/append to message Map
     * @param rule
     * @param messageMap
     */
    private void addMessage(Map<String, String> messageMap, StreetAddressRule rule) {
        if(StringUtils.isNotEmpty(rule.getFields())) {
            if (StringUtils.isNotEmpty(messageMap.get(rule.getFields()))) {
                //There could be multiple messages for the same address line
                messageMap.put(rule.getFields(), messageMap.get(rule.getFields()).concat(", ").concat(rule.getDescription()));
            } else {
                //add to map
                messageMap.put(rule.getFields(), rule.getDescription());
            }
        }
    }


    /**
     * Jsonifies the validation rules
     * @since rewardstep 5.2
     */
    private String getJSONifiedRules(final List<StreetAddressRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        final List<JSONifiedRule> jsonifiedRules = new ArrayList<>();
        JSONifiedRule jsoNifiedRule;
        for (final StreetAddressRule rule : rules) {
            if (rule == null || "AS01".equals(rule.getCode())) {
                continue;
            }
            jsoNifiedRule = new JSONifiedRule();
            jsoNifiedRule.code = rule.getCode();
            jsoNifiedRule.weight = rule.getWeight();
            jsoNifiedRule.fields = rule.getFields();
            jsoNifiedRule.description = rule.getDescription();
            jsonifiedRules.add(jsoNifiedRule);
        }
        final Gson jsonifier = new Gson();
        return jsonifier.toJson(jsonifiedRules);
    }

    //check if the given email address is valid
    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }


    private static class JSONifiedRule {
        String code;
        int weight;
        String fields;
        String description;
    }

    private void addMessageForInvalidResponse(final Map<String, String> melissaErrorMessageMap,final User user) {
        StreetAddressRule rule = new StreetAddressRule();
        final String ruleDescription = AppContext.getApplicationContext().getMessage("unableToVerify", null, "", user.getLocale());
        if (Optional.ofNullable(ruleDescription).isPresent() && !ruleDescription.isEmpty()) {
            rule.setDescription(ruleDescription);
            rule.setFields("all");
        }
        //Following error message will intimate the user to update/edit the address since user address could not be verified by Melissa.
        addMessage(melissaErrorMessageMap, rule);
    }


}
