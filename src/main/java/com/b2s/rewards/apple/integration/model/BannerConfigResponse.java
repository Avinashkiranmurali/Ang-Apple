package com.b2s.rewards.apple.integration.model;

import java.util.Map;

public class BannerConfigResponse {
    private final String categoryId;
    private final String categoryName;
    private final String bannerType;
    private final Map<String, Object> config;

    private BannerConfigResponse(Builder builder) {
        this.categoryId = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.config = builder.config;
        this.bannerType = builder.bannerType;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public String getBannerType() {
        return bannerType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String categoryId;
        private String categoryName;
        private Map<String, Object> config;
        private String bannerType;

        public Builder withCategoryId(final String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder withCategoryName(final String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder withConfig(final Map<String, Object> config) {
            this.config = config;
            return this;
        }

        public Builder withBannerType(final String bannerType) {
            this.bannerType = bannerType;
            return this;
        }

        public BannerConfigResponse build() {
            return new BannerConfigResponse(this);
        }
    }

    @Override
    public String toString() {
        return "BannerConfigResponse{" +
            "categoryId='" + categoryId + '\'' +
            ", categoryName='" + categoryName + '\'' +
            ", config=" + config +
            ", bannerType=" + bannerType +
            '}';
    }
}
