package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.PaymentServerV2Service;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.CreateTransactionResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

@RestController
@SessionAttributes({CommonConstants.USER_SESSION_OBJECT, CommonConstants.APPLE_CART_SESSION_OBJECT})
@RequestMapping(value = "/payment", produces = "application/json;charset=UTF-8")
@ResponseBody
public class PaymentServerV2Controller {

    @Autowired
    private PaymentServerV2Service paymentService;

    @Value("${OVERRIDE_PROGRAM_DEMO_FOR_PAYMENT_SERVER_MODE}")
    private String overrideProgramDemoForPaymentServerMode;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private AppSessionInfo appSessionInfo;


    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServerV2Controller.class);

    @PostMapping(value = "/transaction")
    public ResponseEntity<CreateTransactionResponse> createTransaction(HttpServletRequest httpServletRequest) {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
        Cart sessionCart = (Cart) request.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        final User user = appSessionInfo.currentUser();
        final String randomId = AppleUtil.getRandomId();
        final double chargeAmt = CartCalculationUtil.roundDollar(sessionCart.getCost());

        final String txId = paymentService.createTransaction(user, program, request.getSession().getId(),
            Boolean.valueOf(overrideProgramDemoForPaymentServerMode), AppleUtil.getHostName(request), chargeAmt,
            randomId);

        request.getSession().setAttribute(CommonConstants.PAYMENT_TRANSACTION_ID, txId);

        final String authURL =
            applicationProperties.getProperty(CommonConstants.PAY_SERVER_EXTERNAL_URL) + txId;
        LOGGER.info("Successfully created Auth URI. Auth URL is:{}.", authURL);

        final CreateTransactionResponse response = new CreateTransactionResponse();
        response.setTransactionId(txId);
        response.setUrl(authURL);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}