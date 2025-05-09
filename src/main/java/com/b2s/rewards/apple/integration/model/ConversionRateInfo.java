package com.b2s.rewards.apple.integration.model;

/*** Created by srukmagathan on 6/10/2016.
 */
public class ConversionRateInfo {

    private String baseCurrency;
    private String quoteId;
    private String targetCurrency;
    private double rate;

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(final String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(final String quoteId) {
        this.quoteId = quoteId;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(final double rate) {
        this.rate = rate;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(final String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }
}
