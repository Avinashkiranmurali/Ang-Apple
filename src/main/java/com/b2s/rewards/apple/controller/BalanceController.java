package com.b2s.rewards.apple.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.b2s.apple.services.JwtService;
import com.b2s.rewards.apple.model.BalanceTokenResponse;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@SessionAttributes(CommonConstants.USER_SESSION_OBJECT)
@RequestMapping(value = "/participant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public class BalanceController {
    @Autowired
    private JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    //TODO: Need to coordinate with keystone to change it to JSON
    @PostMapping(value = "/balance", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Object> updateBalance(final HttpServletRequest servletRequest,
        @RequestBody final String token, final HttpServletResponse response) {
        logger.info("AUDIT Update User balance request received ...");
        setInternalApiSecurityHeaders(response);
        // Setting the value of 'Access-Control-Allow-Origin' to '*' won't work,
        // because the 'credentials' flag in the request if true
        // We should allow the exact domain name, which is the SAML attribute 'keystoneBaseUrl'

        XSSRequestWrapper request = new XSSRequestWrapper(servletRequest);
        final Map<String, Object> externalUrls = (Map) request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);
        if (Objects.nonNull(externalUrls)) {
            final Map<String, Object> keystoneUrls = (Map<String, Object>) externalUrls.get(ExternalUrlConstants.KEYSTONE_URLS);
            final String keystoneBaseUrl = (String) keystoneUrls.get(ExternalUrlConstants.KEYSTONE_BASE_URL);
            logger.info("Balance SAML keystoneBaseUrl ---- {}", keystoneBaseUrl);
            response.addHeader("Access-Control-Allow-Origin", XSSRequestWrapper.cleanXSS(keystoneBaseUrl));
            response.addHeader("Access-Control-Allow-Credentials", "true");
        } else {
            logger.warn("Session does not contain any external URLs. keystoneBaseUrl cannot be found");
        }

        User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);

        if (Objects.nonNull(user)) {
            try {
                jwtService.updateUserBalanceFomToken(user, token);
                logger.info("User balance updated. new balance = {}", user.getBalance());
            } catch(JWTVerificationException | IOException e) {
                logger.error("AUDIT Token verification failed -", e);
                return ResponseEntity.badRequest().body("Invalid token");
            }
        } else {
            logger.warn("AUDIT User not found. Cannot update balance");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/balanceToken")
    public ResponseEntity<Object> getToken(final HttpServletRequest servletRequest) {
        logger.info("getToken request received ....");

        User user = (User) servletRequest.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        final BalanceTokenResponse response;
        try {
            response = jwtService.generateKeystoneToken(user.getBalance(), user.getCsid());
        } catch (JsonProcessingException e) {
            logger.error("Token generation failed -", e);
            return ResponseEntity.badRequest().body("Token not generated");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Method to set Internal API Security Headers
     */
    private void setInternalApiSecurityHeaders(final HttpServletResponse response) {
        response.addHeader(CommonConstants.HTTP_HEADER_CONTENT_TYPE, CommonConstants.APPLICATION_JSON);
        response.addHeader("Content-Security-Policy", "frame-ancestors 'none'");
        response.addHeader("Strict-Transport-Security", "max-age=16070400");
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.addHeader("X-Frame-Options", "DENY");
    }
}
