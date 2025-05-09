package com.b2s.rewards.apple.util

import spock.lang.Specification

import java.time.LocalDate
import java.time.Month


/**
 * Created by vkrishnan on 4/11/2019.
 */
class ShipmentQuoteUtilTest extends Specification {

    def "test shipment quote for business days"() {
        setup:
        def shipmentQuoteUtil = new ShipmentQuoteUtil();
        def now = LocalDate.of(2019,Month.APRIL,10)
        expect:
        result  == shipmentQuoteUtil.getShipmentQuoteDate(days,now);

        where:
        days      || result
        "7 business days"      || "2019-04-19"
        "12 business days"      || "2019-04-26"
        "3 weeks"      || "2019-05-09"
    }

    def "test shipment quote for invalid business days"() {
        setup:
        def shipmentQuoteUtil = new ShipmentQuoteUtil();
        def now = LocalDate.of(2019,Month.APRIL,10)
        expect:
        result  == shipmentQuoteUtil.getShipmentQuoteDate(days,now);

        where:
        days      || result
        null      || null
    }
}