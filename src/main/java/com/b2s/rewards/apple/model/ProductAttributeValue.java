package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rpillai on 10/13/2015.
 */
@Entity
@Table(name="product_attribute_value")
public class ProductAttributeValue implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_configuration_id", nullable = false)
    private ProductAttributeConfiguration productAttributeConfiguration;

    @Column(name = "locale")
    private String locale;

    @Column(name = "attribute_value")
    private String attributeValue;

    @Column(name = "order_by")
    private Integer orderBy;

    @Column(name = "attribute_i18n_value")
    private String attributei18nValue;

    @Column(name = "attribute_image_url")
    private String attributeImageUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProductAttributeConfiguration getProductAttributeConfiguration() {
        return productAttributeConfiguration;
    }

    public void setProductAttributeConfiguration(ProductAttributeConfiguration productAttributeConfiguration) {
        this.productAttributeConfiguration = productAttributeConfiguration;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributei18nValue() {
        return attributei18nValue;
    }

    public void setAttributei18nValue(String attributei18nValue) {
        this.attributei18nValue = attributei18nValue;
    }

    public String getAttributeImageUrl() {
        return attributeImageUrl;
    }

    public void setAttributeImageUrl(String attributeImageUrl) {
        this.attributeImageUrl = attributeImageUrl;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Integer orderBy) {
        this.orderBy = orderBy;
    }
}
