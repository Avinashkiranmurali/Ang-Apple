package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.integration.model.UA.PromotionalSubscription;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.b2s.rewards.common.util.CommonConstants.DISCOUNT_TYPE_DOLLAR;

/**
 * Created by ssrinivasan on 4/8/2015.
 */
public class Cart implements Serializable {
    private static final long serialVersionUID = 9013264337275767927L;
    private List<CartItem> cartItems = new ArrayList();
    @JsonIgnore
    private Integer merchandiseSupplierId = Integer.valueOf(10);
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Address shippingAddress = new Address();
    @JsonIgnore
    private Address newShippingAddress = new Address();
    @JsonIgnore
    private Address billingAddress = new Address();
    private CartTotal cartTotal = null;
    private CartTotal displayCartTotal = null;
    @JsonIgnore
    private boolean cartLoadFirstTime = true;
    @JsonIgnore
    private Boolean cartModifiedByUser = false;  // cart items modified by user in different session
    private Boolean cartModifiedBySystem = false; // item no longer available
    private boolean cartTotalModified;
    @JsonIgnore
    private boolean purchased;
    @JsonIgnore
    private List<CartItem> itemsNoLongerAvailable = new ArrayList();
    private List<DiscountCode> discounts;
    private Installment installment;
    private String paymentType;

    private int addPoints;
    private int pointsPayment;
    private int pointsBalance;
    private BigDecimal pointPurchaseRate;
    private double cost = 0;
    private double ccPayment = 0;
    private CreditItem creditItem;
    private long id;
    private String userId;
    private CartItem creditLineItem;

    private String selectedPaymentOption;
    private boolean isEligibleForPayrollDeduction;

    private boolean isPayrollOnly;

    private boolean addressError;

    private SupplementaryPaymentLimit supplementaryPaymentLimit;
    private RedemptionPaymentLimit redemptionPaymentLimit;
    private PaymentLimit paymentLimit;

    private boolean isMaxCartTotalExceeded;
    public String timeZoneId;
    @JsonIgnore
    public double estimatedDownPayment;

    private Double convRate;

    private String ignoreSuggestedAddress;

    private PromotionalSubscription promotionalSubscription;

    private String isAddressChanged;
    private String isEmailChanged;
    private double gstAmount;
    private int earnPoints;
    private String selectedRedemptionOption;
    private int cartItemsTotalCount;

    private Set<Subscription> subscriptions;

    private SmartPrice smartPrice;

    public String getIsAddressChanged() {
        return isAddressChanged;
    }

    public void setIsAddressChanged(final String isAddressChanged) {
        this.isAddressChanged = isAddressChanged;
    }

    public String getIsEmailChanged() {
        return isEmailChanged;
    }

    public void setIsEmailChanged(final String isEmailChanged) {
        this.isEmailChanged = isEmailChanged;
    }

    public PromotionalSubscription getPromotionalSubscription() {
        return promotionalSubscription;
    }

    public void setPromotionalSubscription(
        final PromotionalSubscription promotionalSubscription) {
        this.promotionalSubscription = promotionalSubscription;
    }


    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(final String paymentType) {
        this.paymentType = paymentType;
    }

    private Integer physicalGiftcardMaxValue;

    public Integer getPhysicalGiftcardMaxValue() {
        return physicalGiftcardMaxValue;
    }

    public void setPhysicalGiftcardMaxValue(final Integer physicalGiftcardMaxValue) {
        this.physicalGiftcardMaxValue = physicalGiftcardMaxValue;
    }

    public String getIgnoreSuggestedAddress() {
        return ignoreSuggestedAddress;
    }

    public void setIgnoreSuggestedAddress(final String ignoreSuggestedAddress) {
        this.ignoreSuggestedAddress = ignoreSuggestedAddress;
    }

    public double getEstimatedDownPayment() {
        return estimatedDownPayment;
    }

    public void setEstimatedDownPayment(final double estimatedDownPayment) {
        this.estimatedDownPayment = estimatedDownPayment;
    }

    public boolean getIsEligibleForPayrollDeduction() {
        return isEligibleForPayrollDeduction;
    }

    public void setIsEligibleForPayrollDeduction(boolean isEligibleForPayrollDeduction) {
        this.isEligibleForPayrollDeduction = isEligibleForPayrollDeduction;
    }

    public List<DiscountCode> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(final List<DiscountCode> discounts) {
        this.discounts = discounts;
    }

    public void addDiscount(final DiscountCode discountCode) {
        if(discountCode != null) {
            if(CollectionUtils.isEmpty(discounts)) {
                discounts = new ArrayList<>();
            }
            discounts.add(discountCode);
        }
    }

    public void removeDiscount(final String discountCode) {
        if(CollectionUtils.isNotEmpty(discounts)) {
            discounts.remove(new DiscountCode(discountCode));
        }
    }

    public CreditItem getCreditItem() {
        return creditItem;
    }

    public void setCreditItem(final CreditItem creditItem) {
        this.creditItem = creditItem;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(final double cost) {
        this.cost = cost;
    }

    public double getCcPayment() {
        return ccPayment;
    }

    public void setCcPayment(final double ccPayment) {
        this.ccPayment = ccPayment;
    }

    public int getAddPoints() {
        return addPoints;
    }

    public void setAddPoints(final int addPoints) {
        this.addPoints = addPoints;
    }

    public int getPointsPayment() {
        return pointsPayment;
    }

    public void setPointsPayment(final int pointsPayment) {
        this.pointsPayment = pointsPayment;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(final int pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public BigDecimal getPointPurchaseRate() {
        return pointPurchaseRate;
    }

    public void setPointPurchaseRate(final BigDecimal pointPurchaseRate) {
        this.pointPurchaseRate = pointPurchaseRate;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(final Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(final List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public CartTotal getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(final CartTotal cartTotal) {
        this.cartTotal = cartTotal;
    }

    public CartTotal getDisplayCartTotal() {
        return displayCartTotal;
    }

    public void setDisplayCartTotal(CartTotal displayCartTotal) {
        this.displayCartTotal = displayCartTotal;
    }

    // cart items modified by user in different session
    public Boolean getCartModifiedByUser() {
        return cartModifiedByUser;
    }

    public void setCartModifiedByUser(final Boolean cartModifiedByUser) {
        this.cartModifiedByUser = cartModifiedByUser;
    }

    // item no longer available
    public Boolean getCartModifiedBySystem() {
        return cartModifiedBySystem;
    }

    public void setCartModifiedBySystem(final Boolean cartModifiedBySystem) {
        this.cartModifiedBySystem = cartModifiedBySystem;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(final boolean purchased) {
        this.purchased = purchased;
    }

    public List<CartItem> getItemsNoLongerAvailable() {
        return itemsNoLongerAvailable;
    }

    public void setItemsNoLongerAvailable(final List<CartItem> itemsNoLongerAvailable) {
        this.itemsNoLongerAvailable = itemsNoLongerAvailable;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getSelectedPaymentOption() {
        return selectedPaymentOption;
    }

    public void setSelectedPaymentOption(final String selectedPaymentOption) {
        this.selectedPaymentOption = selectedPaymentOption;
    }

    /**
     * Converts core model shopping cart to apple model cart.
      * @param shoppingCart
     * @return
     */
    public static Cart transform(final ShoppingCart shoppingCart, final User user, final Program program){
        final Cart cart = new Cart();
        try {
            if(shoppingCart==null || shoppingCart.getShoppingCartItems()==null) {
                return cart;
            }
            BeanUtils.copyProperties(cart, shoppingCart);
            final ArrayList<CartItem> cartItems = new ArrayList<>();
            for(final ShoppingCartItem shoppingCartItem: shoppingCart.getShoppingCartItems()){
                if(!shoppingCartItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_AMP)){
                    cartItems.add(CartItem.transform(shoppingCartItem, user, program));
                }
            }
            cart.setCartItems(cartItems);


        }catch(final IllegalAccessException iAE){
            LOG.error("Property access Exception for Shopping Cart to Apple Cart", iAE);
        }catch(final InvocationTargetException iTE){
            LOG.error("Property write Exception for Shopping Cart to Apple Cart", iTE);
        }
        return cart;
    }

    public boolean isAddressError() {
        return addressError;
    }

    public void setAddressError(final boolean addressError) {
        this.addressError = addressError;
    }

    /**
     * Get the total accumulated discount amount from a list of discounts
     * @param
     * @return
     */
    public double getTotalDiscountAmount() {
        double discountAmount = 0d;
        if(CollectionUtils.isNotEmpty(discounts)) {
            for(final DiscountCode discount : discounts) {
                if(discount != null && DISCOUNT_TYPE_DOLLAR.equalsIgnoreCase(discount.getDiscountType())) {
                        discountAmount += discount.getDiscountAmount();
                }

            }
        }
        return discountAmount;
    }

    public boolean isMaxCartTotalExceeded() {
        return isMaxCartTotalExceeded;
    }

    public void setMaxCartTotalExceeded(final boolean maxCartTotalExceeded) {
        isMaxCartTotalExceeded = maxCartTotalExceeded;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public SupplementaryPaymentLimit getSupplementaryPaymentLimit() {
        return supplementaryPaymentLimit;
    }

    public void setSupplementaryPaymentLimit(SupplementaryPaymentLimit supplementaryPaymentLimit) {
        this.supplementaryPaymentLimit = supplementaryPaymentLimit;
    }

    public RedemptionPaymentLimit getRedemptionPaymentLimit() {
        return redemptionPaymentLimit;
    }

    public void setRedemptionPaymentLimit(final RedemptionPaymentLimit redemptionPaymentLimit) {
        this.redemptionPaymentLimit = redemptionPaymentLimit;
    }

    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(Double convRate) {
        this.convRate = convRate;
    }

    public PaymentLimit getPaymentLimit() {
        return paymentLimit;
    }

    public void setPaymentLimit(final PaymentLimit paymentLimit) {
        this.paymentLimit = paymentLimit;
    }

    public boolean getIsPayrollOnly() {
        return isPayrollOnly;
    }

    public void setIsPayrollOnly(final boolean isPayrollOnly) {
        this.isPayrollOnly = isPayrollOnly;
    }

    public boolean isCartTotalModified() {
        return cartTotalModified;
    }

    public void setCartTotalModified(boolean cartTotalModified) {
        this.cartTotalModified = cartTotalModified;
    }

    public Address getNewShippingAddress() {
        return newShippingAddress;
    }

    public void setNewShippingAddress(final Address newShippingAddress) {
        this.newShippingAddress = newShippingAddress;
    }

    public Installment getInstallment() {
        return installment;
    }

    public void setInstallment(final Installment installment) {
        this.installment = installment;
    }
    public boolean isCartLoadFirstTime() {return cartLoadFirstTime;}
    public void setCartLoadFirstTime(boolean cartLoadFirstTime) {
        this.cartLoadFirstTime = cartLoadFirstTime;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(final double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public int getEarnPoints() {
        return earnPoints;
    }

    public void setEarnPoints(final int earnPoints) {
        this.earnPoints = earnPoints;
    }

    public String getSelectedRedemptionOption() {
        return selectedRedemptionOption;
    }

    public void setSelectedRedemptionOption(final String selectedRedemptionOption) {
        this.selectedRedemptionOption = selectedRedemptionOption;
    }

    public CartItem getCreditLineItem() {
        return creditLineItem;
    }

    public void setCreditLineItem(final CartItem creditLineItem) {
        this.creditLineItem = creditLineItem;
    }

    public int getCartItemsTotalCount() {
        return cartItemsTotalCount;
    }

    public void setCartItemsTotalCount(final int cartItemsTotalCount) {
        this.cartItemsTotalCount = cartItemsTotalCount;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(final Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public SmartPrice getSmartPrice() {
        return smartPrice;
    }

    public void setSmartPrice(SmartPrice smartPrice) {
        this.smartPrice = smartPrice;
    }
}
