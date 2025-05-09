package com.b2s.shop.common.order.var.common

import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.model.OrderAttributeValue
import com.b2s.rewards.apple.model.Program
import com.b2s.shop.common.User

/**
 * data factory class  for vAR Order Manager Chase spec class
 *
 * Created by ahajamohideen on 07/05/2019.
 */

class VarOrderManagerChaseTestDataFactory {

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
        program.setVarId("1");
        program.setProgramId("1");
        program.setIsLocal(isLocal)
        program.setIsActive(true)
        return program;
    }


    static Order getOrderDetails() {
        return new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: 'en',
            countryCode: 'US', orderLines: new ArrayList<OrderLine>(),
            orderAttributeValues: new ArrayList<OrderAttributeValue>())
    }


}
