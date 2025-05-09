package com.b2s.rewards.apple.model;

import java.io.Serializable;

public class EngraveFontConfiguration implements Serializable {

    private static final long serialVersionUID = -7897673973376945405L;

    private Integer engraveConfigId;

    private Integer charLengthFrom;

    private Integer charLengthTo;

    private String fontCode;

    public Integer getEngraveConfigId() {
        return engraveConfigId;
    }

    public void setEngraveConfigId(final Integer engraveConfigId) {
        this.engraveConfigId = engraveConfigId;
    }

    public Integer getCharLengthFrom() {
        return charLengthFrom;
    }

    public void setCharLengthFrom(final Integer charLengthFrom) {
        this.charLengthFrom = charLengthFrom;
    }

    public Integer getCharLengthTo() {
        return charLengthTo;
    }

    public void setCharLengthTo(final Integer charLengthTo) {
        this.charLengthTo = charLengthTo;
    }

    public String getFontCode() {
        return fontCode;
    }

    public void setFontCode(final String fontCode) {
        this.fontCode = fontCode;
    }

    @Override
    public String toString() {
        return "EngraveFontConfiguration{" +
            "engraveConfigId=" + engraveConfigId +
            ", charLengthFrom=" + charLengthFrom +
            ", charLengthTo=" + charLengthTo +
            ", fontCode='" + fontCode + '\'' +
            '}';
    }
}
