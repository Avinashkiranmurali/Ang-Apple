package com.b2s.shop.common.order.var.common

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.common.services.discountservice.CouponDetails
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.model.OrderAttributeValue
import com.b2s.rewards.apple.model.PaymentOption
import com.b2s.rewards.apple.model.Program
import com.b2s.shop.common.User

/**
 * data factory class  for all varOrderManager classes
 *
 * Created by ssundaramoorthy on 03/18/2021.
 */

class VarOrderManagerTestDataFactory {

    static User getUserDetails() {
        User user = new User();
        user.setPoints(1000);
        user.setProgramid("1");
        user.setUserid("user_123");
        user.setVarid("1");
        user.setPassword("empass1");
        return user;
    }

    static Program getProgramDetails(Boolean isLocal) {
        Program program = new Program();
        program.setVarId("Delta")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(isLocal)
        program.setIsActive(true)

        PaymentOption paymentOption = new PaymentOption()
        paymentOption.setPaymentOption("POINTS")
        List<PaymentOption> paymentOptionList = new ArrayList<PaymentOption>()
        paymentOptionList.add(paymentOption)
        program.setPayments(paymentOptionList)

        return program
    }

    static DemoUserEntity getDemoUserEntity() {
        DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("RBC")
        demoUserId.setUserId("demo")

        DemoUserEntity demoUserEntity = new DemoUserEntity()
        demoUserEntity.setFirstname("Eric")
        demoUserEntity.setDemoUserId(demoUserId)
        demoUserEntity.setZip("1234-122-123")

        return demoUserEntity
    }

    static CouponDetails getCouponDetails() {
        CouponDetails couponDetails = new CouponDetails()
        couponDetails.setIsValid(true)
        return couponDetails
    }

    static String[] getRelayState() {
        List<String> relayList = new ArrayList<String>()
        relayList.add("https://localhost.bridge2solutions.net/apple-gr?referer=12345")
        return relayList.toArray(new String[relayList.size()])
    }

    static List<PaymentOption> getPaymentListCash() {
        PaymentOption paymentOption = new PaymentOption()
        paymentOption.setPaymentOption("CASH")
        List<PaymentOption> paymentOptionList = new ArrayList<PaymentOption>()
        paymentOptionList.add(paymentOption)
        return paymentOptionList
    }
}