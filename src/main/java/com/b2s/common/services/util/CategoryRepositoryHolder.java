package com.b2s.common.services.util;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rpillai on 10/12/2015.
 */
@Component
public class CategoryRepositoryHolder {

    private static final Map<Locale, CategoryRepository> CATEGORY_REPOSITORY_MAP = new ConcurrentHashMap<>();

    @Value("#{catalogDefaultVarIdMapping}")
    private Map<String, String> catalogDefaultVarIdMapping;

    public CategoryRepository getCategoryRepository(Locale locale) {
        return CATEGORY_REPOSITORY_MAP.get(locale);
    }

    public void addCategoryRepository(Locale locale, CategoryRepository categoryRepository) {
        CATEGORY_REPOSITORY_MAP.put(locale, categoryRepository);
    }

    public String getDefaultVarId(String catalogId) {
        String varId = null;
        if(MapUtils.isNotEmpty(catalogDefaultVarIdMapping)) {
            varId = catalogDefaultVarIdMapping.get(catalogId);
        }
        return varId;
    }

}
