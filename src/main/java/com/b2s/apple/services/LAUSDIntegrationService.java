package com.b2s.apple.services;

import com.b2s.rewards.apple.integration.model.lausd.LAUSDParentResponse;
import com.b2s.rewards.apple.integration.model.lausd.ParentInfo;
import com.b2s.security.oauth.OAuthCredentials;
import com.b2s.security.oauth.Token;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static com.b2s.rewards.common.util.CommonConstants.*;

@Component
public class LAUSDIntegrationService {

    private static final Logger LOGGER= LoggerFactory.getLogger(LAUSDIntegrationService.class);

    @Autowired
    private AppleRestService appleRestService;

    @Value("${rest.api.timeout}")
    private Integer restServiceTimeout;

    @Value("${lausd.parentws}")
    String lausdEndpointURL;

    public List<ParentInfo> getParentInformation(final User user, final OAuthCredentials authCredentials, final Token token){

        LOGGER.info("Start - invoke LAUSD WS for parent information ");
        RestTemplate restTemplate = null;
        ResponseEntity<LAUSDParentResponse> response = null;

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set(AUTH_TOKEN,token.getAccessToken());
        httpHeaders.set(CLIENT_ID_WITH_HYPEN,authCredentials.getClientId());
        httpHeaders.set(STUDENT_EMAIL,user.getUserId());
        httpHeaders.setBasicAuth(authCredentials.getServiceAccountId(),authCredentials.getServiceAccountPassword());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(null,httpHeaders);

        try {
            restTemplate = appleRestService.getRestTemplate(restServiceTimeout);
            response = restTemplate.exchange(lausdEndpointURL,HttpMethod.GET,postRequest,LAUSDParentResponse.class);

        } catch (final Exception e) {
            LOGGER.error("LAUSDIntegrationService - Error while retrieving user information" ,e);
        }

        if (Objects.nonNull(response)&& Objects.nonNull(response.getBody())){
            LOGGER.info("END - invoke LAUSD WS for parent information");
            return response.getBody().getParentEmailList();
        }
        LOGGER.info("END - Empty response on invoking LAUSD WS");
        return null;
    }
}
