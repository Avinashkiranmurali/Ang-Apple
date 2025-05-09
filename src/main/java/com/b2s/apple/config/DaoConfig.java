package com.b2s.apple.config;

import com.b2r.util.address.StreetAddressRule;
import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.entity.OrderLineEntity;
import com.b2s.apple.entity.OrderLineStatusEntity;
import com.b2s.apple.entity.StatusChangeQueueEntity;
import com.b2s.rewards.apple.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author rkumar 2020-01-24
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.b2s.rewards.apple.dao")
public class DaoConfig {

    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new ImprovedNamingStrategy();
    }

    @Bean("dataSource")
    public DataSource dataSource() {
        final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
        dsLookup.setResourceRef(true);
        return dsLookup.getDataSource("java:comp/env/jdbc/b2r");
    }

    @Bean("transactionManager")
    @Primary
    public HibernateTransactionManager transactionManager(
        @Autowired @Qualifier("sessionFactory") final SessionFactory sessionFactory,
        @Autowired @Qualifier("dataSource") final DataSource dataSource) {
        final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(
        @Autowired @Qualifier("transactionManager") final HibernateTransactionManager transactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        return transactionTemplate;
    }

    @Bean({"entityManagerFactory", "sessionFactory"})
    public LocalSessionFactoryBean sessionFactory(
        @Autowired @Qualifier("dataSource") final DataSource dataSource) {
        final String[] packageList = {"com.b2s.rewards.apple.model", "com.b2s.apple.entity"};
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(packageList);
        sessionFactory.setHibernateProperties(hibernateProperties());
        sessionFactory.setAnnotatedClasses(getAnnotatedClass());
        return sessionFactory;
    }

    /**
     * This is the bridge between JPA and Hibernate
     *
     * @param entityManagerFactory
     * @return
     */
    @Bean
    @Qualifier(value = "entityManager")
    public EntityManager entityManager(@Autowired @Qualifier("entityManagerFactory") final EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }
    @Bean("reportingDataSource")
    public DataSource reportingDataSource() {
        final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
        dsLookup.setResourceRef(true);
        return dsLookup.getDataSource("java:comp/env/jdbc/reporting");
    }

    @Bean("reportingTransactionManager")
    public HibernateTransactionManager reportingTransactionManager(
        @Autowired @Qualifier("reportingSessionFactory") final SessionFactory sessionFactory,
        @Autowired @Qualifier("reportingDataSource") final DataSource dataSource) {
        final HibernateTransactionManager reportingTransactionManager = new HibernateTransactionManager();
        reportingTransactionManager.setSessionFactory(sessionFactory);
        reportingTransactionManager.setDataSource(dataSource);
        return reportingTransactionManager;
    }

    @Bean("reportingTransactionTemplate")
    public TransactionTemplate reportingTransactionTemplate(
        @Autowired @Qualifier("reportingTransactionManager")
        final HibernateTransactionManager reportingTransactionManager) {
        final TransactionTemplate reportingTransactionTemplate = new TransactionTemplate();
        reportingTransactionTemplate.setTransactionManager(reportingTransactionManager);
        return reportingTransactionTemplate;
    }

    @Bean("reportingSessionFactory")
    public LocalSessionFactoryBean reportingSessionFactory() {
        final Class<?>[] annotatedClass = new Class<?>[]{Orders.class, OrderLineItem.class, OrderLineStatus.class,
            OrderAttributeValue.class, OrderLineItemAttribute.class, OrderLineItemTax.class, OrderLineItemFee.class,
            OrderLineShipmentNotification.class, OrderLineAdjustment.class, OrderLineStatusHistory.class};
        final LocalSessionFactoryBean reportingSessionFactory = new LocalSessionFactoryBean();
        reportingSessionFactory.setDataSource(reportingDataSource());
        reportingSessionFactory.setHibernateProperties(hibernateProperties());
        reportingSessionFactory.setAnnotatedClasses(annotatedClass);
        return reportingSessionFactory;
    }

    private Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        hibernateProperties.setProperty("hibernate.show_sql", "false");
        hibernateProperties.setProperty("hibernate.generate_statistics", "true");
        hibernateProperties.setProperty("connection.release_mode", "after_transaction");
        hibernateProperties.setProperty("transaction.auto_close_session", "true");
        return hibernateProperties;
    }

    private Class<?>[] getAnnotatedClass() {
        return new Class<?>[]{
            OrderLineAdjustment.class,
            OrderStatus.class,
            ProductAttributeConfiguration.class,
            ProductAttributeValue.class,
            CategoryConfiguration.class,
            EngraveConfiguration.class,
            OrderAttributeValue.class,
            OrderLineItemAttribute.class,
            ShoppingCart.class,
            ShoppingCartItem.class,
            NaughtyWord.class,
            MercSearchFilter.class,
            BannerConfiguration.class,
            PricingModelConfiguration.class,
            VarProgramPaymentOption.class,
            VarProgram.class,
            VarProgramNotification.class,
            VarProgramMessage.class,
            OrderLineShipmentNotification.class,
            VarProgramDomainUserRestriction.class,
            WhiteListWord.class,
            VarProgramFinanceOption.class,
            Otp.class,
            DomainVarMapping.class,
            MerchantEntity.class,
            StatusChangeQueueEntity.class,
            OrderLineStatusEntity.class,
            OrderLineEntity.class,
            StreetAddressRule.class
        };
    }
}
