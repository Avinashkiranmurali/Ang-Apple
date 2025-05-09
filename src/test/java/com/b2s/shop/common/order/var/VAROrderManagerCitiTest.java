package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.integration.model.RedemptionOrderLine;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by ppalpandi on 12/13/2017.
 */
public class VAROrderManagerCitiTest {

    private VAROrderManagerCiti varOrderManagerCiti = new VAROrderManagerCiti();
    private final String VAR_ORDER_LINE_ID = "2008797047" ;

    @Test
    public void nullVarOrderLineIdTest()  throws Exception {
        final Order order = new Order();
        order.setOrderLines(Arrays.asList(new OrderLine()));
        varOrderManagerCiti.setOrderLineType(order,Arrays.asList(new RedemptionOrderLine()));
        order.getOrderLines().forEach(orderLineItem ->{
            final OrderLine orderLine =(OrderLine)orderLineItem;
            assertNull(VAR_ORDER_LINE_ID, orderLine.getOrderLineType());
        });

    }

    @Test
    public void testSetOrderLineType() throws Exception {
        final Order order = new Order();
        order.setOrderLines(Arrays.asList(new OrderLine(), new OrderLine()));
        varOrderManagerCiti.setOrderLineType(order,getRedemptionOrderLines());
        order.getOrderLines().forEach(orderLineItem ->{
            final OrderLine orderLine =(OrderLine)orderLineItem;
            assertNotNull(VAR_ORDER_LINE_ID, orderLine.getOrderLineType());
        });

    }
    private List<RedemptionOrderLine> getRedemptionOrderLines(){

        RedemptionOrderLine redemptionOrderLine1 = new RedemptionOrderLine();
        redemptionOrderLine1.setOrderLineId("1");
        redemptionOrderLine1.setVarOrderLineId(VAR_ORDER_LINE_ID);

        RedemptionOrderLine redemptionOrderLine2 = new RedemptionOrderLine();
        redemptionOrderLine2.setOrderLineId("2");
        redemptionOrderLine2.setVarOrderLineId(VAR_ORDER_LINE_ID);
        return Arrays.asList(redemptionOrderLine1,redemptionOrderLine2);
    }

    @Test
    public void testApplyWithIgnoreProfileAddress() {
        Program program = new Program();
        program.getConfig().put(CommonConstants.IGNORE_PROFILE_ADDRESS, Boolean.TRUE);
        User user = new UserCiti();
        user.setAddr1("NONEMPTYVALUE");
        user.setAddr2("NONEMPTYVALUE");
        user.setCity("NONEMPTYVALUE");
        user.setZip("NONEMPTYVALUE");
        varOrderManagerCiti.applyIgnoreProfileToUser(user, program);
        assertNull("Addr1 needs to be null", user.getAddr1());
        assertNull("Addr2 needs to be null", user.getAddr2());
        assertNull("City needs to be null", user.getCity());
        assertNull("Zip needs to be null", user.getZip());
    }

    @Test
    public void testApplyWithOutIgnoreProfileAddress() {
        Program program = new Program();
        program.getConfig().put(CommonConstants.IGNORE_PROFILE_ADDRESS, Boolean.FALSE);
        User user = new UserCiti();
        user.setAddr1("NONEMPTYVALUE");
        user.setAddr2("NONEMPTYVALUE");
        user.setCity("NONEMPTYVALUE");
        user.setZip("NONEMPTYVALUE");
        varOrderManagerCiti.applyIgnoreProfileToUser(user, program);
        assertNotNull("Addr1 needs to be null", user.getAddr1());
        assertNotNull("Addr2 needs to be null", user.getAddr2());
        assertNotNull("City needs to be null", user.getCity());
        assertNotNull("Zip needs to be null", user.getZip());

    }

}