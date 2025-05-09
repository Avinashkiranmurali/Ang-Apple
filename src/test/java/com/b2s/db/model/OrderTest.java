package com.b2s.db.model;

import com.b2s.rewards.common.util.CommonConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: cborn
 * Date: 10/16/13
 * Time: 1:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderTest {
    private Order order;

    @Before
    public void setUp() {
        order = new Order();
        order.setLanguageCode("en");
        order.setCountryCode("US");

        final List<OrderLine> orderLines = new ArrayList<>();

        final List<OrderLineFee> fees1 = new ArrayList<OrderLineFee>();
        fees1.add(createOrderLineFee(40, 62));

        final List<OrderLineTax> taxes1 = new ArrayList<OrderLineTax>();
        taxes1.add(createOrderLineTax(25, 39));
        taxes1.add(createOrderLineTax(52, 81));
        taxes1.add(createOrderLineTax(13, 21));

        final OrderLine orderLine1 =
            createOrderLine(1L, 1, CommonConstants.ORDER_STATUS_DEMO, CommonConstants.SUPPLIER_TYPE_MERC_GENERAL_S, 2, 7690, 4998, 515, 335, 8408, 0, fees1, taxes1);
        orderLines.add(orderLine1);

        final List<OrderLineFee> fees2 = new ArrayList<>();
        fees2.add(createOrderLineFee(40, 62));

        final List<OrderLineTax> taxes2 = new ArrayList<OrderLineTax>();
        taxes2.add(createOrderLineTax(25, 39));
        taxes2.add(createOrderLineTax(52, 81));
        taxes2.add(createOrderLineTax(13, 21));
        final OrderLine orderLine2 =
            createOrderLine(1L, 2, CommonConstants.ORDER_STATUS_PARTIAL_REFUND, CommonConstants.SUPPLIER_TYPE_MERC_GENERAL_S, 1, 7690, 4998, 515, 335, 8408, 10, fees2, taxes2);
        orderLines.add(orderLine2);

        final List<OrderLineFee> fees3 = new ArrayList<>();
        final List<OrderLineTax> taxes3 = new ArrayList<>();
        final OrderLine orderLine3 =
            createOrderLine(1L, 3, CommonConstants.ORDER_STATUS_DEMO, CommonConstants.SUPPLIER_TYPE_CREDIT_S, 1, -10, -7, 0, 0, -10, 0, fees3, taxes3);
        orderLines.add(orderLine3);

        order.setOrderLines(orderLines);
    }

    @After
    public void tearDown() {
        order = null;
    }

    @Test
    public void testOrderTotalInPoints() {
        assertEquals("total order in points is not equal", 25224, order.getOrderTotalInPoints());
    }

    @Test
    public void testOrderTotalPointsIncludingDiscountsAndCredits() {
        assertEquals("total order in points including Discounts and Credits is not equal", 25214, order.getOrderTotalPointsIncludingDiscountsAndCredits());
    }

    @Test
    public void testOrderTotalInMoney() {
        assertEquals("total order in money is not equal", BigDecimal.valueOf(549.99), order.getOrderTotalInMoney()
            .getAmount());
    }

    @Test
    public void testOrderTotalTaxesAndFeesInMoney() {
        assertEquals("total order fees and taxes in money is not equal", BigDecimal.valueOf(390).setScale(2), order.getOrderTotalTaxesAndFeesInMoney().getAmount());
    }

    @Test
    public void testOrderTotalTaxesAndFeesInPoints() {
        assertEquals("total order fees and taxes in money is not equal", 609, order.getOrderTotalTaxesAndFeesInPoints());
    }

    @Test
    public void testOrderTotalFeesInMoney() {
        assertEquals("total order fees in money is not equal", BigDecimal.valueOf(120).setScale(2), order.getOrderTotalFeesInMoney().getAmount());
    }

    @Test
    public void testOrderTotalFeesInPoints() {
        assertEquals("total order fees in money is not equal", 186, order.getOrderTotalFeesInPoints());
    }

    @Test
    public void testOrderTotalTaxesInMoney() {
        assertEquals("total order taxes in money is not equal", BigDecimal.valueOf(270).setScale(2), order.getOrderTotalTaxesInMoney().getAmount());
    }

    @Test
    public void testOrderTotalTaxesInPoints() {
        assertEquals("total order taxes in money is not equal", 423, order.getOrderTotalTaxesInPoints());
    }

    @Test
    public void testOrderSubTotalInMoney() {
        assertEquals("total order subtotal in money is not equal", BigDecimal.valueOf(149.94).setScale(2), order.getOrderSubTotalInMoney().getAmount());
    }

    @Test
    public void testOrderSubTotalInPoints() {
        assertEquals("total order subtotal in points is not equal", 23070, order.getOrderSubTotalInPoints());
    }

    @Test
    public void testOrderTotalShippingInMoney() {
        assertEquals("total order shipping in money is not equal", BigDecimal.valueOf(10.05).setScale(2), order.getOrderTotalShippingInMoney().getAmount());
    }

    @Test
    public void testOrderTotalShippingInPoints() {
        assertEquals("total order shipping in points is not equal", 1545, order.getOrderTotalShippingInPoints());
    }

    @Test
    public void testOrderTotalMoneyPaid() {
        assertEquals("total order money paid is not equal", BigDecimal.valueOf(00.07).setScale(2), order.getOrderTotalMoneyPaid().getAmount());
    }

    @Test
    public void testOrderTotalPointsPaid() {
        assertEquals("total order points paid is not equal", 25214, order.getOrderTotalPointsPaid());
    }

    @Test
    public void testGetOrderTotalPointsPaidFilterCriteria() {
        final int expectedTotalPointsPaid1 = 25214;
        final int totalPointsPaid1 = order.getOrderTotalPointsPaid(new Order.FilterCriteria() {
            @Override
            public boolean isInclusionCriteriaSatisfied(final OrderLine orderLine) {
                return orderLine.getSupplierId() != CommonConstants.SUPPLIER_TYPE_CREDIT_S;
            }
        });
        assertThat(
            "totalPointsPaid should be equalTo " + expectedTotalPointsPaid1,
            totalPointsPaid1,
            is(equalTo(expectedTotalPointsPaid1))
        );

        final int expectedTotalPointsPaid2 = 16816;
        final int totalPointsPaid2 = order.getOrderTotalPointsPaid(new Order.FilterCriteria() {
            @Override
            public boolean isInclusionCriteriaSatisfied(final OrderLine orderLine) {
                return orderLine.getSupplierId() != CommonConstants.SUPPLIER_TYPE_CREDIT_S &&
                    orderLine.getOrderStatus() != CommonConstants.ORDER_STATUS_PARTIAL_REFUND;
            }
        });
        assertThat(
            "totalPointsPaid should be equalTo " + expectedTotalPointsPaid2,
            totalPointsPaid2,
            is(equalTo(expectedTotalPointsPaid2))
        );
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private OrderLine createOrderLine(
        final Long orderId,
        final int lineNbr,
        final int orderStatus,
        final String supplierId,
        final int quantity,
        final double itemPoints,
        final int supplierItemPrice,
        final double shippingPoints,
        final int supplierShippingPoints,
        final int orderLinePoints,
        final int cashBuyInPoints,
        final List<OrderLineFee> orderLineFees,
        final List<OrderLineTax> orderLineTaxes
    ) {
        final OrderLine orderLine = new OrderLine();

        orderLine.setOrderId(orderId);
        orderLine.setLineNum(lineNbr);
        orderLine.setOrderStatus(orderStatus);
        orderLine.setSupplierId(supplierId);
        orderLine.setQuantity(quantity);
        orderLine.setItemPoints(itemPoints);
        orderLine.setSupplierItemPrice(supplierItemPrice);
        orderLine.setShippingPoints(shippingPoints);
        orderLine.setSupplierShippingPrice(supplierShippingPoints);
        orderLine.setCashBuyInPoints(BigDecimal.valueOf(cashBuyInPoints));
        orderLine.setOrderLinePoints(orderLinePoints);
        orderLine.setFees(orderLineFees);
        orderLine.setTaxes(orderLineTaxes);

        return orderLine;
    }

    private OrderLineFee createOrderLineFee(final double amount, final int points) {
        final OrderLineFee orderLineFee = new OrderLineFee();
        orderLineFee.setAmount(BigDecimal.valueOf(amount));
        orderLineFee.setPoints(points);

        return orderLineFee;
    }

    private OrderLineTax createOrderLineTax(final double amount, final int points) {
        final OrderLineTax orderLineTax = new OrderLineTax();
        orderLineTax.setAmount(BigDecimal.valueOf(amount));
        orderLineTax.setPoints(points);

        return orderLineTax;
    }
}
