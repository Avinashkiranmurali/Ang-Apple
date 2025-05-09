package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.common.util.CommonConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by meddy on 7/2/2015.
 */
@Repository("categoryConfigurationDao")
public class CategoryConfigurationDaoImpl extends BaseDaoWrapper<CategoryConfiguration, Long> implements CategoryConfigurationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryConfigurationDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    /**
     * Get category configuration by category name
     *
     * @param categoryName
     * @return
     */
    @Override
    @Cacheable(value=CommonConstants.CACHE_CATEGORY_CONF_NAME, key="#categoryName")
    public CategoryConfiguration getCategoryConfigurationByName(String categoryName) {
        Optional<CategoryConfiguration> result = null ;
        try {
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<CategoryConfiguration> criteriaQuery = criteriaBuilder.createQuery(CategoryConfiguration.class);
            Root<CategoryConfiguration> rootClass = criteriaQuery.from(CategoryConfiguration.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(rootClass.get("categoryName"), categoryName));
            predicates.add(criteriaBuilder.equal(rootClass.get("isActive"), true));
            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);

            criteriaQuery.select(rootClass).where(predicateArray);
             result =
                 session.createQuery(criteriaQuery).setHint("org.hibernate.cacheable", true).uniqueResultOptional();


        } catch (Exception e) {
            LOGGER.error("Error occurred while retrieving CategoryConfiguration information ", e);
        }

        return result.orElse(null);
    }

    /**
     * Get all category configuration objects
     *
     * @return
     */
    @Override
    public List<CategoryConfiguration> getAllCategoryConfiguration() {
        try {
            return getAll(-1);
        } catch (Exception e) {
            LOGGER.error("Error occurred while retrieving CategoryConfiguration ", e);
            return null;
        }
    }

}
