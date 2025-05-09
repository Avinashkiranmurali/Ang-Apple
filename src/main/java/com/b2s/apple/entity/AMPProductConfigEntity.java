package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author ssundaramoorthy Date : 7/7/2021 Time : 08:25 PM
 */

@Entity
@Table(name = "amp_product_configuration")
public class AMPProductConfigEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "var_id", nullable = false)
    private String varId;

    @Column(name = "program_id", nullable = false)
    private String programId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "use_static_link")
    private boolean useStaticLink;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "duration")
    private Integer duration;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(final Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean getUseStaticLink() {
        return useStaticLink;
    }

    public void setUseStaticLink(final boolean useStaticLink) {
        this.useStaticLink = useStaticLink;
    }

    public boolean getIsActive() { return isActive; }

    public void setIsActive(final boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getDuration() { return duration; }

    public void setDuration(final Integer duration) { this.duration = duration; }
}