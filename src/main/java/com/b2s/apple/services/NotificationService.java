package com.b2s.apple.services;

import com.b2s.apple.entity.OrderLineAttributeEntity;
import com.b2s.apple.mapper.NotificationMapper;
import com.b2s.apple.model.SubscriptionResponseDTO;
import com.b2s.apple.util.NotificationServiceUtil;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.dao.OrderLineAttributeDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.notification.api.NotificationResponse;
import com.b2s.shop.common.constant.Constant;
import com.b2s.shop.common.order.OrderTransactionManager;
import com.b2s.rewards.model.ShippingMethod;
import com.b2s.spark.api.apple.IAppleSparkEmailService;
import com.b2s.spark.api.apple.to.EmailResponse;
import com.b2s.spark.api.apple.to.IAppleEmailRequest;
import com.b2s.spark.api.apple.to.IAppleEmailResponse;
import com.b2s.spark.client.to.EmailData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.b2s.rewards.common.util.CommonConstants.NotificationName;

/*** Created by rpillai on 8/25/2016.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private ProgramService programService;

    @Autowired
    private OrderTransactionManager manager;

    @Autowired
    private NotificationServiceUtil notificationServiceUtil;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private IAppleSparkEmailService sparkEmailService;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    @Autowired // should plan to remove this
    private OrderAttributeValueDao orderAttributeValueDao;

    @Autowired
    private OrderLineAttributeDao orderLineAttributeDao;

    @Autowired
    private AppleProductServiceClient appleProductServiceClient;

    @Value("${disableSendNotification}")
    private boolean disableSendNotification;

    @Value("${disableShipmentDelayNotification:false}")
    private boolean disableShipmentDelayNotification;

    public void sendAsyncNotification(final Order order, Program program) {
        CompletableFuture.supplyAsync(() ->
                sendNotification(order, program,
                        CommonConstants.NotificationName.CONFIRMATION, null, null))
                .thenAcceptAsync(notificationResponse -> processNotificationResponse(notificationResponse, order, false, 0));

        order.getOrderLines().stream()
                .filter(orderLineAMP -> CommonConstants.SUPPLIER_TYPE_AMP_S.equalsIgnoreCase(orderLineAMP.getSupplierId()))
                .parallel()
                .forEach(orderLineAMP ->
                        CompletableFuture.supplyAsync(() ->
                            sendNotification(order, program,
                                CommonConstants.NotificationName.get(orderLineAMP.getItemId()), null,
                                orderLineAMP.getLineNum()))
                            .thenAcceptAsync(
                                notificationResponse -> processNotificationResponse(notificationResponse, order, true,
                                    orderLineAMP.getLineNum())));
    }

    /**
     * Process Notification request.
     * This method is not called for Confirmation notification.
     * Admin not calling /sendEmail endpoint to resend confirmation email
     *
     * @param notifRequest
     * @param order
     * @param orderLine
     * @return
     */
    public EmailNotification processNotification(final EmailNotification notifRequest, final Order order, final OrderLine orderLine) {
        logger.info("processNotification : started to send notification email");

        try {
            final String locale = order.getLanguageCode() + '_' + order.getCountryCode();
            final Program program =
                    programService.getProgram(order.getVarId(), order.getProgramId(), LocaleUtils.toLocale(locale));

            CommonConstants.NotificationName notificationName = getNotificationName(notifRequest, orderLine);
            if (Objects.isNull(notificationName)) {
                return notifRequest;
            }

            if (isEmailConfigured(locale, notificationName, program)) {
                NotificationResponse notificationResponse = sendNotification(order, program, notificationName, notifRequest.getEmailId(), notifRequest.getLineNum());

                if (Objects.isNull(notificationResponse) ||
                        !Constant.SUCCESS.equalsIgnoreCase(notificationResponse.getStatusMessage()) ) {
                    setEmailNotificationToError(notifRequest,
                            CommonConstants.StatusChangeQueueProcessStatus.FAILED,
                            "Error in SparkPost response");
                    throw new ServiceException(ServiceExceptionEnums.EMAIL_FAILURE);
                } else {
                    setEmailNotificationToError(notifRequest,
                            CommonConstants.StatusChangeQueueProcessStatus.EMAIL_SENT,
                            "Email sent");
                    if (NotificationName.SERVICE_PLAN.equals(notificationName)) {
                        processNotificationResponse(notificationResponse, order, true, notifRequest.getLineNum());
                    }
                }
            } else {
                setEmailNotificationToError(notifRequest,
                        CommonConstants.StatusChangeQueueProcessStatus.EMAIL_NOT_CONF,
                        "Email not configured");
                logger.info(
                    "processNotification: Email not configured in DB for orderId: {}, lineNum: {} and orderStatus: {}",
                    notifRequest.getOrderId(), notifRequest.getLineNum(), notifRequest.getOrderStatus());
            }
            notifRequest.setMessage(Constant.SUCCESS);
        } catch (final Exception e) {
            setEmailNotificationToError(notifRequest,
                    CommonConstants.StatusChangeQueueProcessStatus.FAILED,
                    e.getMessage());
            logger.error("Error sending email notification", e);
        }
        return notifRequest;
    }

    /**
     * This method sends order related email notification
     *
     * @param order
     * @param program
     * @return
     */
    private NotificationResponse sendNotification(final Order order, Program program,
                                                  final CommonConstants.NotificationName notifName, final String emailRecipient, final Integer lineNum) {
        NotificationResponse notificationResponse = null;
        try {
            logger.info("OrderTransactionManger.sendNotification(): Generating {} Notification for order: {}",
                    notifName, order.getOrderId());

            Optional<Notification> notificationOpt =
                    notificationServiceUtil.getEmailNotification(program, notifName);
            if (notificationOpt.isPresent()) {
                ResponseEntity<EmailResponse> emailResponse = sendDataEmail(order, program, notifName, lineNum, emailRecipient);
                if (emailResponse != null && emailResponse.getStatusCode().equals(HttpStatus.OK)) {
                    logger.info("OrderTransactionManger.sendNotification(): {} Response from sparkpost: {}",
                            notifName.value, emailResponse);
                    notificationResponse = new NotificationResponse();
                    if (emailResponse.getBody() != null && emailResponse.getBody().getResult() != null) {
                        notificationResponse
                            .setNotificationId(Long.valueOf(emailResponse.getBody().getResult().getId()));
                        notificationResponse.setStatusMessage(Constant.SUCCESS);
                    }
                }
            } else{
                logger.error(
                        "Order Placement: Failed to send Confirmation email notification - Unable to find notification " +
                                "template for varid-{} and programId-{}",
                        order.getVarId(), order.getProgramId());
            }
        } catch (final Exception x) {
            logger.error("Order Placement: Failed to send confirmation email", x);
            return null;
        }
        return notificationResponse;
    }

    private void setEmailNotificationToError(EmailNotification statusChangeQueue,
                                             final CommonConstants.StatusChangeQueueProcessStatus status,
                                             final String description) {
        statusChangeQueue.setProcessStatus(status.toString());
        statusChangeQueue.setProcessDescription(description);
        statusChangeQueue.setMessage(description);
        statusChangeQueue.setProcessDate(new Date());
    }

    private NotificationName getNotificationName(final EmailNotification notifRequest, final OrderLine orderLine) {
        NotificationName notificationName = null;

        if (Objects.isNull(orderLine)) {
            notificationName = NotificationName.CONFIRMATION;
        } else if (CommonConstants.SUPPLIER_TYPE_AMP_S.equals(orderLine.getSupplierId())) {
            notificationName = NotificationName.get(orderLine.getItemId());
        } else {
            if (CommonConstants.ORDER_STATUS_COMPLETED == notifRequest.getOrderStatus()) {
                if (CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S.equals(orderLine.getSupplierId())) {
                    notificationName = NotificationName.SERVICE_PLAN;
                } else {
                    if (ShippingMethod.ELECTRONIC.getLabel().equalsIgnoreCase(orderLine.getShippingMethod())) {
                        notificationName = CommonConstants.NotificationName.ECERT;
                    } else {
                        notificationName = NotificationName.SHIPMENT;
                    }
                }
            } else {
                notificationName = NotificationName.SHIPMENT_DELAY;
            }
        }
        return notificationName;
    }

    private boolean isEmailConfigured(final String locale, final CommonConstants.NotificationName notificationName,
        final Program program) {
        boolean isConfigured = false;

        // Service Plan & AMP do not have necessarily a notification configured at program level
        if (CommonConstants.NotificationName.SERVICE_PLAN.equals(notificationName) ||
                AppleUtil.isNotificationAmp(notificationName)) {
            isConfigured = true;
        } else if (CollectionUtils.isNotEmpty(program.getNotifications())) {
            isConfigured = program.getNotifications().stream()
                    .anyMatch(emailConfig ->
                            locale.equalsIgnoreCase(emailConfig.getLocale()) &&
                            notificationName.name().equalsIgnoreCase(emailConfig.getName()));
        }
        return isConfigured;
    }

    private void processNotificationResponse(final NotificationResponse notificationResponse, final Order order,
        final boolean isAmp, final Integer lineNum) {
        logger.info(
            "OrderTransactionManger.processNotificationResponse(): Processing Notification Response for order: {}",
            order.getOrderId());
        if (notificationResponse != null && notificationResponse.getStatusMessage().toLowerCase().contains("success")) {
            try {
                notificationServiceUtil.updateNotificationByKey(order.getOrderId(), notificationResponse.getNotificationId(),
                    isAmp, lineNum);
            } catch (Exception e) {
                logger.error(
                    "Order Placement: Error while updating notification id {} in order line table for order id {}, " +
                        "exception: {}",
                    notificationResponse.getNotificationId(), order.getOrderId(), e);
            }
        } else {
            if (notificationResponse == null) {
                logger.error(
                    "Order Placement: We got an empty response from notification service to email: {} for order " +
                        "confirmation email for order #{}",
                    order.getEmail(), order.getOrderId());
            } else {
                logger.error(
                    "Order Placement: We got an error response from notification service for order confirmation email" +
                        " to id:{} for order #{}, status from notification service: {}, error message from " +
                        "notification service: {}",
                    order.getEmail(), order.getOrderId(), notificationResponse.getStatus(),
                    notificationResponse.getStatusMessage());
            }
        }
    }

    public ResponseEntity<EmailResponse> sendDataEmail(final Order order, Program program,
                                                       CommonConstants.NotificationName notificationName,
                                                       Integer lineNum, final String emailRecipient) {
        ResponseEntity<EmailResponse> emailResponse = null;
        String locale = order.getLanguageCode() + "_" + order.getCountryCode();

        Optional<Notification> notificationOpt = notificationServiceUtil.getEmailNotification(program, notificationName);
        if (notificationOpt.isPresent()) {
            final Properties dbProperties = varProgramMessageService.getMessages(Optional.ofNullable(order.getVarId()),
                    Optional.ofNullable(order.getProgramId()), locale);
            EmailData emailData = getEmailData(order, program, notificationName, lineNum, locale,
                    notificationOpt.get(), dbProperties);

            if (Objects.nonNull(emailData)) {
                //set emailRecipient from Admin call if exist otherwise proceed with existing way from order attributes
                final String[] notificationEmailAddress = StringUtils.isNotBlank(emailRecipient) ?
                        new String[]{emailRecipient} : getNotificationEmailAddress(order, program);
                String returnPath = null;
                if (dbProperties != null && !dbProperties.isEmpty()) {
                    returnPath = (String) dbProperties.get(CommonConstants.SPARK_POST_RETURN_PATH);
                }
                final IAppleEmailRequest appleEmailRequest = notificationMapper
                        .populateOrderEmailRequest(notificationEmailAddress, notificationOpt, emailData,
                                true, returnPath);
                IAppleEmailResponse appleEmailResponse = sparkEmailService.sendEmail(appleEmailRequest);
                emailResponse = appleEmailResponse.getEmailResponse();
            }
        }
        return emailResponse;
    }

    private EmailData getEmailData(Order order, Program program, NotificationName notificationName, Integer lineNum,
                                   String locale, Notification notification, Properties dbProperties) {
        EmailData emailData = null;
        if (NotificationName.CONFIRMATION.equals(notificationName)) {
            emailData = notificationMapper.getOrderEmailData(order, program, notification, null,
                    dbProperties, null);
        } else if (NotificationName.SHIPMENT.equals(notificationName) ||
                NotificationName.SHIPMENT_DELAY.equals(notificationName) ||
                NotificationName.SERVICE_PLAN.equals(notificationName) ||
                NotificationName.ECERT.equals(notificationName) ||
                AppleUtil.isNotificationAmp(notificationName)) {
            String subscriptionUrl = null;
            if (AppleUtil.isNotificationAmp(notificationName)) {
                subscriptionUrl = persistAndGetSubscriptionUrl(program, order.getOrderId(), lineNum, locale,
                        dbProperties, notification);
                if (StringUtils.isBlank(subscriptionUrl)) {
                    logger.error("Unable to send AMP Subscription Email: AMP Subscription URL is Empty.");
                    return null;
                }
            }
            emailData = notificationMapper.getOrderEmailData(order, program, notification, lineNum,
                    dbProperties, subscriptionUrl);
        }
        return emailData;
    }

    private String[] getNotificationEmailAddress(Order order, Program program) {
        List<String> emailRecipientList = new ArrayList<>();
        if (program.getConfig().get(CommonConstants.PROFILE_EMAIL_NOTIFICATION) != null &&
                Boolean.parseBoolean(program.getConfig().get(CommonConstants.PROFILE_EMAIL_NOTIFICATION).toString())) {
            getEmailRecipientsForProfileNotification(order, emailRecipientList);
        } else {
            emailRecipientList.add(order.getEmail());
            // To set Parents email addresses for LAUSD
            getEmailRecipientsForLAUSD(order, emailRecipientList);
        }
        return emailRecipientList.toArray(new String[emailRecipientList.size()]);
    }

    private void getEmailRecipientsForProfileNotification(Order order, List<String> emailRecipientList) {
        List<OrderAttributeValue> orderAttrbuteList = orderAttributeValueDao.getByOrder(order.getOrderId());
        for (OrderAttributeValue orderAttrubute : orderAttrbuteList) {
            if (orderAttrubute.getName().equalsIgnoreCase(CommonConstants.EmailType.SHIPPING.getValue()) ||
                orderAttrubute.getName().equalsIgnoreCase(CommonConstants.EmailType.PROFILE.getValue())) {
                emailRecipientList.add(orderAttrubute.getValue());
            }
        }
    }

    private void getEmailRecipientsForLAUSD(Order order, List<String> emailRecipientList) {
        List<OrderAttributeValue> orderAttributeList = orderAttributeValueDao.getByOrder(order.getOrderId());
        if(CollectionUtils.isNotEmpty(orderAttributeList)){
            for (OrderAttributeValue orderAttribute : orderAttributeList) {
                if(orderAttribute.getName().contains(CommonConstants.PARENT_EMAIL)){
                    emailRecipientList.add(orderAttribute.getValue());
                }
            }
        }
    }

    /**
     * Logic to retrieve Subscription URL and persist the Subscription info
     * useStaticLink - true -> fetch from VPM table
     * useStaticLink - false -> Call PS to retrieve the info
     * Persist the info in Order Line Attribute table
     * Admin Flow(Resend AMP email) - call PS only if subscriptionUrl not persisted in Order Line Attributes table
     */
    private String persistAndGetSubscriptionUrl(final Program program, final Long orderId, final Integer lineNum,
        final String locale, final Properties dbProperties, final Notification notification) {
        String subscriptionUrl = null;
        try {
            final List<OrderLineAttributeEntity> subscriptionURLs = orderLineAttributeDao
                .findByOrderIdAndLineNumAndName(orderId, lineNum, CommonConstants.SUBSCRIPTION_URL);
            if (CollectionUtils.isNotEmpty(subscriptionURLs)) {
                subscriptionUrl = subscriptionURLs.stream()
                    .filter(orderLineAttributeEntity -> StringUtils.isNotBlank(orderLineAttributeEntity.getValue()))
                    .findAny()
                    .map(OrderLineAttributeEntity::getValue)
                    .orElse(null);
            }
            if (StringUtils.isBlank(subscriptionUrl)) {
                Optional<AMPConfig> ampConfigOpt = program.getAmpSubscriptionConfig().parallelStream()
                    .filter(ampConfig -> ampConfig.getItemId().equalsIgnoreCase(notification.getName()))
                    .findAny();
                if (ampConfigOpt.isPresent()) {
                    final AMPConfig ampConfig = ampConfigOpt.get();
                    logger.info("Adding Order Line Attributes for Subscription Line item...");
                    if (ampConfig.getUseStaticLink()) {
                        subscriptionUrl =
                            persistVPMSubscriptionUrl(orderId, lineNum, locale, dbProperties, ampConfig,
                                program);
                    } else {
                        subscriptionUrl = persistSubscriptionDataFromPS(orderId, lineNum, locale, notification);
                    }
                } else {
                    logger.error(
                        "Unable to send AMP Subscription Email: Subscription is not mapped in AMP table for {}, " +
                            "OrderId: {}, OrderLineNum: {}, Var: {}, Program: {}, Locale: {}.",
                        notification.getName(), orderId, lineNum, program.getVarId(), program.getProgramId(), locale);
                }
            }
        } catch (final Exception ex) {
            logger.error(
                "Unable to send AMP Subscription Email: Error while retrieving AMP Subscription URL for {}, OrderId: " +
                    "{}, OrderLineNum: {}, Var: {}, Program: {}, Locale: {}. Error Message: {}.",
                notification.getName(), orderId, lineNum, program.getVarId(), program.getProgramId(), locale,
                ex.getMessage(), ex);
        }
        return subscriptionUrl;
    }

    /**
     * Persist Subscription URL in Order Line Attribute from Var Program Message if use_static_link is TRUE
     * Use case:useStaticLink - true -> fetch from VPM table
     */
    private String persistVPMSubscriptionUrl(final Long orderId, final Integer lineNum, final String locale,
                                             final Properties dbProperties, final AMPConfig subscriptionItem,
                                             final Program program) {
        String subscriptionUrl = null;
        String subscriptionItemId = subscriptionItem.getItemId();

        if (Objects.nonNull(dbProperties) && !dbProperties.isEmpty()) {
            //Persisting AMP Subscription URL to order line attribute
            if (Objects.nonNull(subscriptionItem.getDuration())) {
                subscriptionUrl = (String) dbProperties.get(subscriptionItemId + "-" + subscriptionItem.getDuration()
                        + "-" + CommonConstants.SPARK_POST_AMP_STATIC_LINK);
            } else {
                subscriptionUrl =
                        (String) dbProperties.get(subscriptionItemId + "-" + CommonConstants.SPARK_POST_AMP_STATIC_LINK);
            }
            persistOrderLineAttribute(orderId, lineNum, CommonConstants.SUBSCRIPTION_URL, subscriptionUrl);
        }
        if (StringUtils.isBlank(subscriptionUrl)) {
            logger.error(
                    "Unable to send AMP Subscription Email: Subscription URL is not configured in VPM table for {}, " +
                            "OrderId: {}, OrderLineNum: {}, Var: {}, Program: {}, Locale: {}.",
                    subscriptionItemId, orderId, lineNum, program.getVarId(), program.getProgramId(), locale);
        }
        return subscriptionUrl;
    }

    /**
     * Retrieve Subscription Information from PS call and Persist the same in Order Line Attributes table
     * Use case:useStaticLink - false -> Call PS to retrieve the info
     */
    private String persistSubscriptionDataFromPS(final Long orderId, final Integer lineNum, final String locale,
        final Notification notification) {
        String subscriptionUrl = null;

        final SubscriptionResponseDTO subscriptionResponseDTO = appleProductServiceClient
            .getSubscriptionInfo(notification.getName(), LocaleUtils.toLocale(locale).getDisplayLanguage());
        if (Objects.nonNull(subscriptionResponseDTO)) {
            logger.info("Persist Subscription from PS Response to Order Line Attributes Table.");
            subscriptionResponseDTO.getExpirationDateTime().ifPresent(
                date -> persistOrderLineAttribute(orderId, lineNum, CommonConstants.EXPIRATION_DATE_TIME,
                        date.toString())
            );
            subscriptionResponseDTO.getRemainingCount().ifPresent(
                remainingCount -> persistOrderLineAttribute(orderId, lineNum, CommonConstants.REMAINING_COUNT,
                        remainingCount.toString())
            );
            subscriptionUrl = subscriptionResponseDTO.getRedemptionUrl();
            persistOrderLineAttribute(orderId, lineNum, CommonConstants.SUBSCRIPTION_URL, subscriptionUrl);
        } else {
            logger.error("Unable to send AMP Subscription Email: {}, OrderId: {}, OrderLineNum: {}, Locale: {}.",
                notification.getName(), orderId, lineNum, locale);
        }
        return subscriptionUrl;
    }

    private void persistOrderLineAttribute(final Long orderId, final Integer lineNum, final String name,
        final String value) {
        final OrderLineAttributeEntity orderLineAttribute = new OrderLineAttributeEntity();
        orderLineAttribute.setOrderId(orderId);
        orderLineAttribute.setLineNum(lineNum);
        orderLineAttribute.setName(name);
        if (StringUtils.isNotBlank(value)) {
            orderLineAttribute.setValue(value);
            orderLineAttributeDao.save(orderLineAttribute);
        }
    }
}
