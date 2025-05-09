package com.b2s.apple.entity;

import com.b2s.rewards.apple.model.VarProgramLocaleIf;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "banner_configuration")
@Entity
public class BannerConfigEntity implements Serializable, VarProgramLocaleIf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long configId;
    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "locale")
    private String locale;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "config_type")
    private String configType;
    @Column(name = "category")
    private String category;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "banner_id")
    })
    private BannerEntity bannerEntity;


    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(final Long configId) {
        this.configId = configId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }

    public String getConfigType() { return configType; }

    public void setConfigType(final String configType) { this.configType = configType; }

    public String getCategory() { return category; }

    public void setCategory(final String category) { this.category = category; }

    public BannerEntity getBannerEntity() {
        return bannerEntity;
    }

    public void setBannerEntity(final BannerEntity bannerEntity) {
        this.bannerEntity = bannerEntity;
    }
}
