package com.b2s.apple.entity;

import javax.persistence.*;

@Entity
@Table(name="engrave_font_configuration")
public class EngraveFontConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "engrave_config_id")
    private Integer engraveConfigId;

    @Column(name = "char_length_from")
    private Integer charLengthFrom;

    @Column(name = "char_length_to")
    private Integer charLengthTo;

    @Column(name = "font_code")
    private String fontCode;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

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
}
