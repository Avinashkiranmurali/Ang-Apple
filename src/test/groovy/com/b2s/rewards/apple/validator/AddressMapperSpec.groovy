package com.b2s.rewards.apple.validator

import com.b2r.util.address.melissadata.GlobalAddress
import com.b2s.rewards.apple.model.Address
import spock.lang.Specification
import spock.lang.Subject

class AddressMapperSpec extends Specification {

    @Subject
    private AddressMapper addressMapper = new AddressMapper()

    def 'test transformGlobalAddressLines()'() {

        given:
        final def GlobalAddress globalAddress = new GlobalAddress(countryISO3166_1_Alpha2: 'CH', organization: 'org',
                addressLine1: 'addr1', addressLine2: 'addr2', locality: 'local', postalCode: 'post')

        final def Address address = new Address()

        when:
        addressMapper.transformGlobalAddressLines(globalAddress, address)

        then:
        address.state == ""


    }

    /**
     * Melissa sometimes returns Locality in addressLine2 or addressLine3 for GB (UK) addresses.
     * This test makes sure that addressLine2 or addressLine3 does not have Locality.
     * GB address does not have states and this test makes sure that the state is empty
     * GB address has Locality in uppercase.
     * @return
     */
    def 'GB test transformGlobalAddressLines()'() {


        given:
        final def GlobalAddress globalAddress = new GlobalAddress(countryISO3166_1_Alpha2: country,
                addressLine1: addressLine1, addressLine2: addressLine2,
                addressLine3: addressLine3, locality: locality, postalCode: postalCode)

        final def Address address = new Address()
        address.address2 = 'COVENTRY'
        address.address3 = 'COVENTRY'
        address.state = 'STATE'

        when:
        addressMapper.transformGlobalAddressLines(globalAddress, address)

        then:
        address.address1 == globalAddress.addressLine1
        address.address2 != globalAddress.locality
        address.address3 != globalAddress.locality
        address.city == globalAddress.locality.toUpperCase()
        address.state == ""


        where:
        country | addressLine1 | addressLine2 | addressLine3 | locality   | postalCode
        'GB'    | 'Broad Lea'  | ''           | ''           | 'COVENTRY' | 'CV6 2EP'
        'GB'    | 'Broad Lea'  | 'New Road'   | ''           | 'CoVeNTrY' | 'CV6 2EP'
        'GB'    | 'Broad Lea'  | 'New Road'   | 'address3'   | 'CoVeNTrY' | 'CV6 2EP'
        'GB'    | 'Broad Lea'  | 'New Road'   | 'COVENTRY'   | 'COVENTRY' | 'CV6 2EP'
        'GB'    | 'Broad Lea'  | 'COVENTRY'   | ''           | 'COVENTRY' | 'CV6 2EP'
        'GB'    | 'Broad Lea'  | 'COVENTRY'   | 'COVENTRY'   | 'CoVeNTrY' | 'CV6 2EP'
    }
}