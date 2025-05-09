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
public class OrderLineTaxTest {
    @Test
    public void testAllProperties() {
        OrderLineTax tax = new OrderLineTax();
        tax.setOrderID(1L);
        tax.setOrderLine(1);
        tax.setAmount(new BigDecimal(100));
        tax.setName("tax1");
        tax.setPoints(1000);

        assertEquals("order id is not equal", new Long(1), tax.getOrderID());
        assertEquals("order line is not equal", new Integer(1), tax.getOrderLine());
        assertEquals("name is not equal", "tax1", tax.getName());
        assertEquals("amount is not equal", new BigDecimal(100), tax.getAmount());
        assertEquals("points is not equal", new Integer(1000), tax.getPoints());
    }

    @Test
    public void testEqualsNull() {
        OrderLineTax tax = new OrderLineTax();
        assertNotNull(tax);
    }

    @Test
    public void testEqualsSameObject() {
        OrderLineTax tax = new OrderLineTax();
        OrderLineTax taxBis = new OrderLineTax();
        assertTrue(tax.equals(taxBis));
        assertEquals(tax.hashCode(), taxBis.hashCode());
    }

    @Test
    public void testEqualsDifferentName() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax1");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(1L);
        tax2.setOrderLine(1);
        tax2.setAmount(new BigDecimal(100));
        tax2.setPoints(1000);
        tax2.setName("tax2");

        assertEquals("equals returned true", false, tax1.equals(tax2));
        assert(tax1.hashCode() !=  tax2.hashCode());
    }

    @Test
    public void testEqualsDifferentOrderID() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(2L);
        tax2.setOrderLine(1);
        tax2.setAmount(new BigDecimal(100));
        tax2.setPoints(1000);
        tax2.setName("tax");

        assertEquals("equals returned true", false, tax1.equals(tax2));
        assert(tax1.hashCode() !=  tax2.hashCode());
    }

    @Test
    public void testEqualsDifferentOrderLineID() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(1L);
        tax2.setOrderLine(2);
        tax2.setAmount(new BigDecimal(100));
        tax2.setPoints(1000);
        tax2.setName("tax");

        assertEquals("equals returned true", false, tax1.equals(tax2));
        assert(tax1.hashCode() !=  tax2.hashCode());
    }

    @Test
    public void testEqualsDifferentAmount() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(1L);
        tax2.setOrderLine(1);
        tax2.setAmount(new BigDecimal(101));
        tax2.setPoints(1000);
        tax2.setName("tax");

        assertEquals("equals returned true", false, tax1.equals(tax2));
        assert(tax1.hashCode() !=  tax2.hashCode());
    }

    @Test
    public void testEqualsDifferentPoints() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(1L);
        tax2.setOrderLine(1);
        tax2.setAmount(new BigDecimal(100));
        tax2.setPoints(2000);
        tax2.setName("tax");

        assertEquals("equals returned true", false, tax1.equals(tax2));
        assert(tax1.hashCode() !=  tax2.hashCode());
    }

    @Test
    public void testEqualsSameData() {
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setOrderID(1L);
        tax1.setOrderLine(1);
        tax1.setAmount(new BigDecimal(100));
        tax1.setPoints(1000);
        tax1.setName("tax1");

        OrderLineTax tax2 = new OrderLineTax();
        tax2.setOrderID(1L);
        tax2.setOrderLine(1);
        tax2.setAmount(new BigDecimal(100));
        tax2.setPoints(1000);
        tax2.setName("tax1");

        assertEquals("equals returned true", true, tax1.equals(tax2));
        assert(tax1.hashCode() ==  tax2.hashCode());
    }
}
