package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.common.util.CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN;

/**
 * Apple Cart Item, included in Apple Cart
 * Created by ssrinivasan on 4/8/2015.
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = -720662839578313298L;
    private Long id;
    private Date addedDate;
    private Integer supplierId;
    private String productId;
    private String productName;
    private String imageURL;
    private String parentProductId;
    private String merchantId;
    private Integer quantity;
    private Engrave engrave;
    private Gift gift;
    private Product productDetail;
    private Long productGroupId;
    private String productGroupName;
    private String productGroupDisplayName ;
    private Integer maxQuantity;
    private Double payPeriodPrice;
    private String shippingMethod;
    private Integer giftCardMaxQuantity;
    private Money giftCardDenomination;
    private Installment installment;
    private CartAddOns selectedAddOns = new CartAddOns();
    private Double discount;
    private String discountType;

    public Installment getInstallment() {
        return installment;
    }

    public void setInstallment(final Installment installment) {
        this.installment = installment;
    }

    public Integer getGiftCardMaxQuantity() {
        return giftCardMaxQuantity;
    }

    public void setGiftCardMaxQuantity(final Integer giftCardMaxQuantity) {
        this.giftCardMaxQuantity = giftCardMaxQuantity;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Double getPayPeriodPrice() {
        return payPeriodPrice;
    }

    public void setPayPeriodPrice(final Double payPeriodPrice) {
        this.payPeriodPrice = payPeriodPrice;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(final Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public Long getId() {
        return id;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getParentProductId() {
        return (parentProductId != null ? parentProductId : "");
    }

    public void setParentProductId(String parentProductId) {
        this.parentProductId = parentProductId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Product getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(Product product) {
        this.productDetail = product;
    }

    public Engrave getEngrave() {
        return (engrave != null ? engrave : new Engrave());
    }

    public void setEngrave(Engrave engrave) {
        this.engrave = engrave;
    }

    public Gift getGift() {
        return (gift != null ? gift : new Gift());
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public Long getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(Long productGroupId) {
        this.productGroupId = productGroupId;
    }

    public String getProductGroupName() {
        return productGroupName;
    }

    public void setProductGroupName(final String productGroupName) {
        this.productGroupName = productGroupName;
    }

    public String getProductGroupDisplayName() {
        return productGroupDisplayName;
    }

    public void setProductGroupDisplayName(final String productGroupDisplayName) {
        this.productGroupDisplayName = productGroupDisplayName;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(final Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(final String discountType) {
        this.discountType = discountType;
    }

    public CartAddOns getSelectedAddOns() {
        return selectedAddOns;
    }

    public void setSelectedAddOns(CartAddOns selectedAddOns) {
        this.selectedAddOns = selectedAddOns;
    }

    @JsonIgnore
    public boolean hasEngraveMessage() {
        return (this.engrave != null);
    }
    @JsonIgnore
    public boolean hasGiftMessage() {
        return (this.gift != null && this.gift.hasGiftMessage());
    }

    /**
     * Converts core model shopping cart item to apple model cart item.
     * @param shoppingCartItem
     * @return
     */
    public static CartItem transform(ShoppingCartItem shoppingCartItem, final User user, final Program program){
        CartItem cartItem = new CartItem();
        try {
            BeanUtils.copyProperties(cartItem, shoppingCartItem);
            // Gift & EngraveMessage
            //Engrave-Gift message as object
            if (shoppingCartItem.getOptionsXml() != null) {
                Gson gson = new Gson();
                Map<String, String> optionMap = shoppingCartItem.convertToMap(shoppingCartItem.getOptionsXml());
                if (optionMap != null ) {
                    extractDataFromOptionsXml(cartItem, gson, optionMap,
                        getProgramConfigValueAsBoolean(program, ENABLE_APPLE_CARE_SERVICE_PLAN));
                }
            }
        }catch(IllegalAccessException iAE){
            LOG.error("Property access Exception for Shopping Cart Item to Apple Cart Item", iAE);
        }catch(InvocationTargetException iTE){
            LOG.error("Property write Exception for Shopping Cart Item to Apple Cart Item", iTE);
        }
        if(Objects.nonNull(program.getConfig().get(CommonConstants.LIMIT_MAX_QUANTITY)) &&
                Integer.parseInt(program.getConfig().get(CommonConstants.LIMIT_MAX_QUANTITY).toString()) > 1 ){
            cartItem.setMaxQuantity(Integer.parseInt(program.getConfig().get(CommonConstants.LIMIT_MAX_QUANTITY).toString()));
        }
        return cartItem;
    }

    private static void extractDataFromOptionsXml(final CartItem cartItem, final Gson gson,
        final Map<String, String> optionMap, final boolean enableAppleCareServicePlan) {
        String engraveMessageJson = optionMap.get(CartItemOption.ENGRAVE.getValue());
        if (!StringUtils.isEmpty(engraveMessageJson)) {
            Engrave engraveMessage = gson.fromJson(engraveMessageJson, Engrave.class);
            cartItem.setEngrave(engraveMessage);
        }
        String giftMessageJson = optionMap.get(CartItemOption.GIFT.getValue());
        if (!StringUtils.isEmpty(giftMessageJson)) {
            Gift giftMessage = gson.fromJson(giftMessageJson, Gift.class);
            cartItem.setGift(giftMessage);
        }
        String discountedItemJson = optionMap.get(CartItemOption.GIFT_ITEM.getValue());
        if (StringUtils.isNotBlank(discountedItemJson)) {
            final CartItem discountedItem = gson.fromJson(discountedItemJson, CartItem.class);
            discountedItem.setQuantity(1);
            if (enableAppleCareServicePlan) {
                populateServicePlanForGiftItems(gson, discountedItemJson, discountedItem);
            }
            cartItem.getSelectedAddOns().setGiftItem(discountedItem);
        }
        if(enableAppleCareServicePlan){
            final String servicePlanPsId = optionMap.get(CartItemOption.SERVICE_PLAN.getValue());
            if (StringUtils.isNotBlank(servicePlanPsId)) {
                final CartItem servicePlanProduct = new CartItem();
                servicePlanProduct.setProductId(servicePlanPsId);
                servicePlanProduct.setQuantity(1);
                cartItem.getSelectedAddOns().setServicePlan(servicePlanProduct);
            }
        }
    }

    private static void populateServicePlanForGiftItems(final Gson gson, final String discountedItemJson,
        final CartItem freeGiftItemMessage) {
        GiftItem giftItemSelected = gson.fromJson(discountedItemJson, GiftItem.class);
        if (Objects.nonNull(giftItemSelected.getServicePlan())) {
            final CartItem selectedServicePlanForGift = new CartItem();
            selectedServicePlanForGift.setProductId(giftItemSelected.getServicePlan());
            selectedServicePlanForGift.setQuantity(1);
            freeGiftItemMessage.getSelectedAddOns().setServicePlan(selectedServicePlanForGift);
        }
    }


    public Money getGiftCardDenomination() {
        return giftCardDenomination;
    }

    public void setGiftCardDenomination(final Money giftCardDenomination) {
        this.giftCardDenomination = giftCardDenomination;
    }

    /**
     * Apply Product with Discounted Gift With Purchase Promotion
     */
    public void applyDiscountedGwpPromotion(){
        if (Objects.nonNull(this.getProductDetail()) && Objects.nonNull(this.getDiscount())) {
            final Promotion.Builder promotionalBuilder = Promotion.builder();
            if(CommonConstants.DGWP_PERCENTAGE.equalsIgnoreCase(this.getDiscountType())){
                promotionalBuilder
                    .withDiscountPercentage(BigDecimal.valueOf(this.getDiscount()));
            }
            if(CommonConstants.DGWP_POINTS.equalsIgnoreCase(this.getDiscountType())){
                promotionalBuilder
                    .withFixedPointPrice(Money.of(CurrencyUnit.of("PNT"),
                        BigDecimal.valueOf(this.getDiscount().intValue())));
            }
            this.getProductDetail().setPromotion(Optional.ofNullable(promotionalBuilder.build()));
        }
    }
}
