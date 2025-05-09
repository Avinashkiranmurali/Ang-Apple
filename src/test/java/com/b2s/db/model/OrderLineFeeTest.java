package com.b2s.db.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: cborn
 * Date: 9/16/13
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class OrderLineFeeTest {
    @Test
    public void testAllProperties() {
        OrderLineFee fee = new OrderLineFee();
        fee.setOrderID(1L);
        fee.setOrderLine(1);
        fee.setAmount(new BigDecimal(100));
        fee.setName("fee1");
        fee.setPoints(100);

        assertEquals("order id is not equal", new Long(1), fee.getOrderID());
        assertEquals("order line is not equal", new Integer(1), fee.getOrderLine());
        assertEquals("name is not equal", "fee1", fee.getName());
        assertEquals("amount is not equal", new BigDecimal(100), fee.getAmount());
        assertEquals("points is not equal", new Integer(100), fee.getPoints());
    }

    @Test
    public void testEqualsNull() {
        OrderLineFee fee = new OrderLineFee();
        assertNotNull(fee);
    }

    @Test
    public void testEqualsSameObject() {
        OrderLineFee fee = new OrderLineFee();
        OrderLineFee feeBis = new OrderLineFee();
        assertTrue(fee.equals(feeBis));
        assertEquals(fee.hashCode(),  feeBis.hashCode());
    }


    @Test
    public void testEqualsDifferentName() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee1");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(1L);
        fee2.setOrderLine(1);
        fee2.setAmount(new BigDecimal(100));
        fee2.setName("fee2");
        fee2.setPoints(100);

        assertEquals("equals returned true", false, fee1.equals(fee2));
        assert(fee1.hashCode() !=  fee2.hashCode());
    }

    @Test
    public void testEqualsDifferentOrderID() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(2L);
        fee2.setOrderLine(1);
        fee2.setAmount(new BigDecimal(100));
        fee2.setName("fee");
        fee2.setPoints(100);

        assertEquals("equals returned true", false, fee1.equals(fee2));
        assert(fee1.hashCode() !=  fee2.hashCode());
    }

    @Test
    public void testEqualsDifferentOrderLineID() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(1L);
        fee2.setOrderLine(2);
        fee2.setAmount(new BigDecimal(100));
        fee2.setName("fee");
        fee2.setPoints(100);

        assertEquals("equals returned true", false, fee1.equals(fee2));
        assert(fee1.hashCode() !=  fee2.hashCode());
    }

    @Test
    public void testEqualsDifferentAmount() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(1L);
        fee2.setOrderLine(1);
        fee2.setAmount(new BigDecimal(101));
        fee2.setName("fee");
        fee2.setPoints(100);

        assertEquals("equals returned true", false, fee1.equals(fee2));
        assert(fee1.hashCode() !=  fee2.hashCode());
    }

    @Test
    public void testEqualsDifferentPoints() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(1L);
        fee2.setOrderLine(1);
        fee2.setAmount(new BigDecimal(101));
        fee2.setName("fee");
        fee2.setPoints(101);

        assertEquals("equals returned true", false, fee1.equals(fee2));
        assert(fee1.hashCode() !=  fee2.hashCode());
    }

    @Test
    public void testEqualsSameData() {
        OrderLineFee fee1 = new OrderLineFee();
        fee1.setOrderID(1L);
        fee1.setOrderLine(1);
        fee1.setAmount(new BigDecimal(100));
        fee1.setName("fee1");
        fee1.setPoints(100);

        OrderLineFee fee2 = new OrderLineFee();
        fee2.setOrderID(1L);
        fee2.setOrderLine(1);
        fee2.setAmount(new BigDecimal(100));
        fee2.setName("fee1");
        fee2.setPoints(100);

        assertEquals("equals returned true", true, fee1.equals(fee2));
        assert(fee1.hashCode() ==  fee2.hashCode());
    }
}
