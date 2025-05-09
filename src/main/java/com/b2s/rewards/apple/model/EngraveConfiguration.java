package com.b2s.rewards.apple.model;

import com.b2s.apple.entity.EngraveFontConfigurationEntity;
import com.b2s.rewards.apple.util.AppleUtil;

import javax.persistence.*;
import java.util.List;

/**
 * Created by rperumal on 7/15/2015.
 */

@Entity
@Table(name="engrave_configuration")
public class EngraveConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_slug_id")
    private Integer categorySlugId;

    @Column(name = "model")
    private String model;

    @Column(name = "country")
    private String country;

    @Column(name = "locale")
    private String locale;

    @Column(name = "font")
    private String font;

    @Column(name = "font_code")
    private String fontCode;

    @Column(name = "max_chars_per_line")
    private String maxCharsPerLine;

    @Column(name = "width_dimension")
    private String widthDimension;

    @Column(name = "no_of_lines")
    private int noOfLines;

    @Column(name = "template_class")
    private String templateClass;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_preview")
    private boolean isPreview;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "is_default_preview_enabled")
    private boolean isDefaultPreviewEnabled;

    @Column(name = "is_upper_case_enabled")
    private boolean isUpperCaseEnabled;


    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "engrave_config_id" , insertable = false, updatable = false)
    private List<EngraveFontConfigurationEntity> engraveFontConfigurations;

    public List<EngraveFontConfigurationEntity> getEngraveFontConfigurations() {
        return engraveFontConfigurations;
    }

    public void setEngraveFontConfigurations(
        final List<EngraveFontConfigurationEntity> engraveFontConfigurations) {
        this.engraveFontConfigurations = engraveFontConfigurations;
    }

    public Integer getCategorySlugId() {
        return categorySlugId;
    }

    public void setCategorySlugId(Integer categorySlugId) {
        this.categorySlugId = categorySlugId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCountry() {
        return AppleUtil.replaceNull(country);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocale() {
        return AppleUtil.replaceNull(locale);
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFont() {
        return AppleUtil.replaceNull(font);
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getFontCode() {
        return AppleUtil.replaceNull(fontCode);
    }

    public void setFontCode(String fontCode) {
        this.fontCode = fontCode;
    }

    public String getMaxCharsPerLine() {
        return AppleUtil.replaceNull(maxCharsPerLine);
    }

    public void setMaxCharsPerLine(String maxCharsPerLine) {
        this.maxCharsPerLine = maxCharsPerLine;
    }

    public String getWidthDimension() {
        return AppleUtil.replaceNull(widthDimension);
    }

    public void setWidthDimension(String widthDimension) {
        this.widthDimension = widthDimension;
    }

    public int getNoOfLines() {
        return noOfLines;
    }

    public void setNoOfLines(final int noOfLines) {
        this.noOfLines = noOfLines;
    }

    public String getTemplateClass() {
        return templateClass;
    }

    public void setTemplateClass(final String templateClass) {
        this.templateClass = templateClass;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(final String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public boolean getIsPreview() {
        return isPreview;
    }

    public void setIsPreview(final boolean preview) {
        isPreview = preview;
    }

    public boolean getIsDefaultPreviewEnabled() {
        return isDefaultPreviewEnabled;
    }

    public void setIsDefaultPreviewEnabled(final boolean defaultPreviewEnabled) {
        isDefaultPreviewEnabled = defaultPreviewEnabled;
    }

    public boolean getIsUpperCaseEnabled() {
        return isUpperCaseEnabled;
    }

    public void setIsUpperCaseEnabled(final boolean upperCaseEnabled) {
        isUpperCaseEnabled = upperCaseEnabled;
    }
}