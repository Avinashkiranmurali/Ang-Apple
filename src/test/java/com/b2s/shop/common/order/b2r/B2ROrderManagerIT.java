package com.b2s.shop.common.order.b2r;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.db.model.*;
import com.b2s.rewards.apple.dao.OrderDao;
import com.b2s.rewards.apple.model.Program;
import com.b2s.shop.common.User;
import com.b2s.apple.entity.OrderEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
    DataSourceTestConfiguration.class})
@WebAppConfiguration
public class B2ROrderManagerIT {

    @Autowired
    public B2ROrderManager b2ROrderManager;

    @Autowired
    public OrderDao orderDao;


    /**
     * This method valid the rollback feature.
     *
     * B2ROrderManager.placeOrder()  is expected to be atomic.  Save all or roll back all inserts.
     * To test this behaviour we are trying to inert null value into not null column in order_line table which will
     * throw exception which will automatically rollback orders table insertion.
     *
     * 1.   Insert into orders table successfully
     * 2.   Try to insert null value into order_line's not null column supplier_id
     * 3.   Step 2 insertion will fail which eventually rollback orders table insert as well.
     * 4.   Assert by retrieving orders entry.  We should get null value
     */

    @Test
    public void testAB2RPlaceOrderFailureRollbackScenario(){

        Order order=new Order();
        OrderLine orderLine=new OrderLine();
        orderLine.setLineNum(1);
        List<OrderLine> line=new ArrayList<>();
        List<OrderLineTax> taxes=new ArrayList<>();
        List<OrderLineFee> fees=new ArrayList<>();
        List<OrderLineAttribute> attributes=new ArrayList<>();

        orderLine.setTaxes(taxes);
        orderLine.setFees(fees);
        orderLine.setOrderAttributes(attributes);
        orderLine.setOrderLinePoints(100);
        orderLine.setQuantity(1);
        line.add(orderLine);
        order.setOrderLines(line);
        User user = new User();
        Program program=new Program();
        program.setIsLocal(false);

        boolean exceptionOccured =false;
        try{
            b2ROrderManager.placeOrder(order,user,program);
        }catch (final Exception e){
            exceptionOccured=true;
        }

        Assert.assertTrue(exceptionOccured);

        OrderEntity orderEntity=orderDao.get(1L);
        Assert.assertNull(orderEntity);
    }


    /**
     * On successful insertion we should get order & order_line objects.
     */

    @Test
    public void testBB2RPlaceOrderSuccessScenario(){
        b2ROrderManager.placeOrder(getOrder(), getUser(), getProgram());

        OrderEntity orderEntity=orderDao.get(2L);
        Assert.assertNotNull(orderEntity);
        Assert.assertEquals(1,orderEntity.getOrderLines().size());
    }

    @Test
    public void testSelectOrderByIdNonNull(){
        Order order = getOrder();

        b2ROrderManager.placeOrder(order, getUser(), getProgram());
        OrderEntity orderEntity=orderDao.get(order.getOrderId());
        order = b2ROrderManager.selectOrderById(order.getOrderId());

        Assert.assertNotNull(orderEntity);
        Assert.assertNotNull(order);
        Assert.assertEquals(1,orderEntity.getOrderLines().size());

    }

    @Test
    public void testSelectOrderByIdNull(){
        Long orderId = 100L;
        OrderEntity orderEntity=orderDao.get(orderId);
        Order order = b2ROrderManager.selectOrderById(orderId);

        Assert.assertNull(orderEntity);
        Assert.assertNull(order);
    }

    Program getProgram(){
        Program program=new Program();
        program.setIsLocal(false);
        return program;
    }


    User getUser(){
        return new User();
    }

    Order getOrder(){
        Order order=new Order();
        OrderLine orderLine=new OrderLine();
        orderLine.setLineNum(1);
        orderLine.setSupplierId("200");

        List<OrderLine> line=new ArrayList<>();
        List<OrderLineTax> taxes=new ArrayList<>();
        List<OrderLineFee> fees=new ArrayList<>();
        List<OrderLineAttribute> attributes=new ArrayList<>();

        orderLine.setTaxes(taxes);
        orderLine.setFees(fees);
        orderLine.setOrderAttributes(attributes);
        orderLine.setOrderLinePoints(100);
        orderLine.setQuantity(1);
        line.add(orderLine);
        order.setOrderLines(line);
        return order;
    }
}
