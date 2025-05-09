package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by rpillai on 10/2/2017.
 */
@JsonDeserialize(builder = Organization.OrganizationBuilder.class)
public class Organization {

    private final long organizationId;
    private final String companyName;
    private final Optional<String> dunsNumber;
    private final boolean overrideShipping;
    private final boolean drpEnabled;
    private final boolean eppEnabled;
    private final String companySizeRange;
    private final List<Catalog> catalogs;
    private final String defaultLocale;
    private final boolean payrollDeductionEnabled;
    private final boolean displayBackorderedProducts;
    private final boolean employerManaged;
    private final int offlinePayrollInstallment;
    private final String offlinePayrollInstallmentType;
    private final Map<String,Object> config;
    private final boolean payrollAgreementEnabled;


    private Organization(final OrganizationBuilder builder) {
        Assert.hasText(builder.companyName, "Invalid values found");
        Assert.hasText(builder.companySizeRange, "Invalid values found");
        this.organizationId = builder.organizationId;
        this.companyName = builder.companyName;
        this.dunsNumber = builder.dunsNumber;
        this.overrideShipping = builder.overrideShipping;
        this.drpEnabled = builder.drpEnabled;
        this.eppEnabled = builder.eppEnabled;
        this.companySizeRange = builder.companySizeRange;
        this.catalogs = builder.catalogs;
        this.payrollDeductionEnabled = builder.payrollDeductionEnabled;
        this.displayBackorderedProducts = builder.displayBackorderedProducts;
        this.defaultLocale = builder.defaultLocale;
        this.employerManaged = builder.employerManaged;
        this.offlinePayrollInstallment = builder.offlinePayrollInstallment;
        this.offlinePayrollInstallmentType = builder.offlinePayrollInstallmentType;
        this.config = builder.config;
        this.payrollAgreementEnabled = builder.payrollAgreementEnabled;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Optional<String> getDunsNumber() {
        return dunsNumber;
    }

    public boolean isOverrideShipping() {
        return overrideShipping;
    }

    public boolean isDrpEnabled() {
        return drpEnabled;
    }

    public boolean isEppEnabled() {
        return eppEnabled;
    }

    public String getCompanySizeRange() {
        return companySizeRange;
    }

    public List<Catalog> getCatalogs() {
        return catalogs;
    }

    public boolean isPayrollDeductionEnabled() {
        return payrollDeductionEnabled;
    }

    public boolean isDisplayBackorderedProducts() { return displayBackorderedProducts; }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public boolean isEmployerManaged() {
        return employerManaged;
    }

    public int getOfflinePayrollInstallment() {
        return offlinePayrollInstallment;
    }

    public String getOfflinePayrollInstallmentType() {
        return offlinePayrollInstallmentType;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public boolean isPayrollAgreementEnabled() {
        return payrollAgreementEnabled;
    }

    public static OrganizationBuilder builder(){
        return new OrganizationBuilder();
    }

    public static class OrganizationBuilder {

        private long organizationId;
        private String companyName;
        private Optional<String> dunsNumber;
        private boolean overrideShipping;
        private boolean drpEnabled;
        private boolean eppEnabled;
        private String companySizeRange;
        private List<Catalog> catalogs;
        private String defaultLocale;
        private boolean payrollDeductionEnabled;
        private boolean displayBackorderedProducts;
        private boolean employerManaged;
        private int offlinePayrollInstallment;
        private String offlinePayrollInstallmentType;
        private  Map<String,Object> config;
        private boolean payrollAgreementEnabled;

        public OrganizationBuilder() {
            catalogs = new ArrayList<>();
        }

        public OrganizationBuilder withOrganizationId(long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public OrganizationBuilder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public OrganizationBuilder withDunsNumber(String dunsNumber) {
            this.dunsNumber = Optional.ofNullable(dunsNumber);
            return this;
        }

        public OrganizationBuilder withOverrideShipping(boolean overrideShipping) {
            this.overrideShipping = overrideShipping;
            return this;
        }

        public OrganizationBuilder withDrpEnabled(boolean drpEnabled) {
            this.drpEnabled = drpEnabled;
            return this;
        }

        public OrganizationBuilder withEppEnabled(boolean eppEnabled) {
            this.eppEnabled = eppEnabled;
            return this;
        }

        public OrganizationBuilder withCompanySizeRange(final String companySizeRange) {
            this.companySizeRange = companySizeRange;
            return this;
        }

        public OrganizationBuilder withDefaultLocale(final String defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        public OrganizationBuilder withCatalogs(final List<Catalog> catalogs) {
            this.catalogs.clear();
            if(catalogs != null) {
                this.catalogs.addAll(catalogs);
            }
            return this;
        }

        public OrganizationBuilder withPayrollDeductionEnabled(final boolean payrollDeductionEnabled){
            this.payrollDeductionEnabled = payrollDeductionEnabled;
            return this;
        }

        public OrganizationBuilder withDisplayBackorderedProducts(final boolean displayBackorderedProducts) {
            this.displayBackorderedProducts = displayBackorderedProducts;
            return this;
        }

        public OrganizationBuilder withEmployerManaged(final boolean employerManaged) {
            this.employerManaged = employerManaged;
            return this;
        }

        public OrganizationBuilder withOfflinePayrollInstallment(final int offlinePayrollInstallment) {
            this.offlinePayrollInstallment = offlinePayrollInstallment;
            return this;
        }

        public OrganizationBuilder withOfflinePayrollInstallmentType(final String offlinePayrollInstallmentType) {
            this.offlinePayrollInstallmentType = offlinePayrollInstallmentType;
            return this;
        }

        public OrganizationBuilder withConfig(final Map<String, Object> config) {
            this.config = config;
            return this;
        }

        public OrganizationBuilder withPayrollAgreementEnabled(final boolean payrollAgreementEnabled) {
            this.payrollAgreementEnabled = payrollAgreementEnabled;
            return this;
        }

        public Organization build() {
            return new Organization(this);
        }
    }
}
