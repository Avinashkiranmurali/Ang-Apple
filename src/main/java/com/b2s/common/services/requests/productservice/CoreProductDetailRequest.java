package com.b2s.common.services.requests.productservice;

import com.google.common.collect.ImmutableSet;
import org.joda.money.CurrencyUnit;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * This class encapsulates query information used for finding product detail
 * information
 @author sjonnalagadda
  * Date: 7/29/13
  * Time: 11:45 AM
 *
 */
public class CoreProductDetailRequest {
    private final String psid;
    private final boolean needVariationsInfo;
    private final boolean needRealTimeInfo;
    private final Set<CurrencyUnit> targetCurrencies;

    private CoreProductDetailRequest(final CoreProductDetailRequestBuilder builder) {
        Assert.hasLength(builder.psid, "Product detail request PSID is not present");
        this.psid = builder.psid;
        this.needVariationsInfo = builder.needVariationsInfo;
        this.needRealTimeInfo = builder.needRealTimeInfo;
        this.targetCurrencies = builder.targetCurrencies;
    }

    public String getPsid() {
        return psid;
    }
    public boolean isNeedVariationsInfo() {
        return needVariationsInfo;
    }
    public boolean isNeedRealTimeInfo() {
        return needRealTimeInfo;
    }
    public Set<CurrencyUnit> getTargetCurrencies() {
        return ImmutableSet.copyOf(targetCurrencies);
    }

    public static CoreProductDetailRequestBuilder builder() {
        return new CoreProductDetailRequestBuilder();
    }

    public static class CoreProductDetailRequestBuilder {
        private String psid;
        private boolean needVariationsInfo;
        private boolean needRealTimeInfo;
        private final Set<CurrencyUnit> targetCurrencies;

        public CoreProductDetailRequestBuilder() {
            this.targetCurrencies = new HashSet<>();
        }

        public CoreProductDetailRequestBuilder withPsid(String psid) {
            this.psid = psid;
            return this;
        }

        public CoreProductDetailRequestBuilder withNeedVariationsInfo(boolean needVariationsInfo) {
            this.needVariationsInfo = needVariationsInfo;
            return this;
        }

        public CoreProductDetailRequestBuilder withNeedRealTimeInfo(boolean needRealTimeInfo) {
            this.needRealTimeInfo = needRealTimeInfo;
            return this;
        }

        public CoreProductDetailRequestBuilder withTargetCurrencies(final Set<CurrencyUnit> targetCurrencies) {
            this.targetCurrencies.clear();
            if(targetCurrencies != null) {
                this.targetCurrencies.addAll(targetCurrencies);
            }
            return this;
        }

        public CoreProductDetailRequest build() {
            return new CoreProductDetailRequest(this);
        }
    }
}
