package com.b2s.rewards.apple.model;


import java.util.List;
import java.util.Map;

/**
 * Created by rpillai on 4/1/2016.
 */
public class ProductResponse {

    private List<Product> products;

    private int totalFound;

    private List<CategoryPrice> categoryPrices;

    private Map<String,List<Option>> optionsConfigurationData;

    private Map<String,List<Option>> facetsFilters;

    private SearchRedirectModel searchRedirect;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }

    public Map<String, List<Option>> getOptionsConfigurationData() {
        return optionsConfigurationData;
    }

    public void setOptionsConfigurationData(Map<String, List<Option>> optionsConfigurationData) {
        this.optionsConfigurationData = optionsConfigurationData;
    }

    public List<CategoryPrice> getCategoryPrices() {
        return categoryPrices;
    }

    public void setCategoryPrices(List<CategoryPrice> categoryPrices) {
        this.categoryPrices = categoryPrices;
    }

    public Map<String, List<Option>> getFacetsFilters() {
        return facetsFilters;
    }

    public void setFacetsFilters(
        final Map<String, List<Option>> facetsFilters) {
        this.facetsFilters = facetsFilters;
    }

    public SearchRedirectModel getSearchRedirect() {
        return searchRedirect;
    }

    public void setSearchRedirect(final SearchRedirectModel searchRedirect) {
        this.searchRedirect = searchRedirect;
    }
}
