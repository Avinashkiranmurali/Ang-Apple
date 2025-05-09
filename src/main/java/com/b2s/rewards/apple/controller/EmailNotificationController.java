package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.NotificationService;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.model.EmailNotification;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.BasicAuthValidation;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.constant.Constant;
import com.b2s.shop.common.order.OrderTransactionManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-11-21
 */
@RestController
@RequestMapping(value = "/notification")
public class EmailNotificationController {

    @Autowired
    private BasicAuthValidation basicAuthValidation;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrderTransactionManager manager;

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationController.class);

    @PostMapping(value = "/sendEmail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity sendEmailNotification(@Valid @RequestBody final EmailNotification emailNotificationRequest) {

        LOG.info("AUDIT Email request received ...");
        if (Objects.isNull(emailNotificationRequest.getOrderId()) || emailNotificationRequest.getOrderId() == 0) {
            LOG.error("Invalid order ID # {}", emailNotificationRequest.getOrderId());
            emailNotificationRequest.setMessage("Invalid orderId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emailNotificationRequest);
        }
        LOG.info("AUDIT Order ID = {}", emailNotificationRequest.getOrderId());

        // validate basic auth role to identify user access
        if (!basicAuthValidation.hasAccess()) {
            LOG.error("AUDIT User does not have permission to send Email.");
            emailNotificationRequest.setMessage("Unauthorized Access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(emailNotificationRequest);
        }

        try {
            final Order order = manager.getOrder(emailNotificationRequest.getOrderId());
            if (Objects.isNull(order)) {
                LOG.error("sendNotification : Order not found for order id # {}", emailNotificationRequest.getOrderId());
                emailNotificationRequest.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
                emailNotificationRequest.setMessage("Order not found");
                return ResponseEntity.badRequest().body(emailNotificationRequest);
            }
            OrderLine orderLine = null;
            if (Objects.nonNull(emailNotificationRequest.getLineNum()) && emailNotificationRequest.getLineNum() != 0) {
                final Optional<OrderLine> orderLineOpt =
                        order.getOrderLines().stream()
                                .filter(o -> o.getLineNum().equals(emailNotificationRequest.getLineNum()))
                                .findFirst();
                if (orderLineOpt.isEmpty()) {
                    LOG.error("sendNotification : Order Line not found for order id # {}, line nume # {}",
                            emailNotificationRequest.getOrderId(), emailNotificationRequest.getLineNum());
                    emailNotificationRequest.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
                    emailNotificationRequest.setMessage("Order Line not found");
                    return ResponseEntity.badRequest().body(emailNotificationRequest);
                }
                orderLine = orderLineOpt.get();
            }

            if (Objects.nonNull(orderLine) && !validateRequest(orderLine, emailNotificationRequest)) {
                return ResponseEntity.badRequest().body(emailNotificationRequest);
            }

            final EmailNotification emailNotification =
                    notificationService.processNotification(emailNotificationRequest, order, orderLine);

            if (StringUtils.isNotBlank(emailNotification.getMessage()) &&
                    Constant.SUCCESS.equalsIgnoreCase(emailNotification.getMessage())) {
                LOG.info(
                        "Email Notification response created successfully for the orderId: {}, lineNum: {}",
                        emailNotification.getOrderId(), emailNotification.getLineNum());
                return ResponseEntity.ok(emailNotification);
            } else {
                LOG.warn("Unable to send Email Notification for the orderId : {}, lineNum : {}, message : {} ",
                        emailNotification.getOrderId(), emailNotification.getLineNum(), emailNotification.getMessage());
                return ResponseEntity.badRequest().body(emailNotification);
            }

        } catch (Exception ex) {
            LOG.error("Failed to send Email Notification for the orderId : {}, lineNum : {}, orderStatus : {} ",
                    emailNotificationRequest.getOrderId(), emailNotificationRequest.getLineNum(),
                    emailNotificationRequest.getOrderStatus(), ex);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateRequest(final OrderLine orderLine, final EmailNotification request) {
        boolean isValid = true;
        if (CommonConstants.SUPPLIER_TYPE_AMP_S.equals(orderLine.getSupplierId())) {
            //AMP item ID should starts with amp-(Hyphen)
            if (StringUtils.isBlank(request.getItemId()) ||
                    !AppleUtil.isNotificationAmp(CommonConstants.NotificationName.get(request.getItemId()))) {
                LOG.error("Invalid Item ID # {}", request.getItemId());
                request.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
                request.setMessage("Invalid itemId");
                isValid = false;
            }
            //Email ID should not be blank
            if (StringUtils.isBlank(request.getEmailId())) {
                LOG.error("Invalid Email ID # {}", request.getEmailId());
                request.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
                request.setMessage("Invalid emailId");
                isValid = false;
            }
        } else {
            if (Objects.isNull(request.getOrderStatus()) ||
                    (request.getOrderStatus() != 3 && request.getOrderStatus() != 18)) {
                LOG.error("Invalid order status # {}", request.getOrderStatus());
                request.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
                request.setMessage("Invalid orderStatus");
                isValid = false;
            }
        }
        return isValid;
    }

}
