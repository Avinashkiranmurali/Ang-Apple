package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "banner")
@Entity
public class BannerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long bannerId;
    @Column(name = "banner_type")
    private String bannerType;
    @Column(name = "page")
    private String page;
    @Column(name = "banner_name")
    private String bannerName;
    @Column(name = "display_order")
    private int displayOrder;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_date")
    private Date modifiedDate;

    public Long getBannerId() {
        return bannerId;
    }

    public void setBannerId(final Long bannerId) {
        this.bannerId = bannerId;
    }

    public String getBannerType() {
        return bannerType;
    }

    public void setBannerType(final String bannerType) {
        this.bannerType = bannerType;
    }

    public String getPage() {
        return page;
    }

    public void setPage(final String page) {
        this.page = page;
    }

    public String getBannerName() {
        return bannerName;
    }

    public void setBannerName(final String bannerName) {
        this.bannerName = bannerName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(final int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(final String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
