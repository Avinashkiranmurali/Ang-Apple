package com.b2s.shop.common.order;

import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.model.DiscountCode;
import com.b2s.rewards.apple.model.Program;


import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.common.services.discountservice.CouponDetails;
import com.b2s.common.services.discountservice.DiscountServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.List;

/*** Created by srukmagathan on 7/9/2016.
 */
@Component
public class DiscountCodeTransactionManager {
    @Autowired
    private DiscountServiceClient discountServiceClient;
    private static final int CONVERT_DOLLAR_TO_CENTS = 100;

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeTransactionManager.class);

    public static OrderLine addDiscountCodeOrderLine(final User user, final DiscountCode code, final Program program) {
        logger.info("Adding Discount Code Line item...");
        final OrderLine line = new OrderLine();

        final Object[] objectParam = new Object[]{
            code.getDiscountCode(),
            code.getDiscountAmount()
        };

        line.setCreateDate(new Date());
        line.setAttr1("");
        line.setAttr2("");
        line.setAttr3("");
        line.setB2sItemMargin(0.0d);
        line.setB2sShippingMargin(0.0d);
        line.setB2sTaxProfitPrice(0);
        line.setB2sShippingProfitPrice(0);
        line.setColor(" ");
        line.setAttr1(code.getDiscountCode());
        line.setAttr2(code.getDiscountAmount().toString());
        line.setComment(code.getLongDescription());
        line.setConvRate(program.getConvRate());
        line.setIsEligibleForSuperSaverShipping("N");
        line.setItemId(code.getDiscountCode());
        line.setItemPoints(-(getAmountInCents(code.getDiscountAmount())* program.getConvRate()));
        line.setSupplierId(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S);
        line.setLineNum(2);
        line.setName(code.getLongDescription());
        line.setOrderLinePoints(-getAmountInCents(code.getDiscountAmount()));
        line.setOrderStatus(CommonConstants.ORDER_STATUS_STARTED);
        line.setCategory(CommonConstants.CAT_DISCOUNTCODE_STR);
        line.setProgramId(user.getProgramId());
        line.setQuantity(1);
        line.setShippingPoints(0.0d);
        line.setSize(" ");
        line.setSupplierItemPrice(-getAmountInCents(code.getDiscountAmount()));
        line.setSupplierPerShipmentPrice(0);
        line.setSupplierShippingPrice(0);
        line.setSupplierShippingUnit("");
        line.setSupplierShippingUnitPrice(0);
        line.setSupplierSingleItemShippingPrice(0);
        line.setSupplierTaxPrice(0);
        line.setTaxPoints(Double.valueOf(0));
        line.setTaxRate(0);
        line.setB2sItemProfitPrice(0);
        line.setB2sTaxPrice(0);
        line.setB2sTaxPoints(Double.valueOf(0));
        line.setB2sTaxRate(0);
        line.setVarId(user.getVarId());
        line.setVarItemMargin(0.0d);
        line.setVarItemProfitPrice(0);
        line.setVarShippingMargin(0.0d);
        line.setVarTaxProfitPrice(0);
        line.setVarShippingProfitPrice(0);
        line.setVarOrderLinePrice(-getAmountInCents(code.getDiscountAmount()));

        line.setWeight(0);
        line.setIsQuantityUsed(false);
        return line;
    }


    // To update coupon code usage. This method is also used to rollback coupon code usage on even of place order failure.
    public CouponDetails redeemDiscountCode(final DiscountCode discountCode,final User user){
        logger.info("method redeemDiscountCode # {}",discountCode);

        final CouponDetails couponDetails;
        // Call client to redeem discount via coupon code service
        couponDetails = discountServiceClient.redeemDiscountCode(discountCode.getDiscountCode(),user);

        return couponDetails;
    }

    public void rollbackDiscountCoderedeemption(final List<DiscountCode> discountCodeList,final User user){
        logger.info("Rollback discountCode redemption starts");
       try {
           discountCodeList.forEach(discountCode -> {
               discountServiceClient.rollbackDiscountCodeRedeemption(discountCode.getDiscountCode(), user);
           });
           logger.info("Rollback discountCode redemption completed");
       }catch(final RuntimeException e){
           logger.error("rollback failed : Error while rollback discount code usage. Please have a look at Log trace ", e);
       }
    }

    private static int getAmountInCents(final Double amount) {
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(CONVERT_DOLLAR_TO_CENTS)).round(MathContext.UNLIMITED).intValue();
    }


}
