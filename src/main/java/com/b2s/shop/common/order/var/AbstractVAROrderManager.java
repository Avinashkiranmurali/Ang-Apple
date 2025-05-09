package com.b2s.shop.common.order.var;

import com.b2s.apple.entity.DemoUserEntity;
import com.b2s.apple.services.ProgramService;
import com.b2s.common.services.discountservice.CouponDetails;
import com.b2s.common.services.discountservice.DiscountServiceClient;
import com.b2s.common.util.EncryptionUtil;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.dao.DemoUserDao;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.dao.OrderLineItemAttributeDao;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.apple.util.AppleUtil.getHostName;
import static com.b2s.rewards.apple.util.AppleUtil.getValueFromRequest;
import static com.b2s.rewards.apple.util.AppleUtil.replaceNullString;
import static com.b2s.rewards.common.util.CommonConstants.COUNTRY_CODES;


/**
 * VAR Template. New VARs should extend this class and define their specific implementation details.
 *
 * @author dmontoya
 * @version 1.0, 10/21/12 2:25 PM
 * @since b2r-rewardstep 5.1
 */
abstract class AbstractVAROrderManager implements VAROrderManagerIF {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractVAROrderManager.class);

    protected static final String PROGRAM_ID_PARAM = "programid";

    protected Message message;
    protected String VAROrderId;

    @Autowired
    protected MessageSource messageSource;


    @Autowired
    protected Properties applicationProperties;

    @Autowired
    protected ProgramService programService;

    @Autowired
    protected OrderLineItemAttributeDao orderLineItemAttributeDao;

    @Autowired
    protected OrderAttributeValueDao orderAttributeValueDao;

    @Autowired
    protected DiscountServiceClient discountServiceClient;

    @Autowired
    protected DemoUserDao demoUserDao;

    /**
     * VAROrderManager config.
     */
    private final Map<String, Object> config = new HashMap<String, Object>();

    /**
     * Gets the current instance VAR ID
     *
     * @return the VAR ID
     */
    protected abstract String getVARId();

    @Override
    public abstract User selectUser(final HttpServletRequest request) throws B2RException;

    protected Properties getApplicationProperties() {
        return this.applicationProperties;
    }

    protected void setSessionTimeOut(HttpServletRequest request, User user) {
        String sessionTimeOut =
            applicationProperties.getProperty(user.getVarId().toLowerCase() + "." + user.getProgramId().toLowerCase() + "." + CommonConstants.SESSION_TIMEOUT_MINUTES);

        // if the varId.programId.session.timeout.minutes does not have a valid value,
        // then check for varId.session.timeout.minutes
        if (!NumberUtils.isNumber(sessionTimeOut)) {
            sessionTimeOut =
                    applicationProperties.getProperty(user.getVarId().toLowerCase() + "." + CommonConstants.SESSION_TIMEOUT_MINUTES);
        }

        if (NumberUtils.isNumber(sessionTimeOut)) {
            request.getSession().setMaxInactiveInterval(Integer.valueOf(sessionTimeOut) * 60);
        } else {
            sessionTimeOut = applicationProperties.getProperty(CommonConstants.SESSION_TIMEOUT_MINUTES);
            if (NumberUtils.isNumber(sessionTimeOut)) {
                request.getSession().setMaxInactiveInterval(Integer.valueOf(sessionTimeOut) * 60);
            }
        }
    }

    protected User getUser(final HttpServletRequest httpServletRequest){
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final User user=new User();
        user.setVarId(request.getParameter("varid"));
        user.setProgramId(request.getParameter(PROGRAM_ID_PARAM));
        user.setUserId(request.getParameter("userid"));
        user.setLocale(new Locale(CommonConstants.DEFAULT_LANGUAGE_CODE, CommonConstants.COUNTRY_CODE_US));
        user.setEmail(request.getParameter("email"));

        return user;
    }

    public void getBalanceUserPoints(Order order, User user){
        int finalUserPoints = user.getInitialUserBalance() - order.getOrderTotalPointsIncludingDiscountsAndCredits();
        user.setPoints(finalUserPoints < 0 ? 0:finalUserPoints);
    }

    public AccountInfo getAccountInfo(final User user, final boolean isLocal,
        final VarIntegrationServiceLocalImpl varIntegrationServiceLocalImpl,
        final VarIntegrationServiceRemoteImpl varIntegrationServiceRemoteImpl) {

        AccountInfo accountInfo = null;
        if (isLocal) {
            accountInfo = varIntegrationServiceLocalImpl
                .getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        } else {
            accountInfo = varIntegrationServiceRemoteImpl
                .getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        }
        return accountInfo;
    }

    /**
     * Find out login is anoymous and program supports anonymous purchase
     *
     * @param request
     * @return
     */
    protected boolean anonymousLogin(final HttpServletRequest request) {
        final String userId = request.getParameter(CommonConstants.USER_ID);
        final String varId = request.getParameter(CommonConstants.VAR_ID);
        final String programId = request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE);
        final Optional<String> anonymousPurchaseOpt = programService.getProgramConfigValue(varId, programId, CommonConstants.ANONYMOUS_PURCHASE);
        final boolean anonymousPurchase = Boolean.valueOf(anonymousPurchaseOpt.orElse(CommonConstants.FALSE_VALUE));
        final String anonymousUserId = CommonConstants.ANONYMOUS_USER_ID.toLowerCase(Locale.ENGLISH);
        return (anonymousPurchase && StringUtils.isNotBlank(userId) && StringUtils.startsWith(userId.toLowerCase(Locale.ENGLISH),anonymousUserId));
    }

    /**
     * Validates the current user and gets its data from the Client. This method should only call the client web service
     * to get the user's data.
     *
     * @return validated user
     */
    protected abstract User getUser(String sessionId, Program program,
                                    boolean selectProgram, ServletRequest request);

    /**
     * Sends an order to the VAR client. This method should only call the client web service and set the success or
     * error message on the @{code message} field.
     *
     * @param order the order
     * @param user  the current user
     * @return true if success
     */
    @Override
    public abstract boolean placeOrder(Order order, User user, Program program);

    @Override
    public abstract boolean updateOrderStatus(Map<String, Object> properties, String varOrderId, String orderId,
                                              String lineNum,
                                              String carrier, String shippingDesc, String trackingNum,
                                              String status, Double points);

    /**
     * This method depends on getUser.
     *
     * @param user current user
     * @return user current balance
     * @see #getUser
     */
    @Override
    public abstract int getUserPoints(final User user, final Program program) throws B2RException;

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public String getVAROrderId() {
        return VAROrderId;
    }


    @Override
    public boolean isSendOrderConfirmationEmailToUser() {
        return true;
    }

    @Override
    public boolean isOrderReadyForProcessing() {
        return true;
    }

    /**
     * This is legacy, we need to redefine the contract on VAROrderManagerIF
     */
    @Override
    public Map<String, Object> getProperties(final String varId, final String programId) {
        return config;
    }

    protected static void prepareUserAddress(final User user) {
        if(user != null) {
            trimUserAddressDetails(user);

            if(StringUtils.isBlank(user.getCountry())) {
                user.setCountry(CommonConstants.COUNTRY_CODE_US);
                user.setState(CommonConstants.DEFAULT_US_STATE);
            } else {
                COUNTRY_CODES.forEach((k, v) -> {
                    if (StringUtils.equalsIgnoreCase(user.getCountry().trim(), k)) {
                        user.setCountry(v);
                    }
                });
            }

            user.setAddressPresent ( StringUtils.isNotEmpty(user.getState()) && StringUtils.isNotEmpty(user.getZip()) &&
                    StringUtils.isNotEmpty(user.getCity()) && StringUtils.isNotEmpty(user.getAddr1())  );
        }

    }

    private static void trimUserAddressDetails(User user) {
        if(StringUtils.isNotBlank(user.getAddr1())) {
            user.setAddr1(user.getAddr1().trim());
        }
        if(StringUtils.isNotBlank(user.getAddr2())) {
            user.setAddr2(user.getAddr2().trim());
        }
        if(StringUtils.isNotBlank(user.getCity())) {
            user.setCity(user.getCity().trim());
        }
        if(StringUtils.isNotBlank(user.getState())) {
            user.setState(user.getState().trim());
        }

        if(StringUtils.isNotBlank(user.getZip())) {
            user.setZip(user.getZip().trim());
        }
    }

    protected static void prepareMultipleUserAddress(final User user) {
        final List<com.b2s.common.services.model.Address> addresses = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(user.getAddresses())) {
            user.getAddresses().forEach(address -> {
                final com.b2s.common.services.model.Address.AddressBuilder addressBuilder =
                    com.b2s.common.services.model.Address
                        .builder()
                        .withAddressId(address.getAddressId())
                        .withAddress1(address.getAddress1())
                        .withAddress2(address.getAddress2())
                        .withCity(address.getCity())
                        .withState(address.getState())
                        .withPhoneNumber(address.getPhoneNumber())
                        .withZip5(address.getZip5())
                        .withZip4(address.getZip4());

                if (StringUtils.isBlank(address.getCountry())) {
                    addressBuilder.withCountry(CommonConstants.COUNTRY_CODE_US);
                    addressBuilder.withState(CommonConstants.DEFAULT_US_STATE);
                } else {
                    COUNTRY_CODES.forEach((k, v) -> {
                        if (StringUtils.equalsIgnoreCase(address.getCountry().trim(), k)) {
                            addressBuilder.withCountry(v);
                        }
                    });
                }
                addresses.add(addressBuilder.build());
            });
            user.setAddresses(addresses);

            final com.b2s.common.services.model.Address address = addresses.get(0);
            final String postalCode =
                (StringUtils.isNotBlank(address.getZip5()) && (StringUtils.isNotBlank(address.getZip4()))) ?
                    address.getZip5() + "-" + address.getZip4() : address.getZip5();
            user.setAddr1(address.getAddress1());
            user.setAddr2(address.getAddress2());
            user.setCity(address.getCity());
            user.setState(address.getState());
            user.setCountry(address.getCountry());
            user.setPhone(address.getPhoneNumber());
            user.setZip(postalCode);
            user.setAddressPresent(StringUtils.isNotEmpty(address.getState())
                && StringUtils.isNotEmpty(address.getZip5())
                && StringUtils.isNotEmpty(address.getCity())
                && StringUtils.isNotEmpty(address.getAddress1()));
        }
    }

    /**
     * in case of FIVE BOX
     *
     * @param request
     * @param user
     * @return
     */
    public User selectLocalUser(final HttpServletRequest request, final User user) {
        try {
            // in this method we're setting 'user' object fields
            updateUser(request, user, null, false);

            request.getSession().setAttribute(ExternalUrlConstants.PURCHASE_POST_URL,
                    applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL));
            final User returnUser = getDemoUserFromDB(user,
                    user.getProgramId(),
                    user.getVarId(),
                    user.getUserId(),
                    demoUserDao,
                    getValueFromRequest(CommonConstants.USER_PD, request));

            return returnUser;
        } catch (final Exception ex) {
            LOG.error("Unknown Error", ex);
        }
        return null;
    }

    /**
     * in case of userid equals to 'Anonymous'
     *
     * @param request
     * @param user
     * @param anonymous
     * @return
     */
    public User updateUser(final HttpServletRequest request, final User user, final String country, final boolean anonymous) {
        user.setUserId(getValueFromRequest(CommonConstants.USER_ID, request));
        user.setProgramId(getValueFromRequest(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE, request));
        user.setVarId(getValueFromRequest(CommonConstants.VAR_ID, request));
        final String localeFromRequest = getValueFromRequest(CommonConstants.LOCALE, request);
        final Locale locale = StringUtils.isNotBlank(localeFromRequest) ? LocaleUtils.toLocale(localeFromRequest) : null;
        user.setLocale(locale);
        user.setCountry(country);
        user.setAnonymous(anonymous);
        user.setAgentBrowse(Objects.nonNull(request.getParameter(CommonConstants.AGENT_BROWSE)) ?
            BooleanUtils.toBooleanObject(request.getParameter(CommonConstants.AGENT_BROWSE)) : false);

        return user;
    }

    @Override
    public boolean isValidLogin(final User user, final Program program) {
        return true;
    }

    @Override
    public void computePricingModel(final Product product, final User user, final Program program) {
        LOG.debug("Apple Product Detail: Skipping Pricing model calculation");
    }

    @Override
    public Map<String, Map<String, String>> getAllActivationFees(final String varId, final String programId) {

        return new HashMap<>();
    }

    protected void applyDiscountCode(final HttpServletRequest request, final User user, final String discountCode) {
        final CouponDetails couponDetails = discountServiceClient.getValidDiscountCode(discountCode, user);
        if (couponDetails != null && couponDetails.isValid()) {
            final Cart cart = new Cart();
            Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            if (Objects.isNull(program)) {
                program = programService.getProgram(user.getVarId(), user.getProgramId(), user.getLocale());
                request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
            }
            cart.setShippingAddress(AddressMapper.getAddress(user, program));
            final DiscountCode discount =
                    new DiscountCode(discountCode, couponDetails.getFriendlyNameDesc(), couponDetails.getCouponDesc(),
                            couponDetails.getTypeOffer(), couponDetails.getAmountOff());
            cart.addDiscount(discount);
            user.setDiscounts(cart.getDiscounts());
            request.getSession().setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
        } else {
            LOG.error("Error while applying discount code: {} in cart. Message: {}", discountCode,
                    (Objects.nonNull(couponDetails) && Objects.nonNull(couponDetails.getCouponError())) ?
                            couponDetails.getCouponError().getErrorMessage():"Unknown Error");
        }
    }

    protected void applyDiscountFromUrl(final HttpServletRequest httpServletRequest, final User user) {
        if(httpServletRequest != null) {
            XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
            Object discountCodeKeyObj = applicationProperties.get(CommonConstants.DISCOUNTCODE_QUERY_PARAM_OTP);
            if(discountCodeKeyObj != null) {
                String discountCode = request.getParameter(discountCodeKeyObj.toString());
                if(StringUtils.isNotBlank(discountCode)) {
                    applyDiscountCode(request, user, discountCode);
                }
            }

        }
    }

    protected void addOrUpdateExternalUrls(final HttpServletRequest request, final Program program, final String locale, final CommonConstants.LoginType loginType) {
        //get existing external_urls
        final String hostName = getHostName(request);
        final HashMap<String, Serializable> tempExternalUrls = (HashMap<String, Serializable>)request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);
        HashMap<String, Serializable> externalUrls = programService.addOrUpdateExternalUrls(program, loginType, hostName, locale, tempExternalUrls);
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    protected void addUrlsFromProperties(final HttpServletRequest request, final User user, final String key) {
        Map<String, String> externalUrls = new HashMap<>();
        //get existing external_urls
        final Map<String, String> tempExternalUrls = (Map<String, String>)request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);
        if(tempExternalUrls!=null){
            externalUrls = tempExternalUrls;
        }

        String url = null;
        if(user != null && StringUtils.isNotBlank(user.getVarId()) && StringUtils.isNotBlank(user.getProgramId())) {
            url = applicationProperties.getProperty(user.getVarId().toLowerCase()+ '.'+user.getProgramId().toLowerCase()+ '.' +key);
        }
        if (StringUtils.isEmpty(url) && user != null && StringUtils.isNotBlank(user.getVarId())) {
            url = applicationProperties.getProperty(user.getVarId().toLowerCase() + '.' + key);
        }
        externalUrls.put(key, url);
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, (HashMap<String, String>) externalUrls);
    }

    public boolean performAPIPostBack(final Order order, final User user, final Program program){
        return true;
    }

    public void initializeLocaleDependents(final HttpServletRequest request, final User user, final String defaultLocale, final String country) {

        final Locale locale = getLocale(request, defaultLocale);
        if(Objects.nonNull(country)) {
            user.setCountry(country);
        }
        else {
            user.setCountry(locale.getCountry());
        }

        user.setLocale(locale);
    }

    protected static Locale getLocale(final HttpServletRequest request, final String defaultLocale) {
        String localStr = "";
        if (request.getAttribute(CommonConstants.LOCALE) != null) {
            localStr = (String) request.getAttribute(CommonConstants.LOCALE);
        }

        if (StringUtils.isBlank(localStr)) {
            localStr = request.getParameter(CommonConstants.LOCALE);
        }
        if (StringUtils.isBlank(localStr)) {
            localStr = defaultLocale;
            request.setAttribute(CommonConstants.LOCALE, localStr);
        }

        return SetLocaleSupport.parseLocale(localStr);
    }

    public Program getProgram(final User user)
        throws B2RException {
        final Program program = programService.getProgram(user.getVarId(), user.getProgramId(), user.getLocale());

        if (Objects.isNull(program) || !program.getIsActive()) {
            throw new B2RException("Program doesn't exist or inactive for programId " + user.getProgramId());
        }

        return program;
    }

    public void updateAdditionalAttributesInSession(final HttpServletRequest request, final Program program, final User user, final CommonConstants.LoginType loginType) throws B2RException {
        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);

        //Bag Menu URLs from DB is having less precedence than SAML response
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);
        if (CommonConstants.LoginType.ANONYMOUS == loginType) {
            getAnonymousTimeOutUrl(request, program);
        }
    }

    /**
     * This method returns false if user address is changed
     *
     * @param order
     * @param user
     * @return
     */
    protected boolean isDefaultAddress(final Order order, final User user) {
        if(order != null && user != null && user instanceof UserCiti) {
            Address transformedAddressLines = new Address();
            getTransformedAddressLines(transformedAddressLines,(UserCiti) user);
            return compareString(transformedAddressLines.getAddress1(), order.getAddr1())
                    && compareString(transformedAddressLines.getAddress2(), order.getAddr2())
                    && compareString(transformedAddressLines.getAddress3(), order.getAddr3())
                    && compareString(user.getCity(), order.getCity())
                    && compareString(user.getState(), order.getState())
                    && compareString(user.getCountry(), order.getCountry())
                    && compareString(user.getZip(), order.getZip());
        }
        return false;
    }

    private Address getTransformedAddressLines(Address address, final UserCiti user){
        address.setAddress1(user.getAddr1());
        address.setAddress2(user.getAddr2());
        address.setAddress3(user.getAddr3());
        AddressMapper.transformCitiUserAddress(address,user);
        if (CommonConstants.COUNTRY_CODE_TH.equalsIgnoreCase(user.getVarId()) && StringUtils.isNotBlank(user.getAddr4())) {
            address.setAddress3(Stream.of(address.getAddress3(),user.getAddr4()).filter(s -> StringUtils.isNotEmpty(s)).collect(Collectors.joining(", ")));
        }
        return address;
    }

    protected boolean compareString(final String first, final String second) {
        if (replaceNullString(first).equalsIgnoreCase(replaceNullString(second))) {
            return true;
        }
        return false;
    }

    /**
     * Apply ignore profile VAR Program config to user.
     * This method clear the User Address if the ignoreProfileAddress is set  true
     * If the VAR has additional address line please clear them in the VAR specific Manager
     * @param user    the user
     * @param program the program
     */
    public boolean applyIgnoreProfileToUser(User user, final Program program) {
        //Defaults to "false"
        if (null == program) {
            return false;
        }
        boolean ignoreProfileAddress = (Boolean) program.getConfig().getOrDefault(CommonConstants.IGNORE_PROFILE_ADDRESS, Boolean.FALSE);
        //Clear the User Address if the ignoreProfileAddress = true
        if (ignoreProfileAddress) {
            user.setAddr1(null);
            user.setAddr2(null);
            user.setCity(null);
            user.setStateNull();
            user.setZip(null);
        }
        return ignoreProfileAddress;
    }

    /**
     * Method to set Anonymous user parameters
     * @param request HttpServletRequest
     * @return user User
     */
    public User setAnonymousUser(final HttpServletRequest request, final User user) {
        user.setAnonymous(Boolean.valueOf((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE)));
        user.setLocale(LocaleUtils.toLocale((String) request.getAttribute(CommonConstants.LOCALE)));
        user.setVarId(getVARId());
        user.setProgramId((String) request.getAttribute(CommonConstants.PROGRAM_ID));
        user.setPoints(Integer.parseInt((String) request.getAttribute(CommonConstants.POINTS_BALANCE)));
        user.setBrowseOnly((Boolean.valueOf((String)request.getAttribute(CommonConstants.BROWSE_ONLY))));
        user.setAgentBrowse(Objects.nonNull(request.getAttribute(CommonConstants.AGENT_BROWSE)) ?
            BooleanUtils.toBooleanObject((String)request.getAttribute(CommonConstants.AGENT_BROWSE)) : false);
        final String userId = request.getUserPrincipal().getName();
        if(Objects.nonNull(request.getUserPrincipal().getName())) {
            user.setUserId(userId);
        } else {
            user.setUserId(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE);

        }
        return user;
    }

    /**
     * Method is used validate user locale with profile address country
     * @param user
     * @return boolean
     */
    public boolean verifyLocaleWithCountry(final User user) {
        return StringUtils.equalsIgnoreCase(user.getLocale().getCountry(),user.getCountry());
    }

    public User getDemoUserFromDB(final User user, final String programId, final String varId, final String demoUser,
        final DemoUserDao demoUserDao, final String password) {
        final DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId();
        demoUserId.setProgramId(programId);
        demoUserId.setVarId(varId);
        demoUserId.setUserId(demoUser);
        final DemoUserEntity demoUserEntity = demoUserDao
            .findByDemoUserIdAndPassword(demoUserId, EncryptionUtil.encrypt(password));
        return user.select(demoUserEntity);
    }

    /**
     * Method to get External URLs from HttpServletRequest, if not exist, initialize the External URLs map
     * @param request HttpServletRequest
     * @return Map<String, String> externalUrls
     */
    protected HashMap<String, Serializable> getExternalUrlsFromRequest(final HttpServletRequest request){
        HashMap<String, Serializable> externalUrls = (HashMap<String, Serializable>)request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);
        if(Objects.isNull(externalUrls)) {
            externalUrls = new HashMap<>();
        }
        return externalUrls;
    }

    /**
     * Method to get Time_out_url from VarProgramConfigEntity for Anonymous programId and set it in session
     * @param request, varId, programId
     * @return void
     */
    protected void getAnonymousTimeOutUrl(final HttpServletRequest request, final Program program) {
        String timeOutURL = (String) program.getConfig().get(CommonConstants.ANONYMOUS_TIME_OUT_URL);
        if (StringUtils.isNotBlank(timeOutURL)) {
            final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
            externalUrls.put(ExternalUrlConstants.TIME_OUT_URL, getHostName(request) + timeOutURL);
            request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
        }
    }

    protected void setKeystoneUrls(final HashMap<String, Serializable> externalUrls, final String keystoneBaseUrl){
        if(StringUtils.isNotBlank(keystoneBaseUrl)){
            final HashMap<String, Serializable> keystoneUrls = new HashMap<>();
            keystoneUrls.put(ExternalUrlConstants.KEYSTONE_BASE_URL, keystoneBaseUrl);
            setKeystoneBalanceUpdateUrls(keystoneUrls, keystoneBaseUrl);
            setKeystoneKeepAliveUrls(keystoneUrls, keystoneBaseUrl);
            setKeystoneLogOutUrls(keystoneUrls, keystoneBaseUrl);

            externalUrls.put(ExternalUrlConstants.KEYSTONE_URLS, keystoneUrls);
        }
    }

    private void setKeystoneBalanceUpdateUrls(final HashMap<String, Serializable> keystoneUrls, final String keystoneBaseUrl){
        final List<String> keystoneBalanceUpdateUrls = List.of(keystoneBaseUrl + ExternalUrlConstants.B2R_BALANCE_PATH,
                keystoneBaseUrl + ExternalUrlConstants.MERCH_BALANCE_PATH,
                keystoneBaseUrl + ExternalUrlConstants.TRAVEL_BALANCE_PATH);

        LOG.debug("Keystone BalanceUpdateUrls ---- {}", keystoneBalanceUpdateUrls);
        keystoneUrls.put(ExternalUrlConstants.BALANCE_UPDATE, new ArrayList<>(keystoneBalanceUpdateUrls));
    }

    private void setKeystoneKeepAliveUrls(final HashMap<String, Serializable> keystoneUrls, final String keystoneBaseUrl){
        final List<String> keystoneKeepAliveUrls = List.of(keystoneBaseUrl + ExternalUrlConstants.B2R_KEEP_ALIVE_PATH,
                keystoneBaseUrl + ExternalUrlConstants.MERCH_KEEP_ALIVE_PATH,
                keystoneBaseUrl + ExternalUrlConstants.TRAVEL_KEEP_ALIVE_PATH);

        LOG.debug("Keystone KeepAliveUrls ---- {}", keystoneKeepAliveUrls);
        keystoneUrls.put(ExternalUrlConstants.KEEP_ALIVE, new ArrayList<>(keystoneKeepAliveUrls));
    }

    private void setKeystoneLogOutUrls(final HashMap<String, Serializable> keystoneUrls, final String keystoneBaseUrl){
        final List<String> keystoneLogOutUrls = List.of(keystoneBaseUrl + ExternalUrlConstants.B2R_LOG_OUT_PATH);

        LOG.info("Keystone LogOutUrls ---- {}", keystoneLogOutUrls);
        keystoneUrls.put(ExternalUrlConstants.LOG_OUT, new ArrayList<>(keystoneLogOutUrls));
    }
}
