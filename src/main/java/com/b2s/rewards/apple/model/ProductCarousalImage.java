package com.b2s.rewards.apple.model;

import javax.persistence.*;

@Entity
@Table(name = "product_carousal_image")
public class ProductCarousalImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_order")
    private Integer imageOrder;

    @Column(name = "is_active")
    private boolean isActive;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getImageOrder() {
        return imageOrder;
    }

    public void setImageOrder(final Integer imageOrder) {
        this.imageOrder = imageOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }
}