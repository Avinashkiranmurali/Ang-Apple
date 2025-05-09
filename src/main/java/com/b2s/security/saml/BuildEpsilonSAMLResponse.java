package com.b2s.security.saml;

import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.lang.invoke.MethodHandles;

/**
 * Created by ppalpandi on 11/14/2017.
 */
@Component
public class BuildEpsilonSAMLResponse {

    Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private BuildEpsilonSAMLResponseFromString buildEpsilonSAMLResponseFromString;

    public Response perform(final String encodedSAMLResponse) throws SAMLException, EpsilonSAMLResponseException {
        // Sanity check to make sure the SAML token is there...
        if (encodedSAMLResponse == null) {
            throw SAMLException.fromCode(SAMLException.Code.ENCODED_RESPONSE_MISSING);
        }
        // ...and not empty
        if (encodedSAMLResponse.isEmpty()) {
            throw SAMLException.fromCode(SAMLException.Code.ENCODED_RESPONSE_EMPTY);
        }

        // Decode the encoded response string
        final String rawSAMLResponseString;
        try {
            rawSAMLResponseString = new String(DatatypeConverter.parseBase64Binary(encodedSAMLResponse.trim()));
            if (logger.isInfoEnabled()) {
                logger.info("rawSAMLResponseString: {}", rawSAMLResponseString);
            }
        } catch (final IllegalArgumentException e) {
            throw SAMLException.fromCode(SAMLException.Code.ENCODED_RESPONSE_INVALID, e);
        }

        // Marshall the content into a Response object and validate it
        final Response response = buildEpsilonSAMLResponseFromString.perform(rawSAMLResponseString);

        return response;
    }
}
