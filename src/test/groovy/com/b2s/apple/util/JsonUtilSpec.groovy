package com.b2s.apple.util

import org.springframework.core.io.ClassPathResource
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

class JsonUtilSpec extends Specification {

    @Unroll
    def "test remove json element "() {
        given:
            final def cpr = new ClassPathResource(jsonFile)

            final def jsonString = Files.readString(Paths.get(cpr.getURI()))
        when:
        final def result = JsonUtil.removeHeader(jsonString )
        then:
        result.contains(elementToRemove) == expectedResult

        where:
        jsonFile                | elementToRemove   | expectedResult
        'json/configData.json'  | 'header'          | false
        'json/configData.json'  | ''                | true
        'json/configData.json'  | 'templates'       | true
    }

    @Unroll
    def "test add json element "() {
        given:
        final def cpr = new ClassPathResource(jsonFile)

        final def jsonString = Files.readString(Paths.get(cpr.getURI()))
        when:
        final def result = JsonUtil.addElementTo(jsonString, rootElement, elementToAdd, 'Yes' )
        then:
        result.contains(elementToAdd) == expectedResult

        where:
        jsonFile                | rootElement   |  elementToAdd               | expectedResult
        'json/configData.json'  | 'configData'  |'suppressHeader'             | true
        'json/configData.json'  | 'configData'  |'moo'                        | true
        'json/configData.json'  | 'configData'  |'header'                     | true
        'json/configData.json'  | 'configData/templates/header'      |'suppressLogoTemplate'       | true
    }
}
