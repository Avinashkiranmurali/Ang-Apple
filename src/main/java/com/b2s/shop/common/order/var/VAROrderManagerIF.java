package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public interface VAROrderManagerIF {

    User selectUser(HttpServletRequest request) throws B2RException;

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean placeOrder(Order order, User user, Program program);

    boolean cancelOrder(Order order);

    boolean cancelOrder(Order order, User user, Program program);

    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "MethodWithTooManyParameters"})
    boolean updateOrderStatus(Map<String, Object> properties, String varOrderId, String orderId, String lineNum,
        String carrier, String shippingDesc, String trackingNum, String status, Double points);

    int getUserPoints(User userId, Program program)
        throws IOException, B2RException;

    Message getMessage();

    String getVAROrderId();

    boolean isSendOrderConfirmationEmailToUser();

    @SuppressWarnings("rawtypes")
    Map getProperties(String varId, String programId);

    boolean isOrderReadyForProcessing();

    boolean isValidLogin(User user, Program program);

    void computePricingModel(Product product, User user, Program program);

    Map<String, Map<String,String>> getAllActivationFees(String varId, String programId);

    boolean performAPIPostBack(final Order order, final User user, final Program program);
}
