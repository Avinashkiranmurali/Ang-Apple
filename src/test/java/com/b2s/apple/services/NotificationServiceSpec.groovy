package com.b2s.apple.services

import com.b2s.apple.mapper.NotificationMapper
import com.b2s.apple.util.NotificationServiceUtil
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.OrderAttributeValueDao
import com.b2s.rewards.apple.model.Notification
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.spark.api.SparkStatus
import com.b2s.spark.api.apple.impl.AppleSparkEmailServiceImpl
import com.b2s.spark.api.apple.to.EmailResponse
import com.b2s.spark.api.apple.to.EmailResult
import com.b2s.spark.api.apple.to.IAppleEmailResponse
import com.b2s.spark.api.apple.to.impl.OrderEmailData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject

class NotificationServiceSpec extends Specification {

    NotificationServiceUtil notificationServiceUtil = Mock()
    NotificationMapper notificationMapper = Mock()
    AppleSparkEmailServiceImpl sparkEmailService = Mock()
    OrderAttributeValueDao orderAttributeValueDao = Mock()
    VarProgramMessageService varProgramMessageService = Mock()

    @Subject
    NotificationService notificationService = new NotificationService(
            notificationServiceUtil: notificationServiceUtil, notificationMapper: notificationMapper,
            orderAttributeValueDao: orderAttributeValueDao, sparkEmailService: sparkEmailService,
            varProgramMessageService: varProgramMessageService)

    def "test sendDataEmail"() {
        setup:
        def order = getOrders()
        def program = new Program()
        Map<String, Object> configs = new HashMap<>()
        program.setConfig(configs)

        notificationServiceUtil.getEmailNotification(_, _) >> Optional.of(getNotification())
        notificationMapper.getOrderEmailData(_, _, _, _, _, _) >> new OrderEmailData()

        sparkEmailService.sendEmail(_) >> new IAppleEmailResponse() {
            @Override
            ResponseEntity<EmailResponse> getEmailResponse() {
                EmailResponse emailResponse = new EmailResponse();
                emailResponse.setErrors(null)
                emailResponse.setResult(new EmailResult())
                return new ResponseEntity<>(emailResponse, HttpStatus.OK)
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

        ResponseEntity<EmailResponse> confirmationResult = notificationService.sendDataEmail(order, program,
                CommonConstants.NotificationName.CONFIRMATION, 1, null)

        ResponseEntity<EmailResponse> shipmentResult = notificationService.sendDataEmail(order, program,
                CommonConstants.NotificationName.SHIPMENT, 1, null)

        expect:
        confirmationResult != null
        confirmationResult.getStatusCode().toString() == '200 OK'

        shipmentResult != null
        shipmentResult.getStatusCode().toString() == '200 OK'
    }

    Order getOrders() {
        Order order = new Order()
        order.setLanguageCode('en')
        order.setCountryCode('US')
        order.setProgramId('b2s_qa_only')
        return order
    }

    Notification getNotification() {
        Notification notification = new Notification()
        notification.setProgramId('b2s_qa_only')
        notification.setLocale('en_US')
        notification.setName('SHIPMENT')
        return notification
    }
}
