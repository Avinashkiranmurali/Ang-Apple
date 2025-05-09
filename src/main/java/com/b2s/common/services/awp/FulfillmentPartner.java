package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;

/**
 * Created by srajendran on 8/3/2018.
 */
@JsonDeserialize(builder = FulfillmentPartner.FulfillmentPartnerBuilder.class)
public class FulfillmentPartner {

    private final Long fulfillmentPartnerId;
    private final String fulfillmentPartnerName;
    private final String displayName;
    private final String imageUrl;
    private final List<String> country;
    private final Boolean active;

    private FulfillmentPartner(final FulfillmentPartnerBuilder builder){
        this.fulfillmentPartnerId = builder.fulfillmentPartnerId;
        this.fulfillmentPartnerName = builder.fulfillmentPartnerName;
        this.displayName = builder.displayName;
        this.imageUrl = builder.imageUrl;
        this.country = builder.country;
        this.active = builder.active;
    }

    public Long getFulfillmentPartnerId() { return fulfillmentPartnerId; }

    public String getFulfillmentPartnerName() { return fulfillmentPartnerName; }

    public String getDisplayName() { return displayName; }

    public String getImageUrl() { return imageUrl; }

    public List<String> getCountry() { return Collections.unmodifiableList(country); }

    public Boolean getActive() { return active; }

    public static class FulfillmentPartnerBuilder {

        private Long fulfillmentPartnerId;
        private String fulfillmentPartnerName;
        private String displayName;
        private String imageUrl;
        private List<String> country;
        private Boolean active;


        public FulfillmentPartnerBuilder withFulfillmentPartnerId(final Long fulfillmentPartnerId) {
            this.fulfillmentPartnerId = fulfillmentPartnerId;
            return this;
        }

        public FulfillmentPartnerBuilder withFulfillmentPartnerName(final String fulfillmentPartnerName) {
            this.fulfillmentPartnerName = fulfillmentPartnerName;
            return this;
        }

        public FulfillmentPartnerBuilder withDisplayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public FulfillmentPartnerBuilder withImageUrl(final String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public FulfillmentPartnerBuilder withCountry(final List<String> country) {
            this.country = country;
            return this;
        }

        public FulfillmentPartnerBuilder withActive(final Boolean active) {
            this.active = active;
            return this;
        }

        public FulfillmentPartner build() {
            return new FulfillmentPartner(this);
        }

    }
}
