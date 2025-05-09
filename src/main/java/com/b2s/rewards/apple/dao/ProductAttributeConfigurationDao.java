package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.ProductAttributeConfiguration;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by ssrinivasan on 6/29/2015.
 */
@Repository("productAttributeConfigurationDao")
@Transactional
public interface ProductAttributeConfigurationDao extends BaseDao<ProductAttributeConfiguration,Long> {

    List<ProductAttributeConfiguration> findByCategoryConfiguration(CategoryConfiguration categoryConfiguration);

    List<ProductAttributeConfiguration> findByCategoryConfigurationAndCategorySlugNullAndDetails(CategoryConfiguration categoryConfiguration);

}
