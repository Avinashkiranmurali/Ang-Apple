package com.b2s.rewards.apple.model;


import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Category Object passed as JSON to UI, translated from the product service
 * and Pricing Service Responses.
 */
public class Category implements Serializable {

    private static final long serialVersionUID = -4372852267042799117L;
    private String imageUrl = "";
    private String i18nName = "";
    private String slug = "";
    private String name = "";
    private boolean isConfigurable;
    private String templateType;
    private String defaultImage;
    private String summaryIconImage;
    private Integer displayOrder;
    private String engraveBgImageLocation;
    private boolean isMultilineEngravable;
    private boolean isActive;
    private List<Category> subCategories = new ArrayList<>();
    private List<Category> parents = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private Map<String,String> images = new HashMap<>();
    private String detailUrl;
    private boolean isNew;
    private String psid;
    private Integer depth;

    private transient List<CategoryVarProgram> categoryVarPrograms;

    private transient List<String> supportedLocales;

    public Category(Category category) {
        this.imageUrl = category.imageUrl;
        this.i18nName = category.i18nName;
        this.slug = category.slug;
        this.name = category.name;
        this.isConfigurable = category.isConfigurable;
        this.templateType = category.templateType;
        this.defaultImage = category.defaultImage;
        this.summaryIconImage = category.summaryIconImage;
        this.displayOrder = category.displayOrder;
        this.engraveBgImageLocation = category.engraveBgImageLocation;
        this.isMultilineEngravable = category.isMultilineEngravable;
        this.isActive = category.isActive;
        this.subCategories = category.subCategories;
        this.parents = category.parents;
        this.products = category.products;
        this.images = category.images;
        this.isNew = category.isNew;
        this.psid = category.psid;
        this.depth = category.depth;
        this.categoryVarPrograms = category.categoryVarPrograms;
        this.supportedLocales = category.supportedLocales;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public Category setImages(Map<String, String> images) {
        this.images = images;
        return this;
    }

    public Category(final String slug, final String name) {
        this.slug = slug;
        this.name = name;
    }

    public Category(final String slug) {
        this.slug = slug;
    }

    public Category() {}

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getI18nName() {
        return i18nName;
    }

    public void setI18nName(String i18nName) {
        this.i18nName = i18nName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConfigurable() {
        return isConfigurable;
    }

    public void setConfigurable(boolean isConfigurable) {
        this.isConfigurable = isConfigurable;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories;
    }

    public List<Category> getParents() {
        return (parents == null ? new ArrayList<Category>() : parents);
    }

    public void setParents(List<Category> parents) {
        this.parents = parents;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getTemplateType() {
        return (templateType == null? "" : templateType);
    }
    public String getDefaultImage(){
        return (defaultImage == null? "" : defaultImage);
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }



    public String getEngraveBgImageLocation() {
        return engraveBgImageLocation;
    }

    public void setEngraveBgImageLocation(String engraveBgImageLocation) {
        this.engraveBgImageLocation = engraveBgImageLocation;
    }

    public String getSummaryIconImage() {
        return summaryIconImage;
    }

    public void setSummaryIconImage(String summaryIconImage) {
        this.summaryIconImage = summaryIconImage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public static Category transformCloning(final Category fromCategory, final List<Category> parents){
        Category newCategory = new Category(fromCategory);
        newCategory.setParents(new ArrayList<>());
        if (Objects.nonNull(parents)) {
            // get child categories
            for (Category parentCategory : parents) {
                Category tCategory = transformCloning(parentCategory, parentCategory.getParents());
                tCategory.setSubCategories(new ArrayList<>());
                newCategory.getParents().add(tCategory);
            }
        }

        return newCategory;
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

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(final String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
