package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Table(name = "quick_link")
@Entity
public class QuickLinkEntity {

    @EmbeddedId
    private QuickLinkId quickLinkId;

    @Column(name = "link_text")
    private String linkText;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "priority")
    private int priority;

    @Column(name = "show_unauthenticated")
    private boolean showUnauthenticated;

    @Column(name = "display")
    private boolean display;

    public QuickLinkId getQuickLinkId() {
        return quickLinkId;
    }

    public void setQuickLinkId(QuickLinkId quickLinkId) {
        this.quickLinkId = quickLinkId;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isShowUnauthenticated() {
        return showUnauthenticated;
    }

    public void setShowUnauthenticated(boolean showUnauthenticated) {
        this.showUnauthenticated = showUnauthenticated;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    @Embeddable
    public static class QuickLinkId implements Serializable {
        @Column(name = "locale")
        private String locale;

        @Column(name = "var_id")
        private String varId;

        @Column(name = "program_id")
        private String programId;

        @Column(name = "link_code")
        private String linkCode;

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getVarId() {
            return varId;
        }

        public void setVarId(String varId) {
            this.varId = varId;
        }

        public String getProgramId() {
            return programId;
        }

        public void setProgramId(String programId) {
            this.programId = programId;
        }

        public String getLinkCode() {
            return linkCode;
        }

        public void setLinkCode(String linkCode) {
            this.linkCode = linkCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuickLinkId)) return false;
            QuickLinkId that = (QuickLinkId) o;
            return Objects.equals(getLocale(), that.getLocale()) &&
                    Objects.equals(getVarId(), that.getVarId()) &&
                    Objects.equals(getProgramId(), that.getProgramId()) &&
                    Objects.equals(getLinkCode(), that.getLinkCode());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLocale(), getVarId(), getProgramId(), getLinkCode());
        }
    }
}


