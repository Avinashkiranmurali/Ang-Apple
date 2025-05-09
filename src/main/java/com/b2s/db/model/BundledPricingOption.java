package com.b2s.db.model;

/**
 * Created by cborn on 2/20/14.
 */
public enum BundledPricingOption {
    BUNDLED("N"),
    UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT("Y"),
    BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT("A");

    private BundledPricingOption(final String s) {
        databaseValue = s;
    }

    private final String databaseValue;

    public String getDatabaseValue() {
        return databaseValue;
    }

    public static BundledPricingOption fromDatabaseValue(String s) {
        BundledPricingOption bundledPricingOption = null;
        if (s != null) {
            switch (s) {
                case "N":  bundledPricingOption = BundledPricingOption.BUNDLED;
                    break;
                case "Y":  bundledPricingOption = BundledPricingOption.UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT;
                    break;
                case "A":  bundledPricingOption = BundledPricingOption.BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT;
                    break;
                default:
                    break;
            }
        }
        return bundledPricingOption;
    }
}
