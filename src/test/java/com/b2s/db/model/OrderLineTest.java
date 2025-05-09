package com.b2s.db.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: cborn
 * Date: 10/16/13
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderLineTest {
    OrderLine orderLine = null;

    @Before
    public void setUp() {
        orderLine = new OrderLine();
        orderLine.setOrderId(1L);
        orderLine.setLineNum(1);
        orderLine.setQuantity(1);
        orderLine.setItemPoints(Double.valueOf(3847));
        orderLine.setSupplierItemPrice(2500);
        orderLine.setShippingPoints(Double.valueOf(0));
        orderLine.setSupplierShippingPrice(0);

        List<OrderLineFee> fees = new ArrayList<OrderLineFee>();
        OrderLineFee fee = new OrderLineFee();
        fee.setAmount(BigDecimal.valueOf(0));
        fee.setPoints(0);
        fees.add(fee);
        orderLine.setFees(fees);

        List<OrderLineTax> taxes = new ArrayList<OrderLineTax>();
        OrderLineTax tax1 = new OrderLineTax();
        tax1.setAmount(BigDecimal.valueOf(3.25));
        tax1.setPoints(500);
        taxes.add(tax1);
        OrderLineTax tax2 = new OrderLineTax();
        tax2.setAmount(BigDecimal.valueOf(0));
        tax2.setPoints(0);
        taxes.add(tax2);
        OrderLineTax tax3 = new OrderLineTax();
        tax3.setAmount(BigDecimal.valueOf(0));
        tax3.setPoints(0);
        taxes.add(tax3);
        orderLine.setTaxes(taxes);

        orderLine.setOrderLinePoints(4347);
    }

    @After
    public void tearDown() {
        orderLine = null;
    }

    @Test
    public void testTotalFeesInPoints() {
        assertEquals("total fees in points is not equal", Integer.valueOf(0), orderLine.getTotalFeesInPoints());
    }

    @Test
    public void testTotalTaxesInPoints() {
        assertEquals("total taxes in points is not equal", Integer.valueOf(500), orderLine.getTotalTaxesInPoints());
    }

    @Test
    public void testTotalFeesInMoneyMinor() {
        assertEquals("total taxes in money is not equal", BigDecimal.valueOf(0), orderLine.getTotalFeesInMoneyMinor
            ());
    }

    @Test
    public void testTotalTaxesInMoneyMinor() {
        assertEquals("total taxes in money is not equal", BigDecimal.valueOf(3.25), orderLine.getTotalTaxesInMoneyMinor
            ());
    }

    @Test
    public void testShippingPoints() {
        assertEquals("supplier shipping in points is not equal", Double.valueOf(0), orderLine.getShippingPoints());
    }

    @Test
    public void testSupplierShippingPrice() {
        assertEquals("supplier shipping in money is not equal", Integer.valueOf(0), orderLine
            .getSupplierShippingPrice());
    }

    @Test
    public void testSupplierTotalInMoneyMinor() {
        assertEquals("total supplier in money is not equal", BigDecimal.valueOf(2825).setScale(2), orderLine
            .getSupplierTotalInMoneyMinor());
    }

    @Test
    public void testOrderLinePoints() {
        assertEquals("total order line points is not equal", Integer.valueOf(4347), orderLine.getOrderLinePoints());
    }
}
