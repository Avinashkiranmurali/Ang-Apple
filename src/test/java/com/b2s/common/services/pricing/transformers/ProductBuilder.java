package com.b2s.common.services.pricing.transformers;

import com.b2s.rewards.model.CoreProductType;
import com.b2s.rewards.model.Offer;
import com.b2s.rewards.model.Product;
import com.b2s.rewards.model.SupplierCategory;
import com.b2s.service.model.Merchant;
import org.joda.money.CurrencyUnit;

import java.util.Collections;

/**
* @author jkattookaren Created on 9/23/2014.
*/
class ProductBuilder {

    public static final Product GENERIC_PRODUCT = merchandise()
            .withPsid("psid")
            .withSupplierCategoryId(123)
            .withItemPrice(Double.valueOf(100.00d))
            .withShippingPrice(Double.valueOf(10.00d))
            .withListItemPrice(Double.valueOf(150.00d)).build();

    public static final Product GENERIC_CA_PRODUCT = merchandise()
            .withPsid("psid")
            .withMerchant(Merchant.FUTURE_SHOP)
            .withSupplierCategoryId(123)
            .withItemPrice(Double.valueOf(100.00d))
            .withShippingPrice(Double.valueOf(10.00d))
            .withListItemPrice(Double.valueOf(150.00d))
            .withCurrencyUnit(CurrencyUnit.CAD).build();

    public static final Product GENERIC_US_PRODUCT = merchandise()
            .withPsid("psid")
            .withMerchant(Merchant.BEST_BUY)
            .withSupplierCategoryId(123)
            .withItemPrice(Double.valueOf(100.00d))
            .withShippingPrice(Double.valueOf(10.00d))
            .withListItemPrice(Double.valueOf(150.00d))
            .withCurrencyUnit(CurrencyUnit.USD).build();

    private String psid;
    private Merchant merchant;
    private int supplierCategoryId;
    private Double itemPrice;
    private Double shippingPrice;
    private Double msrpPrice;
    private CurrencyUnit currencyUnit;
    private CoreProductType coreProductType;

    ProductBuilder() {
    }

    public static ProductBuilder merchandise() {
        return new ProductBuilder()
                .withPsid("")
                .withMerchant(Merchant.WALMART)
                .withSupplierCategoryId(0)
                .withItemPrice(Double.valueOf(0.00d))
                .withShippingPrice(Double.valueOf(0.00d))
                .withListItemPrice(Double.valueOf(0.00d))
                .withCurrencyUnit(CurrencyUnit.USD)
                .withCoreProductType(CoreProductType.merchandise);
    }

    public static ProductBuilder giftCard() {
        return new ProductBuilder()
                .withPsid("")
                .withMerchant(Merchant.CASHSTAR)
                .withSupplierCategoryId(0)
                .withItemPrice(Double.valueOf(0.00d))
                .withShippingPrice(Double.valueOf(0.00d))
                .withListItemPrice(Double.valueOf(0.00d))
                .withCurrencyUnit(CurrencyUnit.USD)
                .withCoreProductType(CoreProductType.giftcard);
    }

    public Product build() {
        final Product product = new Product();
        product.setPsid(this.psid);
        product.setCoreProductType(this.coreProductType);

        final SupplierCategory supplierCategory = new SupplierCategory();
        supplierCategory.setSupplierCategoryId(Integer.valueOf(supplierCategoryId));
        product.setSupplierCategory(supplierCategory);

        final Offer defaultOffer = new Offer();
        defaultOffer.setMerchant(createProductMerchant(this.merchant));
        defaultOffer.setOrgItemPrice(this.itemPrice);
        defaultOffer.setCurrency(this.currencyUnit);
        defaultOffer.setShippingPrice(this.shippingPrice);
        defaultOffer.setItemListPrice(this.msrpPrice);

        product.setOffers(Collections.singletonList(defaultOffer));
        return product;
    }

    public com.b2s.rewards.model.Merchant createProductMerchant(final Merchant merchantFrom) {
        final com.b2s.rewards.model.Merchant productMerchant = new com.b2s.rewards.model.Merchant();
        productMerchant.setMerchantId(String.valueOf(merchantFrom.getMerchantCode()));
        productMerchant.setName(merchantFrom.getSimpleName());
        return productMerchant;
    }

    public ProductBuilder withPsid(final String psidFrom) {
        this.psid = psidFrom;
        return this;
    }

    public ProductBuilder withMerchant(final Merchant merchantFrom) {
        this.merchant = merchantFrom;
        return this;
    }

    public ProductBuilder withSupplierCategoryId(final int supplierCategoryIdFrom) {
        this.supplierCategoryId = supplierCategoryIdFrom;
        return this;
    }

    public ProductBuilder withItemPrice(final Double baseItemPrice) {
        this.itemPrice = baseItemPrice;
        return this;
    }

    public ProductBuilder withShippingPrice(final Double b2sShippingPrice) {
        this.shippingPrice = b2sShippingPrice;
        return this;
    }

    public ProductBuilder withListItemPrice(final Double msrpPriceFrom) {
        this.msrpPrice = msrpPriceFrom;
        return this;
    }

    public ProductBuilder withCurrencyUnit(final CurrencyUnit currencyUnitFrom) {
        this.currencyUnit = currencyUnitFrom;
        return this;
    }

    public ProductBuilder withCoreProductType(final CoreProductType coreProductType) {
        this.coreProductType = coreProductType;
        return this;
    }
}
