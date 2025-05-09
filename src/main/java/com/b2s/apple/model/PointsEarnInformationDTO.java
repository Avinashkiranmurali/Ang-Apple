package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.money.BigMoney;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class PointsEarnInformationDTO {
    private final BigMoney earnRate;
    private final int unitPointsEarned;

    private PointsEarnInformationDTO(final Builder builder) {
        this.earnRate = Optionals.checkPresent(builder.earnRate, "earnRate");
        this.unitPointsEarned = Optionals.checkPresent(builder.unitPointsEarned, "unitPointsEarned");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static PointsEarnInformationDTO create(
        @JsonProperty("earnRate") final BigMoney theEarnRate,
        @JsonProperty("unitPointsEarned") final Integer theUnitPointsEarned) {
        return builder()
            .withEarnRate(theEarnRate)
            .withUnitPointsEarned(theUnitPointsEarned)
            .build();
    }

    public BigMoney getEarnRate() {
        return this.earnRate;
    }

    public int getUnitPointsEarned() {
        return this.unitPointsEarned;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final PointsEarnInformationDTO other = (PointsEarnInformationDTO) o;
        return Objects.equals(this.earnRate, other.earnRate)
            && Objects.equals(this.unitPointsEarned, other.unitPointsEarned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(earnRate, unitPointsEarned);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withEarnRate(earnRate)
            .withUnitPointsEarned(unitPointsEarned);
    }

    public static final class Builder {
        private Optional<BigMoney> earnRate = Optional.empty();
        private Optional<Integer> unitPointsEarned = Optional.empty();

        private Builder() {
        }

        public Builder withEarnRate(final BigMoney theEarnRate) {
            this.earnRate = Optionals.from(theEarnRate);
            return this;
        }

        public Builder withUnitPointsEarned(final Integer theUnitPointsEarned) {
            this.unitPointsEarned = Optionals.from(theUnitPointsEarned);
            return this;
        }

        public PointsEarnInformationDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new PointsEarnInformationDTO(this));
        }
    }
}
