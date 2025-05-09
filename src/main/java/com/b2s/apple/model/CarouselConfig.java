package com.b2s.apple.model;

import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.CarouselType;

public class CarouselConfig {
    private final CarouselType type;
    private final String page;
    private final List<String> displayPages;
    private final Integer maxProductCount;
    private final String templateName;
    private final List<String> programExclusion;

    private CarouselConfig(final Builder builder) {
        this.type = builder.type;
        this.page = builder.page;
        this.displayPages = builder.displayPages;
        this.maxProductCount = builder.maxProductCount;
        this.templateName = builder.templateName;
        this.programExclusion = builder.programExclusion;
    }

    public CarouselType getType() {
        return type;
    }

    public String getPage() {
        return page;
    }

    public List<String> getDisplayPages() {
        return displayPages;
    }

    public Integer getMaxProductCount() {
        return maxProductCount;
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<String> getProgramExclusion() {
        return programExclusion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CarouselType type;
        private String page;
        private List<String> displayPages;
        private Integer maxProductCount;
        private String templateName;
        private List<String> programExclusion;

        public Builder withType(final CarouselType type) {
            this.type = type;
            return this;
        }

        public Builder withPage(final String page) {
            this.page = page;
            return this;
        }

        public Builder withDisplayPages(final List<String> displayPages) {
            this.displayPages = displayPages;
            return this;
        }

        public Builder withMaxProductCount(final Integer maxProductCount) {
            this.maxProductCount = maxProductCount;
            return this;
        }

        public Builder withTemplateName(final String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder withProgramExclusion(final List<String> programExclusion) {
            this.programExclusion = programExclusion;
            return this;
        }

        public CarouselConfig build() {
            return new CarouselConfig(this);
        }
    }

    @Override
    public String toString() {
        return "CarouselConfig{" +
            "type=" + type +
            ", page='" + page + '\'' +
            ", displayPages=" + displayPages +
            ", maxProductCount=" + maxProductCount +
            ", templateName='" + templateName + '\'' +
            ", programExclusion=" + programExclusion +
            '}';
    }
}
