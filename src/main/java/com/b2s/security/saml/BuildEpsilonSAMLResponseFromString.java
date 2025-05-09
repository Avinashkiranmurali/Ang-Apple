package com.b2s.security.saml;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by ppalpandi on 11/14/2017.
 */
@Component
public class BuildEpsilonSAMLResponseFromString {
    @Autowired
    private XMLDecrypter xmlDecrypter;
    private final String DECRYPTION_ALIAS = "CITIGR-Assertion-Encryption";
    @Value("${Citi.SAML.forceEncryption:false}")
    private boolean forceEncryption;
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public BuildEpsilonSAMLResponseFromString() {
        try {
            documentBuilderFactory.setNamespaceAware(true);
            // Disable parsing entities to avoid XML External Entity attacks
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Exception configuring DocumentBuilderFactory");
        }
    }

    /**
     * Builds a SAML <code>Response</code> from the provided <code>String</code>
     *
     * @param responseString from which to parse the SAML <code>Response</code>
     * @return SAML <code>Response</code> parsed from the provided String
     * @throws com.b2s.rewards.saml.exception.SAMLException if there is any problem creating the <code>Response</code> from the provided <code>String</code>
     */
    public Response perform(final String responseString) throws SAMLException {

        final Document document;

        try {
            document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(responseString)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE, e);
        }
        final Document unwrappedDocument = xmlDecrypter.getDecryptedDocument(document,forceEncryption,DECRYPTION_ALIAS);
        // Marshall the Document into an object model
        final Element element = unwrappedDocument.getDocumentElement();
        if (element == null) {
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE,
                    new IllegalArgumentException("SAML Response document contains no root element"));
        }

        final Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(element);

        if (unmarshaller == null) {
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE,
                    new IllegalStateException("Un-marshaller for element " + element.getLocalName() + " is null"));
        }

        final Response response;

        try {
            response = (Response) unmarshaller.unmarshall(element);
        } catch (final UnmarshallingException e) {
            throw SAMLException.fromCode(SAMLException.Code.ERROR_PARSING_RESPONSE, e);
        }

        return response;
    }
}
