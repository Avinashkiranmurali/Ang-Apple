package com.b2s.rewards.apple.model;

import javax.persistence.*;

@Entity
@Table(name = "product_options_config")
public class ProductOptionsConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "locale")
    private String locale;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "psid")
    private String psid;

    @Column(name = "options")
    private String options;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "is_active")
    private boolean isActive;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(final String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(final String psid) {
        this.psid = psid;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(final String options) {
        this.options = options;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }
}