package com.b2s.rewards.apple.model;

import com.b2s.apple.model.CarouselConfig;
import com.b2s.db.model.BundledPricingOption;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.joda.money.CurrencyUnit;
import static com.b2s.rewards.common.util.CommonConstants.PaymentOption.PAYROLL_DEDUCTION;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.CarouselType;

/**
 * Created by rpillai on 6/24/2016.
 */
public class Program implements Serializable{

    private static final long serialVersionUID = -1934424148739458946L;

    private String varId;

    private String programId;

    private String name;

    private String imageUrl;

    private Double convRate;

    private String pointName;

     private String formatPointName;

    private BigDecimal pointPurchaseRate;

    private String pointFormat;

    private Boolean isDemo;

    private Boolean isActive;

    private Boolean isLocal;

    private String catalogId;

    private BundledPricingOption bundledPricingOption;

    private List<PaymentOption> payments;

    private String pricingTier;

    private List<CCBin> ccFilters;

    private Set<AMPConfig> ampSubscriptionConfig;

    @JsonIgnore
    private transient Map<String, Map<CarouselType, CarouselConfig>> carouselConfig;

    private List<String> carouselPages;

    private final List<PricingModel> pricingModels = new ArrayList<>();

    private Map<String, List<VarProgramRedemptionOption>> redemptionOptions;

    private Boolean enableAcknowledgeTermsConds;

    public Map<String, List<VarProgramRedemptionOption>> getRedemptionOptions() {
        return redemptionOptions;
    }

    public void setRedemptionOptions(
        final Map<String, List<VarProgramRedemptionOption>> redemptionOptions) {
        this.redemptionOptions = redemptionOptions;
    }

    public List<PricingModel> getPricingModels() {
        return pricingModels;
    }

    public void setPricingModels(final List<PricingModel> pricingModels) {
        this.pricingModels.clear();
        if (pricingModels != null) {
            this.pricingModels.addAll(pricingModels);
        }
    }

    public String getPricingTier() {
        return pricingTier;
    }

    public void setPricingTier(final String pricingTier) {
        this.pricingTier = pricingTier;
    }

    @JsonIgnore
    private transient List<Notification> notifications;

    private transient Map<String, Object> config = new HashMap<>();

    private transient Map<String, Object> sessionConfig = new HashMap<>();

    private CurrencyUnit targetCurrency;

    private List<CategoryPrice> categoryPrices;

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(Double convRate) {
        this.convRate = convRate;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointFormat() {
        return pointFormat;
    }

    public void setPointFormat(String pointFormat) {
        this.pointFormat = pointFormat;
    }

    public String getFormatPointName() {
        return formatPointName;
    }

    public void setFormatPointName(String formatPointName) {
        this.formatPointName = formatPointName;
    }

    public BigDecimal getPointPurchaseRate() {
        return pointPurchaseRate;
    }

    public void setPointPurchaseRate(BigDecimal pointPurchaseRate) {
        this.pointPurchaseRate = pointPurchaseRate;
    }

    public BundledPricingOption getBundledPricingOption() {
        return bundledPricingOption;
    }

    public void setBundledPricingOption(BundledPricingOption bundledPricingOption) {
        this.bundledPricingOption = bundledPricingOption;
    }

    public List<PaymentOption> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentOption> payments) {
        this.payments = payments;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public Map<String, Object> getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(Map<String, Object> sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public Boolean getIsDemo() {
        return isDemo;
    }

    public void setIsDemo(Boolean isDemo) {
        this.isDemo = isDemo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public CurrencyUnit getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(CurrencyUnit targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public List<CategoryPrice> getCategoryPrices() {
        return categoryPrices;
    }

    public void setCategoryPrices(List<CategoryPrice> categoryPrices) {
        this.categoryPrices = categoryPrices;
    }

    public List<CCBin> getCcFilters() {
        return ccFilters;
    }

    public void setCcFilters(final List<CCBin> ccFilters) {
        this.ccFilters = ccFilters;
    }

    public Set<AMPConfig> getAmpSubscriptionConfig() {
        return ampSubscriptionConfig;
    }

    public void setAmpSubscriptionConfig(final Set<AMPConfig> ampSubscriptionConfig) {
        this.ampSubscriptionConfig = ampSubscriptionConfig;
    }

    public Map<String, Map<CarouselType, CarouselConfig>> getCarouselConfig() {
        return carouselConfig;
    }

    public void setCarouselConfig(final Map<String, Map<CarouselType, CarouselConfig>> carouselConfig) {
        this.carouselConfig = carouselConfig;
    }

    public List<String> getCarouselPages() {
        return carouselPages;
    }

    public void setCarouselPages(final List<String> carouselPages) {
        this.carouselPages = carouselPages;
    }

    public Boolean getEnableAcknowledgeTermsConds() {

        return enableAcknowledgeTermsConds;
    }

    public void setEnableAcknowledgeTermsConds(final Boolean enableAcknowledgeTermsConds) {

        this.enableAcknowledgeTermsConds = enableAcknowledgeTermsConds;
    }

    public boolean isSkipAddressValidation() {
        Boolean isSkipAddressValidation = false;
        if ( config != null ) {
            isSkipAddressValidation= (Boolean)config.get("skipAddressValidation");
            if ( isSkipAddressValidation == null ) {
                isSkipAddressValidation=false;
            }
        }
        return isSkipAddressValidation;
    }



    public boolean isProgramEligibleForPayrollDeduction() {
        if(CollectionUtils.isNotEmpty(this.getPayments())) {
            final Optional<PaymentOption> paymentOptionOpt = this.getPayments().stream()
                    .filter(paymentOption -> (PAYROLL_DEDUCTION.name().equals(paymentOption.getPaymentOption()) && paymentOption.getIsActive()))
                    .findFirst();
            return paymentOptionOpt.isPresent();
        }
      return  false;
    }

    public static class CCBin implements Serializable {

        private static final long serialVersionUID = 7661683801900069356L;
        private String filter;
        public String getFilter() { return filter; }
        public void setFilter(final String filter) { this.filter = filter; }
        public CCBin(final String filter){this.filter = filter;}
    }
}
