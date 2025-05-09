package com.b2s.apple.config.jwt;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public class JwtProvider implements RSAKeyProvider {
    private static Logger log = LoggerFactory.getLogger(RSAKeyProvider.class);

    private Resource keystoreResource;
    private String privateKeyId;
    private String type;
    private char[] keystorePassword;
    private String privateKeyAlias;
    private Map<String, String> aliasKeyidMapping;

    public JwtProvider(final Resource keystoreResource, final String privateKeyId, final String type,
        final char[] keystorePassword, final String privateKeyAlias, final Map<String, String> aliasKeyidMapping) {
        this.keystoreResource = keystoreResource;
        this.privateKeyId = privateKeyId;
        this.type = type;
        this.keystorePassword = keystorePassword;
        this.privateKeyAlias = privateKeyAlias;
        this.aliasKeyidMapping = aliasKeyidMapping;
    }

    @Override
    public RSAPublicKey getPublicKeyById(final String keyId) {
        try (final InputStream jwtKeystoreStream = keystoreResource.getInputStream()) {
            //load keystore with private/public key
            final KeyStore jwtKeyStore = KeyStore.getInstance(type);
            jwtKeyStore.load(jwtKeystoreStream, keystorePassword);

            //extract public key
            final String alias = aliasKeyidMapping.get(keyId);
            if (StringUtils.isBlank(alias)) {
                log.error("The public key id {} does not have an associated alias", keyId);
                throw new IllegalArgumentException("Public Key alias not found");
            }
            final Certificate certificate = jwtKeyStore.getCertificate(alias);
            return (RSAPublicKey) certificate.getPublicKey();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            log.error("Error loading public key : ", e);
            throw new IllegalArgumentException("error loading public key");
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        try (final InputStream jwtKeystoreStream = keystoreResource.getInputStream()) {
            //load keystore with private/public key
            final KeyStore jwtKeyStore = KeyStore.getInstance(type);
            jwtKeyStore.load(jwtKeystoreStream, keystorePassword);

            //extract private key
            final Key rsaPrivateKey = jwtKeyStore.getKey(privateKeyAlias, keystorePassword);
            return (RSAPrivateKey) rsaPrivateKey;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            log.error("Error loading private key : ", e);
            throw new IllegalArgumentException("error loading private key");
        }
    }

    @Override
    public String getPrivateKeyId() {
        return privateKeyId;
    }
}
