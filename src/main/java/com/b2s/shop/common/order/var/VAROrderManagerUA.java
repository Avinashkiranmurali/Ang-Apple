package com.b2s.shop.common.order.var;

import com.b2s.apple.services.CartOrderConverterService;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.integration.model.Address;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.integration.model.UA.PromotionalSubscription;
import com.b2s.rewards.apple.integration.model.UA.UASubscriptionResponse;
import com.b2s.rewards.apple.integration.model.UserInformation;
import com.b2s.rewards.apple.model.OrderAttributeValue;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.common.order.util.OAuthRequestParam;
import com.google.common.base.MoreObjects;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.replaceNullString;

/**
 * Created by rpillai on 9/27/2016.
 * <p/>
 * Class used to interact with UA for retrieving user information and Place/update/cancel orders via VIS. It
 */
@Component("varOrderManagerUA")
public class VAROrderManagerUA extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerUA.class);

    @Autowired
    @Qualifier("httpClientUtil")
    private HttpClientUtil httpClient;

    private static final String VAR_ID = "UA";

    private static final String PROGRAM_ID_DEFAULT="MP";
    SecureRandom random = CommonConstants.SECURE_RANDOM;

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws  B2RException{
        User user = null;
        Program program = null;
        CommonConstants.LoginType loginType = null;
        try {
            if(StringUtils.isNotBlank(request.getParameter(OAuthRequestParam.ONLINE_AUTH_CODE.getValue()))) {
                user = getUser(request);
                initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);

                //VIS call to get user profile
                final AccountInfo accountInfo = getAccountInfo(user, program.getIsLocal(),
                    varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl);


                if(Objects.nonNull(accountInfo)) {
                    // createUserObject from VIS response
                    createUserObject(accountInfo,user);

                } else {
                    LOGGER.error("AccountInfo from VIS is empty for user id = {} / varId = {}", user.getUserId(),user.getVarId());
                    return null;
                }
                loginType = CommonConstants.LoginType.OAUTH;

            }else if(request.getParameter(CommonConstants.USER_ID).toLowerCase().contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())){
                user = updateUser(request, new User(), null, true);
                loginType = CommonConstants.LoginType.ANONYMOUS;
            }
            else {
                user = selectLocalUser(request, new User());
                user.setPricingTier(request.getParameter(CommonConstants.PRICING_TIER));
                if(StringUtils.isNotBlank(user.getPricingTier())){
                    user.getAdditionalInfo().put(CommonConstants.PRICING_TIER, user.getPricingTier());
                }
                loginType = CommonConstants.LoginType.FIVEBOX;
            }

            user.setLoginType(loginType.getValue());

            //Select program information from database
            if(Objects.isNull(program)){
                initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);
            }
            program.setPricingTier(user.getPricingTier());
            initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
            if (MapUtils.isNotEmpty(program.getConfig()) && StringUtils.isBlank((String) program.getConfig().get(CommonConstants.SIGNIN_URL_KEY))) {
                program.getConfig().put(CommonConstants.SIGNIN_URL_KEY, AppleUtil.getHostName(request) + "/auth/login");
            }
            updateAdditionalAttributesInSession(request, program, user, loginType);

            //Set Bag Menu Urls based on Precedence
            setBagMenuUrls(request);
            setDefaultCountryCode(user);
            setSessionTimeOut(request, user);
            prepareUserAddress(user);
            getPromotionalSubscription(user, program);


            if(program.getIsLocal()){
                final String promotion = request.getParameter(CommonConstants.PROMOTION);
                user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX, promotion);
                user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED,promotion);
            }

        } catch (final RuntimeException e) {
            LOGGER.error("Error while user log in for united airlines", e);
            // On event of exception, return null to show error page
            return null;
        }

        return user;
    }

    private void setDefaultCountryCode(User user) {
        // ASVO-6 : if Country Comes as empty add country code to locale country code otherwise default to 'US'
        // Latest : D-02338  .  Default country to US
        if (StringUtils.isBlank(replaceNullString(user.getCountry())) || !user.getCountry().equalsIgnoreCase(CommonConstants.COUNTRY_CODE_US)) {
            LOGGER.info("UA country defaulted to US ");
            user.setCountry(CommonConstants.COUNTRY_CODE_US);
        }
    }

    /**
     * Set Bag Menu Urls based on Precedence.
     * SAML response is having higher precedence
     * DB(var_program_config) is next level
     * Value for Application properties is having least preference
     */
    private void setBagMenuUrls(final HttpServletRequest request) {
        // Set Existing SignOut and NavigateBackUrl from Application Properties
        setSignOutUrl(request);
        setNavigateBackUrl(request);
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            final Message msg = new Message();
            final RedemptionResponse redemptionResponse;
            if(user.getAdditionalInfo() == null) {
                user.setAdditionalInfo(new HashMap<>());
            }
            String bankTransactionId = order.getOrderId().toString();
            if(bankTransactionId.length() > 9) {
                bankTransactionId = bankTransactionId.substring(bankTransactionId.length() - 9, bankTransactionId.length());
            }
            user.getAdditionalInfo().put(CommonConstants.BANK_TRANSACTION_ID, bankTransactionId);

            if (program != null && program.getIsLocal()) {
                //If demo user
                redemptionResponse = varIntegrationServiceLocalImpl.performRedemption(order, user, program);
            } else {
                //Redemption from VIS
                redemptionResponse = varIntegrationServiceRemoteImpl.performRedemption(order, user, program);
            }

            if (redemptionResponse == null) {
                return false;
            }

            //invoke update Promotional Subscription - remoteOnly
            if (Objects.nonNull(program)) {
                final String strChannelCode = (String) program.getConfig().get(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY);
                if (!program.getIsLocal() && StringUtils.isNotBlank(strChannelCode)) {
                    processUpdatePromotionalSubscription(user, strChannelCode);
                }
            }

            //assign/update order with the redemption response
            order.setVarOrderId(redemptionResponse.getVarOrderId());

            //This method also sets order on hold based Fraud status
            if (isFraudStatusDeclined(order, msg, redemptionResponse)){
                return false;
            }

            //adjust user points
            getBalanceUserPoints(order,user);

            setMessageSuccess(order, msg);

            message = msg;

        } catch (final Exception ex) {
            placeOrderExceptionHandler(order, user, ex);
        }

        return message.isSuccess();
    }

    //This method sets order on hold based Fraud status
    private boolean isFraudStatusDeclined(Order order, Message msg, RedemptionResponse redemptionResponse) {
        if(MapUtils.isNotEmpty(redemptionResponse.getAdditionalInfo())) {
            String fraudStatus = (String)redemptionResponse.getAdditionalInfo().get("fraudStatus");
            if(StringUtils.isNotBlank(fraudStatus)) {
                if(CommonConstants.UAHoldQueueStatus.INREVIEW.getValue().equals(fraudStatus)) {
                    LOGGER.info("The order having order id: {} is on hold from UA", order.getOrderId());
                    msg.setOrderOnHold(true);
                } else if(CommonConstants.UAHoldQueueStatus.DECLINED.getValue().equals(fraudStatus)) {
                    LOGGER.error("The order having order id: {} has been declined from UA", order.getOrderId());
                    return true;
                }
            } else {
                msg.setOrderOnHold(true);
            }
        } else {
            msg.setOrderOnHold(true);
        }
        return false;
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, Program program) {
        order.setOrderAttributeValues(new ArrayList<>());

        String bankTransactionId = order.getOrderId().toString();
        if (bankTransactionId.length() > 9) {
            bankTransactionId = bankTransactionId.substring(bankTransactionId.length() - 9, bankTransactionId.length());
        }
        final OrderAttributeValue orderAttribute =
            CartOrderConverterService.buildOrderAttribute("bankTransactionID", bankTransactionId, order.getOrderId());
        if (Objects.nonNull(orderAttribute)) {
            order.getOrderAttributeValues().add(orderAttribute);
        }
        return performCancelRedemption(order, program);
    }

    @Override
    public int getUserPoints(final User user, Program program) throws B2RException {

        if (program != null && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            final Optional<String> sid=Optional.ofNullable(user.getSid());
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
        }

    }

    @Override
    protected User getUser(final HttpServletRequest request){
        final User user =new User();

        final String varId=request.getParameter(OAuthRequestParam.VARID.getValue());
        final String memberId=request.getParameter(OAuthRequestParam.MEMBER_ID.getValue());
        final String programId=request.getParameter(OAuthRequestParam.PROGRAM_ID.getValue());

        user.setVarId(StringUtils.isBlank(varId) ? VAR_ID : varId);
        user.setProgramId(StringUtils.isBlank(programId) ? PROGRAM_ID_DEFAULT : programId);
        //Dummy userID, VIS don't care about user id since we pass auth token
        user.setUserId(StringUtils.isBlank(memberId) ? "123456789":memberId);

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.LOGIN_TIME, new SimpleDateFormat(CommonConstants.DATE_TIME_FORMAT).format(new Date()));
        additionalInfo.put(CommonConstants.ONLINE_AUTH_CODE, request.getParameter(OAuthRequestParam.ONLINE_AUTH_CODE.getValue()));
        additionalInfo.put(CommonConstants.OFFLINE_AUTH_CODE, request.getParameter(OAuthRequestParam.OFFLINE_AUTH_CODE.getValue()));

        return user;
    }

    public void createUserObject(final AccountInfo accountInfo,final User user){

        final UserInformation userInfo = accountInfo.getUserInformation();
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setEmail(userInfo.getEmailAddresses().length > 0 ? userInfo.getEmailAddresses()[0].getEmail() : "");
        user.setPhone(userInfo.getPhoneNumbers().length > 0 ? (userInfo.getPhoneNumbers())[0].getNumber() : "");

        final Address address = userInfo.getAddress();

        if (Objects.nonNull(address)) {
            user.setAddr1(MoreObjects.firstNonNull(address.getLine1(), ""));
            user.setAddr2(MoreObjects.firstNonNull(address.getLine2(), ""));
            user.setCity(MoreObjects.firstNonNull(address.getCity(), ""));
            user.setZip(MoreObjects.firstNonNull(address.getPostalCode(), ""));
            user.setState(MoreObjects.firstNonNull(address.getStateCode(), ""));
            user.setCountry(MoreObjects.firstNonNull(address.getCountryCode(), ""));
        }

        if(!accountInfo.getUserInformation().getAdditionalInfo().isEmpty()) {
            user.getAdditionalInfo().putAll(accountInfo.getUserInformation().getAdditionalInfo());
            user.getAdditionalInfo().put(CommonConstants.PRICING_TIER, accountInfo.getPricingTier());
            if (accountInfo.getUserInformation().getAdditionalInfo().containsKey("memberID")) {
                user.setUserId(accountInfo.getUserInformation().getAdditionalInfo().get("memberID"));
            }
            if (accountInfo.getUserInformation().getAdditionalInfo().containsKey("fraudSessionID")) {
                user.setSessionId(accountInfo.getUserInformation().getAdditionalInfo().get("fraudSessionID"));
            }
        }
        user.setPricingTier(accountInfo.getPricingTier());
        // Set points balance
        user.setPoints(accountInfo.getAccountBalance().getPointsBalance());


    }

    private void setSignOutUrl(final HttpServletRequest request){
        //get existing external_urls
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        externalUrls.put(ExternalUrlConstants.SIGN_OUT_URL,
                applicationProperties.getProperty("ua.signOutUrl") + applicationProperties.getProperty("ua.ssoApi.key"));
        externalUrls.put(ExternalUrlConstants.TIME_OUT_URL,
                applicationProperties.getProperty("ua.signOutUrl") + applicationProperties.getProperty("ua.ssoApi.key"));
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }
    private void setNavigateBackUrl(final HttpServletRequest request) {
        //get existing external_urls
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL,
                applicationProperties.getProperty("ua.navigateBackUrl"));
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private boolean isOptedInForAllRequiredSubscription(final UASubscriptionResponse response,final Program program){
        String strChannelCode = (String)program.getConfig().get(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY);
        if(StringUtils.isNotBlank(strChannelCode)){
            Set<String> channelCode= new HashSet(Arrays.asList(strChannelCode.split(",")));
            Long count=response.getSubscriptionData().stream()
                    .filter(subscription -> channelCode.contains(subscription.getSubscription().getCommunicationTypeCode()))
                    .count();

            if(count==channelCode.size()){
                return true;
            }
        }

        return false;
    }

    private void getPromotionalSubscription(final User user,final Program program) {
        //Invoke this only for Remote
        String strChannelCode = (String) program.getConfig().get(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY);
        if (!program.getIsLocal() && StringUtils.isNotBlank(strChannelCode)) {
            PromotionalSubscription subscription = null;
            UASubscriptionResponse response = null;
            StringBuilder url = new StringBuilder();
            url.append(applicationProperties.getProperty(CommonConstants.UA_SERVICE_ENDPOINT))
                    .append(applicationProperties.getProperty(CommonConstants.UA_SERVICE_SID))
                    .append(CommonConstants.SLASH)
                    .append(CommonConstants.UA_SERVICE_GET_SUBSCRIPTION)
                    .append(CommonConstants.QUESTION + CommonConstants.UA_SERVICE_PARAM_TRANSACTION_ID)
                    .append(random.nextDouble())
                    .append(CommonConstants.UA_SERVICE_PARAM_AUTHENTICATION_CODE)
                    .append(user.getAdditionalInfo().get(CommonConstants.ONLINE_AUTH_CODE));

            try {
                response = httpClient.getHttpResponse(url.toString(), UASubscriptionResponse.class, HttpMethod.GET, null);
            } catch (Exception e) {
                LOGGER.error("Error in invoking UA_SERVICE_GET_SUBSCRIPTION: ", e);
            }

            subscription = new PromotionalSubscription();
            if (ObjectUtils.isEmpty(response) || response.getException() != null) {
                subscription.setChecked(false);
                subscription.setDisplayCheckbox(true);
            } else {
                if (response.getSubscriptionData().isEmpty()) {
                    subscription.setChecked(true);
                    subscription.setDisplayCheckbox(true);
                } else if (!isOptedInForAllRequiredSubscription(response, program)) {
                        subscription.setChecked(false);
                        subscription.setDisplayCheckbox(true);
                    }
                }
            user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX, String.valueOf(subscription.isDisplayCheckbox()));
            user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED, String.valueOf(subscription.isChecked()));

        }
    }

    private void processUpdatePromotionalSubscription ( final User user, final String strChannelCode){
        if (Boolean.valueOf(user.getAdditionalInfo().get(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX))) {
            String[] channelCode = strChannelCode.split(",");
            String optInValue = Boolean.parseBoolean(user.getAdditionalInfo().get(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED)) ? "In" : "Out";
            if ("In".equals(optInValue)) {
                for (String commTypeCode : channelCode) {
                    updatePromotionalSubscription(user, optInValue, commTypeCode);
                }
            }
        }
    }


    private void updatePromotionalSubscription(final User user,final String optInValue,final String commTypeCode){
        StringBuilder url=new StringBuilder();
        url.append(applicationProperties.getProperty(CommonConstants.UA_SERVICE_ENDPOINT))
                .append(applicationProperties.getProperty(CommonConstants.UA_SERVICE_SID))
                .append(CommonConstants.SLASH)
                .append(CommonConstants.UA_SERVICE_UPDATE_SUBSCRIPTION)
                .append(CommonConstants.QUESTION+CommonConstants.UA_SERVICE_PARAM_TRANSACTION_ID)
                .append(random.nextDouble())
                .append(CommonConstants.UA_SERVICE_PARAM_AUTHENTICATION_CODE)
                .append(user.getAdditionalInfo().get(CommonConstants.ONLINE_AUTH_CODE))
                .append(CommonConstants.UA_SERVICE_PARAM_OTHERS)
                .append(CommonConstants.UA_SERVICE_PARAM_COMMUNICATION_CODE).append(commTypeCode)
                .append(CommonConstants.UA_SERVICE_PARAM_DISPLAY_AND_OPT_CODE).append(optInValue);

        try {
            httpClient.getHttpResponse(url.toString(), UASubscriptionResponse.class, HttpMethod.POST,null);
        } catch (Exception e) {
            LOGGER.error("Error in invoking UA_SERVICE_UPDATE_SUBSCRIPTION: ",e);
        }
    }

}
