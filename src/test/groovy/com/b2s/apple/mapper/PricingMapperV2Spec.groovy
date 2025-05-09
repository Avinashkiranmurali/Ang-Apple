package com.b2s.apple.mapper

import spock.lang.Specification
import spock.lang.Subject

class PricingMapperV2Spec extends  Specification{

    @Subject
    PricingMapperV2 mapperV2=new PricingMapperV2()

    def 'test retrieveDeliveryMethod'(){

        when:
        def result=mapperV2.retrieveDeliveryMethod(shippingMethod)

        then:
        result.name()==expectedOutput

        where:
        shippingMethod      ||  expectedOutput
        "ELECTRONIC"        ||  "EMAIL"
        "DOMESTIC_SHIPPING" || "DOMESTIC_SHIPPING"
        ""                  || "DOMESTIC_SHIPPING"
        null                || "DOMESTIC_SHIPPING"
    }

}
