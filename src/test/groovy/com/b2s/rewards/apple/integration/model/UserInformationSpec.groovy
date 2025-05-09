package com.b2s.rewards.apple.integration.model

import com.b2s.shop.common.User
import spock.lang.Specification
import spock.lang.Unroll

class UserInformationSpec extends Specification {

    UserInformation userInformation

    def setup() {
        userInformation = new UserInformation()
    }

    def 'successful transform with additional addresses' () {
        given:
        userInformation.firstName = "test"
        userInformation.lastName = "last"

        userInformation.address = address("add1", "line2", "test", "89898")
        userInformation.additionalAddresses = [address("line1", "line2Test", "additionalCity", "99768")]
        User user = new User()

        when:
        userInformation.copyDataToUser(user)

        then:
        noExceptionThrown()
        user.firstName == "test"
        user.lastName == "last"
        user.addr1 == "add1"
        user.addr2 == "line2"
        user.city == "test"
        user.state == "ATL"
        user.zip == "89898"
        user.country == "US"

        user.addresses.size() == 2

    }

    @Unroll
    def 'successful transform with #scenario' () {
        given:
        userInformation.firstName = "test"
        userInformation.lastName = "last"

        userInformation.address = address("add1", "line2", "test", "89898")
        userInformation.additionalAddresses = [address("line1", "line2Test", "additionalCity", "99768")]
        User user = new User()

        when:
        userInformation.copyDataToUser(user, disableAdditonalAddress)

        then:
        noExceptionThrown()
        user.firstName == "test"
        user.lastName == "last"
        user.addr1 == "add1"
        user.addr2 == "line2"
        user.city == "test"
        user.state == "ATL"
        user.zip == "89898"
        user.country == "US"
        if(disableAdditonalAddress) {
            user.addresses == expectedResult
        }else {
            user.addresses*.class == expectedResult
        }

        where:
        scenario                        | expectedResult                                                          | disableAdditonalAddress
        'disable additional address'    |   null                                                                  | true
        'enable additional address'     |   [com.b2s.common.services.model.Address, com.b2s.common.services.model.Address] | false

    }

    Address address(String line1, String line2, String city, String postalCode) {

        Address address = new Address()
        address.line1 = line1
        address.line2 = line2
        address.city = city
        address.stateCode = "ATL"
        address.countryCode = "US"
        address.postalCode = postalCode

        address
    }
}
