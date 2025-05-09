package com.b2s.security.saml;

/**
 * Exception indicating a problem with a decoded SAML Response
 *
 * @author Brian Purvis (bpurvis@bridge2solutions.com)
 */
public final class SAMLResponseException extends Exception {

    private static final long serialVersionUID = -9087672264730121094L;
    private final Code code;

    /**
     * Create a SAMLResponseException from the given <code>Code</code>
     *
     * @param code for this SAMLResponseException
     */
    private SAMLResponseException(final Code code) {
        super(code.toString() + ": " + code.getText());
        this.code = code;
    }

    /**
     * Create a SAMLResponseException from the given <code>Code</code>
     *
     * @param code           for this SAMLResponseException
     * @param additionalText for this SAMLResponseException
     */
    private SAMLResponseException(final Code code, final String additionalText) {
        super(code.toString() + ": " + code.getText() + " [" + additionalText + ']');
        this.code = code;
    }

    /**
     * Create a SAMLResponseException from the given <code>Code</code> and with the given Throwable as the cause.
     *
     * @param code  for this SAMLResponseException
     * @param cause of this SAMLResponseException
     */
    private SAMLResponseException(final Code code, final Throwable cause) {
        super(code.toString() + ": " + code.getText(), cause);
        this.code = code;
    }

    /**
     * Build a SAMLResponseException from the provided <code>Code</code>
     *
     * @param theCode for this SAMLResponseException
     * @return exception with the provided code
     */
    public static SAMLResponseException fromCode(final Code theCode) {
        Throw.when("code", theCode).isNull();
        return new SAMLResponseException(theCode);
    }

    /**
     * Build a SAMLResponseException from the provided <code>Code</code> and additional text
     *
     * @param theCode        for this SAMLResponseException
     * @param additionalText for this SAMLResponseException
     * @return exception with the provided code and additional text
     */
    public static SAMLResponseException fromCode(final Code theCode, final String additionalText) {
        Throw.when("code", theCode).isNull();
        Throw.when("additionalText", additionalText).isEmptyString();
        return new SAMLResponseException(theCode, additionalText);
    }

    /**
     * Build a SAMLResponseException from the provided <code>Code</code> and additional text
     *
     * @param theCode for this SAMLResponseException
     * @param cause   for this SAMLResponseException
     * @return exception with the provided code and cause
     */
    public static SAMLResponseException fromCode(final Code theCode, final Throwable cause) {
        Throw.when("code", theCode).isNull();
        Throw.when("cause", cause).isNull();
        return new SAMLResponseException(theCode, cause);
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
     * Helper enum to encapsulate the root cause of a SAML Response exception.
     * <p>
     * This enum also allows for a programmatic comparison of exception causes without a plethora of sub classes
     * or message text matching.
     */
    public enum Code {
        RESPONSE_SIGNATURE_INVALID("SAML Response signature does not validate"),
        ASSERTIONS_EMPTY("SAML response contains no Assertions"),
        ASSERTION_DECRYPTION_ERROR("Error decrypting encrypted Assertion"),
        ASSERTION_ID_NULL("Assertion ID is null"),
        ASSERTION_ID_EMPTY("Assertion ID is empty"),
        ASSERTION_TOO_EARLY("Assertion contains a notOnOrBefore date which is too early"),
        ASSERTION_EXPIRED("Assertion contains a notOnOrAfter date that was expired"),
        ASSERTION_MISSING_SUBJECT("Assertion is missing a subject"),
        ASSERTION_SUBJECT_MISSING_NAME_ID("Assertion subject is missing a name id"),
        ASSERTION_SUBJECT_NULL_NAME_ID("Assertion subject has a null name id"),
        ASSERTION_SUBJECT_EMPTY_NAME_ID("Assertion subject has an empty name id"),
        ASSERTION_MISSING_ATTRIBUTE_STATEMENTS("Assertion is missing AttributeStatements"),
        ASSERTION_INCORRECT_NUMBER_OF_ATTRIBUTE_STATEMENTS("Assertion has an incorrect number of AttributeStatements"),
        ASSERTION_MISSING_ATTRIBUTE("Assertion is missing an Attribute"),
        ASSERTION_DUPLICATE("Assertion id has already been seen"),
        ENCRYPTED_ASSERTION_MISSING("SAML response is missing encrypted assertion");

        private final String text;

        Code(final String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

}
