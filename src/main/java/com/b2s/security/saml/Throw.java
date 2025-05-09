package com.b2s.security.saml;

import java.util.Optional;

/**
 * Provides functionality related to parameter checking
 *
 * @author Brian Purvis (bpurvis@bridge2solutions.com)
 */
public final class Throw<T> {

    private final T reference;
    private final String referenceName;
    private final Optional<String> message;

    private Throw(final String referenceName, final T reference) {
        this.referenceName = referenceName;
        this.reference = reference;
        this.message = Optional.empty();
    }

    private Throw(final String referenceName, final T reference, final String message) {
        this.referenceName = referenceName;
        this.reference = reference;
        this.message = Optional.ofNullable(message);
    }

    public static <T> Throw<T> when(final String name, final T object) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        return new Throw<>(name, object);
    }

    public static <T> Throw<T> when(final String name, final T object, final String optionalMessage) {
        if (optionalMessage == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        return new Throw<>(name, object, optionalMessage);
    }

    public T isNull() {
        if (reference == null) {
            throw new IllegalArgumentException(referenceName + " cannot be null" + getMessage());
        }
        return reference;
    }

    public T isEmptyString() {
        isNull();
        if (reference instanceof String) {
            if (((String) reference).isEmpty()) {
                throw new IllegalArgumentException(referenceName + " cannot be an empty string" + getMessage());
            }
            return reference;
        }
        throw new IllegalStateException(referenceName + " is not of type String");
    }

    private String getMessage() {
        return message.map(s -> ' ' + s).orElse("");
    }

}
