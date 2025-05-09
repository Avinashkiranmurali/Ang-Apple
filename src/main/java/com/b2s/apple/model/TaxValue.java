package com.b2s.apple.model;

/**
 * @author rkumar 2019-09-11
 */
public enum TaxValue {
    HST("hstTax"),
    GST("gstTax"),
    PST("pstTax"),
    CITY("cityTax"),
    CITYDISTRICT("cityDistrictTax"),
    COUNTY("countyTax"),
    COUNTYDISTRICT("countyDistrictTax"),
    STATE("stateTax");

    public final String displayName;

    TaxValue(final String displayName) {
        this.displayName = displayName;
    }
}
