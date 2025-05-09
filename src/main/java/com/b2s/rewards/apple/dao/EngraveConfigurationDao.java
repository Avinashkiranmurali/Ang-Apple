package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.EngraveConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rperumal on 7/15/2015.
 */
@Repository
@Transactional
public interface EngraveConfigurationDao extends JpaRepository<EngraveConfiguration, Long> {

    EngraveConfiguration findByLocaleAndCategorySlugId(final String locale, final Integer categorySlugId);

    default EngraveConfiguration getByLocale(final String userLocale, final Integer categorySlugId){
        return findByLocaleAndCategorySlugId(userLocale, categorySlugId);
    }
}