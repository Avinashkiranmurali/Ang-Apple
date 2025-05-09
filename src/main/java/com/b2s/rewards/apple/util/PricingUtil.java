package com.b2s.rewards.apple.util;

import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by rpillai on 8/19/2016.
 */
public class PricingUtil {

    private static Logger log = LoggerFactory.getLogger(PricingUtil.class);

    /**
     * Find pricing model based on psid first. If not found find based on category slug.
     *
     * @param product
     * @param pricingModels
     * @return
     */
    public static Optional<PricingModel> getPricingModel(final Product product, final List<PricingModel> pricingModels) {
        final String psid = product.getPsid();
        Optional<PricingModel> pricingModelOptional = pricingModels
                .stream()
                .filter(pricingModel -> psid.equals(pricingModel.getPriceKey()))
                .findFirst();
        if(!pricingModelOptional.isPresent()) {
            // TODO. Right now, we take first category's pricing model.
            // We need to include other categories also, if product has multiple categories.
            // Then there is question. If we get multiple pricing models, which one we should use ?
            final String categorySlug = CollectionUtils.isNotEmpty(product.getCategories())?
                product.getCategories().get(0).getSlug(): StringUtils.EMPTY;
            pricingModelOptional= pricingModels
                    .stream()
                    .filter(pricingModel -> categorySlug.equals(pricingModel.getPriceKey()))
                    .findFirst();
        }
        return pricingModelOptional;
    }

    public static boolean isEligibleForPayrollDeduction(final User user, final Program program, final Price price) {
        boolean isEligibleForPayrollDeduction = false;
        if(program != null && user != null) {
            boolean isEpp = MapUtils.isNotEmpty(program.getConfig()) &&
                Objects.nonNull(program.getConfig().get(CommonConstants.EPP)) &&
                Boolean.valueOf(program.getConfig().get(CommonConstants.EPP).toString());
            if (user.getIsEligibleForPayrollDeduction() && isEpp) {
                isEligibleForPayrollDeduction = isPriceWithinLimits(user, program, price);
            }
        }
        return isEligibleForPayrollDeduction;
    }

    private static boolean isPriceWithinLimits(final User user, final Program program, final Price price) {
        boolean isEligibleForPayrollDeduction = true;
        if(price != null) {
            final Optional<PaymentOption> paymentOption = program.getPayments().stream()
                .filter(paymentOption1 -> paymentOption1 != null && paymentOption1.getIsActive() &&
                    "PAYROLL_DEDUCTION".equals(paymentOption1.getPaymentOption()))
                .findFirst();
            final Double minLimit;
            final Double maxLimit;
            if(paymentOption.isPresent()) {
                minLimit = paymentOption.get().getPaymentMinLimit();
                maxLimit = paymentOption.get().getPaymentMaxLimit();

                if(minLimit != null) {
                    isEligibleForPayrollDeduction = price.getAmount() >= minLimit;
                }

                if(maxLimit != null){
                    isEligibleForPayrollDeduction = price.getAmount() <= maxLimit;
                }
                log.info("is User {} eligibility for payroll deduction based on cart price and config - {}",
                    user.getUserId(), isEligibleForPayrollDeduction);
            } else {
                isEligibleForPayrollDeduction = false;
            }

        }
        return isEligibleForPayrollDeduction;
    }

    /**
     * Calculate Upgrade Cost
     * FinancedAmount is received in SAML attributes for Apollo program. For other programs it's calculated
     * For Apollo program, the value can only be >= 0
     *
     * @param user
     * @param program
     * @param retailCost
     * @param activationFee
     * @param financedAmount
     * @param paymentValue
     * @param delta
     * @return
     */
    public static Double calculateUpgradeCost(final User user, final Program program, final Double retailCost, final Double activationFee, final Double financedAmount, final Double paymentValue, final Double delta) {
        BigDecimal priceMinusActFee = new BigDecimal(retailCost).subtract(new BigDecimal(activationFee));
        BigDecimal maxRepay;
        if (Objects.isNull(financedAmount)) {
            maxRepay =
                (BigDecimal.valueOf(paymentValue).multiply(BigDecimal.valueOf(CommonConstants.VITALITY_MAX_MONTH_TERM)))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            maxRepay = new BigDecimal(financedAmount);
        }
        BigDecimal upgradeCost = priceMinusActFee.subtract(maxRepay);
        if (upgradeCost.doubleValue() >= delta) {
            upgradeCost = upgradeCost.subtract(new BigDecimal(delta)).setScale(2, RoundingMode.HALF_UP);
        }
        Double result = upgradeCost.doubleValue();
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIMS_PRICING_API) && upgradeCost.signum() < 0) {
            log.warn("Upgrade cost for the user {} is equal to {}. Displaying 0 instead of negative value", user.getUserId(), upgradeCost.doubleValue());
            result = 0.0;
        }
        return result;
    }


    /**
     * Calculate Total Due Today Before Tax
     * For Apollo program, the value can only be >= 0
     *
     * @param user
     * @param program
     * @param upgradeCost
     * @param activationFee
     * @return
     */
    public static Double calculateTotalDueTodayBeforeTax(final User user, final Program program, final Double upgradeCost, final Double activationFee) {
        double totalDue = activationFee + upgradeCost;
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIMS_PRICING_API) && totalDue < 0.0) {
            log.warn("Upgrade cost for the user {} is equal to {}. Displaying 0 instead of negative value", user.getUserId(), upgradeCost);
            totalDue = 0.0;
        }
        return totalDue;
    }

    /**
     * Calculate Total Due Today After Tax
     * @param totalDueTodayBeforeTax
     * @param tax
     * @return
     */
    public static Double calculateTotalDueTodayAfterTax(final Double totalDueTodayBeforeTax, final Double tax) {
        return totalDueTodayBeforeTax + tax;
    }

}
