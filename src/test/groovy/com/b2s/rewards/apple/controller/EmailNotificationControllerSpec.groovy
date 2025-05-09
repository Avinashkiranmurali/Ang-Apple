package com.b2s.rewards.apple.controller

import com.b2s.apple.entity.OrderLineAttributeEntity
import com.b2s.apple.mapper.NotificationMapper
import com.b2s.apple.model.SubscriptionResponseDTO
import com.b2s.apple.services.AppleProductServiceClient
import com.b2s.apple.services.NotificationService
import com.b2s.apple.services.ProgramService
import com.b2s.apple.services.VarProgramMessageService
import com.b2s.apple.util.NotificationServiceUtil
import com.b2s.common.services.exception.ServiceException
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.dao.OrderAttributeValueDao
import com.b2s.rewards.apple.dao.OrderLineAttributeDao
import com.b2s.rewards.apple.exceptionhandler.CustomRestExceptionHandler
import com.b2s.rewards.apple.model.AMPConfig
import com.b2s.rewards.apple.model.EmailNotification
import com.b2s.rewards.apple.model.Notification
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.AppleUtil
import com.b2s.rewards.apple.util.BasicAuthValidation
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.OrderTransactionManager
import com.b2s.spark.api.SparkStatus
import com.b2s.spark.api.apple.impl.AppleSparkEmailServiceImpl
import com.b2s.spark.api.apple.to.EmailResponse
import com.b2s.spark.api.apple.to.EmailResult
import com.b2s.spark.api.apple.to.IAppleEmailResponse
import com.b2s.spark.api.apple.to.impl.OrderEmailData
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.OffsetDateTime

class EmailNotificationControllerSpec extends Specification {

    NotificationServiceUtil notificationServiceUtil = Mock()
    NotificationMapper notificationMapper = Mock()
    AppleSparkEmailServiceImpl sparkEmailService = Mock()
    OrderAttributeValueDao orderAttributeValueDao = Mock()
    OrderLineAttributeDao orderLineAttributeDao = Mock()
    OrderTransactionManager manager = Mock()
    ProgramService programService = Mock()
    BasicAuthValidation basicAuthValidation = Mock()
    VarProgramMessageService varProgramMessageService = Mock()
    AppleProductServiceClient appleProductServiceClient = Mock()
    private MockMvc mvc;
    private User user

    @Subject
            NotificationService = new NotificationService(
                    notificationServiceUtil: notificationServiceUtil, notificationMapper: notificationMapper,
                    orderAttributeValueDao: orderAttributeValueDao, sparkEmailService: sparkEmailService,
                    manager: manager, programService: programService, orderLineAttributeDao: orderLineAttributeDao,
                    varProgramMessageService: varProgramMessageService,
                    appleProductServiceClient: appleProductServiceClient)
    EmailNotificationController emailNotificationController = new EmailNotificationController(
            notificationService: notificationService, basicAuthValidation: basicAuthValidation, manager: manager)

    MockHttpSession session=new MockHttpSession();


    def setup() {
        user = new User()

        mvc = MockMvcBuilders.standaloneSetup(emailNotificationController)
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build()
    }

    def cleanup(){
        user = null
        mvc = null
    }

    @Unroll
    def "test sendNotification"() {
        setup:
        def order = getOrder()
        order.setOrderLines(getOrderLines(supplierId))
        EmailNotification ampEmail = getEmailNotification("abc@bakkt.com", lineNo, 'amp-tv-plus', 2100119446, statusId)

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")

        ResponseEntity<EmailNotification> emailResponse = emailNotificationController.sendEmailNotification(ampEmail)

        expect:
        emailResponse != null
        emailResponse.getBody().getMessage() == response
        emailResponse.statusCode == responseStatus

        where:
        lineNo | statusId | supplierId || response  || responseStatus
        null   | null     | null       || 'success' || HttpStatus.OK    // Confirmation
        2      | null     | '40000'    || 'success' || HttpStatus.OK    // AMP
        2      | 3        | '50000'    || 'success' || HttpStatus.OK    // Service Plan
        2      | 3        | '200'      || 'success' || HttpStatus.OK    // Shipment
        2      | 18       | '200'      || 'success' || HttpStatus.OK    // Delayed Shipment
    }

    @Unroll
    def "test sendNotification - validate responseStatus"() {
        setup:
        def order = getOrder
        EmailNotification ampEmail = getEmailNotification(emailId, lineNo, 'amp-tv-plus', orderId, statusId)

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        if (order != null) {
            order.setOrderLines(getOrderLines(supplierId))
        }
        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> hasAccess
        programService.getProgram(_, _, _) >> getProgram(true, true, null)

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")

        ResponseEntity<EmailNotification> emailResponse = emailNotificationController.sendEmailNotification(ampEmail)

        expect:
        emailResponse != null
        emailResponse.getBody().getMessage() == response
        emailResponse.statusCode == responseStatus

        where:
        getOrder   | orderId    | lineNo | statusId | supplierId | emailId         | hasAccess || response               || responseStatus
        getOrder() | 2100119446 | 2      | null     | '40000'    | "abc@bakkt.com" | true      || 'success'              || HttpStatus.OK
        getOrder() | 2100119446 | 2      | null     | '40000'    | "abc@bakkt.com" | false     || 'Unauthorized Access'  || HttpStatus.UNAUTHORIZED
        null       | 2100119446 | 2      | null     | '40000'    | "abc@bakkt.com" | true      || 'Order not found'      || HttpStatus.BAD_REQUEST
        getOrder() | 0          | 2      | null     | '40000'    | "abc@bakkt.com" | true      || 'Invalid orderId'      || HttpStatus.BAD_REQUEST
        getOrder() | null       | 2      | null     | '40000'    | "abc@bakkt.com" | true      || 'Invalid orderId'      || HttpStatus.BAD_REQUEST
        getOrder() | 2100119446 | 4      | null     | '40000'    | "abc@bakkt.com" | true      || 'Order Line not found' || HttpStatus.BAD_REQUEST
        getOrder() | 2100119446 | 2      | 3        | '200'      | "abc@bakkt.com" | true      || 'success'              || HttpStatus.OK
        getOrder() | 2100119446 | 2      | null     | '40000'    | null            | true      || 'Invalid emailId'      || HttpStatus.BAD_REQUEST
        getOrder() | 2100119446 | 2      | 6        | '200'      | "abc@bakkt.com" | true      || 'Invalid orderStatus'  || HttpStatus.BAD_REQUEST
        getOrder() | 2100119446 | 2      | null     | '200'      | "abc@bakkt.com" | true      || 'Invalid orderStatus'  || HttpStatus.BAD_REQUEST
    }

    @Unroll
    def "test sendAmpNotification"() {
        setup:
        def order = getOrder()
        EmailNotification ampEmail = getEmailNotification("abc@bakkt.com", 2, itemId, 2100119446, null)

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        order.setOrderLines(getOrderLines(supplierId))
        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(statLink, progAMP, duration)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes(olaURL)
        varProgramMessageService.getMessages(_, _, _) >> vpmData
        appleProductServiceClient.getSubscriptionInfo(_, _) >> PS_Response

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        ResponseEntity<EmailNotification> emailResponse = emailNotificationController.sendEmailNotification(ampEmail)

        expect:
        emailResponse != null
        emailResponse.getBody().getMessage() == response
        emailResponse.statusCode == responseStatus

        where:
        progAMP | duration | supplierId | itemId        | olaURL | statLink | vpmData     | PS_Response | response               || responseStatus
        true    | 30       | '40000'    | 'amp-tv-plus' | "url"  | true     | vpmProp()   | null        | 'success'              || HttpStatus.OK
        true    | null     | '40000'    | 'amp-tv-plus' | "url"  | true     | vpmProp()   | null        | 'success'              || HttpStatus.OK
        true    | 30       | '20000'    | 'amp-tv-plus' | "url"  | true     | vpmProp()   | null        | 'Invalid orderStatus'  || HttpStatus.BAD_REQUEST
        true    | null     | '40000'    | ''            | "url"  | true     | vpmProp()   | null        | 'Invalid itemId'       || HttpStatus.BAD_REQUEST
        true    | 30       | '40000'    | null          | "url"  | true     | vpmProp()   | null        | 'Invalid itemId'       || HttpStatus.BAD_REQUEST
        //use_static_link = true
        true    | 30       | '40000'    | 'amp-tv-plus' | ""     | true     | vpmProp()   | null        | 'success'              || HttpStatus.OK
        true    | null     | '40000'    | 'amp-tv-plus' | ""     | true     | vpmProp()   | null        | 'success'              || HttpStatus.OK
        false   | 30       | '40000'    | 'amp-tv-plus' | ""     | true     | vpmProp()   | null        | 'Failed to send email' || HttpStatus.BAD_REQUEST
        true    | null     | '40000'    | 'amp-tv-plus' | ""     | true     | null        | null        | 'Failed to send email' || HttpStatus.BAD_REQUEST
        true    | 30       | '40000'    | 'amp-tv-plus' | ""     | true     | emptyProp() | null        | 'Failed to send email' || HttpStatus.BAD_REQUEST
        //use_static_link = false
        true    | 30       | '40000'    | 'amp-tv-plus' | ""     | false    | null        | getPSResp() | 'success'              || HttpStatus.OK
        true    | null     | '40000'    | 'amp-tv-plus' | ""     | false    | null        | getPSResp() | 'success'              || HttpStatus.OK
        false   | 30       | '40000'    | 'amp-tv-plus' | ""     | false    | null        | getPSResp() | 'Failed to send email' || HttpStatus.BAD_REQUEST
        true    | null     | '40000'    | 'amp-tv-plus' | ""     | false    | null        | null        | 'Failed to send email' || HttpStatus.BAD_REQUEST
    }

    def "test getDisplayLanguageFromLocale"() {
        expect:
        language == LocaleUtils.toLocale(locale).getDisplayLanguage()

        where:
        locale  || language
        'en_US' || 'English'
        'en_TH' || 'English'
        'en_SG' || 'English'
        'en_PH' || 'English'
        'en_MY' || 'English'
        'en_HK' || 'English'
        'en_AU' || 'English'
        'en_CA' || 'English'
        'es_MX' || 'Spanish'
        'fr_CA' || 'French'
        'ta_IN' || 'Tamil'
        'th_TH' || 'Thai'
        'zh_TW' || 'Chinese'
        'zh_HK' || 'Chinese'
    }

    def 'test without content-type & accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()

        EmailNotification emailNotificationRequest = new EmailNotification()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                        .content(AppleUtil.asJsonString(emailNotificationRequest))
        .session(session))
        .andReturn()

        then:
        //HTTP response status code is 415, if the content-type & accept both are unsupported or not present
        result.response.status == 415
    }

    def 'test mismatch/not acceptable content-type without accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()

        EmailNotification emailNotificationRequest = new EmailNotification()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 415, if the content-type is not acceptable or mismatch without accept
        result.response.status == 415
    }

    def 'test mismatch/not acceptable accept without content-type - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()

        EmailNotification emailNotificationRequest = new EmailNotification()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .accept(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 406, with out content-type and mismatch accept
        result.response.status == 406
    }

    def 'test Invalid content-type & Invalid accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()

        EmailNotification emailNotificationRequest = new EmailNotification()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 406, if the content-type & accept both are not acceptable or mismatch
        result.response.status == 406
    }

    def 'test valid content-type without accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def order = getOrder()

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        order.setOrderLines(getOrderLines('40000'))
        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification("abc@bakkt.com", 2, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 200, if the content-type is valid and without accept
        result.response.status == 200
    }

    def 'test without content-type & valid accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()

        EmailNotification emailNotificationRequest = new EmailNotification()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 415, with out content-type and a valid accept
        result.response.status == 415
    }

    def 'test valid content-type & valid accept - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def order = getOrder()

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        order.setOrderLines(getOrderLines('40000'))
        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification("abc@bakkt.com", 2, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 200, if the content-type and accept is valid
        result.response.status == 200
    }

    def 'test valid HTTP codes - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def emailId = "abc@bakkt.com"
        def lineNo = 2

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> hasAccess
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification(emailId, lineNo, 'amp-tv-plus', orderId, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(contentType)
                .accept(accept)
                .session(session))
                .andReturn()

        then:
        result.response.status == responseStatus

        where:
        orderId    | order                                | hasAccess | contentType                           | accept                                || responseStatus
        2100119446 | getOrderWithOrderLines()             | true      | MediaType.APPLICATION_JSON_UTF8_VALUE | MediaType.APPLICATION_JSON_UTF8_VALUE || 200 //OK
        null       | getOrderWithOrderLines()             | true      | MediaType.APPLICATION_JSON_UTF8_VALUE | MediaType.APPLICATION_JSON_UTF8_VALUE || 400 //BAD_REQUEST
        2100119446 | getOrderWithOrderLines()             | false     | MediaType.APPLICATION_JSON_UTF8_VALUE | MediaType.APPLICATION_JSON_UTF8_VALUE || 401 //UNAUTHORIZED
        null       | getOrderWithOrderLines()             | true      | MediaType.APPLICATION_ATOM_XML        | MediaType.APPLICATION_ATOM_XML        || 406 //NOT_ACCEPTABLE
        2100119446 | { throw new ServiceException() }     | true      | MediaType.APPLICATION_JSON_UTF8_VALUE | MediaType.APPLICATION_JSON_UTF8_VALUE || 500 //INTERNAL_SERVER_ERROR

    }

    def 'test valid HTTP code 404 - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def emailId = "abc@bakkt.com"
        def lineNo = 2

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> getOrderWithOrderLines()
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification(emailId, lineNo, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status == responseStatus


        where:
        url                               || responseStatus
        '/notification/sendEmail'         || 200 //OK
        '/notification/sendEmailTest'     || 404 //NOT_FOUND
        '/notificationTest/sendEmail'     || 404 //NOT_FOUND
        '/notificationTest/sendEmailTest' || 404 //NOT_FOUND

    }


    def 'test valid HTTP code 405 with GET instead of POST - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def emailId = "abc@bakkt.com"

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification(emailId, 2, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status  == 405 //METHOD_NOT_ALLOWED

    }

    def 'test valid HTTP code 405 with PUT instead of POST - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def emailId = "abc@bakkt.com"

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification(emailId, 2, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status  == 405 //METHOD_NOT_ALLOWED

    }

    def 'test valid HTTP code 415 - sendEmail API'() {

        given:
        session.getAttribute("USER") >> getUser()
        def emailId = "abc@bakkt.com"

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        manager.getOrder(_) >> order
        basicAuthValidation.hasAccess() >> true
        programService.getProgram(_, _, _) >> getProgram(true, true, null)
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_, _, _) >> getOrderLineAttributes("url")
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        appleProductServiceClient.getSubscriptionInfo(_, _) >> null

        sparkEmailService.sendEmail(_) >> getDefaultEmailResponse()

        EmailNotification emailNotificationRequest = getEmailNotification(emailId, 2, 'amp-tv-plus', 2100119446, null)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post('/notification/sendEmail')
                .content(AppleUtil.asJsonString(emailNotificationRequest))
                .session(session))
                .andReturn()

        then:
        result.response.status  == 415 //UNSUPPORTED_MEDIA_TYPE

    }


    def getUser(){
        user.varId="UA"
        user.programId="MP"
        user.locale=Locale.US
        return user
    }

    SubscriptionResponseDTO getPSResp() {
        return SubscriptionResponseDTO.builder()
                .withRedemptionUrl("amp-news-plus")
                .withExpirationDateTime(OffsetDateTime.now())
                .withRemainingCount(100)
                .build();
    }

    Properties vpmProp() {
        Properties properties = new Properties()
        properties.put("amp-tv-plus-sp-subscription-url", "https://tv.apple.com/channel/tvs.sbd.4000?at=1000l36zP&ct=Bakkt_TV&itscg=30200&itsct=tv_box_link")
        properties.put("amp-tv-plus-30-sp-subscription-url", "https://tv.apple.com/channel/tvs.sbd.4000?at=1000l36zP&ct=Bakkt_TV&itscg=30200&itsct=tv_box_link")
        return properties
    }

    Properties emptyProp() {
        Properties properties = new Properties()
        properties.put("amp-tv-plus-sp-subscription-url", "")
        properties.put("amp-tv-plus-30-sp-subscription-url", "")
        return properties
    }

    IAppleEmailResponse getDefaultEmailResponse() {
        return new IAppleEmailResponse() {
            @Override
            ResponseEntity<EmailResponse> getEmailResponse() {
                EmailResponse emailResponse = new EmailResponse();
                emailResponse.setErrors(null)
                emailResponse.setResult(new EmailResult())
                return getEmailResponseEntity()
            }

            @Override
            SparkStatus getSparkStatus() {
                return null
            }

            @Override
            Long getNotificationId() {
                return null
            }
        }
    }

    ResponseEntity<EmailResponse> getEmailResponseEntity() {
        EmailResponse emailResponse = new EmailResponse();
        emailResponse.setErrors(null)
        EmailResult emailResult = new EmailResult();
        emailResult.setId("123456")
        emailResponse.setResult(emailResult)
        return ResponseEntity.ok(emailResponse);
    }

    Order getOrder() {
        Order order = new Order()
        order.setOrderId(1L)
        order.setProgramId('b2s_qa_only')
        order.setLanguageCode('en')
        order.setCountryCode('US')
        return order
    }

    Order getOrderWithOrderLines() {
        Order order = new Order()
        order.setOrderId(1L)
        order.setProgramId('b2s_qa_only')
        order.setLanguageCode('en')
        order.setCountryCode('US')
        order.setOrderLines(getOrderLines('40000'))
        return order
    }

    List<OrderLineAttributeEntity> getOrderLineAttributes(String url) {
        List<OrderLineAttributeEntity> orderLineAttributeList = new ArrayList<>()
        OrderLineAttributeEntity orderLineAttribute = new OrderLineAttributeEntity()
        orderLineAttribute.setOrderId(1L)
        orderLineAttribute.setLineNum(2)
        orderLineAttribute.setName(CommonConstants.SUBSCRIPTION_URL)
        if (StringUtils.isNotBlank(url)) {
            orderLineAttribute.setValue(url)
            orderLineAttributeList.add(orderLineAttribute)
        }
        return orderLineAttributeList
    }

    Notification getNotification() {
        Notification notification = new Notification()
        notification.setProgramId('b2s_qa_only')
        notification.setLocale('en_US')
        notification.setName('amp-tv-plus')
        return notification
    }

    EmailNotification getEmailNotification(String emailId, Integer lineNo, String itemId, Long orderId, Integer orderStatus) {
        def emailNotification = new EmailNotification()
        emailNotification.setOrderId(orderId)
        emailNotification.setLineNum(lineNo)
        emailNotification.setItemId(itemId)
        emailNotification.setEmailId(emailId)
        emailNotification.setOrderStatus(orderStatus)
        return emailNotification;
    }

    private static List<OrderLine> getOrderLines(String supplierId) {
        List<OrderLine> orderLines = new ArrayList<>();
        OrderLine orderLine = new OrderLine()
        orderLine.setOrderId(1L);
        orderLine.setLineNum(1)
        orderLine.setSupplierId("200")
        orderLine.setQuantity(1)
        orderLine.setItemPoints(Double.valueOf(3847))
        orderLine.setSupplierItemPrice(2500)
        orderLine.setShippingPoints(Double.valueOf(0))
        orderLine.setSupplierShippingPrice(0)
        orderLine.setConvRate(0.01)
        orderLine.setB2sItemProfitPrice(1)
        orderLine.setMerchantId("1")
        orderLine.setOrderLinePoints(300)
        orderLines.add(orderLine)

        OrderLine orderLine2 = new OrderLine()
        orderLine2.setOrderId(1L);
        orderLine2.setLineNum(2)
        orderLine2.setQuantity(1)
        orderLine2.setItemPoints(Double.valueOf(3847))
        orderLine2.setSupplierItemPrice(2500)
        orderLine2.setShippingPoints(Double.valueOf(0))
        orderLine2.setSupplierShippingPrice(0)
        orderLine2.setConvRate(0.01)
        orderLine2.setB2sItemProfitPrice(1)
        orderLine2.setSupplierId(supplierId)
        orderLine2.setMerchantId("1")
        orderLine2.setOrderLinePoints(300)
        orderLine2.setItemId("amp-tv-plus")
        orderLines.add(orderLine2)
        return orderLines;
    }

    def getProgram(boolean useStaticLink, boolean ampConf, Integer duration) {
        def varId = "UA"
        def programId = "MP"
        def locale = "en_US"
        Program program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        Set<AMPConfig> ampConfigs = new HashSet<>()
        if (ampConf) {
            AMPConfig config = AMPConfig.builder()
                    .withCategory("ipad")
                    .withItemId("amp-tv-plus")
                    .withUseStaticLink(useStaticLink)
                    .withUpdateDate(new java.util.Date())
                    .withUpdatedBy("user")
                    .withDuration(duration)
                    .build();
            ampConfigs.add(config)
        }
        program.setAmpSubscriptionConfig(ampConfigs)

        Notification confNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "CONFIRMATION")
        Notification shipNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "SHIPMENT")
        Notification shipDelayNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "SHIPMENT_DELAY")
        Notification ampMusicNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "AMP_MUSIC")
        Notification ampNewsNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "AMP_NEWS_PLUS")
        Notification ampTVNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "AMP_TV_PLUS")
        Notification servPlanNotification = new Notification(varId: varId, programId: programId, locale: locale, name: "SERVICE_PLAN")

        List<Notification> notifications = new ArrayList<>()
        notifications.addAll(confNotification, shipNotification, shipDelayNotification,
                ampMusicNotification, ampNewsNotification, ampTVNotification, servPlanNotification)
        program.setNotifications(notifications)
        return program
    }
}