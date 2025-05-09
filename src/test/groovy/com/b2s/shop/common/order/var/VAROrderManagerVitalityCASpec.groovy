package com.b2s.shop.common.order.var

import com.b2s.shop.common.User
import spock.lang.Specification
import spock.lang.Subject

class VAROrderManagerVitalityCASpec extends Specification {

    @Subject
    def varOrderManagerVITALITY =
            new VAROrderManagerVITALITYCA()

    def "test ValidLogin"() {

        setup:
        User user = new User()

        when:
        boolean result = varOrderManagerVITALITY.isValidLogin(user, null)

        then:
        result == false

    }

}