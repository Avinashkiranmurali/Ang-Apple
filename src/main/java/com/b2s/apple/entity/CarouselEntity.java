package com.b2s.apple.entity;

import com.b2s.rewards.apple.model.VarProgramIf;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "carousel")
public class CarouselEntity implements VarProgramIf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "type")
    private String type;
    @Column(name = "display_pages")
    private String displayPages;
    @Column(name = "program_exclusion")
    private String programExclusion;
    @Column(name = "template_name")
    private String templateName;
    @Column(name = "max_product_count")
    private Integer maxProductCount;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_date")
    private Date modifiedDate;

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

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDisplayPages() {
        return displayPages;
    }

    public void setDisplayPages(final String displayPages) {
        this.displayPages = displayPages;
    }

    public String getProgramExclusion() {
        return programExclusion;
    }

    public void setProgramExclusion(final String programExclusion) {
        this.programExclusion = programExclusion;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public Integer getMaxProductCount() {
        return maxProductCount;
    }

    public void setMaxProductCount(final Integer maxProductCount) {
        this.maxProductCount = maxProductCount;
    }

    public boolean getIsActive() {
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