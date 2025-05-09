package com.b2s.rewards.apple.controller;

import com.b2s.apple.model.finance.CreditCardDetails;
import com.b2s.apple.services.PaymentServerV2Service;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to populate Card Details and Billing address
 *
 */
@RestController
@ResponseBody
public class CardDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(CardDetailsController.class);

    @Autowired
    private PaymentServerV2Service paymentService;

    /**
     * populating Card Details and Billing address
     * @param cardDetails
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/ccEntry/init"}, method = RequestMethod.POST)
    public ResponseEntity<Void> saveCardDetails(@RequestBody final CreditCardDetails cardDetails,
        final HttpServletRequest request)
        throws Exception {
        logger.info("Init call started");

        //get session attributes for cart and user to set the card details
        Cart sessionCart = (Cart) request.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);

        paymentService.addCCLineItemInCart(cardDetails, sessionCart, user, program);

        request.getSession().setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, sessionCart);
        request.getSession().setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        logger.info("Init call completed");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
