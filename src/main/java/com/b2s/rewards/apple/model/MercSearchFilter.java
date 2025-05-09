package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by rperumal on 10/20/2015.
 */

@Entity
@Table(name="merc_search_filter")
public class MercSearchFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_filter_id", nullable = false)
    private Integer searchFilterId;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "filter_type")
    private String filterType;

    @Column(name = "filter_scope")
    private String filterScope;

    @Column(name = "filter_name")
    private String filterName;

    @Column(name = "filter_value")
    private String filterValue;

    @Column(name = "is_active")
    private String isActive;  // Y/N value

    @Column(name = "added_date")
    private Date addedDate;

    @Column(name = "added_by")
    private String addedBy;

    @Column(name = "comment")
    private String comment;

    public Integer getSearchFilterId() {
        return searchFilterId;
    }

    public void setSearchFilterId(Integer searchFilterId) {
        this.searchFilterId = searchFilterId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterScope() {
        return filterScope;
    }

    public void setFilterScope(String filterScope) {
        this.filterScope = filterScope;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}