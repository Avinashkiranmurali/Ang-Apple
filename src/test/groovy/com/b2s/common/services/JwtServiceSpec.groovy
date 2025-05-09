package com.b2s.common.services

import com.auth0.jwt.exceptions.TokenExpiredException
import com.b2s.apple.config.jwt.JwtConfig
import com.b2s.apple.services.JwtService
import com.b2s.shop.common.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@TestPropertySource(properties = [
        'jwt.keystore.resource=classpath:oauth/aplgr-jwt-credentials.jceks',
        'jwt.keystore.password=changeit',
        'jwt.keystore.type=JCEKS',
        'jwt.keystore.private.alias=jwt',
        'jwt.keystore.private.keyid=apple-gr',
        'jwt.keystore.public.central.keyid=apple-gr',
        'jwt.keystore.public.central.alias=jwt',
        'jwt.keystore.public.travel.keyid=travel',
        'jwt.keystore.public.travel.alias=travel',
        'jwt.keystore.public.merch.keyid=merch',
        'jwt.keystore.public.merch.alias=merch',
        'jwt.token.issuers=core,apple',
        'jwt.request.expiration.seconds=20',
        'jwt.request.drift.seconds=30'])
@ContextConfiguration(classes = [JwtConfig])
class JwtServiceSpec extends Specification {

    @Autowired
    JwtService jwtService;

    def "create a token with points and cid then verify it"() {
        given:
        jwtService.expirationSeconds = 500L
        def token = jwtService.generateKeystoneToken(123, 'CSID')
        User user = new User();

        when:
        jwtService.updateUserBalanceFomToken(user, token.getToken())

        then:
        user.getPoints() == 123
        user.isUpdateBalance() == true
    }

    def "create an expired token then verification should throws an exception"() {
        given:
        jwtService.expirationSeconds = -200L
        def token = jwtService.create(new HashMap<String, String>())

        when:
        def balance = jwtService.parse(token, "iss", String.class)

        then:
        thrown(TokenExpiredException)
    }

    def "create a token then try to parse a wrong claim"() {
        given:
        jwtService.expirationSeconds = 500L
        def token = jwtService.create(new HashMap<String, String>())

        when:
        def result = jwtService.parse(token, "wrong", String.class)

        then:
        result == null
    }
}
