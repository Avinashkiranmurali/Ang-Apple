package com.b2s.shop.common.order.var;

import com.b2s.apple.services.VarProgramMessageService;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.integration.model.CancelRedemptionResponse;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.integration.model.RedemptionOrderLine;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.VarProgramRedemptionOption;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.common.util.CommonConstants.COUNTRY_CODES;
import static java.lang.Integer.valueOf;

@Component("varOrderManagerCiti")
public class VAROrderManagerCiti extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerCiti.class);
    private static final List<String> COUNTRY_REQUIRING_DEFAULT_VALUE = Arrays.asList("HK");

    @Autowired
    private VarIntegrationServiceCitiGRRemoteImpl varIntegrationServiceCitiGRRemoteImpl;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    private static final String[] dataLite = {"TW"};

    private static final String AU_DEFAULT_SOURCECODE = "GRCB";

    private static final Pattern NAME_SPLIT_PATTERN = Pattern.compile("\\s");

    private enum SAMLAttributes {

        MEMBER_ID("MEMBER_ID"),
        MBR_TIER_CODE("MBR_TIER_CODE"),
        COUNTRY_CODE("COUNTRY_CODE"),
        LANGUAGE_CODE("LANGUAGE_CODE"),
        DISPLAY_CURRENCY_CODE("DISPLAY_CURRENCY_CODE"),
        SESSION_STATE("SESSION_STATE"),
        IP_ADDRESS("IP_ADDRESS"),
        SESSION_ID("SESSION_ID"),
        MEMBER_ALLOWED_TO_REDEEM("MEMBER_ALLOWED_TO_REDEEM"),
        MBR_SPLIT_TENDER_ELIG("MBR_SPLIT_TENDER_ELIG"),
        SOURCE_CODE("SOURCE_CODE"),
        AGENT_ID("AGENT_ID"),
        MBR_DISPLAY_NAME("MBR_DISPLAY_NAME"),
        MBR_FIRST_NAME("FIRST_NAME"),
        MBR_MIDDLE_NAME("MIDDLE_NAME"),
        MBR_LAST_NAME("LAST_NAME"),
        MBR_PREFIX("PREFIX"),
        MBR_SUFFIX("SUFFIX"),
        MBR_AVAILABLE_BALANCE("MBR_AVAILABLE_BALANCE"),
        MBR_POINTS_SUB_TYPE("MBR_POINTS_SUB_TYPE"),
        MBR_ACCOUNT_KEY("MBR_ACCOUNT_KEY"),
        MBR_ACCOUNT_NUMBER("MBR_ACCOUNT_NUMBER"),
        MBR_EMAIL_ID("MBR_EMAIL_ID"),
        EMAIL_UNDELEVERABLE_IND("EMAIL_UNDELEVERABLE_IND"),
        USER_CONSENTED("USER_CONSENTED"),
        MBR_ADDRESS_1("MBR_ADDRESS_1"),
        MBR_ADDRESS_2("MBR_ADDRESS_2"),
        MBR_ADDRESS_3("MBR_ADDRESS_3"),
        MBR_ADDRESS_4("MBR_ADDRESS_4"),
        MBR_ADDRESS_5("MBR_ADDRESS_5"),
        MBR_ADDRESS_6("MBR_ADDRESS_6"),
        MBR_CITY("MBR_CITY"),
        MBR_STATE("MBR_STATE"),
        MBR_COUNTRY("MBR_COUNTRY"),
        MBR_FORMATTED_POSTAL_CODE("MBR_FORMATTED_POSTAL_CODE"),
        MBR_HOME_PHONE("MBR_HOME_PHONE"),
        MBR_CELL_PHONE("MBR_CELL_PHONE"),
        MBR_BUSINESS_PHONE("MBR_BUSINESS_PHONE"),
        RELAY_STATE("RELAY_STATE"),
        NAV_BACK_URL("NAV_BACK_URL"),
        KEEP_ALIVE_URL("KEEP_ALIVE_URL"),
        LOGOUT_URL("LOGOUT_URL");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    public enum Language {

        ENGLISH("eng", "en"),
        SPANISH("spa", "es"),
        CZECH("cze", "cs"),
        CHINESE_TRADITIONAL("zho", "zh"),
        THAI("tha", "th"),
        PORTUGUESE("por", "pt");

        private final String threeLetterCode;
        private final String twoLetterCode;

        private static final Map<String, Language> TWO_LETTER_LANGUAGES = new HashMap<>();
        private static final Map<String, Language> THREE_LETTER_LANGUAGES = new HashMap<>();

        static {
            for (final Language language : values()) {
                TWO_LETTER_LANGUAGES.put(language.getTwoLetterCode(), language);
                THREE_LETTER_LANGUAGES.put(language.getThreeLetterCode(), language);
            }
        }

        Language(final String threeLetterCode, final String twoLetterCode) {
            this.threeLetterCode = threeLetterCode;
            this.twoLetterCode = twoLetterCode;
        }

        public String getThreeLetterCode() {
            return threeLetterCode;
        }

        public String getTwoLetterCode() {
            return twoLetterCode;
        }

        public static Language fromThreeLetterCode(final String rawCode) {
            final String code = validateCode(rawCode);

            if (THREE_LETTER_LANGUAGES.containsKey(code)) {
                return THREE_LETTER_LANGUAGES.get(code);
            }

            throw new IllegalArgumentException("invalid code: " + code);
        }

        public static Language fromTwoLetterCode(final String rawCode) {
            final String code = validateCode(rawCode);

            if (TWO_LETTER_LANGUAGES.containsKey(code)) {
                return TWO_LETTER_LANGUAGES.get(code);
            }

            throw new IllegalArgumentException("invalid code: " + code);
        }

        private static String validateCode(final String rawCode) {
            if (rawCode == null) {
                throw new IllegalArgumentException("code cannot be null");
            }

            final String rawCodeTrimmed = rawCode.trim();

            if (rawCodeTrimmed.isEmpty()) {
                throw new IllegalArgumentException("code cannot be empty string");
            }

            return rawCodeTrimmed.toLowerCase();
        }

    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final UserCiti user;
        final Function<String, String> fetchRequestAttribute = key -> CommonConstants.getRequestAttribute(request, key);

        CommonConstants.LoginType loginType = null;
        if ((request.getParameter(CommonConstants.CITI_SAML_REQ_PARAM_FOR_SAML_RESP) == null)
                && (request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) == null)) {
            user = (UserCiti) selectLocalUser(request, new UserCiti());
            setUserAdditionalInfo(user);
            setSourceCode(user);
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            // User creation for SAML login.
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes -> {
                LOGGER.info("Citi SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value));
            });

            samlAttributesValidation(fetchRequestAttribute);
            user = getUser(request);
            loginType = CommonConstants.LoginType.SAML;
        }
        user.setLoginType(loginType.getValue());
        //Fix to address the CC Bin Restriction via SAML flow.
        //Citi will have multiple VAR so we will be using the new VAR ID PAram
        final Program program = getProgram(user);

        //This method also overrides payment-options for OBO users based on obo_redeemable configuration
        if (isCitiOBORedeemRestricted(request, user, program)) {
            return null;
        }

        setUserNames(user, program);

        boolean ignoreProfileAddress = applyIgnoreProfileToUser(user, program);

        //Only citi VAR has Addr3, Addr4, Addr5, Addr6
        if (ignoreProfileAddress) {
            user.setAddr3(null);
            user.setAddr4(null);
            user.setAddr5(null);
            user.setAddr6(null);
        }
        initializeLocaleDependents(request, user);

        if (MapUtils.isNotEmpty(program.getConfig())) {
            final StringBuilder url = new StringBuilder();

            url.append(applicationProperties.getProperty(CommonConstants.CITI_SAML_IDP_ENDPOINT_KEY));
            url.append("partnerCode=").append(applicationProperties.getProperty(CommonConstants.CITI_SAML_IDP_PARTNER_CODE_KEY));
            url.append("&relayState=").append(applicationProperties.getProperty(CommonConstants.CITI_SAML_IDP_PARTNER_RELAY_STATE)).append("cart");
            url.append("&sessionState=").append(CommonConstants.CITI_SAML_IDP_LOGGED_IN_SESSION_STATE);

            program.getConfig().put(CommonConstants.SIGNIN_URL_KEY, url.toString());

            if (request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) != null) {
                program.getConfig().put(CommonConstants.CITI_LOGIN_REQUIRED, request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED));
            }
            final Boolean browseOnly = Boolean.parseBoolean(String.valueOf(program.getConfig().get(CommonConstants.IS_BROWSE_ONLY)));
            if(browseOnly){
                user.setBrowseOnly(browseOnly);
                program.getConfig().put(CommonConstants.CITI_LOGIN_REQUIRED, browseOnly);
            }
        }


        setSessionTimeOut(request, user, program);

        //Bag Menu URLs from DB is having less precedence than SAML response
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);

        //Override the Bag Menu URLs from SAML response
        setKeepAliveRequestUrl(request, fetchRequestAttribute);

        prepareUserAddress(user);

        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        return user;
    }

    //This method also overrides payment-options for OBO users based on obo_redeemable configuration
    private boolean isCitiOBORedeemRestricted(HttpServletRequest request, UserCiti user, Program program) {
        final String sessionState = (String) request.getSession().getAttribute(CommonConstants.CITI_SAML_SESSION_STATE);
        final String oboRedeemable = (String) program.getConfig().get(CommonConstants.CITI_OBO_REDEEMABLE);

        if (StringUtils.isNotEmpty(sessionState)) {
            if (StringUtils.isNotEmpty(oboRedeemable) &&
                sessionState.equals(CommonConstants.CITI_OBO_SESSION_STATE)) {
                if (oboRedeemable.equals(CommonConstants.CITI_OBO_REDEEM_POINTS_ONLY)) {
                    overridePaymentOptionForOBO(program);
                } else if (oboRedeemable.equals(CommonConstants.CITI_OBO_REDEEM_RESTRICTED)) {
                    return true;
                }
            }
            if (CommonConstants.CITI_OBO_ANONYMS_STATE.equals(sessionState)) {
                user.setAnonymous(true);
            }
        }
        return false;
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            RedemptionResponse redemptionResponse = null;
            if (program != null && program.getIsLocal()) {
                redemptionResponse = varIntegrationServiceLocalImpl.performRedemption(order, user, program);
            } else {
                user.getAdditionalInfo().put(CommonConstants.VIS_ADDTNL_INFO_KEY_IS_DEFAULT_ADDRESS, String.valueOf(isDefaultAddress(order, user)));
                redemptionResponse = varIntegrationServiceCitiGRRemoteImpl.performRedemption(order, user, program);
            }

            //assign/update order with the redemption response
            if (redemptionResponse == null) {
                return false;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            final List<RedemptionOrderLine> redemptionOrderLines = redemptionResponse.getOrderLines();
            //save varOrderLineId into order_line.order_line_type
            setOrderLineType(order,redemptionOrderLines);

            getBalanceUserPoints(order,user);

            message = new Message();
            message.setSuccess(true);
            message.setContentText("VAROrderManagerCITI successfully placed order: Order# "+order.getOrderId());
            message.setVAROrderId(VAROrderId);
            message.setCode(OrderCodeStatus.SUCCESS.getValue());

        } catch (final Exception ex) {
            LOGGER.error("Error updating user Var details in VarIntegrationService...Exception trace:  ", ex.getMessage(), ex);
            insertMessageVISException(order, user);

        }

        return message.isSuccess();
    }


    @Override
    public boolean cancelOrder(final Order order) {
        return true;
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, final Program program) {

        try {

            CancelRedemptionResponse redemptionResponse = null;
            if (program != null && program.getIsLocal()) {
                redemptionResponse = varIntegrationServiceLocalImpl.performPartialCancelRedemption(order, user, program);
            } else {
                redemptionResponse = varIntegrationServiceCitiGRRemoteImpl.performPartialCancelRedemption(order, user, program);
            }

            if (redemptionResponse != null) {
                return true;
            }

        }catch (Exception ex){
                LOGGER.error("Error cancelling var transaction in VarIntegrationService...Exception trace:  ", ex.getMessage(), ex);
                return false;
        }

        return false;

    }

    @Override
    public int getUserPoints(final User user, final Program program)  throws B2RException {
        int userPoints = 0;
        if(program != null && program.getIsLocal()) {
            userPoints = varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        }
        else {
            userPoints = varIntegrationServiceCitiGRRemoteImpl
                .getUserPoints(user.getVarId(), user.getProgramId(), user.getUserId(),user.getAdditionalInfo());
        }
        return userPoints;
    }

    @Override
    protected String getVARId() {
        return null;
    }

    /**
     * This method simply serves to extract some locale setting logic that get pushed into various objects.
     * @param request
     * @param user
     */
    private void initializeLocaleDependents(final HttpServletRequest request, final User user) {
        String localStr = "";
        if (request.getAttribute(CommonConstants.LOCALE) != null) {
            localStr = (String) request.getAttribute(CommonConstants.LOCALE);
        }

        if (StringUtils.isBlank(localStr)) {
            localStr = request.getParameter("locale");
        }

        if (StringUtils.isBlank(localStr)) {
            request.setAttribute(CommonConstants.LOCALE, user.getLocale().toString());
        }

    }

    @Override
    protected UserCiti getUser(final HttpServletRequest request) {

        final UserCiti user = new UserCiti();
        final String[] sessionList = {"L", "O"};
        final Function<String, String> fetchRequestAttribute = key -> CommonConstants.getRequestAttribute(request, key);

        if (!ArrayUtils.contains(sessionList, fetchRequestAttribute.apply(SAMLAttributes.SESSION_STATE.value))) {
            request.setAttribute(CommonConstants.CITI_LOGIN_REQUIRED, true);
        }

        user.setVarId(fetchRequestAttribute.apply(SAMLAttributes.COUNTRY_CODE.value));
        user.setCountry(fetchRequestAttribute.apply(SAMLAttributes.COUNTRY_CODE.value));
        user.setProgramId(fetchRequestAttribute.apply(SAMLAttributes.MBR_TIER_CODE.value));

        String languageCode = fetchRequestAttribute.apply(SAMLAttributes.LANGUAGE_CODE.value);

        //Mexican catalog will be in Spanish even when the SAML user language is English.
        if (user.getCountry().equalsIgnoreCase(CommonConstants.VAR_MX) && Language.ENGLISH.getThreeLetterCode().equalsIgnoreCase(languageCode)){
            languageCode = Language.SPANISH.threeLetterCode;
        }


        user.setLocale(new Locale(Language.fromThreeLetterCode(languageCode).getTwoLetterCode(), user.getCountry()));

        final String userSessionState = fetchRequestAttribute.apply(SAMLAttributes.SESSION_STATE.value);
        //Set it to Session
        request.getSession().setAttribute(CommonConstants.CITI_SAML_SESSION_STATE, userSessionState);
        user.setBrowseOnly(!ArrayUtils.contains(sessionList, userSessionState));
        user.setMemberId(fetchRequestAttribute.apply(SAMLAttributes.MEMBER_ID.value));
        user.setUserId(fetchRequestAttribute.apply(SAMLAttributes.MEMBER_ID.value));

        final String pointBalance = fetchRequestAttribute.apply(SAMLAttributes.MBR_AVAILABLE_BALANCE.value);
        if (StringUtils.isNotEmpty(pointBalance) && StringUtils.isNumeric(pointBalance)) {
            user.setPoints(valueOf(pointBalance));
        }

        user.setFirstName(fetchRequestAttribute.apply(SAMLAttributes.MBR_FIRST_NAME.value));
        user.setMiddleName(fetchRequestAttribute.apply(SAMLAttributes.MBR_MIDDLE_NAME.value));
        user.setLastName(fetchRequestAttribute.apply(SAMLAttributes.MBR_LAST_NAME.value));
        user.setPrefixName(fetchRequestAttribute.apply(SAMLAttributes.MBR_PREFIX.value));
        user.setSuffixName(fetchRequestAttribute.apply(SAMLAttributes.MBR_SUFFIX.value));
        user.setDisplayName(fetchRequestAttribute.apply(SAMLAttributes.MBR_DISPLAY_NAME.value));

        user.setAddr1(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_1.value));
        user.setAddr2(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_2.value));
        user.setAddr3(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_3.value));
        user.setAddr4(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_4.value));
        user.setAddr5(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_5.value));
        user.setAddr6(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_6.value));
        user.setCity(fetchRequestAttribute.apply(SAMLAttributes.MBR_CITY.value));
        user.setState(fetchRequestAttribute.apply(SAMLAttributes.MBR_STATE.value));
        user.setCountry(fetchRequestAttribute.apply(SAMLAttributes.MBR_COUNTRY.value));

        populatePostalCode(user, fetchRequestAttribute);

        final String cellPhone = fetchRequestAttribute.apply(SAMLAttributes.MBR_CELL_PHONE.value);

        final String homePhone = fetchRequestAttribute.apply(SAMLAttributes.MBR_HOME_PHONE.value);

        final String businessPhone = fetchRequestAttribute.apply(SAMLAttributes.MBR_BUSINESS_PHONE.value);

        final Optional<String> validPhoneNumber = Stream.of(cellPhone, homePhone, businessPhone)
                .filter(o -> StringUtils.isNotEmpty(o))
                .filter(o -> isValidPhoneNumber(o, user.getCountry()))
                .findFirst();

        if(validPhoneNumber.isPresent()) {
            user.setPhone(validPhoneNumber.get());
        }else{
            final Optional<String> phoneNumber = Stream.of(cellPhone, homePhone, businessPhone)
                    .filter(o -> StringUtils.isNotEmpty(o))
                    .findFirst();

            if(phoneNumber.isPresent()) {
                user.setPhone(phoneNumber.get());
            }
        }

        user.setEmail(fetchRequestAttribute.apply(SAMLAttributes.MBR_EMAIL_ID.value));
        user.setAgentId(fetchRequestAttribute.apply(SAMLAttributes.AGENT_ID.value));
        user.setUserConsented(fetchRequestAttribute.apply(SAMLAttributes.USER_CONSENTED.value));
        user.setSourceCode(fetchRequestAttribute.apply(SAMLAttributes.SOURCE_CODE.value));

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_COUNTRY_CODE, user.getCountry());
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_LANGUAGE_CODE,
            user.getLocale().getISO3Language().toUpperCase());
        user.setAdditionalInfo(additionalInfo);
        setSourceCode(user);

        if (request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) == null && ArrayUtils.contains(dataLite, user.getCountry()) && !user.isUserConsented()) {
            getUserConsent(request, user.getSourceCode());
        }

        final String relayState = fetchRequestAttribute.apply(SAMLAttributes.RELAY_STATE.value);

        if (StringUtils.isNotEmpty(relayState)&& relayState.contains(AppleUtil.getHostName(request))) {
           request.setAttribute(CommonConstants.CITI_RELAY_STATE,relayState);
        }
        return user;
    }

    private void populatePostalCode(UserCiti user, Function<String, String> fetchRequestAttribute) {
        // Set the postal code only if the country is not listed in the countries with no postal code list
        // If postal code is received in SAML for country which doesnt support postal code, Information is logged and the postal code is ignored.
        final String postalCode = getPostalCode(fetchRequestAttribute.apply(SAMLAttributes.MBR_FORMATTED_POSTAL_CODE.value), fetchRequestAttribute.apply(SAMLAttributes.MBR_COUNTRY.value));
        if (!CommonConstants.COUNTRIES_WITH_NO_POSTALCODE.contains(user.getCountry())) {
            user.setZip(postalCode);
        } else if(StringUtils.isNotEmpty(postalCode)) {
            LOGGER.info("++++++ Zip code received from SAML for the country {} which doesn't support ZIP code", user.getCountry());
        }
    }

    /**
     * The postal code fot HK if it is not present
     * @param postalCode
     * @return
     */
    private String getPostalCode(String postalCode, String country) {
        if (StringUtils.isBlank(postalCode) && COUNTRY_REQUIRING_DEFAULT_VALUE.contains(country)) {
            return "00000";
        }
        return postalCode;
    }

    private void getUserConsent(final HttpServletRequest request, final String sourceCode) {
        request.setAttribute(CommonConstants.CITI_USER_CONSENT, Boolean.TRUE);
        if (StringUtils.isNotBlank(sourceCode)) {
            request.getSession().setAttribute(CommonConstants.CITI_SOURCE_CODE, sourceCode);
        }
    }

    private void setKeepAliveRequestUrl(final HttpServletRequest request,
        final Function<String, String> fetchRequestAttribute) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        if (request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) == null) {
            externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE, AppleUtil.buildReturnHostPrefix(request) + "/apple-gr/service/" + ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE_REQ_MAPPING);
            externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL, fetchRequestAttribute.apply(SAMLAttributes.KEEP_ALIVE_URL.value));
            //SET the base keepalive URL if we want it to append timestamp.
            externalUrls.put(ExternalUrlConstants.BASE_KEEP_ALIVE_URL, fetchRequestAttribute.apply(SAMLAttributes.KEEP_ALIVE_URL.value));
        }

        final String navBackUrl = fetchRequestAttribute.apply(SAMLAttributes.NAV_BACK_URL.value);
        if(StringUtils.isNotBlank(navBackUrl)){
            externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL, navBackUrl);
        }

        final String logOutUrl = fetchRequestAttribute.apply(SAMLAttributes.LOGOUT_URL.value);
        if(StringUtils.isNotBlank(logOutUrl)){
            externalUrls.put(ExternalUrlConstants.TIME_OUT_URL, logOutUrl);
            externalUrls.put(ExternalUrlConstants.SIGN_OUT_URL, logOutUrl);
        }
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private void samlAttributesValidation(final Function<String, String> fetchRequestAttribute) {
        // Validate mandatory fields for All session states
        AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.COUNTRY_CODE.value),SAMLAttributes.COUNTRY_CODE.value);
        AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.LANGUAGE_CODE.value),SAMLAttributes.LANGUAGE_CODE.value);
        AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.MBR_TIER_CODE.value),SAMLAttributes.MBR_TIER_CODE.value);
        AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.SESSION_STATE.value),SAMLAttributes.SESSION_STATE.value);

        final String userSessionState = fetchRequestAttribute.apply(SAMLAttributes.SESSION_STATE.value);

        // Validate mandatory fields for LoggedIn state
        if ("L".equalsIgnoreCase(userSessionState)) {
            AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.MEMBER_ID.value), SAMLAttributes.MEMBER_ID.value);
            AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.MBR_DISPLAY_NAME.value), SAMLAttributes.MBR_DISPLAY_NAME.value);
            AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.MBR_AVAILABLE_BALANCE.value), SAMLAttributes.MBR_AVAILABLE_BALANCE.value);
            AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.MBR_ADDRESS_1.value), SAMLAttributes.MBR_ADDRESS_1.value);
    }

        // Validate mandatory field(s) for Australia
        if ("AU".equalsIgnoreCase(fetchRequestAttribute.apply(SAMLAttributes.COUNTRY_CODE.value))){
            AppleUtil.checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.SOURCE_CODE.value),SAMLAttributes.SOURCE_CODE.value);
        }
    }

    private boolean isValidPhoneNumber(final String phoneNumber, final String countryCode){

        final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            return phoneUtil.isValidNumber(phoneUtil.parse(phoneNumber, countryCode));
        }catch(final NumberParseException e) {
            return false;
        }

    }

    private List<String> getFirstNameLastName(final String displayName) {
        if(StringUtils.isBlank(displayName)) return Collections.emptyList();
        return Arrays.stream(NAME_SPLIT_PATTERN.split(displayName, 2) ).map( s -> s.trim() ).filter( s-> StringUtils.isNotEmpty(s)).collect(Collectors.toList());
    }

    private void prepareUserAddress(final UserCiti user) {
        if (user != null) {
            trimUserDetails(user);
            if(StringUtils.isNotBlank(user.getCountry())) {
                user.setCountry(user.getCountry());
            } else {
                COUNTRY_CODES.forEach((k, v) -> {
                    if (StringUtils.equalsIgnoreCase(StringUtils.trim(user.getCountry()), k)) {
                        user.setCountry(v);
                    }
                });
            }

            user.setAddressPresent(StringUtils.isNotEmpty(user.getState()) && StringUtils.isNotEmpty(user.getZip()) &&
                StringUtils.isNotEmpty(user.getAddr1()));
        }
    }

    private void trimUserDetails(UserCiti user) {
        if (StringUtils.isNotBlank(user.getAddr1())) {
            user.setAddr1(user.getAddr1().trim());
        }
        if (StringUtils.isNotBlank(user.getAddr2())) {
            user.setAddr2(user.getAddr2().trim());
        }
        if (StringUtils.isNotBlank(user.getAddr3())) {
            user.setAddr3(user.getAddr3().trim());
        }
        if (StringUtils.isNotBlank(user.getAddr3())) {
            user.setAddr4(user.getAddr4().trim());
        }
        if (StringUtils.isNotBlank(user.getAddr3())) {
            user.setAddr5(user.getAddr5().trim());
        }
        if (StringUtils.isNotBlank(user.getAddr3())) {
            user.setAddr6(user.getAddr6().trim());
        }
        if(StringUtils.isNotBlank(user.getCity())) {
            user.setCity(user.getCity().trim());
        }
        if (StringUtils.isNotBlank(user.getState())) {
            user.setState(user.getState().trim());
        }
        if (StringUtils.isNotBlank(user.getZip())) {
            user.setZip(user.getZip().trim());
        }
    }


    public void setOrderLineType(final Order order, final List<RedemptionOrderLine> redemptionOrderLines){
        //Programs that has remote turned off will have this value as null.
        if (null != redemptionOrderLines) {
            order.getOrderLines().forEach(orderLineItem ->{
                final OrderLine orderLineObject = (OrderLine) orderLineItem;
                final Optional<RedemptionOrderLine> redemptionOrderLineOptional = redemptionOrderLines.stream().filter(redemptionOrderLine -> StringUtils.isNotEmpty(redemptionOrderLine.getVarOrderLineId())).findAny();
                if (redemptionOrderLineOptional.isPresent()) {
                    orderLineObject.setOrderLineType(redemptionOrderLineOptional.get().getVarOrderLineId());
                }
            });
        }
    }

    private void setUserAdditionalInfo(final User user){
        final Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_COUNTRY_CODE, user.getCountry());
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_LANGUAGE_CODE,user.getLocale().getISO3Language().toUpperCase());
        user.setAdditionalInfo(additionalInfo);
    }

    private void setSourceCode(final UserCiti user) {
        if ( CommonConstants.COUNTRY_CODE_AU.equals(user.getCountry()) ) {
            if(!StringUtils.isBlank(user.getSourceCode()))
                user.getAdditionalInfo().put(CommonConstants.CITI_SOURCE_CODE, user.getSourceCode());
            else
                user.getAdditionalInfo().put(CommonConstants.CITI_SOURCE_CODE, AU_DEFAULT_SOURCECODE);
        }
    }

    private void overridePaymentOptionForOBO(final Program program){
        program.getConfig().put(CommonConstants.CITI_OBO_REDEEM_PAYMENT_TEMPLATE, CommonConstants.CITI_OBO_REDEEM_POINTS_DEFAULT);
        program.getConfig().put(CommonConstants.CITI_OBO_REDEEM_PAYMENT_TYPE, CommonConstants.CITI_OBO_REDEEM_POINTS_ONLY);

        Map<String, List<VarProgramRedemptionOption>> varProgramRedemptionOptionMap = program.getRedemptionOptions();
        varProgramRedemptionOptionMap.keySet().removeIf(key -> !key.equalsIgnoreCase(PaymentOptions.POINTSONLY.getPaymentOption())) ;

    }

    private void setSessionTimeOut(HttpServletRequest request, User user,Program program) {

        final String programLevelTimeOut = String.valueOf(program.getConfig().get(CommonConstants.SESSION_TIMEOUT));
        if (StringUtils.isNotBlank(programLevelTimeOut) && NumberUtils.isNumber(programLevelTimeOut)){
            request.getSession().setMaxInactiveInterval(Integer.valueOf(programLevelTimeOut) * 60);
        }
        else{
            super.setSessionTimeOut(request, user);
        }
    }

    private void setUserNames(final UserCiti user, final Program program){
        String nameFormat  = (String)varProgramMessageService.getMessages(Optional.of(user.getVarId()), Optional.of(user.getProgramId()), user.getLocale().toString()).getOrDefault(CommonConstants.CITI_HEADER_NAME_FORMAT, null);
        if(StringUtils.isNotBlank(nameFormat)){
            final String headerName = StringUtils.replaceEach(nameFormat.toLowerCase(), new String[]{"firstname","lastname", "middlename", "prefix", "suffix"},
                    new String[]{StringUtils.stripToEmpty(user.getFirstName()),
                            StringUtils.stripToEmpty(user.getLastName()),
                            StringUtils.stripToEmpty(user.getMiddleName()),
                            StringUtils.stripToEmpty(user.getPrefixName()),
                            StringUtils.stripToEmpty(user.getSuffixName())}
            );

            user.setHeaderName(headerName);
        }

        final List<String> firstAndLastName = getFirstNameLastName(user.getDisplayName());
        if (firstAndLastName.size() > 0) {
            user.setFirstName(firstAndLastName.get(0));
            if (firstAndLastName.size() > 1) {
                user.setLastName(firstAndLastName.get(1).trim().replaceAll("\\s+", " "));
            }
        }

    }

    @Override
    public boolean isSendOrderConfirmationEmailToUser() {
        return false;
    }
}