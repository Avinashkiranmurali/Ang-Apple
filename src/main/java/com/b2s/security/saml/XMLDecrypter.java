package com.b2s.security.saml;

import com.b2s.security.saml.config.SAMLConfiguration;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;

/**
 * Created by ppalpandi on 12/4/2018.
 */
@Component
public class XMLDecrypter {
    private static final Logger logger = LoggerFactory.getLogger(XMLDecrypter.class);

    private KeyStore keyStore;
    private SAMLConfiguration samlConfiguration;

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setSamlConfiguration(SAMLConfiguration samlConfiguration) {
        this.samlConfiguration = samlConfiguration;
    }

    public Document getDecryptedDocument(final Document document, final Boolean forceEncryption, final String decryptionAlias) throws SAMLException {
        Document decryptedDocument;
        try {
            XMLCipher cipher = XMLCipher.getInstance();
            cipher.init(XMLCipher.DECRYPT_MODE, null);
            Element encryptedDataElement = (Element) document.getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);
            if(Objects.isNull(encryptedDataElement)){
                if (forceEncryption){
                    throw SAMLException.fromCode(SAMLException.Code.ENCRYPTED_DATA_NOT_FOUND);
                }
                else {
                    return document;
                }
            }
            EncryptedData encryptedData = cipher.loadEncryptedData(document, encryptedDataElement);
            KeyInfo ki = encryptedData.getKeyInfo();
            EncryptedKey encryptedKey = ki.itemEncryptedKey(0);
            XMLCipher cipher2 = XMLCipher.getInstance();
            final String keyPassword = samlConfiguration.getAliasCredential(decryptionAlias);
            final Key rsaKey = keyStore.getKey(decryptionAlias,keyPassword.toCharArray());
            cipher2.init(XMLCipher.UNWRAP_MODE, rsaKey);
            Key key = cipher2.decryptKey(encryptedKey, encryptedData.getEncryptionMethod().getAlgorithm());
            cipher.init(XMLCipher.DECRYPT_MODE, key);
            decryptedDocument = cipher.doFinal(document, encryptedDataElement);
            if(Objects.nonNull(decryptedDocument)){
                final String documentToXMLString = documentToXMLString(decryptedDocument);
                logger.info("Decrypted SAML Response from Epsilon : \n{}", documentToXMLString);
            }
            return decryptedDocument;
        } catch (Exception e) {
            throw SAMLException.fromCode(SAMLException.Code.ERROR_DECRYPTING_RESPONSE, e.getCause());
        }
    }

    public static String documentToXMLString(Document document){
        String XMLString = "";
        try{
            final OutputFormat format       = new OutputFormat(document);
            final StringWriter stringWriter = new StringWriter ();
            final XMLSerializer serializer  = new XMLSerializer (stringWriter, format);
            serializer.serialize(document);
            return stringWriter.toString();
        }
        catch(IOException e){
            logger.debug("Problem while transforming from DOM Document to XML String",e.getCause());
            return XMLString;
        }
    }
}
