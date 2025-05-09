package com.b2s.shop.common.order;

import com.b2s.apple.services.VarProgramMessageService;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.model.AMPConfig;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.Subscription;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubscriptionManager {

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    //Check all the values persisted in DB and put needed value
    public OrderLine addSubscriptionOrderLine(final User user, final Subscription subscription, final Program program) {
        logger.info("Adding Subscription Line item... {}", subscription.getItemId());
        final OrderLine line = new OrderLine();

        line.setCreateDate(new Date());
        line.setSupplierId(CommonConstants.AMP_SUPPLIER_ID_STRING);
        line.setItemId(subscription.getItemId());
        line.setOrderStatus(CommonConstants.ORDER_STATUS_COMPLETED);
        line.setCategory("AMP");
        line.setProgramId(user.getProgramId());
        line.setName("AMP Subscription");
        line.setConvRate(0.0d);
        line.setQuantity(1);

        //It's an indicator that the subscription url is whether static, reusable vs one time use
        line.setAttr1("static_url");

        //Below fields are set with default values like 0 or empty.
        line.setAttr2("");
        line.setAttr3("");
        line.setComment("");
        line.setB2sItemMargin(0.0d);
        line.setB2sShippingMargin(0.0d);
        line.setB2sTaxProfitPrice(0);
        line.setB2sShippingProfitPrice(0);
        line.setColor(" ");
        line.setConvRate(program.getConvRate());
        line.setIsEligibleForSuperSaverShipping("");
        line.setItemPoints(0.0d);
        line.setOrderLinePoints(0);
        line.setShippingPoints(0.0d);
        line.setSize(" ");
        line.setSupplierItemPrice(0);
        line.setSupplierPerShipmentPrice(0);
        line.setSupplierShippingPrice(0);
        line.setSupplierShippingUnit("");
        line.setSupplierShippingUnitPrice(0);
        line.setSupplierSingleItemShippingPrice(0);
        line.setSupplierTaxPrice(0);
        line.setTaxPoints(0.0d);
        line.setTaxRate(0);
        line.setB2sItemProfitPrice(0);
        line.setB2sTaxPrice(0);
        line.setB2sTaxPoints(0.0d);
        line.setB2sTaxRate(0);
        line.setVarId(user.getVarId());
        line.setVarItemMargin(0.0d);
        line.setVarItemProfitPrice(0);
        line.setVarShippingMargin(0.0d);
        line.setVarTaxProfitPrice(0);
        line.setVarShippingProfitPrice(0);
        line.setVarOrderLinePrice(0);

        line.setWeight(0);
        line.setIsQuantityUsed(false);

        Optional<AMPConfig> ampConfigOpt = program.getAmpSubscriptionConfig().parallelStream()
                .filter(ampConfig -> ampConfig.getItemId().equalsIgnoreCase(subscription.getItemId()))
                .findAny();

        if (ampConfigOpt.isPresent() && ampConfigOpt.get().getUseStaticLink()) {
            //Add Order Line Attributes
            List<OrderLineAttribute> orderLineAttributes = new ArrayList<>();
            orderLineAttributes.add(buildOrderLineAttribute(user, subscription, program));
            line.setOrderAttributes(orderLineAttributes);
        }
        return line;
    }

    public OrderLineAttribute buildOrderLineAttribute(final User user, final Subscription subscription,
        final Program program) {

        logger.info("Adding Order Line Attributes for Subscription Line item...");
        OrderLineAttribute orderLineAttribute = new OrderLineAttribute();

        if (Objects.nonNull(user) && Objects.nonNull(subscription) && Objects.nonNull(program)) {
            final Properties dbProperties =
                varProgramMessageService.getMessages(Optional.ofNullable(program.getVarId()),
                    Optional.ofNullable(program.getProgramId()), user.getLocale().toString());

            if (dbProperties != null && !dbProperties.isEmpty()) {
                //Persisting AMP Subscription URL to order line attribute
                orderLineAttribute.setName(CommonConstants.SUBSCRIPTION_URL);
                Object value = null;
                if (Objects.nonNull(subscription.getDuration())) {
                    value = dbProperties.get(subscription.getItemId() + "-" + subscription.getDuration() + "-" +
                        CommonConstants.SPARK_POST_AMP_STATIC_LINK);
                } else {
                    value =
                        dbProperties.get(subscription.getItemId() + "-" + CommonConstants.SPARK_POST_AMP_STATIC_LINK);
                }

                orderLineAttribute.setValue(String.valueOf(value));
            }
        }
        return orderLineAttribute;
    }

}
