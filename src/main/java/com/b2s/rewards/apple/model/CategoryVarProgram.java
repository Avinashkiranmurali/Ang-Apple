package com.b2s.rewards.apple.model;

import java.util.Objects;

/**
 * Created by rpillai on 1/13/2016.
 */
public class CategoryVarProgram {

    private String categoryName;
    private String varId;
    private String programId;
    private String templateType;
    private String defaultTemplateType;
    private boolean isConfigurable;
    private String defaultImage;
    private String imageUrl;
    private String summaryIconImage;
    private Integer displayOrder;

    private boolean isActive;
    private boolean isNew;

    public CategoryVarProgram() {}
    public CategoryVarProgram(String categoryName, String varId, String programId, String templateType, String defaultTemplateType) {
        this.categoryName = categoryName;
        this.varId = varId;
        this.programId = programId;
        this.templateType = templateType;
        this.defaultTemplateType = defaultTemplateType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getDefaultTemplateType() {
        return defaultTemplateType;
    }

    public void setDefaultTemplateType(String defaultTemplateType) {
        this.defaultTemplateType = defaultTemplateType;
    }

    public boolean isConfigurable() {
        return isConfigurable;
    }

    public void setIsConfigurable(boolean isConfigurable) {
        this.isConfigurable = isConfigurable;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public String getSummaryIconImage() {
        return summaryIconImage;
    }

    public void setSummaryIconImage(String summaryIconImage) {
        this.summaryIconImage = summaryIconImage;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryVarProgram that = (CategoryVarProgram) o;
        return Objects.equals(categoryName, that.categoryName) &&
                Objects.equals(varId, that.varId) &&
                Objects.equals(programId, that.programId) &&
                Objects.equals(templateType, that.templateType) &&
                Objects.equals(defaultTemplateType, that.defaultTemplateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, varId, programId, templateType, defaultTemplateType);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(final boolean isNew) {
        this.isNew = isNew;
    }
}
