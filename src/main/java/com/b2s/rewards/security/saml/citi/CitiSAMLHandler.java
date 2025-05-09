package com.b2s.rewards.security.saml.citi;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.saml.AbstractSAMLHandler;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.security.saml.BuildEpsilonSAMLResponse;
import com.b2s.security.saml.EpsilonSAMLResponseException;
import com.b2s.security.saml.SAMLException;
import com.b2s.security.saml.SAMLResponseException;
import com.b2s.security.saml.config.SAMLConfiguration;
import com.b2s.security.saml.security.SAMLAssertionEncrypter;
import com.b2s.security.saml.security.SAMLObjectSigner;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by vprasanna on 11/15/17.
 */
@Component("epsilonSAMLHandler")
public class CitiSAMLHandler extends AbstractSAMLHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CitiSAMLHandler.class);
    private static final String SAML_SIGNING_FLAG = "citi.ignore.signing.flag";

    @Autowired
    private BuildEpsilonSAMLResponse buildEpsilonSAMLResponse;

    @Autowired
    private SAMLObjectSigner samlObjectSigner;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private SAMLConfiguration samlConfiguration;

    @Autowired
    private SAMLAssertionEncrypter samlAssertionEncrypter;

    @Value("${Citi.SAML.forceEncryption:false}")
    private boolean forceEncryption;

    @Override
    public Response getSamlResponseFromRequest(HttpServletRequest httpServletRequest) throws SAMLException {
        try {
            LOG.debug("Reading Epsilon response!");
            XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
            final Response response = buildEpsilonSAMLResponse.perform(
                request.getParameter(CommonConstants.CITI_SAML_REQ_PARAM_FOR_SAML_RESP)
            );
            return response;
        } catch (EpsilonSAMLResponseException e) {
            LOG.error("Problem while reading SAML Response", e);
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE, e);
        }
    }

    @Override
    public void validateSAMLResponse(final Response response) throws SAMLException, SAMLResponseException {

        final int assertionsSize = response.getAssertions() == null ? 0 : response.getAssertions().size();
        final int encryptedAssertionsSize = response.getEncryptedAssertions() == null ? 0 : response.getEncryptedAssertions().size();

        // Validate that at least 1 Assertion or EncryptedAssertion exists
        if (assertionsSize == 0 && encryptedAssertionsSize == 0) {
            throw SAMLResponseException.fromCode(SAMLResponseException.Code.ASSERTIONS_EMPTY);
        }

        if (assertionsSize != 0) {
            for (final Assertion assertion : response.getAssertions()) {
                validateAssertion(assertion);
            }
        }

        // The signing verification is controlled using the flag.
        // if citi.ignore.signing.flag is set to true then we donot validate the SAML Signing.
        // This feature will be used only in lower environments.
        // Null check not required:
        // Boolean.parseBoolean(null) is false
        // Boolean.parseBoolean("true") is true
        // Boolean.parseBoolean("false") is false
        // Boolean.parseBoolean("junk") is false
        Boolean ignoreSigning = Boolean.parseBoolean(applicationProperties.getProperty(SAML_SIGNING_FLAG));

        //Verification for certificate Signing.
        if (!ignoreSigning) {
            final String signingAlias = "citigr-assertion-signing";
            this.samlObjectSigner.validate(response, signingAlias);
        }
    }

    @Override
    public void validateAssertion(final Assertion assertion) throws SAMLException, SAMLResponseException {
        if (assertion.getID() == null) {
            throw SAMLResponseException.fromCode(SAMLResponseException.Code.ASSERTION_ID_NULL);
        }

        if (assertion.getID().length() <= 0) {
            throw SAMLResponseException.fromCode(SAMLResponseException.Code.ASSERTION_ID_EMPTY);
        }

        final Calendar notBefore = Calendar.getInstance();
        notBefore.setTime(assertion.getConditions().getNotBefore().toDate());

        final Calendar notOnOrAfter = Calendar.getInstance();
        notOnOrAfter.setTime(assertion.getConditions().getNotOnOrAfter().toDate());

        final Calendar now = Calendar.getInstance();

        if (now.after(notOnOrAfter)) {
            throw SAMLResponseException.fromCode(SAMLResponseException.Code.ASSERTION_EXPIRED,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.getDefault()).format(notOnOrAfter.getTime()));
        }
    }

    public void getSAMLAssertion(Response response) throws SAMLResponseException{
        final Assertion assertion;
        if (response.getEncryptedAssertions().isEmpty()) {
            if (forceEncryption){
                LOG.error("Encrypted Assertion not found!");
                throw SAMLResponseException.fromCode(SAMLResponseException.Code.ENCRYPTED_ASSERTION_MISSING);
            }
            assertion = response.getAssertions().get(0);
        } else {
            LOG.info("Encrypted SAML Assertion Detected");
            samlAssertionEncrypter.setSamlConfiguration(samlConfiguration);
            assertion = samlAssertionEncrypter.decrypt(response.getEncryptedAssertions().get(0),"CITIGR-Assertion-Encryption");
        }
        response.getAssertions().clear();
        response.getEncryptedAssertions().clear();
        response.getAssertions().add(assertion);
    }
}
