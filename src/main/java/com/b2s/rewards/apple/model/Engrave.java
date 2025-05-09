package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.util.AppleUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rperumal on 6/5/2015.
 */
public class Engrave implements Serializable {

    private static final long serialVersionUID = 6887644810522266775L;
    private String line1;
    private String line2;
    private String font;
    private String fontCode;
    private String maxCharsPerLine;
    private String widthDimension;
    private int noOfLines;
    private String engraveBgImageLocation;
    private boolean isSkuBasedEngraving;
    private String templateClass;
    private List<EngraveFontConfiguration> engraveFontConfigurations;
    private boolean isPreview;
    private String previewUrl;
    private boolean isDefaultPreviewEnabled;
    private boolean isUpperCaseEnabled;

    //Required for JSON deserialization
    public Engrave() {
    }

    public Engrave(String line1Message, String line2Message) {
        this.line1 = line1Message;
        this.line2 = line2Message;
    }

    public String getLine1() {
        return (!StringUtils.isEmpty(line1)? line1 : "" );
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return (!StringUtils.isEmpty(line2)? line2 : "") ;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getFontCode() {
        return AppleUtil.replaceNull(fontCode);
    }

    public void setFontCode(String engraveCode) {
        this.fontCode = engraveCode;
    }

    public String getFont() {
        return AppleUtil.replaceNull(font);
    }

    public void setFont(String font) {
        this.font = font;
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

    public Boolean hasEngraveMessage() {
        return (!StringUtils.isEmpty(getLine1()) || !StringUtils.isEmpty(getLine2()));
    }

    public int getNoOfLines() {
        return noOfLines;
    }

    public void setNoOfLines(final int noOfLines) {
        this.noOfLines = noOfLines;
    }

    public String getEngraveBgImageLocation() {
        return engraveBgImageLocation;
    }

    public void setEngraveBgImageLocation(final String engraveBgImageLocation) {
        this.engraveBgImageLocation = engraveBgImageLocation;
    }

    public boolean getIsSkuBasedEngraving() {
        return isSkuBasedEngraving;
    }

    public void setIsSkuBasedEngraving(final boolean isSkuBasedEngraving) {
        this.isSkuBasedEngraving = isSkuBasedEngraving;
    }

    public String getTemplateClass() {
        return templateClass;
    }

    public void setTemplateClass(final String templateClass) {
        this.templateClass = templateClass;
    }

    public List<EngraveFontConfiguration> getEngraveFontConfigurations() {
        return engraveFontConfigurations;
    }

    public void setEngraveFontConfigurations(
        final List<EngraveFontConfiguration> engraveFontConfigurations) {
        this.engraveFontConfigurations = engraveFontConfigurations;
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

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(final String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
