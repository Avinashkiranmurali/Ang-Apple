package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.util.Assert;

/**
 * Created by rpillai on 4/13/2018.
 */
@JsonDeserialize(builder = Catalog.Builder.class)
public class Catalog {

    private final String name;
    private final String psCatalogId;
    private final String promoTag;

    public String getName() {
        return name;
    }

    public String getPsCatalogId() {
        return psCatalogId;
    }

    public String getPromoTag() {
        return promoTag;
    }

    private Catalog(final Builder builder) {
        Assert.notNull(builder.name, "Name is required");
        this.name = builder.name;
        this.psCatalogId = builder.psCatalogId;
        this.promoTag = builder.promoTag;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String psCatalogId;
        private String promoTag;

        private Builder() {}

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }
        public Builder withPsCatalogId(final String psCatalogId) {
            this.psCatalogId = psCatalogId;
            return this;
        }
        public Builder withPromoTag(final String promoTag) {
            this.promoTag = promoTag;
            return this;
        }

        public Catalog build(){
            return new Catalog(this);
        }
    }
}
