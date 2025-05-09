package com.b2s.rewards.security.saml;

import com.b2s.security.saml.SAMLException;
import com.b2s.security.saml.SAMLResponseException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by vprasanna on 11/15/17.
 */
public abstract class AbstractSAMLHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSAMLHandler.class);

    public abstract Response getSamlResponseFromRequest(final HttpServletRequest httpServletRequest) throws SAMLException;

    public abstract void validateSAMLResponse(final Response response) throws SAMLException, SAMLResponseException;

    public abstract void validateAssertion(final Assertion assertion) throws SAMLException, SAMLResponseException;

    public Response handleSAMLResponse(final HttpServletRequest httpServletRequest) throws SAMLException {
        LOG.info("Performing Custom SAML Handling");
        try {
            LOG.info("Reading SAML response from the REQUEST");
            final Response response = getSamlResponseFromRequest(httpServletRequest);

            LOG.info("Validating SAML Response");
            validateSAMLResponse(response);

            // Get the one and only Assertion
            final Assertion assertion = response.getAssertions().get(0);

            LOG.info("Validating SAML Assertion");
            validateAssertion(assertion);

            LOG.info("All SAML Checks are done");
            return response;
        } catch (final SAMLException e) {
            LOG.error("Custom SAML assertion flow failed", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Custom SAML assertion flow failed", e);
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE, e);
        }
    }
}
