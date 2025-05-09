package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="product_attribute_configuration")
public class ProductAttributeConfiguration implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "attribute_type")
    private String attributeType;

    @Column(name = "attribute_name")
    private String attributeName;

    @Column(name = "available_for_search")
    private boolean availableForSearch;

    @Column(name = "available_for_detail")
    private boolean availableForDetail;

    @Column(name = "lastupdate_time")
    private Date lastUpdatedTimestamp;

    @Column(name = "lastupdate_user")
    private String lastUpdatedUser;

    @Column(name = "order_by")
    private Integer orderBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryConfiguration categoryConfiguration;

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final Integer orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(final String attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public boolean isAvailableForSearch() {
        return availableForSearch;

    }

    public void setAvailableForSearch(final boolean availableForSearch) {
        this.availableForSearch = availableForSearch;
    }

    public boolean isAvailableForDetail() {
        return availableForDetail;
    }

    public void setAvailableForDetail(final boolean availableForDetail) {
        this.availableForDetail = availableForDetail;
    }


    public Date getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(String lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public CategoryConfiguration getCategoryConfiguration() {
        return categoryConfiguration;
    }

    public void setCategoryConfiguration(CategoryConfiguration categoryConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
    }
}