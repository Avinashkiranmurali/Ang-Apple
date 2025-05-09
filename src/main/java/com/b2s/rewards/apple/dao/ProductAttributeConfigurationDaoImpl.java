package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.ProductAttributeConfiguration;
import com.b2s.rewards.common.util.CommonConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ssrinivasan on 6/29/2015.
 */
@Repository("productAttributeConfigurationDao")
@Transactional
public class ProductAttributeConfigurationDaoImpl extends BaseDaoWrapper<ProductAttributeConfiguration, Long>
    implements ProductAttributeConfigurationDao {
    public static final String CATEGORY_CONFIGURATION = "categoryConfiguration";

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    @Cacheable(value= CommonConstants.CACHE_PROD_ATTRIBUTE_CONF, key="#categoryConfiguration.id")
    public List<ProductAttributeConfiguration> findByCategoryConfiguration(CategoryConfiguration categoryConfiguration) throws
        DataAccessException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ProductAttributeConfiguration> criteriaQuery = criteriaBuilder.createQuery(ProductAttributeConfiguration.class);
        Root<ProductAttributeConfiguration> rootClass = criteriaQuery.from(ProductAttributeConfiguration.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(rootClass.get(CATEGORY_CONFIGURATION).get("id"), categoryConfiguration.getId()));
        criteriaQuery.select(rootClass).where(predicates.toArray(new Predicate[0]));
        return session.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public List<ProductAttributeConfiguration> findByCategoryConfigurationAndCategorySlugNullAndDetails(CategoryConfiguration categoryConfiguration)
        throws DataAccessException{
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ProductAttributeConfiguration> criteriaQuery = criteriaBuilder.createQuery(ProductAttributeConfiguration.class);
        Root<ProductAttributeConfiguration> rootClass = criteriaQuery.from(ProductAttributeConfiguration.class);

        rootClass.join(CATEGORY_CONFIGURATION, JoinType.LEFT).alias("c");

        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(categoryConfiguration)) {
            predicates.add(criteriaBuilder.and(
                criteriaBuilder.or(
                    rootClass.get(CATEGORY_CONFIGURATION).isNull(),
                    criteriaBuilder.equal(rootClass.get(CATEGORY_CONFIGURATION).get("id"), categoryConfiguration.getId())
                ),
                criteriaBuilder.equal(rootClass.get("attributeType"), "additionalInfo"),
                criteriaBuilder.equal(rootClass.get("availableForDetail"), true)
            ));
        } else {
            predicates.add(criteriaBuilder.and(
                rootClass.get(CATEGORY_CONFIGURATION).isNull(),
                criteriaBuilder.equal(rootClass.get("availableForDetail"), true)
            ));
        }

        criteriaQuery.select(rootClass).where(predicates.toArray(new Predicate[0]));
        return session.createQuery(criteriaQuery).getResultList();
    }

}