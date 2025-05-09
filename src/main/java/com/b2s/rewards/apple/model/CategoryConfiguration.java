package com.b2s.rewards.apple.model;

import com.b2s.rewards.common.util.CommonConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by meddy on 7/2/2015.
 */
@Entity
@Table(name="category_configuration")
public class CategoryConfiguration implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "template")
    private String template;

    @Column(name = "default_template")
    private String defaultTemplate;

    @Column(name = "is_configurable")
    private boolean isConfigurable;

    @Column(name = "order_by")
    private int orderBy;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_multiline_engravable")
    private boolean isMultilineEngravable;

    @Column(name = "summary_image_icon_url")
    private String summaryImageIconUrl;

    @Column(name = "default_product_image")
    private String defaultProductImage;

    @Column(name = "brief_description")
    private String briefDescription;

    @Column(name = "engrave_bg_image_url")
    private String engraveBgImageLocation;

    @Column(name = "deep_link_url")
    private String deepLinkUrl;

    @Column(name = "is_new")
    private boolean isNew;

    @Column(name = "psid")
    private String psid;

    /**
     * The addition of this column deprecates isMultilineEngravable since
     * the UI looks at noOfLines value in EngraveConfiguration
     */
    @Column(name = "is_engravable")
    private boolean isEngravable;

    @Column(name = "is_active")
    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }

    public Set<ProductAttributeConfiguration> getProductAttributes() {
        return productAttributes;
    }

    public void setProductAttributes(
        final Set<ProductAttributeConfiguration> productAttributes) {
        this.productAttributes = productAttributes;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "categoryConfiguration", cascade = CascadeType.ALL)
    private Set<ProductAttributeConfiguration> productAttributes;

    public CategoryConfiguration(){}

    public CategoryConfiguration(int id, String categoryName){
        this.id = id;
        this.categoryName = categoryName;
    }

    @Override
    public String toString(){
        return String.format("Category Configurations[id=%d, category_name=%s," +
            "template=%s, default_template=%s",id,categoryName,template,defaultTemplate);
    }

    public Integer getId() {
        return id;
    }


    public String getCategoryName() {
        if (categoryName.equals(CommonConstants.DEFAULT_VAR_PROGRAM)) {
            return psid;
        }
        return categoryName;
    }

    public void setCategoryName(final String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(final String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public boolean isConfigurable() {
        return isConfigurable;
    }

    public void setIsConfigurable(final boolean isConfigurable) {
        this.isConfigurable = isConfigurable;
    }

    public int getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final int orderBy) {
        this.orderBy = orderBy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Deprecated
    public boolean isMultilineEngravable() {
        return isMultilineEngravable;
    }

    public void setIsMultilineEngravable(final boolean isMultilineEngravable) {
        this.isMultilineEngravable = isMultilineEngravable;
    }

    public String getSummaryImageIconUrl() {
        return summaryImageIconUrl;
    }

    public void setSummaryImageIconUrl(final String summaryImageIconUrl) {
        this.summaryImageIconUrl = summaryImageIconUrl;
    }

    public String getDefaultProductImage() {
        return defaultProductImage;
    }

    public void setDefaultProductImage(final String defaultProductImage) {
        this.defaultProductImage = defaultProductImage;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(final String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getEngraveBgImageLocation() {
        return engraveBgImageLocation;
    }

    public void setEngraveBgImageLocation(String engraveBgImageLocation) {
        this.engraveBgImageLocation = engraveBgImageLocation;
    }

    public String getDeepLinkUrl() {
        return deepLinkUrl;
    }

    public void setDeepLinkUrl(String deepLinkUrl) {
        this.deepLinkUrl = deepLinkUrl;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(final boolean isNew) {
        this.isNew = isNew;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(final String psid) {
        this.psid = psid;
    }

    public boolean isEngravable() {
        return isEngravable;
    }

    public void setEngravable(boolean engravable) {
        isEngravable = engravable;
    }
}
