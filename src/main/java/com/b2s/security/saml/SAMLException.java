package com.b2s.security.saml;

/**
 * Created by ppalpandi on 11/14/2017.
 */
public class SAMLException extends Exception {

    private static final long serialVersionUID = -6577257481873731723L;

    /**
     * Helper enum to encapsulate the root cause of a <code>SAMLException</code>.
     * <p>
     * This enum also allows for a programmatic comparison of Exception causes without a plethora of sub classes
     * or message text matching.
     */
    public enum Code {
        ERROR_INIT_SAML_TOOL("Error initializing the SAMLTool"),
        ERROR_PARSING_RESPONSE("Error parsing SAML response from string"),
        ENCODED_RESPONSE_MISSING("Encoded SAML Response is missing"),
        ENCODED_RESPONSE_EMPTY("Encoded SAML Response is empty"),
        ENCODED_RESPONSE_INVALID("Encoded SAML Response is invalid"),
        ERROR_DECRYPTING_RESPONSE ("Error decrypting SAML response from string"),
        ENCRYPTED_DATA_NOT_FOUND("EncryptedData not found"),
        ENCRYPTED_KEY_NOT_FOUND("EncryptedKey not found"),
        RELAY_STATE_INVALID("Invalid Relay State");

        private final String text;

        Code(final String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private final Code code;

    /**
     * Create a SAMLResponseException from the given Code
     *
     * @param code for this SAMLResponseException
     */
    private SAMLException(final Code code) {
        super(code.toString() + ": " + code.getText());
        this.code = code;
    }

    /**
     * Create a SAMLResponseException from the given Code and with the given Throwable as the cause.
     *
     * @param code  for this SAMLResponseException
     * @param cause of this SAMLResponseException
     */
    private SAMLException(final Code code, final Throwable cause) {
        super(code.toString() + ": " + code.getText() + " [" + cause.getClass().getName() + ": " + cause.getMessage() + ']', cause);
        this.code = code;
    }

    /**
     * Get the code
     *
     * @return code
     */
    public Code getCode() {
        return code;
    }

    /**
     * Build a <code>SAMLException</code> from a code
     *
     * @param theCode from which to build the SAMLException
     * @return the SAMLException with the supplied code
     * @throws IllegalStateException if theCode is null
     */
    public static SAMLException fromCode(final Code theCode) {
        return new SAMLException(theCode);
    }

    /**
     * Build a <code>SAMLException</code> from a code
     *
     * @param theCode  from which to build the SAMLException
     * @param theCause related to the SAMLException
     * @return the SAMLException with the supplied code and cause.
     * @throws IllegalStateException if theCode or theCause is null
     */
    public static SAMLException fromCode(final Code theCode, final Throwable theCause) {
        return new SAMLException(theCode, theCause);
    }

}
