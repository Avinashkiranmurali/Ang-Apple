package com.b2s.common.services.util;

import com.b2s.rewards.apple.model.CategoryPrice;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rpillai on 8/1/2016.
 */
@Component
public class CategoryPriceHolder {

    private static final Map<String, CategoryPrice> CATEGORY_PRICE_MAP = new ConcurrentHashMap<>();

    public CategoryPrice getCategoryPrice(String categoryName) {
        return CATEGORY_PRICE_MAP.get(categoryName);
    }

    public void addCategoryPrice(String categoryName, CategoryPrice categoryPrice) {
        CATEGORY_PRICE_MAP.put(categoryName, categoryPrice);
    }

    public Collection<CategoryPrice> getCategoryPrices() {
        if(MapUtils.isNotEmpty(CATEGORY_PRICE_MAP)) {
            return CATEGORY_PRICE_MAP.values();
        }
        return null;
    }
}
