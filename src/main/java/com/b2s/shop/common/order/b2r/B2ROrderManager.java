package com.b2s.shop.common.order.b2r;

import com.b2s.db.model.*;
import com.b2s.rewards.apple.dao.*;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.OrderCommitStatusService;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.apple.entity.*;
import com.b2s.apple.services.MessageService;
import com.b2s.rewards.model.OrderSource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class B2ROrderManager {

    public static final int SUCCESS = 1;
    public static final int FAIL = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(B2ROrderManager.class);

    @Autowired
    protected OrderDao orderDao;

    @Autowired
    protected OrderLineDao orderLineDao;

    @Autowired
    protected VarProgramAccountActivityDao varProgramAccountActivityDao;

    @Autowired
    private MessageService messageService;

    @Autowired
    private OrderCommitStatusService orderCommitStatusService;

    @Autowired
    private OrderDiagnosticInfoDao orderDiagnosticInfoDao;

    @Autowired
    private OrderAdditionalInfoDao orderAdditionalInfoDao;


    private Message message;


    public Message getMessage() {
        return message;
    }


    public Order selectOrderById(final Long orderId) {

        Order order = null;
        final OrderEntity orderEntity = orderDao.get(orderId);
        if(Objects.nonNull(orderEntity)){
            order = mapToModelOrder(orderEntity);
        }

        return order;
    }

    private void truncateOrderLineColor(final OrderLineEntity orderLine) {
        orderLine.setColor(String.format("%.99s", orderLine.getColor()));
        LOGGER.debug("Resizing color to size 99 for {} {} {}", orderLine.getItemId(), orderLine.getSupplierId(), orderLine.getImageUrl());
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW)
    public boolean placeOrder(final Order orderModel, final User user, final Program program) {

        OrderEntity order = mapToOrderEntity(orderModel);

        LOGGER.info(
            "Order Placement: Start Persisting Order related entries for user/var/program: {} / {} / {}.....",
            order.getUserId(),
            order.getVarId(),
            order.getProgramId());

        //BR-5393 -start
        setDefaultAddressAndCountry(user, order);
        //BR-5393 -end
            int totalOrderPoints = 0;
            for (final OrderLineEntity orderLine : order.getOrderLines()) {
                truncateOrderLineColor(orderLine);
                totalOrderPoints += orderLine.getOrderLinePoints() * orderLine.getQuantity();
            }


            orderDao.save(order);
            LOGGER.info("Order Placement: Persisted order entry with orderId {}",order.getOrderId());

            final long orderId = order.getOrderId();
            orderModel.setOrderId(orderId);

            order.getOrderLines().forEach( orderLineEntity -> {
                orderLineEntity.getOrderLineId().setOrderId(orderId);
                if(Objects.nonNull(orderLineEntity.getTaxes())){
                    orderLineEntity.getTaxes().forEach(orderLineTaxEntity -> orderLineTaxEntity.getOrderLineTaxId().setOrderId(orderId));
                }
                if(Objects.nonNull(orderLineEntity.getFees())){
                    orderLineEntity.getFees().forEach(orderLineFeeEntity ->  orderLineFeeEntity.getOrderLineFeeId().setOrderId(orderId));
                }
                if(Objects.nonNull(orderLineEntity.getOrderLineAttributes())){
                    orderLineEntity.getOrderLineAttributes().forEach(orderLineAttributeEntity ->  orderLineAttributeEntity.setOrderId(orderId));
                }

            });
            orderModel.getOrderLines().forEach( o ->  ((OrderLine)o).setOrderId(orderId) );

            orderLineDao.saveAll(order.getOrderLines());
            LOGGER.info("Order Placement: Persisted orderLines/Tax/Fees entry");

            final OrderAdditionalInfoEntity addInfo = new OrderAdditionalInfoEntity();
            addInfo.setOrderId(orderId);
            addInfo.setAttr2(order.getIpAddress());

            final OrderDiagnosticInfoEntity orderDiagnosticInfo = new OrderDiagnosticInfoEntity();
            orderDiagnosticInfo.setHostname(user.getHostName());
            orderDiagnosticInfo.setIpAddress(user.getIPAddress());
            orderDiagnosticInfo.setOrderId(orderId);

            orderAdditionalInfoDao.save(addInfo);
            orderDiagnosticInfoDao.save(orderDiagnosticInfo);

            LOGGER.info("Order Placement: Persisted AdditionalInfo/DiagnosticInfo");

            //Persisting var program account activity
            if (program.getIsLocal()) {

                final VarProgramAccountActivityEntity act = new VarProgramAccountActivityEntity();
                act.setAdminUser("");
                act.setAmount(totalOrderPoints * -1);
                act.setLineNum(0);
                act.setOrderId(orderId);
                act.setTranType(CommonConstants.REDEMPTION);
                act.setName("ORDER:" + " " + order.getOrderId());
                act.setVarId(program.getVarId());
                act.setProgramId(program.getProgramId());
                act.setUserId(user.getUserId());
                act.setCreateDate(new Date(System.currentTimeMillis()));
                varProgramAccountActivityDao.save(act);
                LOGGER.info("Order Placement: Persisted VAR Program Account Activity....");
            }

            LOGGER.info("Order Placement: End persisting Order.... ");

            final Message msg = new Message();
            msg.setSuccess(true);
            msg.setContentText("B2R successfully placed order: Order# " + orderId);
            msg.setB2ROrderId(Long.toString(orderId));
            msg.setCode(B2ROrderManager.SUCCESS);
            message = msg;

        // default to true to mark successful complition.  Error will be thrown on failure and hence its handled.
        return true;
    }

    private void setDefaultAddressAndCountry(User user, OrderEntity order) {
        if (order.getAddr2() == null || "NULL".equalsIgnoreCase(order.getAddr2())) {
            if (user.getAddr2() != null && !"NULL".equalsIgnoreCase(user.getAddr2())) {
                order.setAddr2(user.getAddr2());
            } else {
                order.setAddr2("");
            }
        }
        if (order.getCountry() == null || "NULL".equalsIgnoreCase(order.getCountry())) {
            if (user.getCountry() != null && !"NULL".equalsIgnoreCase(user.getCountry())) {
                order.setCountry(user.getCountry());
            } else {
                order.setCountry("");
            }
        }
        if ("USA".equalsIgnoreCase(order.getCountry())) {
            order.setCountry("US");
        }
    }

    public void updateOrderStatus(final Long orderId, final Integer lineNum, final Integer orderStatus, final String supplierId) {
        final OrderLineId orderLineId = new OrderLineId();
        orderLineId.setOrderId(orderId);
        orderLineId.setLineNum(lineNum);
        final OrderLineEntity ol = new OrderLineEntity();
        ol.setOrderLineId(orderLineId);
        ol.setOrderStatus(orderStatus);

        orderLineDao.saveOrUpdate(ol);
    }

    public void updateOrderLine(final OrderLine line) {
        final OrderLineEntity orderLineEntity = mapToOrderLineEntity(line);
        orderLineDao.saveOrUpdate(orderLineEntity);
    }

    public void updateProductOrderLineStatus(final Order order, final int statusCode) {

        try {
            if (order != null && CollectionUtils.isNotEmpty(order.getOrderLines())) {
                order.getOrderLines().stream()
                    .filter(orderLine -> (((OrderLine) orderLine).getOrderStatus() != 11))
                    .filter(orderLine -> (((OrderLine) orderLine).getOrderStatus() != statusCode))
                    .filter(orderLine ->
                        CommonConstants.SUPPLIER_TYPE_MERC_GENERAL_S.equals(((OrderLine) orderLine).getSupplierId()) ||
                            CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S.equals(((OrderLine) orderLine).getSupplierId()) ||
                            CommonConstants.SUPPLIER_TYPE_GIFTCARD_S.equals(((OrderLine) orderLine).getSupplierId()))
                    .peek(orderLine -> ((OrderLine) orderLine).setOrderStatus(statusCode))
                    .forEach(orderLine -> updateOrderLine((OrderLine) orderLine));
            }
        } catch (final Exception e) {
            LOGGER.error("Order Placement: Error while updating order line status to {}. Exception: {}", statusCode, e);
        }
    }

    public void updateNonProductOrderLineStatus(final Order order, final int statusCode) {

        try {
            if (order != null && CollectionUtils.isNotEmpty(order.getOrderLines())) {
                order.getOrderLines().stream()
                    .filter(orderLine -> (((OrderLine) orderLine).getOrderStatus() != 11))
                    .filter(orderLine -> (((OrderLine) orderLine).getOrderStatus() != statusCode))
                    .filter(orderLine ->
                        !CommonConstants.SUPPLIER_TYPE_MERC_GENERAL_S.equals(((OrderLine) orderLine).getSupplierId()) &&
                            !CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S.equals(((OrderLine) orderLine).getSupplierId()) &&
                            !CommonConstants.SUPPLIER_TYPE_GIFTCARD_S.equals(((OrderLine) orderLine).getSupplierId()))
                    .peek(orderLine -> ((OrderLine) orderLine).setOrderStatus(statusCode))
                    .forEach(orderLine -> updateOrderLine((OrderLine) orderLine));
            }
        } catch (final Exception e) {
            LOGGER.error("Order Placement: Error while updating order line status to {}. Exception: {}", statusCode, e);
        }
    }

    public void updateOrderVAROrderId(final Order order, final String varOrderId) {
        try {
            order.setGiftMessage(messageService.replaceOrderIdToken(varOrderId, order.getGiftMessage()));
            OrderEntity orderEntity=orderDao.get(order.getOrderId());

            orderEntity.setSupplierId(order.getSupplierId());
            orderEntity.setOrderId(order.getOrderId());
            orderEntity.setVarOrderId(varOrderId);
            orderEntity.setGiftMessage(order.getGiftMessage());
            orderDao.saveOrUpdate(orderEntity);
        } catch (final Exception ex) {
            LOGGER.info("Order Placement: Failed in updateOrderVAROrderId...", ex);
            messageService.insertMessageException(
                null,
                this.getClass().getName(),
                "ERROR Updating Var Order Id in B2R OrderEntity - Orderid :" + order.getOrderId(),
                ex);
        }
    }

    /**
     * Change the orderline Status to FAILED with status 99
     *
     * @param order
     * @param user
     */
    public void rollBackOrder(final Order order, final User user) {
        try {
            LOGGER.warn(
                "Order Transaction Failed on apple-gr for Order: {}. Updating order lines to FAILED. Please trace server log for Order failure details.",
                order.getOrderId());
            OrderEntity orderEntity=orderDao.get(order.getOrderId());

            orderEntity.getOrderLines().forEach(orderLineEntity -> {

                orderLineEntity.setOrderStatus(CommonConstants.ORDER_STATUS_FAILED);
                orderLineEntity.setComment("Order Transaction Failed on apple-gr for this Order. Please see server log for details around this timestamp.");
                orderLineDao.saveOrUpdate(orderLineEntity);
            });

            LOGGER.info("Removing orders from OrderCommitStatus... pending order scenario");
            orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(), user.getProgramId(), order);
            LOGGER.info(
                "Order Placement: Successfully UPDATED order lines statuses to FAILED for Order {}. Trace this server log for details.",
                order.getOrderId());
        } catch (final Exception ex) {
            LOGGER.error(
                "Order Placement: Failed to update Orderline with FAILED status for Order {}. Exception Trace :{}",
                order.getOrderId(),
                ex.getMessage());
        }
    }

    // Insert PD orderLine
    public void insertOrderLine(final OrderLine orderLine) {

        LOGGER.info("insert orderline for order # {}, lineNum # {}", orderLine.getOrderId(), orderLine.getLineNum());
        orderLineDao.save(mapToOrderLineEntity(orderLine));
    }

    private OrderEntity mapToOrderEntity(final Order order) {

        LOGGER.info("Order Placement: Transforming the order model");
        final OrderEntity orderEntity = new OrderEntity();

        orderEntity.setAddr1(order.getAddr1());
        orderEntity.setAddr2(order.getAddr2());
        orderEntity.setAddr3(order.getAddr3());
        orderEntity.setAppVersion(order.getAppVersion());

        orderEntity.setBusinessName(order.getBusinessName());

        orderEntity.setCity(order.getCity());
        orderEntity.setCountry(order.getCountry());
        orderEntity.setCountryCode(order.getCountryCode());
        orderEntity.setCurrencyCode(order.getCurrencyCode());

        orderEntity.setEmail(order.getEmail());
        orderEntity.setEarnedPoints(order.getEarnedPoints());

        orderEntity.setFirstName(order.getFirstname());

        orderEntity.setGiftMessage(order.getGiftMessage());
        orderEntity.setGstAmount(order.getGstAmount());

        orderEntity.setIsApplySuperSaverShipping(order.getIsApplySuperSaverShipping());
        orderEntity.setIpAddress(order.getIpAddress());
        orderEntity.setIsAddressChanged(order.getIsAddressChanged());

        orderEntity.setLastName(order.getLastname());
        orderEntity.setLastUpdate(order.getLastUpdate());
        orderEntity.setLanguageCode(order.getLanguageCode());

        orderEntity.setNotificationType(order.getNotificationType());

        orderEntity.setOrderId(order.getOrderId());
        orderEntity.setOrderDate(order.getOrderDate());
        orderEntity.setOrderSource(order.getOrderSource());
        orderEntity.setEstablishmentFeesPoints(order.getEstablishmentFeesPoints());
        orderEntity.setEstablishmentFeesPrice(order.getEstablishmentFeesPrice());
        //set OrderLineEntity
        final Set<OrderLineEntity> orderLineEntities = new HashSet<>();
        for (final Object orderLine : order.getOrderLines()) {
            final OrderLineEntity orderLineEntity = mapToOrderLineEntity((OrderLine) orderLine);
            orderLineEntities.add(orderLineEntity);
        }
        orderEntity.setOrderLines(orderLineEntities);

        orderEntity.setOrderAttributeValues(order.getOrderAttributeValues());

        orderEntity.setPhone(order.getPhone());
        orderEntity.setProgramId(order.getProgramId());
        orderEntity.setProxyUserId(order.getProxyUserId());

        orderEntity.setSupplierId(order.getSupplierId());
        orderEntity.setState(order.getState());
        orderEntity.setShipDesc(order.getShipDesc());

        orderEntity.setUserId(order.getUserId());
        orderEntity.setUserPoints(order.getUserPoints());

        orderEntity.setVarId(order.getVarId());
        orderEntity.setVarOrderId(order.getVarOrderId());

        orderEntity.setZip(order.getZip());

        LOGGER.debug("Done converting Order to OrderEntity");

        return orderEntity;
    }

    private OrderLineEntity mapToOrderLineEntity(final OrderLine orderLine) {

        LOGGER.debug("Transforming the order line model with orderId:{} and order line num: {}", orderLine.getOrderId(), orderLine.getLineNum());
        final OrderLineEntity orderLineEntity = new OrderLineEntity();

        orderLineEntity.setAttr1(orderLine.getAttr1());
        orderLineEntity.setAttr2(orderLine.getAttr2());
        orderLineEntity.setAttr3(orderLine.getAttr3());

        orderLineEntity.setB2sTaxPrice(orderLine.getB2sTaxPrice());
        orderLineEntity.setB2sTaxPoints(orderLine.getB2sTaxPoints());
        orderLineEntity.setB2sItemProfitPrice(orderLine.getB2sItemProfitPrice());
        orderLineEntity.setB2sShippingProfitPrice(orderLine.getB2sShippingProfitPrice());
        orderLineEntity.setB2sTaxProfitPrice(orderLine.getB2sTaxProfitPrice());
        orderLineEntity.setB2sShippingMargin(orderLine.getB2sShippingMargin());
        orderLineEntity.setB2sItemMargin(orderLine.getB2sItemMargin());
        orderLineEntity.setB2sTaxRate(orderLine.getB2sTaxRate());
        orderLineEntity.setB2sOnlineFee(orderLine.getB2sOnlineFee());
        orderLineEntity.setB2sOfflineFee(orderLine.getB2sOfflineFee());
        orderLineEntity.setBrand(orderLine.getBrand());
        orderLineEntity.setBookingQuantity(orderLine.getBookingQuantity());

        orderLineEntity.setCategory(orderLine.getCategory());
        orderLineEntity.setCategoryPath(orderLine.getCategoryPath());
        orderLineEntity.setConvRate(orderLine.getConvRate());
        orderLineEntity.setCreateDate(orderLine.getCreateDate());
        orderLineEntity.setColor(orderLine.getColor());
        orderLineEntity.setComment(orderLine.getComment());
        orderLineEntity.setCashBuyInPoints(orderLine.getCashBuyInPoints());
        orderLineEntity.setCashBuyInPrice(orderLine.getCashBuyInPrice());


        orderLineEntity.setDiscountedSupplierItemPrice(orderLine.getDiscountedSupplierItemPrice());
        orderLineEntity.setDiscountedVarOrderLinePrice(orderLine.getDiscountedVarOrderLinePrice());
        orderLineEntity.setDiscountedFees(orderLine.getDiscountedFees());
        orderLineEntity.setDiscountedTaxes(orderLine.getDiscountedTaxes());

        orderLineEntity.setEffConvRate(orderLine.getEffConvRate());

        //set order line fees
        if(Objects.nonNull(orderLine.getFees())){
            final List<OrderLineFeeEntity> orderLineFeeEntityList = orderLine.getFees().stream().map(orderLineFee -> {
                final OrderLineFeeEntity olFee = new OrderLineFeeEntity();
                final OrderLineFeeEntity.OrderLineFeeId orderLineFeeId=new OrderLineFeeEntity.OrderLineFeeId();
                orderLineFeeId.setOrderId(orderLine.getOrderId());
                orderLineFeeId.setLineNum(orderLine.getLineNum());
                orderLineFeeId.setName(orderLineFee.getName());
                olFee.setOrderLineFeeId(orderLineFeeId);
                olFee.setOrderLine(orderLineEntity);
                olFee.setAmount(
                    orderLineFee.getAmount().multiply(new BigDecimal(CommonConstants.CENTS_TO_DOLLARS_DIVISOR))
                        .longValue());
                olFee.setPoints(Long.valueOf(orderLineFee.getPoints()));
                olFee.setCreateTime(orderLineFee.getCreateTime());
                return olFee;
            }).collect(Collectors.toList());
            orderLineEntity.setFees(orderLineFeeEntityList);
        }
        orderLineEntity.setFxRate(orderLine.getFxRate());

        orderLineEntity.setGatewayOrderNumber(orderLine.getGatewayOrderNumber());

        orderLineEntity.setImageUrl(orderLine.getImageUrl());
        orderLineEntity.setItemId(orderLine.getItemId());
        orderLineEntity.setItemPoints(orderLine.getItemPoints());
        orderLineEntity.setIsEligibleForSuperSaverShipping(orderLine.getIsEligibleForSuperSaverShipping());

        orderLineEntity.setListingId(orderLine.getListingId());

        orderLineEntity.setMerchantId(orderLine.getMerchantId());
        orderLineEntity.setManufacturer(orderLine.getManufacturer());

        orderLineEntity.setName(orderLine.getName());
        orderLineEntity.setNotificationId(orderLine.getNotificationId());


        orderLineEntity.setOrderLineNum(orderLine.getLineNum());
        orderLineEntity.setOrderSource(OrderSource.WEB.name());
        orderLineEntity.setOrderLinePoints(orderLine.getOrderLinePoints());
        orderLineEntity.setOrderDelay(orderLine.getOrderDelay());
        orderLineEntity.setOrderStatus(orderLine.getOrderStatus());
        orderLineEntity.setOrderLineType(orderLine.getOrderLineType());
        orderLineEntity.setOrderLineUndiscountedUnitFee(orderLine.getOrderLineUndiscountedUnitFee());

        final OrderLineId orderLineId = new OrderLineId();
        orderLineId.setOrderId(orderLine.getOrderId());
        orderLineId.setLineNum(orderLine.getLineNum());
        orderLineEntity.setOrderLineId(orderLineId);


        //Add line attributes
        if(Objects.nonNull(orderLine.getOrderAttributes())) {
            final List<OrderLineAttributeEntity> orderLineAttributeEntityList =
                orderLine.getOrderAttributes().stream().map(attribute -> {
                    final OrderLineAttributeEntity olAttribute = new OrderLineAttributeEntity();
                    olAttribute.setOrderLine(orderLineEntity);
                    olAttribute.setLineNum(orderLineEntity.getOrderLineNum());
                    olAttribute.setName(attribute.getName());
                    olAttribute.setValue(attribute.getValue());
                    return olAttribute;
                }).collect(Collectors.toList());
            orderLineEntity.setOrderLineAttributes(orderLineAttributeEntityList);
        }

        orderLineEntity.setPointsRoundingIncrement(orderLine.getPointsRoundingIncrement());
        orderLineEntity.setProgramId(orderLine.getProgramId());
        orderLineEntity.setPolicyId(orderLine.getPolicyId());

        orderLineEntity.setQuantity(orderLine.getQuantity());


        orderLineEntity.setSupplierId(orderLine.getSupplierId());
        orderLineEntity.setSupplierItemPrice(orderLine.getSupplierItemPrice());
        orderLineEntity.setSupplierTaxPrice(orderLine.getSupplierTaxPrice());
        orderLineEntity.setSupplierShippingPrice(orderLine.getSupplierShippingPrice());
        orderLineEntity.setShippingPoints(orderLine.getShippingPoints());
        orderLineEntity.setSupplierPerShipmentPrice(orderLine.getSupplierPerShipmentPrice());
        orderLineEntity.setSupplierShippingUnitPrice(orderLine.getSupplierShippingUnitPrice());
        orderLineEntity.setSupplierSingleItemShippingPrice(orderLine.getSupplierSingleItemShippingPrice());
        orderLineEntity.setSupplierShippingUnit(orderLine.getSupplierShippingUnit());
        orderLineEntity.setShippingMethod(orderLine.getShippingMethod());
        orderLineEntity.setSku(orderLine.getSku());
        orderLineEntity.setSupplierOrderId(orderLine.getSupplierOrderId());
        orderLineEntity.setSize(orderLine.getSize());
        orderLineEntity.setSellerId(orderLine.getSellerId());
        orderLineEntity.setStoreId(orderLine.getStoreId());



        orderLineEntity.setTaxPoints(orderLine.getTaxPoints());
        orderLineEntity.setTaxRate(orderLine.getTaxRate());
        //set order line taxes
        if(Objects.nonNull(orderLine.getTaxes())) {
            final List<OrderLineTaxEntity> orderLineTaxEntityList = orderLine.getTaxes().stream().map(orderLineTax -> {
                final OrderLineTaxEntity olTax = new OrderLineTaxEntity();
                OrderLineTaxEntity.OrderLineTaxId orderLineTaxId = new OrderLineTaxEntity.OrderLineTaxId();
                orderLineTaxId.setLineNum(orderLineId.getLineNum());
                orderLineTaxId.setOrderId(orderLineId.getOrderId());
                orderLineTaxId.setName(orderLineTax.getName());
                olTax.setOrderLineTaxId(orderLineTaxId);
                olTax.setOrderLine(orderLineEntity);
                olTax.setAmount(orderLineTax.getAmount().multiply(BigDecimal.valueOf(100.0)).longValue());
                olTax.setPoints(Long.valueOf(orderLineTax.getPoints()));
                olTax.setCreateTime(orderLineTax.getCreateTime());
                return olTax;
            }).collect(Collectors.toList());
            orderLineEntity.setTaxes(orderLineTaxEntityList);
        }
        orderLineEntity.setTravelStartDate(orderLine.getTravelStartDate());
        orderLineEntity.setTravelEndDate(orderLine.getTravelEndDate());

        orderLineEntity.setVarOrderLinePrice(orderLine.getVarOrderLinePrice());
        orderLineEntity.setVarItemProfitPrice(orderLine.getVarItemProfitPrice());
        orderLineEntity.setVarShippingProfitPrice(orderLine.getVarShippingProfitPrice());
        orderLineEntity.setVarTaxProfitPrice(orderLine.getVarTaxProfitPrice());
        orderLineEntity.setVarItemMargin(orderLine.getVarItemMargin());
        orderLineEntity.setVarShippingMargin(orderLine.getVarShippingMargin());
        orderLineEntity.setVarId(orderLine.getVarId());


        orderLineEntity.setWeight(orderLine.getWeight());

        orderLineEntity.setBundleId(orderLine.getBundleId());

        return orderLineEntity;
    }

    private Order mapToModelOrder(final OrderEntity orderEntity) {

        LOGGER.info("Transforming the order entity to model with orderId: {}", orderEntity.getOrderId());
        final Order order = new Order();

        order.setAddr1(orderEntity.getAddr1());
        order.setAddr2(orderEntity.getAddr2());
        order.setAddr3(orderEntity.getAddr3());
        order.setAppVersion(orderEntity.getAppVersion());

        order.setBusinessName(orderEntity.getBusinessName());

        order.setCity(orderEntity.getCity());
        order.setCountry(orderEntity.getCountry());
        order.setCountryCode(orderEntity.getCountryCode());
        order.setCurrencyCode(orderEntity.getCurrencyCode());

        order.setEmail(orderEntity.getEmail());
        order.setEarnedPoints(orderEntity.getEarnedPoints());

        order.setFirstname(orderEntity.getFirstName());

        order.setGiftMessage(orderEntity.getGiftMessage());
        order.setGstAmount(orderEntity.getGstAmount());

        order.setIsApplySuperSaverShipping(orderEntity.getIsApplySuperSaverShipping());
        order.setIpAddress(orderEntity.getIpAddress());
        order.setIsAddressChanged(orderEntity.getIsAddressChanged());

        order.setLastname(orderEntity.getLastName());
        order.setLastUpdate(orderEntity.getLastUpdate());
        order.setLanguageCode(orderEntity.getLanguageCode());

        order.setNotificationType(orderEntity.getNotificationType());

        order.setOrderId(orderEntity.getOrderId());
        order.setOrderDate(orderEntity.getOrderDate());
        order.setOrderSource(orderEntity.getOrderSource());
        order.setEstablishmentFeesPoints(orderEntity.getEstablishmentFeesPoints());
        order.setEstablishmentFeesPrice(orderEntity.getEstablishmentFeesPrice());
        //set OrderLine
        final List<OrderLine> orderLineList =
            orderEntity.getOrderLines().stream().map(orderLineEntity -> mapToModelOrderLine(orderLineEntity)).collect(Collectors.toList());
        order.setOrderLines(orderLineList);
        order.setOrderAttributeValues(orderEntity.getOrderAttributeValues());

        order.setPhone(orderEntity.getPhone());
        order.setProgramId(orderEntity.getProgramId());
        order.setProxyUserId(orderEntity.getProxyUserId());

        order.setSupplierId(orderEntity.getSupplierId());
        order.setState(orderEntity.getState());
        order.setShipDesc(orderEntity.getShipDesc());

        order.setUserId(orderEntity.getUserId());
        order.setUserPoints(orderEntity.getUserPoints());

        order.setVarId(orderEntity.getVarId());
        order.setVarOrderId(orderEntity.getVarOrderId());

        order.setZip(orderEntity.getZip());

        LOGGER.debug("Done converting Order entity to Order model");

        return order;
    }

    private OrderLine mapToModelOrderLine(final OrderLineEntity orderLineEntity) {

        final OrderLineId orderLineId = orderLineEntity.getOrderLineId();
        LOGGER.debug(
            "Transforming the order line entity to model with orderId: {} and order line num: {}",
            orderLineId.getOrderId(),
            orderLineId.getLineNum());

        final OrderLine orderLine = new OrderLine();

        orderLine.setAttr1(orderLineEntity.getAttr1());
        orderLine.setAttr2(orderLineEntity.getAttr2());
        orderLine.setAttr3(orderLineEntity.getAttr3());

        orderLine.setB2sTaxPrice(orderLineEntity.getB2sTaxPrice());
        orderLine.setB2sTaxPoints(orderLineEntity.getB2sTaxPoints());
        orderLine.setB2sItemProfitPrice(orderLineEntity.getB2sItemProfitPrice());
        orderLine.setB2sShippingProfitPrice(orderLineEntity.getB2sShippingProfitPrice());
        orderLine.setB2sTaxProfitPrice(orderLineEntity.getB2sTaxProfitPrice());
        orderLine.setB2sShippingMargin(orderLineEntity.getB2sShippingMargin());
        orderLine.setB2sItemMargin(orderLineEntity.getB2sItemMargin());
        orderLine.setB2sTaxRate(orderLineEntity.getB2sTaxRate());
        orderLine.setB2sOnlineFee(orderLineEntity.getB2sOnlineFee());
        orderLine.setB2sOfflineFee(orderLineEntity.getB2sOfflineFee());
        orderLine.setBrand(orderLineEntity.getBrand());
        orderLine.setBookingQuantity(orderLineEntity.getBookingQuantity());

        orderLine.setCategory(orderLineEntity.getCategory());
        orderLine.setCategoryPath(orderLineEntity.getCategoryPath());
        orderLine.setConvRate(orderLineEntity.getConvRate());
        orderLine.setCreateDate(orderLineEntity.getCreateDate());
        orderLine.setColor(orderLineEntity.getColor());
        orderLine.setComment(orderLineEntity.getComment());
        orderLine.setCashBuyInPoints(orderLineEntity.getCashBuyInPoints());
        orderLine.setCashBuyInPrice(orderLineEntity.getCashBuyInPrice());


        orderLine.setDiscountedSupplierItemPrice(orderLineEntity.getDiscountedSupplierItemPrice());
        orderLine.setDiscountedVarOrderLinePrice(orderLineEntity.getDiscountedVarOrderLinePrice());
        orderLine.setDiscountedFees(orderLineEntity.getDiscountedFees());
        orderLine.setDiscountedTaxes(orderLineEntity.getDiscountedTaxes());

        orderLine.setEffConvRate(orderLineEntity.getEffConvRate());


        orderLine.setFees(mapToOrderLineFees(orderLineEntity.getFees()));
        orderLine.setFxRate(orderLineEntity.getFxRate());

        orderLine.setGatewayOrderNumber(orderLineEntity.getGatewayOrderNumber());

        orderLine.setImageUrl(orderLineEntity.getImageUrl());
        orderLine.setItemId(orderLineEntity.getItemId());
        orderLine.setItemPoints(orderLineEntity.getItemPoints());
        orderLine.setIsEligibleForSuperSaverShipping(orderLineEntity.getIsEligibleForSuperSaverShipping());

        orderLine.setListingId(orderLineEntity.getListingId());
        orderLine.setLineNum(orderLineEntity.getOrderLineId().getLineNum());

        orderLine.setMerchantId(orderLineEntity.getMerchantId());
        orderLine.setManufacturer(orderLineEntity.getManufacturer());

        orderLine.setName(orderLineEntity.getName());
        orderLine.setNotificationId(orderLineEntity.getNotificationId());



        orderLine.setOrderSource(OrderSource.WEB.name());
        orderLine.setOrderLinePoints(orderLineEntity.getOrderLinePoints());
        orderLine.setOrderDelay(orderLineEntity.getOrderDelay());
        orderLine.setOrderStatus(orderLineEntity.getOrderStatus());
        orderLine.setOrderLineType(orderLineEntity.getOrderLineType());
        orderLine.setOrderLineUndiscountedUnitFee(orderLineEntity.getOrderLineUndiscountedUnitFee());
        orderLine.setOrderId(orderLineId.getOrderId());
        orderLine.setOrderAttributes(mapToOrderLineAttributes(orderLineEntity.getOrderLineAttributes()));

        orderLine.setPointsRoundingIncrement(orderLineEntity.getPointsRoundingIncrement());
        orderLine.setProgramId(orderLineEntity.getProgramId());
        orderLine.setPolicyId(orderLineEntity.getPolicyId());

        orderLine.setQuantity(orderLineEntity.getQuantity());


        orderLine.setSupplierId(orderLineEntity.getSupplierId());
        orderLine.setSupplierItemPrice(orderLineEntity.getSupplierItemPrice());
        orderLine.setSupplierTaxPrice(orderLineEntity.getSupplierTaxPrice());
        orderLine.setSupplierShippingPrice(orderLineEntity.getSupplierShippingPrice());
        orderLine.setShippingPoints(orderLineEntity.getShippingPoints());
        orderLine.setSupplierPerShipmentPrice(orderLineEntity.getSupplierPerShipmentPrice());
        orderLine.setSupplierShippingUnitPrice(orderLineEntity.getSupplierShippingUnitPrice());
        orderLine.setSupplierSingleItemShippingPrice(orderLineEntity.getSupplierSingleItemShippingPrice());
        orderLine.setSupplierShippingUnit(orderLineEntity.getSupplierShippingUnit());
        orderLine.setShippingMethod(orderLineEntity.getShippingMethod());
        orderLine.setSku(orderLineEntity.getSku());
        orderLine.setSupplierOrderId(orderLineEntity.getSupplierOrderId());
        orderLine.setSize(orderLineEntity.getSize());
        orderLine.setSellerId(orderLineEntity.getSellerId());
        orderLine.setStoreId(orderLineEntity.getStoreId());



        orderLine.setTaxPoints(orderLineEntity.getTaxPoints());
        orderLine.setTaxRate(orderLineEntity.getTaxRate());
        orderLine.setTaxes(mapToOrderLineTaxes(orderLineEntity.getTaxes()));
        orderLine.setTravelStartDate(orderLineEntity.getTravelStartDate());
        orderLine.setTravelEndDate(orderLineEntity.getTravelEndDate());

        orderLine.setVarOrderLinePrice(orderLineEntity.getVarOrderLinePrice());
        orderLine.setVarItemProfitPrice(orderLineEntity.getVarItemProfitPrice());
        orderLine.setVarShippingProfitPrice(orderLineEntity.getVarShippingProfitPrice());
        orderLine.setVarTaxProfitPrice(orderLineEntity.getVarTaxProfitPrice());
        orderLine.setVarItemMargin(orderLineEntity.getVarItemMargin());
        orderLine.setVarShippingMargin(orderLineEntity.getVarShippingMargin());
        orderLine.setVarId(orderLineEntity.getVarId());


        orderLine.setWeight(orderLineEntity.getWeight());

        return orderLine;
    }

    private List<OrderLineFee> mapToOrderLineFees(final List<OrderLineFeeEntity> fees) {
        if(CollectionUtils.isNotEmpty(fees)){
            return fees
                .stream()
                .map(ob -> {
                    OrderLineFee orderLineFee = new OrderLineFee();
                    orderLineFee.setOrderID(ob.getOrderLineFeeId().getOrderId());
                    orderLineFee.setOrderLine(ob.getOrderLineFeeId().getLineNum());
                    orderLineFee.setName(ob.getOrderLineFeeId().getName());
                    orderLineFee.setAmount(BigDecimal.valueOf(ob.getAmount())
                        .divide(new BigDecimal(CommonConstants.CENTS_TO_DOLLARS_DIVISOR)));
                    orderLineFee.setPoints(ob.getPoints().intValue());
                    orderLineFee.setCreateTime(ob.getCreateTime());
                    return orderLineFee;

                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<OrderLineTax> mapToOrderLineTaxes(final List<OrderLineTaxEntity> taxes) {
        if(CollectionUtils.isNotEmpty(taxes)){
            return taxes
                .stream()
                .map(ob -> {
                    OrderLineTax orderLineTax = new OrderLineTax();
                    orderLineTax.setOrderID(ob.getOrderLineTaxId().getOrderId());
                    orderLineTax.setAmount(new BigDecimal(ob.getAmount()).divide(new BigDecimal(CommonConstants.CENTS_TO_DOLLARS_DIVISOR)));
                    orderLineTax.setPoints(ob.getPoints().intValue());
                    orderLineTax.setName(ob.getOrderLineTaxId().getName());
                    orderLineTax.setOrderLine(ob.getOrderLineTaxId().getLineNum());
                    orderLineTax.setCreateTime(ob.getCreateTime());
                    return orderLineTax;
                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<OrderLineAttribute> mapToOrderLineAttributes(final List<OrderLineAttributeEntity> orderLineAttributes) {
        if(CollectionUtils.isNotEmpty(orderLineAttributes)){
            return orderLineAttributes
                .stream()
                .map(ob -> {
                    OrderLineAttribute orderLineAttribute= new OrderLineAttribute();
                    orderLineAttribute.setName(ob.getName());
                    orderLineAttribute.setValue(ob.getValue());
                    orderLineAttribute.setId(ob.getId().intValue());
                    orderLineAttribute.setLineNum(ob.getLineNum());
                    orderLineAttribute.setOrderId(ob.getOrderId());
                    return orderLineAttribute;
                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
