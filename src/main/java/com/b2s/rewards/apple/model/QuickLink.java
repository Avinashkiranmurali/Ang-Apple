package com.b2s.rewards.apple.model;

import com.b2s.service.utils.lang.Exceptions;

import java.util.Objects;

public class QuickLink {

    private final String locale;
    private final String varId;
    private final String programId;
    private final String linkCode;
    private final String linkText;
    private final String linkUrl;
    private final int order;
    private final boolean showUnauthenticated;
    private final boolean display;

    private QuickLink(final Builder builder) {
        this.locale = builder.locale;
        this.varId = builder.varId;
        this.programId = builder.programId;
        this.linkCode = builder.linkCode;
        this.linkText = builder.linkText;
        this.linkUrl = builder.linkUrl;
        this.order = builder.order;
        this.showUnauthenticated = builder.showUnauthenticated;
        this.display = builder.display;
    }

    public static Builder builder() {
        return new Builder();
    }


    public String getLocale() {
        return locale;
    }

    public String getVarId() {
        return varId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public String getLinkText() {
        return linkText;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public int getOrder() {
        return order;
    }

    public boolean isShowUnauthenticated() {
        return showUnauthenticated;
    }

    public boolean isDisplay() {
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuickLink)) return false;
        QuickLink quickLink = (QuickLink) o;
        return getOrder() == quickLink.getOrder() &&
                isShowUnauthenticated() == quickLink.isShowUnauthenticated() &&
                isDisplay() == quickLink.isDisplay() &&
                Objects.equals(getLocale(), quickLink.getLocale()) &&
                Objects.equals(getVarId(), quickLink.getVarId()) &&
                Objects.equals(getProgramId(), quickLink.getProgramId()) &&
                Objects.equals(getLinkCode(), quickLink.getLinkCode()) &&
                Objects.equals(getLinkText(), quickLink.getLinkText()) &&
                Objects.equals(getLinkUrl(), quickLink.getLinkUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocale(), getVarId(), getProgramId(), getLinkCode(), getLinkText(), getLinkUrl(), getOrder(), isShowUnauthenticated(), isDisplay());
    }

    public static final class Builder {
        private String locale;
        private String varId;
        private String programId;
        private String linkCode;
        private String linkText;
        private String linkUrl;
        private int order;
        private boolean showUnauthenticated;
        private boolean display;

        public Builder withLocale(final String theLocale) {
            this.locale = theLocale;
            return this;
        }

        public Builder withVarId(final String theVarId) {
            this.varId = theVarId;
            return this;
        }

        public Builder withProgramId(final String theProgramId) {
            this.programId = theProgramId;
            return this;
        }

        public Builder withLinkCode(final String theLinkCode) {
            this.linkCode = theLinkCode;
            return this;
        }

        public Builder withLinkText(final String theLinkText) {
            this.linkText = theLinkText;
            return this;
        }

        public Builder withLinkUrl(final String theLinkUrl) {
            this.linkUrl = theLinkUrl;
            return this;
        }

        public Builder withOrder(final Integer theOrder) {
            this.order = theOrder;
            return this;
        }

        public Builder withShowUnauthenticated(final Boolean theShowUnauthenticated) {
            this.showUnauthenticated = theShowUnauthenticated;
            return this;
        }

        public Builder withDisplay(final Boolean theDisplay) {
            this.display = theDisplay;
            return this;
        }

        public QuickLink build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new QuickLink(this));
        }
    }
}