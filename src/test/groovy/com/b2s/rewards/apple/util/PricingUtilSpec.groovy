package com.b2s.rewards.apple.util

import com.b2s.rewards.apple.model.Program
import com.b2s.shop.common.User
import spock.lang.Specification

class PricingUtilSpec extends Specification {

    def 'test calculateUpgradeCost()'() {
        when:
        final Double upgradeCost = PricingUtil.calculateUpgradeCost(new User(), new Program(), retailCost, activationFee, financedAmount, paymentValue, delta)

        then:
        upgradeCost == expected

        where:
        retailCost | activationFee | financedAmount | paymentValue | delta || expected
        399.0      | 7.0           | null           | 8.0          | 0.0   || 200.0
        199.0      | 7.0           | null           | 8.0          | 0.0   || 0.0
        199.0      | 70.0          | null           | 8.0          | 0.0   || -63.0
        279.0      | 7.0           | 192.0          | 8.0          | 0.0   || 80.0
        192.0      | 7.0           | 192.0          | 8.0          | 0.0   || -7.0

        309        | 25            | null           | 10.5         | 2     || 30
        529        | 25            | null           | 15.5         | 2     || 130
        359        | 19            | null           | 7.5          | 0     || 160
        369        | 0             | null           | 10.35        | 0.6   || 120

    }

    def 'test calculateTotalDueTodayBeforeTax()'() {
        when:
        final Double totalDue = PricingUtil.calculateTotalDueTodayBeforeTax(new User(), new Program(), upgradeCost, activationFee)

        then:
        totalDue == expected

        where:
        upgradeCost || activationFee    || expected
        20.0        || 7.0              || 27.0
        0.0         || 7.0              || 7.0
        10.0        || -70.0            || -60.0
    }

    def 'test calculateTotalDueTodayAfterTax()'() {
        when:
        final Double totalDue = PricingUtil.calculateTotalDueTodayAfterTax(totalDueTodayBeforeTax, tax)

        then:
        totalDue == expected

        where:
        totalDueTodayBeforeTax  || tax    || expected
        20.0                    || 7.0    || 27.0
        0.0                     || 7.0    || 7.0
    }
}
