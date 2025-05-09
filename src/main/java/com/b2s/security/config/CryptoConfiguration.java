package com.b2s.security.config;

import com.b2s.crypto.core.AbstractCryptoService;
import com.b2s.crypto.spring.AESCryptoServiceBean;
import com.b2s.crypto.spring.EncryptedKeyResource;
import com.b2s.crypto.spring.KeyResource;
import com.b2s.crypto.spring.context.EncryptedPropertiesFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author rjesuraj Date : 9/6/2019 Time : 9:20 PM
 */
@Configuration
public class CryptoConfiguration {
    private AbstractCryptoService constructCryptoService(final Resource kekKeyResource, final Resource dekKeyResource) {
        // KEK Service
        final AESCryptoServiceBean kekCryptoService = new AESCryptoServiceBean();
        final KeyResource keyResource = new KeyResource();
        keyResource.setHexKeyResource(kekKeyResource);
        kekCryptoService.setKeyResource(keyResource);

        // DEK resource. Uses key encryption key resource.
        final EncryptedKeyResource encryptedKeyResource = new EncryptedKeyResource();
        encryptedKeyResource.setHexKeyResource(dekKeyResource);
        encryptedKeyResource.setCryptoService(kekCryptoService);

        // DEK service
        final AESCryptoServiceBean aesCryptoServiceBean = new AESCryptoServiceBean();
        aesCryptoServiceBean.setKeyResource(encryptedKeyResource);

        return aesCryptoServiceBean;
    }

    @Bean("samlConfigCryptoService")
    public AbstractCryptoService b2rEnvironmentConfigService(
        @Value("${aplgr.keBase}/saml-config-kek.hex") final Resource kekKeyResource,
        @Value("${aplgr.deBase}/saml-config-dek.enc-hex") final Resource dekKeyResource) {

        return constructCryptoService(kekKeyResource, dekKeyResource);
    }

    @Bean("samlAliasCredentials")
    public Properties samlAliasCredentials(
        @Qualifier("samlConfigCryptoService") final AbstractCryptoService samlConfigCryptoService,
        @Value("${aplgr.configUrl}/saml/alias-credentials.properties") final Resource samlAliasCredentialsResource)
        throws IOException {

        final EncryptedPropertiesFactoryBean propertiesFactoryBean = new EncryptedPropertiesFactoryBean();
        propertiesFactoryBean.setCryptoService(samlConfigCryptoService);
        propertiesFactoryBean.setLocation(samlAliasCredentialsResource);
        propertiesFactoryBean.afterPropertiesSet();
        return (Properties) propertiesFactoryBean.getObject();
    }

    @Bean("hostToEntityConfig")
    public Properties hostToEntityConfig(
        @Qualifier("samlConfigCryptoService") final AbstractCryptoService samlConfigCryptoService,
        @Value("${aplgr.configUrl}/saml/host-to-entity.properties") final Resource hostToEntityConfigResource)
        throws IOException {

        final EncryptedPropertiesFactoryBean propertiesFactoryBean = new EncryptedPropertiesFactoryBean();
        propertiesFactoryBean.setCryptoService(samlConfigCryptoService);
        propertiesFactoryBean.setLocation(hostToEntityConfigResource);
        propertiesFactoryBean.afterPropertiesSet();
        return (Properties) propertiesFactoryBean.getObject();
    }

    @Bean("hostToIdpConfig")
    public Properties hostToIdpConfig(
        @Qualifier("samlConfigCryptoService") final AbstractCryptoService samlConfigCryptoService,
        @Value("${aplgr.configUrl}/saml/host-to-idp.properties") final Resource hostToIdpConfigResource)
        throws IOException {

        final EncryptedPropertiesFactoryBean propertiesFactoryBean = new EncryptedPropertiesFactoryBean();
        propertiesFactoryBean.setCryptoService(samlConfigCryptoService);
        propertiesFactoryBean.setLocation(hostToIdpConfigResource);
        propertiesFactoryBean.afterPropertiesSet();
        return (Properties) propertiesFactoryBean.getObject();
    }

    @Bean("oauthCredential")
    public Properties oauthConfig(
        @Qualifier("samlConfigCryptoService") final AbstractCryptoService samlConfigCryptoService,
        @Value("${aplgr.configUrl}/oauth/oauth-credentials.properties") final Resource oauthCredentialResource)
        throws IOException {

        final EncryptedPropertiesFactoryBean propertiesFactoryBean = new EncryptedPropertiesFactoryBean();
        propertiesFactoryBean.setCryptoService(samlConfigCryptoService);
        propertiesFactoryBean.setLocation(oauthCredentialResource);
        propertiesFactoryBean.afterPropertiesSet();
        return (Properties) propertiesFactoryBean.getObject();
    }

}
