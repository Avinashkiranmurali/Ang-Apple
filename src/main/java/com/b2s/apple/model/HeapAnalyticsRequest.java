package com.b2s.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

public class HeapAnalyticsRequest {
    @JsonProperty("app_id")
    private final String appId;
    private final String identity;
    private final String event;
    private final Map<String, Object> properties;

    private HeapAnalyticsRequest(final Builder builder) {
        this.appId = Optionals.checkPresent(builder.appId, "appId");
        this.identity = Optionals.checkPresent(builder.identity, "identity");
        this.event = Optionals.checkPresent(builder.event, "event");
        this.properties = builder.properties;
    }

    public String getAppId() {
        return appId;
    }

    public String getIdentity() {
        return identity;
    }

    public String getEvent() {
        return event;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Optional<String> appId;
        private Optional<String> identity;
        private Optional<String> event;
        private Map<String, Object> properties = Map.of();

        private Builder() {
        }

        public Builder withAppId(final String appId) {
            this.appId = Optionals.from(appId);
            return this;
        }

        public Builder withIdentity(final String identity) {
            this.identity = Optionals.from(identity);
            return this;
        }

        public Builder withEvent(final String event) {
            this.event = Optionals.from(event);
            return this;
        }

        public Builder withProperties(final Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public HeapAnalyticsRequest build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new HeapAnalyticsRequest(this));
        }
    }
}
