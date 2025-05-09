package com.b2s.apple.mapper

import com.b2s.apple.entity.OrderDiagnosticInfoEntity
import com.b2s.apple.services.ServicePlanInfoService
import com.b2s.apple.services.VarProgramMessageService
import com.b2s.db.model.Order
import com.b2s.db.model.OrderAWP
import com.b2s.db.model.OrderLine
import com.b2s.db.model.OrderLineAttribute
import com.b2s.rewards.apple.dao.OrderAttributeValueDao
import com.b2s.rewards.apple.dao.OrderDiagnosticInfoDao
import com.b2s.rewards.apple.dao.ShipmentNotificationDao
import com.b2s.rewards.apple.dao.ShipmentNotificationDaoImpl
import com.b2s.rewards.apple.model.Notification
import com.b2s.rewards.apple.model.OrderAttributeValue
import com.b2s.rewards.apple.model.OrderLineShipmentNotification
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.ServicePlanData
import com.b2s.rewards.apple.util.ContextUtil
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.spark.api.apple.to.IAppleEmailRequest
import com.b2s.spark.api.apple.to.impl.OrderEmailData
import org.apache.commons.lang.LocaleUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

class NotificationMapperTest extends Specification {

    VarProgramMessageService varProgramMessageService
    ServicePlanInfoService servicePlanInfoService
    NotificationMapper notificationMapper
    Properties applicationProperties=Mock(Properties)
    @Shared
    OrderLine ol1
    @Shared
    OrderLine ol2
    @Shared
    Map map

    void cleanup() {

    }
    void setup() {
        varProgramMessageService= Mock(VarProgramMessageService)
        servicePlanInfoService = Mock(ServicePlanInfoService)
    }

    def setupSpec() {
        OrderLineAttribute olAt1 = new OrderLineAttribute()
        olAt1.setName(CommonConstants.ACTIVATION_FEE)
        olAt1.setValue("120")
        OrderLineAttribute olAt2 = new OrderLineAttribute()
        olAt2.setName(CommonConstants.UPGRADE_FEE)
        olAt2.setValue("50")
        List<OrderLineAttribute> attributeList =  new ArrayList<OrderLineAttribute>();
        attributeList.add(olAt1)
        attributeList.add(olAt2)

        ol1 = new OrderLine(lineNum: 1, supplierId: 20000, supplierItemPrice: -24322, supplierTaxPrice: 0, itemPoints: 254,
            supplierShippingPrice: 0, shippingPoints: 0, quantity: 1,
                orderLineAttributes: new ArrayList<OrderLineAttribute>(), orderLinePoints: 10, varOrderLinePrice: 100)
        ol2 = new OrderLine(lineNum: 2, supplierId: 30002, supplierItemPrice: -20434, supplierTaxPrice: 0, itemPoints: 123,
            supplierShippingPrice: 0, shippingPoints: 0, quantity: 1, attr1: 1227,
                orderLineAttributes: attributeList, orderLinePoints: 20, varOrderLinePrice: 100)
        map = new HashMap();
        map['pricingTemplate'] = 'pd_cash'
    }

    @Unroll
    def 'GetOrderEmailData -- check CreditCard Amount and PayRoll Deduction are getting passed'() {
        setup:
        Properties props = new Properties()
        props.setProperty('image.server.url', 'abcdef.com')
        props.setProperty('sparkpost.order.date.format', defaultOrderDtFmt)
        props.setProperty('sparkpost.shipment.date.format', defaultShipDtFmt)
        props.setProperty('vitalityus.sparkpost.shipment.date.format', shipDtFmt)
        props.setProperty('termsAndConditionsPath', 'https://www.domain.com/documents/apple-employee-portal-terms-and-conditions-en.pdf')
        props.setProperty('lgnTermsOfUseUrl', 'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        props.setProperty('privacyPolicyUrl', 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')
        ShipmentNotificationDao shipmentNotificationDao =  Mock(ShipmentNotificationDao.class)
        ContextUtil contextUtil = Mock(ContextUtil.class)
        MessageSource messageSource = new StaticMessageSource()

        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: shipmentNotificationDao,
                orderAttributeValueDao: Mock(OrderAttributeValueDao.class),
                applicationProperties: props,
                contextUtil: contextUtil,
                shipmentTrackingUrls: new HashMap<String, String>(),
                varProgramMessageService: varProgramMessageService)


        def order = new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: 'en',
            countryCode: 'US', orderLines: new ArrayList<OrderLine>(),
            orderAttributeValues: new ArrayList<OrderAttributeValue>())
        order.orderLines.add(ol1)
        order.orderLines.add(ol2)
        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(map)
        shipmentNotificationDao.getShipmentNotification(_, _) >> new OrderLineShipmentNotification(shipmentDate: new Date())
        contextUtil.getMessageSource(_) >> messageSource
        varProgramMessageService.getMessages(_, _, _) >> props
        messageSource.addMessage("lgnTermsOfUseUrl",Locale.US,'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        messageSource.addMessage("privacyPolicyUrl",Locale.US,'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')


        when:
        OrderEmailData emailData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test Confirmation Email', name: 'CONFIRMATION'), null, props, null)
        OrderEmailData shipmentData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test Shipment Email', name: 'SHIPMENT'), 1, props, null);

        then:
        emailData.paymentTenders.ccPaid == '\$243.22'
        emailData.paymentTenders.payrollPeriodPricing == '\$12.27'
        shipmentData.orderInfo.orderDate == new SimpleDateFormat(defaultOrderDtFmt).format(new Date())
        if (StringUtils.isNotBlank(shipDtFmt)) {
            shipmentData.shipment.shipmentDate == new SimpleDateFormat(shipDtFmt).format(new Date())
        } else {
            shipmentData.shipment.shipmentDate == new SimpleDateFormat(defaultShipDtFmt).format(new Date())
        }

        where:
        varId        | programId     | defaultOrderDtFmt        | defaultShipDtFmt | shipDtFmt
        'VitalityUS' | 'JohnHancock' | 'MMM dd, YYYY hh:mm a z' | 'MM/dd/yyyy'     | 'MMM dd, YYYY'
        'VitalityUS' | 'JohnHancock' | 'MMM dd, YYYY'           | 'MM/dd/yyyy'     | 'MMM dd, YYYY'
        'SCOTIA'     | 'Amex'        | 'MMM dd, YYYY hh:mm a z' | 'MM/dd/yyyy'     | ''
    }

    def 'GetOrderEmailData -- emailData branding object is populated correctly'() {
        setup:
        Properties props = new Properties()
        props.setProperty('image.server.url', 'abcdef.com')
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')
        props.setProperty('sparkpost.shipment.date.format', 'MM/dd/yyyy')
        props.setProperty('vitalityus.sparkpost.shipment.date.format', 'MMM dd, YYYY')
        props.setProperty('image.server.vip.url', 'https://localhost/imageserver')
        props.setProperty('document.termsAndConditions.base', '/documents/apple-employee-portal-terms-and-conditions')
        props.setProperty('lgnTermsOfUseUrl', 'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        props.setProperty('privacyPolicyUrl', 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')
        props.setProperty('termAdditionalContent', 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')
        props.setProperty(CommonConstants.SPARK_POST_LOGO_URL, 'http://www.java2s.com')
        props.setProperty(CommonConstants.SPARK_POST_SUBJECT_DELAY, 'Test Delay Email')
        props.setProperty(CommonConstants.SPARK_POST_HIDE_DELAYED_CUSTOMER_TEXT, 'true')

        ContextUtil contextUtil = Mock(ContextUtil.class)
        MessageSource messageSource = new StaticMessageSource()


        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: Mock(OrderAttributeValueDao.class),
                applicationProperties: props,
                contextUtil: contextUtil,
                shipmentTrackingUrls: new HashMap<String, String>(),
                varProgramMessageService: varProgramMessageService)

        def order = new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: language,
            countryCode: country, orderLines: new ArrayList<OrderLine>(),
            orderAttributeValues: new ArrayList<OrderAttributeValue>())
        order.orderLines.add(ol1)
        order.orderLines.add(ol2)
        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(map)
        contextUtil.getMessageSource(_) >> messageSource
        varProgramMessageService.getMessages(_, _, _) >> props

        when:
        messageSource.addMessage("lgnTermsOfUseUrl",new Locale(language,country),'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        messageSource.addMessage("privacyPolicyUrl",new Locale(language,country),'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')
        messageSource.addMessage("termAdditionalContent",new Locale(language,country),'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')

        OrderEmailData emailData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test Delay Email', name: 'SHIPMENT_DELAY'), null, props, null)



        then:
        def locale = emailData.branding.locale
        locale == language + '_' + country
        emailData.branding.termsAndConditionsURL == 'https://localhost/imageserver/documents/apple-employee-portal-terms-and-conditions-' + locale + '.pdf'
        emailData.branding.termsOfUseURL == 'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf'
        emailData.branding.privacyPolicyURL == 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf'
        emailData.branding.termsAdditionalContent == 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf'

        where:
        varId        | programId     | language | country
        'VitalityUS' | 'JohnHancock' | 'en'     | 'US'
        'VitalityCA' | 'manulife'    | 'fr'     | 'CA'
        'SCOTIA'     | 'Amex'        | 'zh'     | 'TW'
        'AEP'        | 'DRP'         | 'ru'     | 'RU'
    }

    def 'PayrollOrderEmailData -- To Ensure Payroll URL is populated correctly'() {
        setup:
        Properties props = new Properties()
        props.setProperty('image.server.url', 'abcdef.com')
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')
        props.setProperty('sparkpost.shipment.date.format', 'MM/dd/yyyy')
        props.setProperty('vitalityus.sparkpost.shipment.date.format', 'MMM dd, YYYY')
        props.setProperty('image.server.vip.url', 'https://localhost/imageserver')
        props.setProperty('document.termsAndConditions.base', '/documents/apple-employee-portal-terms-and-conditions')
        props.setProperty('lgnTermsOfUseUrl', 'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        props.setProperty('privacyPolicyUrl', 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')

        ContextUtil contextUtil = Mock(ContextUtil.class)
        MessageSource messageSource = new StaticMessageSource()


        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: Mock(OrderAttributeValueDao.class),
                applicationProperties: props,
                contextUtil: contextUtil,
                shipmentTrackingUrls: new HashMap<String, String>(),
                varProgramMessageService: varProgramMessageService,
                servicePlanInfoService: servicePlanInfoService)

        def order = new OrderAWP(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: language,
                countryCode: country, orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>())
        order.orderLines.add(ol1)
        order.orderLines.add(ol2)

        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(map)
        contextUtil.getMessageSource(_) >> messageSource
        varProgramMessageService.getMessages(_, _, _) >> props
        ServicePlanData servicePlanData = ServicePlanData.builder().withPlanEndDate(new Date())
                .withLastUpdateDate(new Date()).build()
        servicePlanInfoService.getServicePlanData(_, _) >> servicePlanData
        messageSource.addMessage("lgnTermsOfUseUrl",new Locale(language,country),'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf')
        messageSource.addMessage("privacyPolicyUrl",new Locale(language,country),'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf')


        when:
        order.payrollAgreementUrl = payrollAgreementURL
        OrderEmailData emailData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test service_plan Email', name: 'service_plan'), 1, props, null)


        then:

        def locale = emailData.branding.locale
        locale == language + '_' + country
        emailData.branding.termsAndConditionsURL == 'https://localhost/imageserver/documents/apple-employee-portal-terms-and-conditions-' + locale + '.pdf'
        emailData.branding.termsOfUseURL == 'https://www.domain.com/documents/apple-employee-portal-terms-of-use-en.pdf'
        emailData.branding.privacyPolicyURL == 'https://www.domain.com/documents/apple-employee-portal-privacy-policy-en.pdf'
        emailData.workplace.payrollAgreementURL == payrollAgreementURL

        where:
        varId | programId | language | country | payrollAgreementURL
        'AEP' | 'DRP'     | 'en'     | 'US'    | 'https://awp-vip.apldev.bridge2solutions.net/employee-portal/admin/assets/docs/apple-employee-portal-terms-and-conditions-en_GB.pdf'
        'AEP' | 'DRP'     | 'en'     | 'US'    | null
    }

    def 'GetOrderEmailData -- To Ensure Order History URL is populated correctly'() {
        setup:
        Properties props = new Properties()
        props.setProperty('image.server.url', 'abcdef.com')
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')
        props.setProperty('sparkpost.shipment.date.format', 'MM/dd/yyyy')
        props.setProperty('chase.sparkpost.shipment.date.format', 'MMM dd, YYYY')
        props.setProperty('image.server.vip.url', 'https://localhost/imageserver')
        props.setProperty('document.termsAndConditions.base', '/documents/chase-terms-and-conditions')
        props.setProperty('lgnTermsOfUseUrl', 'https://www.domain.com/documents/chase-terms-of-use-en.pdf')
        props.setProperty('privacyPolicyUrl', 'https://www.domain.com/documents/chase-privacy-policy-en.pdf')

        ContextUtil contextUtil = Mock(ContextUtil.class)
        MessageSource messageSource = new StaticMessageSource()
        OrderAttributeValueDao orderAttributeValueDao = Mock(OrderAttributeValueDao.class)

        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: orderAttributeValueDao,
                applicationProperties: props,
                contextUtil: contextUtil,
                shipmentTrackingUrls: new HashMap<String, String>(),
                varProgramMessageService: varProgramMessageService)

        def order = new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: language,
                countryCode: country, orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>())
        order.orderLines.add(ol1)
        order.orderLines.add(ol2)

        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(map)
        contextUtil.getMessageSource(_) >> messageSource
        varProgramMessageService.getMessages(_, _, _) >> props
        messageSource.addMessage("lgnTermsOfUseUrl",new Locale(language,country),'https://www.domain.com/documents/chase-terms-of-use-en.pdf')
        messageSource.addMessage("privacyPolicyUrl",new Locale(language,country),'https://www.domain.com/documents/chase-privacy-policy-en.pdf')
        orderAttributeValueDao.getByOrder(1234) >> setOrderAttribute(new ArrayList<OrderAttributeValue>(), 1, "orderHistoryURL", orderHistoryURL)


        when:
        OrderEmailData emailData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test Confirmation Email', name: 'CONFIRMATION'), null, props, null)


        then:
        def locale = emailData.branding.locale
        locale == language + '_' + country
        emailData.branding.termsAndConditionsURL == 'https://localhost/imageserver/documents/chase-terms-and-conditions-' + locale + '.pdf'
        emailData.branding.termsOfUseURL == 'https://www.domain.com/documents/chase-terms-of-use-en.pdf'
        emailData.branding.privacyPolicyURL == 'https://www.domain.com/documents/chase-privacy-policy-en.pdf'
        emailData.orderInfo.orderHistoryUrl == orderHistoryURL

        where:
        varId   | programId     | language | country | orderHistoryURL
        'Chase' | 'b2s_qa_only' | 'en'     | 'US'    | 'https://ultimaterewardspointsuat.chase.com/rest/explore-experience/apple?url=/apple-sso/storefront/order-history&AI=6348834'
    }

    def 'test populateOrderEmailRequest '() {
        setup:
        Properties props = new Properties()
        props.setProperty(CommonConstants.EMAIL_USE_DRAFT_TEMPLATE,'true')
        props.setProperty(CommonConstants.SPARK_ENVIRONMENT, env)
        props.setProperty(CommonConstants.SPARK_POST_AUTHORIZATION_CODE_KEY,'newKey')
        Notification notification = getNotification()

        notification.setTemplateId(templateId)
        NotificationMapper notificationMapper = new NotificationMapper(applicationProperties: props)

        when:
        IAppleEmailRequest emailRequest = notificationMapper.populateOrderEmailRequest(new String[1],
        Optional.of(notification),new OrderEmailData(),false, "reply@bounce.123.com")

    then:
    emailRequest != null
    emailRequest.getSparkAuthorizationCode() == sparkAuthorizationCode
    emailRequest.getNotification().getTemplateId() == emailTempId
    emailRequest.getNotification().getId() == 1
    emailRequest.getNotification().getVarId() == 'Chase'
    emailRequest.getNotification().getProgramId() == 'b2s_qa_only'
    emailRequest.getNotification().getType() == null
    emailRequest.getNotification().getName() == 'name'
    emailRequest.getNotification().getLocale() == 'en_US'
    emailRequest.getNotification().getSubject() == 'subject'
    emailRequest.getNotification().isActive == true
    emailRequest.getNotification().getLastupdateTime() == null
    emailRequest.getNotification().getLastupdateUser() == 'user'
    emailRequest.getReturnPath() == 'reply@bounce.123.com'
    emailRequest.getEmailData() != null
    emailRequest.isDraftMode() == true
    emailRequest.getRecipients().length == 1
    emailRequest.getBccRecipients() == null
    emailRequest.getHttpConnectionTimeout() == null

    where:
    env     | templateId      | sparkAuthorizationCode | emailTempId
    'lower' | 'chaseTemplate' | 'newKey'               | 'chaseTemplate-lower'
    'prod'  | 'chaseTemplate' | 'newKey'               | 'chaseTemplate'
    'uat'   | 'chaseTemplate' | 'newKey'               | 'chaseTemplate-uat'
}

    def 'GetOrderEmailData -- Test AMP data for AMP service notifications'() {
        setup:
        Properties props = new Properties()
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')
        props.setProperty('amp-music-sp-subscription-url', 'https://music.apple.com/deeplink')
        props.setProperty('amp-music-sp-footer', 'footer')
        props.setProperty('amp-music-sp-body', 'body')
        props.setProperty('amp-music-sp-subject', 'subject')
        props.setProperty('amp-music-sp-body-list', 'body list')

        ContextUtil contextUtil = Mock(ContextUtil.class)
        MessageSource messageSource = new StaticMessageSource()
        OrderAttributeValueDao orderAttributeValueDao = Mock(OrderAttributeValueDao.class)

        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: orderAttributeValueDao,
                applicationProperties: props,
                contextUtil: contextUtil,
                shipmentTrackingUrls: new HashMap<String, String>(),
                varProgramMessageService: varProgramMessageService)

        def order = new Order(orderId: 999, varOrderId: 'var999', orderDate: new Date(), languageCode: language,
                countryCode: country, orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>())
        order.orderLines.add(ol1)
        order.orderLines.add(ol2)

        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(map)
        contextUtil.getMessageSource(_) >> messageSource
        varProgramMessageService.getMessages(_, _, _) >> props

        when:
        OrderEmailData emailData = notificationMapper.getOrderEmailData(order, program, new Notification(subject:
                'Test AMP Email', name: 'amp-music'), null, props, subscriptionUrl)


        then:
        def locale = emailData.branding.locale
        locale == language + '_' + country
        emailData.ampData.subscriptionUrl == subscriptionUrl

        where:
        varId | programId | language | country | subscriptionUrl
        'UA'  | 'MP'      | 'en'     | 'US'    | 'https://music.apple.com/deeplink'
    }

    def 'setOrderInfo -- Test VIEW_ANONYMOUS_ORDER_DETAIL Flag'() {
        setup:
        Properties props = new Properties()
        props.setProperty(CommonConstants.EMAIL_USE_DRAFT_TEMPLATE,'true')
        props.setProperty(CommonConstants.SPARK_POST_AUTHORIZATION_CODE_KEY,'newKey')
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')

        OrderAttributeValueDao orderAttributeValueDao = Mock(OrderAttributeValueDao.class)
        OrderDiagnosticInfoDao orderDiagnosticInfoDao = Mock(OrderDiagnosticInfoDao.class)

        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: orderAttributeValueDao,
                applicationProperties: props,
                orderDiagnosticInfoDao: orderDiagnosticInfoDao)

        def config = new HashMap<String, Object>()
        config.put('viewAnonymousOrderDetail', keyValue)
        Program program = new Program(varId: varId, programId: programId);
        program.setConfig(config)
        orderDiagnosticInfoDao.getHostNameByOrderId(_) >> getOrderDiagnosticInfoEntity(hostName)

        OrderEmailData orderEmailData = new OrderEmailData()

        when:
        notificationMapper.setOrderInfo(new Order(orderId: 2100466069, varOrderId: 'RBC', orderDate: new Date(),
                languageCode: language, countryCode: country, orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>()), program, orderEmailData, new Locale('en_CA'))

        then:
        orderEmailData.orderInfo.orderHistoryUrl == result

        where:
        varId | programId     | language | country | hostName            | keyValue || result
        'UA'  | 'MP'          | 'en'     | 'US'    | 'https://localhost' | false    || null
        'RBC' | 'acquisition' | 'en'     | 'CA'    | 'https://localhost' | true     || 'https://localhost/apple-gr/AnonLogin?v=RBC&p=acquisition&l=en_CA&uid=MjEwMDQ2NjA2OXxudWxs'
        'RBC' | 'PBA'         | 'fr'     | 'CA'    | 'https://APLUAT'    | true     || 'https://APLUAT/apple-gr/AnonLogin?v=RBC&p=PBA&l=fr_CA&uid=MjEwMDQ2NjA2OXxudWxs'
    }

    def 'formatCashRewardPricing -- Test Cash Reward Value'() {

        setup:
        Properties props = new Properties()
        props.setProperty(CommonConstants.EMAIL_USE_DRAFT_TEMPLATE,'true')
        props.setProperty(CommonConstants.SPARK_POST_AUTHORIZATION_CODE_KEY,'newKey')
        props.setProperty('sparkpost.order.date.format', 'MMM dd, YYYY')

        OrderAttributeValueDao orderAttributeValueDao = Mock(OrderAttributeValueDao.class)
        OrderDiagnosticInfoDao orderDiagnosticInfoDao = Mock(OrderDiagnosticInfoDao.class)

        NotificationMapper notificationMapper = new NotificationMapper(shipmentNotificationDao: Mock(ShipmentNotificationDaoImpl.class),
                orderAttributeValueDao: orderAttributeValueDao,
                applicationProperties: props,
                orderDiagnosticInfoDao: orderDiagnosticInfoDao)

        Locale locale = LocaleUtils.toLocale(localeStr)

        expect:
        cashRewardsInDollar == notificationMapper.formatCashRewardPricing(cashReward, locale, pointName)

        where:
        cashReward | localeStr | pointName      || cashRewardsInDollar
        0          | 'en_US'   | 'Cash Rewards' || '$0.00 Cash Rewards'
        1          | 'en_CA'   | 'Cash Rewards' || '$0.01 Cash Rewards'
        2          | 'fr_CA'   | 'Cash Rewards' || '0,02 $ Cash Rewards'
        3          | 'en_SG'   | 'Cash Rewards' || '$0.03 Cash Rewards'
        9989       | 'en_AU'   | 'Cash Rewards' || '$99.89 Cash Rewards'
        9990       | 'en_HK'   | 'Cash Rewards' || 'HK$99.90 Cash Rewards'
        9991       | 'en_MY'   | 'Cash Rewards' || 'RM99.91 Cash Rewards'
        9992       | 'en_PH'   | 'Cash Rewards' || '₱99.92 Cash Rewards'
        9993       | 'en_US'   | 'Cash Rewards' || '$99.93 Cash Rewards'
        9994       | 'en_TH'   | 'Cash Rewards' || 'THB99.94 Cash Rewards'
        9995       | 'es_MX'   | 'Cash Rewards' || '$99.95 Cash Rewards'
        9996       | 'th_TH'   | 'คะแนน'        || 'THB99.96 คะแนน'
        9997       | 'zh_TW'   | '紅利點數'       || '$99.97 紅利點數'
        9998       | 'en_US'   | 'Cash Rewards' || '$99.98 Cash Rewards'
        9999       | 'en_CA'   | 'Cash Rewards' || '$99.99 Cash Rewards'
        10000      | 'fr_CA'   | 'Cash Rewards' || '100,00 $ Cash Rewards'
        10001      | 'en_SG'   | 'Cash Rewards' || '$100.01 Cash Rewards'
    }

    def getOrderDiagnosticInfoEntity(final String hostname) {
        OrderDiagnosticInfoEntity entity = new OrderDiagnosticInfoEntity()
        entity.setId(99L)
        entity.setOrderId(2100466069L)
        entity.setHostname(hostname)
        entity.setIpAddress("0:0:0:0:0:0:0:1")
        return entity
    }

    def Notification getNotification(){
        Notification notification = new Notification()
        notification.setId(1)
        notification.setVarId('Chase')
        notification.setProgramId('b2s_qa_only')
        notification.setType(null)
        notification.setName('name')
        notification.setLocale('en_US')
        notification.setSubject('subject')
        notification.setIsActive(true)
        notification.setLastupdateUser('user')
        return  notification
    }

    def setOrderAttribute(final List<OrderAttributeValue> orderAttributes, final Integer id, final String name, final
         String value) {
    OrderAttributeValue orderAttributeValue = new OrderAttributeValue()
    orderAttributeValue.setId(id)
    orderAttributeValue.setName(name)
    orderAttributeValue.setValue(value)
    orderAttributes.add(orderAttributeValue)
    return orderAttributes
}


}