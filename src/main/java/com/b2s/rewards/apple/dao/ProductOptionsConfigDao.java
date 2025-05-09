package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.ProductOptionsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProductOptionsConfigDao extends JpaRepository<ProductOptionsConfig, Long> {

    ProductOptionsConfig findByLocaleAndPsidAndIsActiveTrue(final String locale, final String psid);

    default ProductOptionsConfig getConfigBasedOnPsid(final String locale, final String psid) {
        return findByLocaleAndPsidAndIsActiveTrue(locale, psid);
    }

    List<ProductOptionsConfig> findByLocaleAndCategoryNameAndIsActiveTrue(final String locale,
        final String categoryName);

    default List<ProductOptionsConfig> getConfigBasedOnCategoryName(final String locale, final String categoryName) {
        return findByLocaleAndCategoryNameAndIsActiveTrue(locale, categoryName);
    }
}
