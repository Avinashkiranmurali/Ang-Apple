package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by meddy on 7/2/2015.
 */

@Transactional
public interface CategoryConfigurationDao extends BaseDao<CategoryConfiguration, Long> {

    List<CategoryConfiguration> getAllCategoryConfiguration();

    CategoryConfiguration getCategoryConfigurationByName(String categoryName);

}
