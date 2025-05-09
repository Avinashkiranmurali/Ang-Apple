package com.b2s.security.oauth.service;

import com.b2s.apple.services.AppleRestService;
import com.b2s.security.oauth.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Service
public class OAuthTokenService {


    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private OAuthConfig oauthConfig;

    @Autowired
    private AppleRestService appleRestService;

    @Value("${rest.api.timeout}")
    private Integer restServiceTimeout;

    @Value("${oauth.tokenExpiryThreshold}")
    private Integer oauthThreshold;



    private static final String DATE_FORMATTER= "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);

    public Token getTokenFomCode(final String code, final OAuthCredentials credentials, final String redirectUrl) {

        LOG.info("Got the code {}", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add(EnumOAuthInputParameter.GRANT_TYPE.getValue(), credentials.getGrantTypeAuth());
        map.add(EnumOAuthInputParameter.CLIENT_ID.getValue(), credentials.getClientId());
        map.add(EnumOAuthInputParameter.CLIENT_SECRET.getValue(), credentials.getClientSecret());
        map.add(EnumOAuthInputParameter.CODE.getValue(), code);
        map.add(EnumOAuthInputParameter.REDIRECT_URI.getValue(), redirectUrl);

        HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(map, headers);

        return getToken(postRequest,credentials);
    }

    public Token getTokenFormClientCredential(final OAuthCredentials oAuthCredentials){

        LOG.info("getTokenFormClientCredential");

        HttpHeaders headers=new HttpHeaders();
        headers.setBasicAuth(oAuthCredentials.getClientId(),oAuthCredentials.getClientSecret());
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add(EnumOAuthInputParameter.GRANT_TYPE.getValue(),oAuthCredentials.getGrantTypeAuth());
        map.add(EnumOAuthInputParameter.SCOPE.getValue(),oAuthCredentials.getScope());

        HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(map, headers);

        return getToken(postRequest,oAuthCredentials);
    }


    public Token refreshToken(final OAuthAttributes oAuthAttributes){
        LOG.info("Invoking Refresh Token...");
        Token oauthToken = oAuthAttributes.getToken();
        OAuthCredentials credentials = oAuthAttributes.getoAuthCredentials();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add(EnumOAuthInputParameter.GRANT_TYPE.getValue(), credentials.getGrantTypeRefresh());
        map.add(EnumOAuthInputParameter.CLIENT_ID.getValue(), credentials.getClientId());
        map.add(EnumOAuthInputParameter.CLIENT_SECRET.getValue(), credentials.getClientSecret());
        map.add(EnumOAuthInputParameter.REFRESH_TOKEN.getValue(), oauthToken.getRefreshToken());
        map.add(EnumOAuthInputParameter.SCOPE.getValue(), credentials.getScope());

        HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(map, headers);
        return getToken(postRequest,credentials);
    }


    private Token getToken(final HttpEntity postRequest,final OAuthCredentials credentials){
        Token token = null;
        RestTemplate restTemplate = null;
        ResponseEntity<Token> response = null;
        try
        {
            restTemplate = appleRestService.getRestTemplate(restServiceTimeout);
            response = restTemplate.postForEntity(credentials.getTokenUrl(), postRequest, Token.class);

        }catch(Exception e) {
            LOG.error("Exception retrieving token (OAuth Token unavailable): ", e);
        }

        if(Objects.nonNull(response)){
            LOG.info("Got the OAuth token {}", response);
            token  = response.getBody();
            token.setTokenCreatedDateTime(getCurrentDateTime());
            token.setThresholdInterval(oauthThreshold);
        }
        return token;
    }

    public boolean isTokenValid(final Token token){
        String tokenCreatedTime = token.getTokenCreatedDateTime();
        LocalDateTime tokenTime = LocalDateTime.parse(tokenCreatedTime, formatter);
        LocalDateTime currentTime = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(tokenTime,currentTime);
        int iDifference = (int) seconds;
        int desireInterval = token.getExpiresIn() - token.getThresholdInterval();
        LOG.info("OAuth Token desireInterval {}", desireInterval);
        return iDifference<desireInterval;
    }

    private String getCurrentDateTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.format(formatter);
    }


}
