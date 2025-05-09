package com.b2s.apple.mapper.services

import com.b2s.apple.services.VersionService
import com.b2s.rewards.common.util.CommonConstants
import spock.lang.*

import javax.sql.DataSource

class VersionServiceSpec extends Specification {
    def dataSource = Mock(DataSource)

    @Subject
    def versionService = new VersionService(dataSource: dataSource)

    def 'getWebAppHealth() - Health Down'() {
        when:
        def result = versionService.getWebAppHealth(3)

        then:
        result.status == CommonConstants.APP_DOWN
    }

    def 'getWebAppHealth() - Health Down without timeout'() {
        when:
        def result = versionService.getWebAppHealth(null)

        then:
        result.status == CommonConstants.APP_DOWN
    }
}
