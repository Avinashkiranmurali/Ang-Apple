package com.b2s.rewards.apple.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductsRequest {
    private Set<String> categorySlugs = new HashSet<>();
    private Integer minPoints;
    private Integer maxPoints;
    private Integer pageSize;
    private Integer resultOffSet;
    private String keyword;
    private String promoTag;
    private String sort;
    private Long pgId;
    private boolean withVariations;
    private String order;
    private Map<String, List<Option>> facetsFilters;
    private boolean withFacets=true;
    private boolean withProducts=true;

    public Set<String> getCategorySlugs() {
        return categorySlugs;
    }

    public Integer getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(final Integer minPoints) {
        this.minPoints = minPoints;
    }

    public Integer getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(final Integer maxPoints) {
        this.maxPoints = maxPoints;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getResultOffSet() {
        return resultOffSet;
    }

    public void setResultOffSet(final Integer resultOffSet) {
        this.resultOffSet = resultOffSet;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(final String keyword) {
        this.keyword = keyword;
    }

    public String getPromoTag() {
        return promoTag;
    }

    public void setPromoTag(final String promoTag) {
        this.promoTag = promoTag;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

    public Long getPgId() {
        return pgId;
    }

    public void setPgId(final Long pgId) {
        this.pgId = pgId;
    }

    public boolean isWithVariations() {
        return withVariations;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(final String order) {
        this.order = order;
    }

    public Map<String, List<Option>> getFacetsFilters() {
        return facetsFilters;
    }

    public boolean isWithFacets() {
        return withFacets;
    }

    public boolean isWithProducts() { return withProducts; }
}
