package com.b2s.security.oauth;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;


@Component
public class OAuthConfig {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    @Qualifier("oauthCredential")
    private Properties oauthCredential;

    public OAuthCredentials getOAuthCredentials(final String varId) {
        OAuthCredentials credentials = new OAuthCredentials();
        final String clientId = oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.CLIENT_ID.getValue());
        if(StringUtils.isNotBlank(clientId)){
            LOGGER.info("Got oauthCredential for varId id = {} / clientId = {}", varId,clientId);
            credentials.setAuthUrl(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.AUTH_URL.getValue()));
            credentials.setTokenUrl(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.TOKEN_URL.getValue()));
            credentials.setClientId(clientId);
            credentials.setClientSecret(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.CLIENT_SECRET.getValue()));
            credentials.setGrantTypeAuth(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.GRANT_TYPE_AUTH.getValue()));
            credentials.setGrantTypeRefresh(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.GRANT_TYPE_REFRESH.getValue()));
            credentials.setScope(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.SCOPE.getValue()));
            credentials.setRedirectUri(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.REDIRECT_URI.getValue()));
            credentials.setEndSession(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.END_SESSION_URI.getValue()));
            credentials.setCheckSessionIframe(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.CHECK_SESSION_IFRAME_URI.getValue()));
            credentials.setServiceAccountId(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.SERVICE_ACCOUNT_ID.getValue()));
            credentials.setServiceAccountPassword(oauthCredential.getProperty(varId.toLowerCase()+ EnumOAuthKey.SERVICE_ACCOUNT_PASSWORD.getValue()));
            return credentials;
        }else{
            return null;
        }
    }

    public String getOktaFlag(final String varId) {
        return oauthCredential.getProperty(varId.toLowerCase()+EnumOAuthKey.OKTA_FLAG.getValue());
    }

}
