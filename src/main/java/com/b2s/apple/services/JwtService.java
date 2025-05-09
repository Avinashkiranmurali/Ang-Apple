package com.b2s.apple.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.b2s.rewards.apple.model.BalanceTokenResponse;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    public static final String POINTS = "points";
    public static final String CSID = "csid";

    private JWTVerifier jwtVerifier;
    private Algorithm jwtAlgorithm;
    private Long expirationSeconds;

    private static Logger log = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    public JwtService(final JWTVerifier jwtVerifier, final Algorithm jwtAlgorithm, final Long expirationSeconds) {
        this.jwtVerifier = jwtVerifier;
        this.jwtAlgorithm = jwtAlgorithm;
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Parse a token
     *
     * @param token
     * @param name
     * @param clazz
     * @param <T>
     * @return object
     * @throws IOException
     */
    public <T> T parse(final String token, final String name, final Class<T> clazz) throws IOException {
        log.info("JwtService - Parse : name = {}", name);
        final DecodedJWT decodedJWT = jwtVerifier.verify(token);
        final Claim claim = decodedJWT.getClaim(name);
        if (claim instanceof NullClaim) {
            return null;
        }

        final T value = new ObjectMapper().readValue(claim.asString(), clazz);
        log.info("JwtService - Parse : value = {}", value);
        return value;
    }

    /**
     * Create a token
     *
     * @param payload
     * @return token : String
     */
    public String create(final Map<String, Object> payload) throws JsonProcessingException {
        log.info("JwtService - Creating a token");

        final JWTCreator.Builder builder = JWT.create()
            .withIssuer(CommonConstants.APPLE)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + expirationSeconds * 1000));

        if (payload != null && !payload.isEmpty()) {
            for (final Map.Entry<String, Object> entry : payload.entrySet()) {
                builder.withClaim(entry.getKey(), new ObjectMapper().writeValueAsString(entry.getValue()));
            }
        }
        return builder.sign(jwtAlgorithm);
    }

    public BalanceTokenResponse generateKeystoneToken(final int points, final String csid)
        throws JsonProcessingException {
        final Map<String, Object> jsonPayload = new HashMap<>();
        jsonPayload.put(POINTS, Integer.valueOf(points));
        jsonPayload.put(CSID, csid);

        final String token = create(jsonPayload);
        return new BalanceTokenResponse(token, jwtAlgorithm.getSigningKeyId());
    }

    public void updateUserBalanceFomToken(final User user, final String token) throws IOException {
        final Integer points;
        points = this.parse(token, POINTS, Integer.class);
        user.setBalance(points);
        user.setUpdateBalance(true);
    }
}
