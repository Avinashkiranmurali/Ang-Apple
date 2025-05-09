package com.b2s.security.oauth;

import com.b2s.security.oauth.service.OAuthTokenService;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OAuthTokenServiceTest {

    private static final String DATE_FORMATTER= "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);

    @Test
    public void testForExpiredToken(){
        OAuthTokenService oAuthTokenService = new OAuthTokenService();
        Token expiredToken = getExpiredToken();
        boolean tokenFlag = oAuthTokenService.isTokenValid(expiredToken);
        assertFalse(tokenFlag);
    }

    @Test
    public void testForValidToken(){
        OAuthTokenService oAuthTokenService = new OAuthTokenService();
        Token validToken = getValidToken();
        boolean tokenFlag = oAuthTokenService.isTokenValid(validToken);
        assertTrue(tokenFlag);
    }

    Token getExpiredToken(){
        Token token = new Token();
        token.setExpiresIn(300);
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusMinutes(6);
        token.setTokenCreatedDateTime(getFormatedDateTime(localDateTime));
        token.setThresholdInterval(30);
        return token;
    }

    Token getValidToken(){
        Token token = new Token();
        token.setExpiresIn(300);
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusMinutes(3);
        token.setTokenCreatedDateTime(getFormatedDateTime(localDateTime));
        token.setThresholdInterval(30);
        return token;
    }

    public String getFormatedDateTime(final LocalDateTime localDateTime){
        return localDateTime.format(formatter);
    }
}
