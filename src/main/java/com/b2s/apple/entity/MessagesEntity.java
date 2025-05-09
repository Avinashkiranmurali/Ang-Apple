package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@Entity
@Table(name = "messages")
public class MessagesEntity {

    @EmbeddedId
    private MessageId messageId;

    @Column(name = "message")
    private String message;

    @Column(name = "location")
    private String location;


    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(final MessageId messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    @Embeddable
    public static class MessageId implements Serializable {

        @Column(name = "varid")
        private String varId;
        @Column(name = "programid")
        private String programId;
        @Column(name = "supplierid")
        private int supplierId;
        @Column(name = "code")
        private int code;
        @Column(name = "merchantid")
        private String merchantId = "-1";

        public String getVarId() {
            return varId;
        }

        public void setVarId(final String varId) {
            this.varId = varId;
        }

        public String getProgramId() {
            return programId;
        }

        public void setProgramId(final String programId) {
            this.programId = programId;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(final String merchantId) {
            this.merchantId = merchantId;
        }

        public int getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(final int supplierId) {
            this.supplierId = supplierId;
        }

        public int getCode() {
            return code;
        }

        public void setCode(final int code) {
            this.code = code;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final MessageId that = (MessageId) o;
            return varId.equals(that.varId) &&
                programId.equals(that.programId) &&
                supplierId == that.supplierId &&
                code == that.code &&
                merchantId.equals(that.merchantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(varId, programId, supplierId, code, merchantId);
        }

    }

}
