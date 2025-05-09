package com.b2s.apple.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.b2s.apple.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JwtConfig {
    @Value("${jwt.keystore.resource}")
    private Resource keystoreResource;
    @Value("${jwt.keystore.password}")
    private char[] keystorePassword;
    @Value("${jwt.keystore.type}")
    private String type;
    @Value("${jwt.keystore.private.alias}")
    private String privateAlias;
    @Value("${jwt.keystore.private.keyid}")
    private String privateKeyId;
    @Value("${jwt.keystore.public.central.alias}")
    private String centralAlias;
    @Value("${jwt.keystore.public.central.keyid}")
    private String centralKeyId;
    @Value("${jwt.keystore.public.travel.alias}")
    private String travelAlias;
    @Value("${jwt.keystore.public.travel.keyid}")
    private String travelKeyId;
    @Value("${jwt.keystore.public.merch.alias}")
    private String merchAlias;
    @Value("${jwt.keystore.public.merch.keyid}")
    private String merchKeyId;
    @Value("${jwt.request.drift.seconds:20}")
    private Long drift;
    @Value("${jwt.token.issuers}")
    private String[] issuers;
    @Value("${jwt.request.expiration.seconds:30}")
    private Long expirationSeconds;

    @Bean
    public JwtService jwtService(final JWTVerifier jwtVerifier, final Algorithm jwtAlgorithm) {
        return new JwtService(jwtVerifier, jwtAlgorithm, expirationSeconds);
    }

    @Bean
    JWTVerifier jwtVerifier(final Algorithm jwtAlgorithm) {
        return JWT.require(jwtAlgorithm)
            .withIssuer(issuers)
            .acceptLeeway(drift)
            .build();
    }

    @Bean
    Algorithm jwtAlgorithm() {
        final JwtProvider jwtProvider =
            new JwtProvider(keystoreResource, privateKeyId, type, keystorePassword, privateAlias, publicKeysAlias());
        return Algorithm.RSA256(jwtProvider);
    }

    private Map<String, String> publicKeysAlias() {
        Map<String, String> result = new HashMap<>();
        result.put(centralKeyId, centralAlias);
        result.put(travelKeyId, travelAlias);
        result.put(merchKeyId, merchAlias);

        return result;
    }

}
