package com.b2s.rewards.apple.integration.model;

import com.b2s.service.model.DeliveryMethod;

/**
 * Created by rpillai on 5/29/2018.
 */
public class Delivery {

    private DeliveryMethod deliveryMethod;
    private String firstName;
    private String lastName;

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static Builder builder () { return new Builder(); }

    private Delivery(final Builder builder) {
        this.deliveryMethod = builder.deliveryMethod;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public static class Builder {
        private DeliveryMethod deliveryMethod;
        private String firstName;
        private String lastName;

        public Builder withDeliveryMethod(final DeliveryMethod deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
        }
    }
}
