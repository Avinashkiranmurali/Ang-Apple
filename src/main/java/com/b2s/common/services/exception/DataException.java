package com.b2s.common.services.exception;

import java.io.Serializable;
import java.util.List;

/**
  * This exception is thrown for any bad/invalid user data
  * @author rperumal
  * Date: 10/06/15
 */
public class DataException extends Exception {

    private static final long serialVersionUID = 2198069763366269319L;
    private List<EngraveData> errors;

    public DataException(final List<EngraveData> errors) {
        this.errors = errors;
    }

    public List<EngraveData> getErrors() {
        return errors;
    }

    public void setErrors(final List<EngraveData> errors) {
        this.errors = errors;
    }

    public static class EngraveData implements Serializable {

        private static final long serialVersionUID = -7030170344132746880L;
        private final String inputField;
        private final String message;

        public EngraveData(final String inputField, final String message) {
            this.inputField = inputField;
            this.message = message;
        }

        public String getInputField() {
            return inputField;
        }

        public String getMessage() {
            return message;
        }
    }
}
