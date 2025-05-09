package com.b2s.apple.entity;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Objects;

@Table(name = "search_redirect")
@Entity
public class SearchRedirectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "catalog_id")
    private String catalogId;

    @Column(name = "search_keyword")
    private String searchKeyword;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "value")
    private String value;

    @Column(name = "active")
    private boolean active;

    public BigInteger getId() {
        return id;
    }

    public void setId(final BigInteger id) {
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

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(final String catalogId) {
        this.catalogId = catalogId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(final String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchRedirectEntity)) {
            return false;
        }
        SearchRedirectEntity that = (SearchRedirectEntity) o;
        return Objects.equals(getVarId(), that.getVarId()) &&
            Objects.equals(getProgramId(), that.getProgramId()) &&
            Objects.equals(getCatalogId(), that.getCatalogId()) &&
            Objects.equals(getSearchKeyword(), that.getSearchKeyword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVarId(), getProgramId(), getCatalogId(), getSearchKeyword());
    }
}