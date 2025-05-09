package com.b2s.rewards.apple.model

import spock.lang.Specification
import spock.lang.Unroll

class AddressSpec extends Specification {

    @Unroll
    def "test canCalculateWithFeesAndTaxes for #address1, #city, #zip5, #state, #country"() {
        setup:
        Address address = new Address(address1: address1, city: city, zip5: zip5, state: state, country: country,
                email: email, phoneNumber: phoneNumber, firstName: firstName, lastName: lastName)

        when:
        def result = address.canCalculateWithFeesAndTaxes()

        then:
        result == expected

        where:
        address1 | city   | zip5    | state | country | email                       | phoneNumber  | firstName | lastName || expected
        ""       | ""     | ""      | ""    | "US"    | ''                          | ''           | 'Kate'    | 'James'  || false
        "street" | ""     | ""      | ""    | "US"    | 'user@bridge2solutions.com' | '7781234356' | ''        | ''       || false
        ""       | ""     | "12345" | ""    | "US"    | ''                          | ''           | ''        | ''       || false
        ""       | ""     | ""      | "GA"  | "US"    | ''                          | ''           | ''        | ''       || false
        "street" | ""     | ""      | "GA"  | "US"    | ''                          | '7781234356' | ''        | ''       || false
        "street" | ""     | "12345" | ""    | "US"    | ''                          | ''           | ''        | ''       || false
        "street" | "city" | ""      | ""    | "US"    | ''                          | ''           | ''        | ''       || false
        ""       | "city" | "12345" | ""    | "US"    | ''                          | ''           | ''        | ''       || false
        ""       | "city" | ""      | "GA"  | "US"    | ''                          | ''           | ''        | ''       || false
        ""       | "city" | ""      | "GA"  | "US"    | ''                          | ''           | ''        | ''       || false
        ""       | "city" | "12345" | "GA"  | "US"    | 'user@bridge2solutions.com' | ''           | ''        | ''       || false
        ""       | ""     | "12345" | "GA"  | "US"    | ''                          | ''           | ''        | ''       || false
        "street" | ""     | "12345" | "GA"  | "US"    | ''                          | '7781234356' | ''        | ''       || false
        "street" | "city" | ""      | "GA"  | "US"    | ''                          | ''           | 'Kate'    | ''       || false
        "street" | "city" | "12345" | ""    | "US"    | ''                          | ''           | ''        | 'James'  || false
        "street" | "city" | "12345" | "GA"  | "US"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | "city" | "12345" | ""    | "SG"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | "city" | "12345" | ""    | "GB"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | ""     | "12345" | ""    | "SG"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | ""     | "12345" | ""    | "GB"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | ""     | ""      | ""    | "GB"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        "street" | ""     | ""      | ""    | "SG"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        ""       | ""     | ""      | ""    | "GB"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true
        ""       | ""     | ""      | ""    | "SG"    | 'user@bridge2solutions.com' | '7781234356' | 'Kate'    | 'James'  || true

    }
}
