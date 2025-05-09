package com.b2s.shop.common.order;

import com.b2s.apple.entity.PaymentEntity;
import com.b2s.apple.services.PaymentServerV2Service;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.PaymentDao;
import com.b2s.rewards.apple.model.BillTo;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.OrderStatusUpdate;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.model.*;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Service
@Transactional
public class CreditTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(CreditTransactionManager.class);
    private static final int CONVERT_DOLLAR_TO_CENTS = 100;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private PaymentServerV2Service paymentService;

    public static Product getCreditItem(final User user, final Cart cart, final Program program) {
        final Product creditProduct = new Product();
        creditProduct.setBrand("");
        creditProduct.setColor("");
        creditProduct.setProductId(CommonConstants.CAT_CREDIT_STR);

        final List<ProductDescription> prodDescList = new ArrayList<ProductDescription>();
        final ProductDescription prodDesc = new ProductDescription();
        final double addsCost = cart.getCost();
        final Object[] contentArguments = new Object[]{cart.getAddPoints(), program.getPointName(), addsCost};
        final String content = AppContext.getApplicationContext().getMessage("text.product.creditAdd", contentArguments, user.getLocale());
        prodDesc.setContent(content);
        prodDescList.add(prodDesc);
        creditProduct.setProductDescriptions(prodDescList);
        creditProduct.setName(content);

        final SupplierCategory suppCat = new SupplierCategory();
        suppCat.setName("CreditCard");
        creditProduct.setSupplierCategory(suppCat);
        creditProduct.setHasOffers(true);

        final List<ProductImage> imageList = new ArrayList<ProductImage>();
        final ProductImage prodImage = new ProductImage();
        prodImage.setSmallImageURL("/apple-gr/common/img/credit_card_sm.gif");
        prodImage.setThumbnailImageURL("/apple-gr/common/img/credit_card_sm.gif");
        prodImage.setLargeImageURL("/apple-gr/common/img/credit_card.gif");
        prodImage.setMediumImageURL("/apple-gr/common/img/credit_card.gif");
        imageList.add(prodImage);
        final List<List<ProductImage>> finalImageList = new ArrayList<List<ProductImage>>();
        finalImageList.add(imageList);
        creditProduct.setProductImages(finalImageList);

        final Supplier supplier = new Supplier();
        supplier.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT);
        supplier.setName(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
        creditProduct.setSupplier(supplier);

        final SupplierCategory supplierCategory = new SupplierCategory();
        supplierCategory.setName("CreditCard");
        supplierCategory.setSupplierCategoryId(CommonConstants.SUPPLIER_TYPE_CREDIT);
        supplierCategory.setSupplier(supplier);
        creditProduct.setSupplierCategory(supplierCategory);

        final Merchant merchant = new Merchant();
        merchant.setName(CommonConstants.CAT_CREDIT_STR);
        final List<Offer> offerList = new ArrayList<Offer>();
        final Offer offer = new Offer();
        offer.setMerchant(merchant);
        offer.setCurrency(cart.getCreditItem().getCurrency());
        offer.setB2sItemMargin(cart.getCreditItem().getB2sMargin());
        offer.setVarItemMargin(cart.getCreditItem().getVarMargin());

        offer.setSupplierShippingUnit("");
        offer.setShippingPoints(BigDecimal.ZERO.doubleValue());
        offer.setB2sShippingMargin(BigDecimal.ZERO.doubleValue());
        offer.setB2sShippingProfitPrice(BigDecimal.ZERO.doubleValue());
        offer.setVarShippingProfitPrice(BigDecimal.ZERO.doubleValue());
        offer.setB2sTaxPrice(BigDecimal.ZERO.doubleValue());
        offer.setTaxPrice(BigDecimal.ZERO.doubleValue());

        offer.setItemPrice(cart.getCreditItem().getBaseItemPrice() * -1);
        offer.setOrgItemPrice(cart.getCreditItem().getBaseItemPrice() * -1);
        offer.setB2sItemProfitPrice(cart.getCreditItem().getB2sProfit() * -1);
        offer.setVarItemProfitPrice(cart.getCreditItem().getVarProfit() * -1);

        final Money baseItemMoney = Money.of(offer.getCurrency(), cart.getCreditItem().getItemTotal());
        final Integer baseItemPoints = cart.getAddPoints();
        offer.setB2sItemPrice(new Price(baseItemMoney.negated(), baseItemPoints * -1));
        offer.setItemPoints(baseItemPoints * -1d);

        final Money b2sShippingMoney = Money.of(offer.getCurrency(), 0.0d);
        final Price b2sShippingPrice = new Price(b2sShippingMoney, 0);
        offer.setB2sShippingPrice(b2sShippingPrice);

        offer.setVarPrice(Objects.nonNull(cart.getCreditItem().getVarPrice()) ?
                cart.getCreditItem().getVarPrice().negated().getAmountMinorInt() : 0);

        // Same as item price for this
        offer.setB2sPrice(new Price(baseItemMoney.negated(), baseItemPoints * -1));

        final Map<String, Tax> taxes = Collections.emptyMap();
        offer.setTaxes(taxes);
        final Map<String, Fee> fees = Collections.emptyMap();
        offer.setFees(fees);

        offer.setSupplierItemPrice(new Price(Money.of(offer.getCurrency(), offer.getOrgItemPrice()), offer.getItemPoints().intValue()));

        offerList.add(offer);
        creditProduct.setOffers(offerList);

        return creditProduct;
    }


    public static OrderLine addCreditOrderLine(final User user, final com.b2s.rewards.apple.model.Product item, Program program) {
        OrderLine line = new OrderLine();
        line.setCreateDate(new Date());
        line.setAttr1("");
        line.setAttr2("");
        line.setAttr3("");
        line.setB2sItemMargin(item.getDefaultOffer().getB2sItemMargin());
        line.setB2sShippingMargin(0d);
        line.setB2sTaxProfitPrice(0);
        line.setB2sShippingProfitPrice(0);
        //TODO populate B2sItemProfitPrice
        //TODO POPULATE SUPPLIER CATEGORY
        line.setColor(" ");
        line.setComment(" ");
        line.setConvRate(item.getDefaultOffer().getConvRate());
        //TODO POPULATE DFAULT PRODUCT IMAGE
        line.setIsEligibleForSuperSaverShipping("N");
        line.setItemId(item.getPsid());
        line.setItemPoints((double) item.getDefaultOffer().getB2sItemPrice().getPoints());
        line.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
        line.setLineNum(2);
        line.setName(item.getName());
        line.setOrderLinePoints(item.getDefaultOffer().getTotalPrice().getPoints());
        line.setOrderStatus(CommonConstants.ORDER_STATUS_STARTED);
        line.setProgramId(user.getProgramId());
        line.setQuantity(1);
        line.setShippingPoints(0d);
        line.setSize(" ");

        //TODO Populate SUpplier Id
        line.setSupplierItemPrice(getAmountInCents(item.getDefaultOffer().getBasePrice().getAmount()));
        line.setSupplierPerShipmentPrice(0);
        line.setSupplierShippingPrice(0);
        line.setSupplierShippingUnit("");
        line.setSupplierShippingUnitPrice(0);
        line.setSupplierSingleItemShippingPrice(0);
        line.setSupplierTaxPrice(0);
        line.setTaxPoints(Double.valueOf(0));
        line.setTaxRate(0);
        line.setB2sTaxPrice(0);
        line.setB2sTaxPoints(Double.valueOf(0));
        line.setB2sTaxRate(0);
        line.setVarId(user.getVarId());
        if (item.getDefaultOffer().getVarItemMargin()!=null && item.getDefaultOffer().getVarItemMargin() != -1) {
            line.setVarItemMargin(item.getDefaultOffer().getVarItemMargin());
        } else {
            //TODO add varitemProfit Price and varprice
            logger.warn("VarItemProfit & VarPrice logic missing...");
        }
        line.setVarShippingMargin(0d);
         //TODO Add varitemprofitprice and varprice

        line.setVarTaxProfitPrice(0);
        line.setVarShippingProfitPrice(0);

        line.setWeight(0);
        line.setIsQuantityUsed(false);

        return line;
    }


    private static int getAmountInCents(final Double amount) {
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(CONVERT_DOLLAR_TO_CENTS)).round(MathContext.UNLIMITED).intValue();
    }

    public boolean placeCreditPayment(final Order order, final User user, final Cart cart,
                                      final String transactionId) {

        try {
            final double _cost = Math.round(cart.getCost() * 100d) / 100d;
            logger.debug("Charge user ${} for creditadds.", _cost);

            boolean callSuccess = captureByKeystonePaymentCall(_cost, user, cart, order, transactionId);

            if(!callSuccess){
                logger.error("Credit Card Payment - Capture call failed");
                return false;
            }

        } catch (RuntimeException ex){
            logger.error("CREDIT| UserId: {} | VarID: {} | Runtime Exception: {}", user.getUserId(), user.getVarId(),
                ex);
            return false;
        }
        return true;
    }

    private boolean captureByKeystonePaymentCall(final double _cost, final User user, final Cart cart, final Order order,
        final String transactionId){

        final PaymentEntity payment = updatePaymentEntity(_cost, user, cart, order);
        boolean success = paymentService.captureTransaction(user, payment, transactionId);
        if(!success){
            return false;
        }

        //payment credit card holder's name is CcNum
        payment.setCcNum(cart.getCreditItem().getCcFirstName() + " " + cart.getCreditItem().getCcLastName());
        payment.setFirstName(cart.getCreditItem().getCcFirstName());
        payment.setLastName(cart.getCreditItem().getCcLastName());

        paymentDao.save(payment);
        return true;
    }

    public PaymentEntity updatePaymentEntity(final double _cost, final User user, final Cart cart, final Order order) {
        final BillTo bill = user.getBillTo();
        final PaymentEntity payment = new PaymentEntity();
        payment.setAmount(_cost);
        payment.setAddress1(Objects.nonNull(bill) ? bill.getAddressLine() : "");
        payment.setAddress2("");
        payment.setAmount(cart.getCost());
        payment.setCcNum("");
        payment.setCity(Objects.nonNull(bill) ? bill.getCity() : "");
        payment.setPhone("");
        payment.setPoints(cart.getAddPoints());
        payment.setProgramId(user.getProgramId());
        payment.setResponseCode("Success");
        payment.setResponseMessage("Success");
        payment.setState(Objects.nonNull(bill) ? bill.getState() : "");
        payment.setUserId(user.getUserId());
        payment.setVarId(user.getVarId());
        payment.setZip(Objects.nonNull(bill) ? bill.getZip() : "");
        payment.setCountry(Objects.nonNull(bill) ? bill.getCountry() : "");
        payment.setItemAmount(cart.getCreditItem().getItemTotal());
        payment.setB2sProfit(cart.getCreditItem().getB2sProfit());
        payment.setVarProfit(cart.getCreditItem().getVarProfit());
        payment.setLastuser(user.getUserId());
        // payment name is credit cart type name
        if (StringUtils.isNotBlank(cart.getCreditItem().getCreditCardType())) {
            payment.setName(cart.getCreditItem().getCreditCardType());
        } else {
            payment.setName("NA");
        }

        //changed by sol, 2008.08.31; check order first, otherwise set null here
        if (order != null && order.getOrderId() != null) {
            payment.setOrderId(order.getOrderId());
        } else {
            payment.setOrderId(null);
        }
        payment.setTransactionType(CommonConstants.TRANSACTION_TYPE_SALE);
        return payment;
    }

    //Create credit line with just amount
    public static OrderLine addCreditOrderLine(final OrderStatusUpdate status,final String varId, final String programId,final Long orderId, final int lineNum){

        OrderLine line=new OrderLine();

        line.setOrderId(orderId);
        line.setLineNum(lineNum);
        line.setVarId(varId);
        line.setProgramId(programId);
        line.setCategory(CommonConstants.PRODUCT_CATEGORY_CREDIT_CARD);
        line.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
        line.setSupplierOrderId(status.getPartnerReferenceId());
        line.setItemId(CommonConstants.CAT_CREDIT_STR);
        line.setName("Credit Card payment");
        line.setQuantity(1);
        line.setSupplierItemPrice(-getAmountInCents(Double.parseDouble(status.getCcAmount())));
        line.setSupplierTaxPrice(0);
        line.setSupplierPerShipmentPrice(0);
        line.setSupplierShippingUnit("");
        line.setSupplierShippingUnitPrice(0);
        line.setSupplierShippingPrice(0);
        line.setSupplierSingleItemShippingPrice(0);
        line.setConvRate(Double.parseDouble("1"));
        line.setItemPoints(-new Double(getAmountInCents(Double.parseDouble(status.getCcAmount()))));
        line.setOrderLinePoints(-getAmountInCents(Double.parseDouble(status.getCcAmount())));
        line.setTaxPoints(0.0d);
        line.setTaxRate(0);
        line.setB2sItemProfitPrice(0);
        line.setB2sTaxProfitPrice(0);
        line.setB2sShippingProfitPrice(0);
        line.setVarItemProfitPrice(0);
        line.setVarTaxProfitPrice(0);
        line.setVarShippingProfitPrice(0);
        line.setB2sTaxPrice(0);
        line.setB2sTaxPoints(Double.valueOf(0));
        line.setB2sTaxRate(0);
        line.setB2sTaxPoints(0.0d);
        line.setB2sItemMargin(0.0d);
        line.setVarItemMargin(0.0d);
        line.setB2sShippingMargin(0.0d);
        line.setVarShippingMargin(0.0d);
        line.setShippingPoints(0.0d);
        line.setVarShippingMargin(0.0d);
        line.setVarOrderLinePrice(-getAmountInCents(Double.parseDouble(status.getCcAmount())));
        line.setCreateDate(new Date());
        line.setMerchantId(
            "PPC".equalsIgnoreCase(status.getPartnerId()) ? CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S :
                status.getPartnerId());
        line.setGatewayOrderNumber(status.getPartnerReferenceId());
        line.setWeight(0);
        line.setIsQuantityUsed(false);
        line.setOrderStatus(status.getStatus().equalsIgnoreCase(OrderStatusUpdate.Status.SUBMITTED.toString())?CommonConstants.ORDER_STATUS_COMPLETED:CommonConstants.ORDER_STATUS_FAILED);
        line.setComment("Credit Card payment through payroll duduction site");

        return line;
    }

}