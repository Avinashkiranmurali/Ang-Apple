package com.b2s.rewards.apple.util

import spock.lang.Specification

import static com.b2s.rewards.apple.util.ChaseUtil.zipValidation

/**
 * @author rkumar 2019-02-27
 */
class ChaseUtilSpec extends Specification {

    def "test zipCode with 5 and digit"() {
        expect:
        result == zipValidation(zipCode)

        where:
        zipCode      || result
        "12345"      || "12345"
        "123456789"  || "12345-6789"
        "12345-6789" || "12345-6789"
    }

    def "test zipCode with invalid value"() {
        when:
        zipValidation(zipCode)

        then:
        IllegalArgumentException ex = thrown()
        message == ex.message

        where:
        zipCode      || message
        "123456"     || 'ZIP CODE length should be 9 or 5 digits'
        "1234"       || 'ZIP CODE length should be 9 or 5 digits'
        "1234-56789" || "ZIP CODE length should be 9 or 5 digits"

    }
}
