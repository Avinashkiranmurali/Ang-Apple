package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CategorizationDTO {
    private final Optional<String> name;
    private final String slug;
    private final int depth;

    private CategorizationDTO(final Builder builder) {
        this.name = builder.name;
        this.slug = Optionals.checkPresent(builder.slug, "slug");
        this.depth = Optionals.checkPresent(builder.depth, "depth");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CategorizationDTO create(
        @JsonProperty("name") final String name,
        @JsonProperty("slug") String slug,
        @JsonProperty("depth") int depth) {
        return builder()
            .withName(name)
            .withSlug(slug)
            .withDepth(depth)
            .build();
    }

    public Optional<String> getName() {
        return this.name;
    }

    public String getSlug() {
        return this.slug;
    }

    public int getDepth() {
        return this.depth;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CategorizationDTO other = (CategorizationDTO) o;
        return Objects.equals(this.name, other.name)
            && Objects.equals(this.slug, other.slug)
            && Objects.equals(this.depth, other.depth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, slug, depth);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withName(name.orElse(null))
            .withSlug(slug)
            .withDepth(depth);
    }

    public static final class Builder {
        private Optional<String> name = Optional.empty();
        private Optional<String> slug = Optional.empty();
        private Optional<Integer> depth = Optional.empty();

        private Builder() {
        }

        public Builder withName(final String theName) {
            this.name = Optionals.from(theName);
            return this;
        }

        public Builder withSlug(final String theSlug) {
            this.slug = Optionals.from(theSlug);
            return this;
        }

        public Builder withDepth(final Integer theDepth) {
            this.depth = Optionals.from(theDepth);
            return this;
        }

        public CategorizationDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CategorizationDTO(this));
        }
    }
}
