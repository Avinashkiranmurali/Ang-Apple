package com.b2s.apple.spring;

import com.b2r.util.address.StreetAddressRule;
import com.b2s.apple.entity.*;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.context.DBVersionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.b2s.rewards.apple.dao")
public class DataSourceTestConfiguration {

    @Bean()
    public DataSource dataSource() {
        final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder
            .setType(EmbeddedDatabaseType.H2)
            .setName("testDB;MODE=MySQL;NON_KEYWORDS=VALUE;DATABASE_TO_LOWER=TRUE")
            .addScript("classpath:Test_schema_creation.sql")
            .build();
    }

    @Bean()
    public DataSource reportingDataSource() {
        return dataSource();
    }

    @Bean()
    @Primary
    public HibernateTransactionManager transactionManager() {
        final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        final TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager());
        return transactionTemplate;
    }

    @Bean({"entityManagerFactory", "sessionFactory"})
    public LocalSessionFactoryBean sessionFactory() {
        final String[] packageList = {"com.b2s.rewards.apple.model", "com.b2s.apple.entity"};
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(packageList);
        sessionFactory.setHibernateProperties(hibernateProperties());
        sessionFactory.setAnnotatedClasses(getAnnotatedClass());
        return sessionFactory;
    }

    @Bean
    @Qualifier(value = "entityManager")
    public EntityManager entityManager(@Autowired @Qualifier("entityManagerFactory") final EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean()
    public HibernateTransactionManager reportingTransactionManager() {
        final HibernateTransactionManager reportingTransactionManager = new HibernateTransactionManager();
        reportingTransactionManager.setSessionFactory(sessionFactory().getObject());
        reportingTransactionManager.setDataSource(dataSource());
        return reportingTransactionManager;
    }

    @Bean()
    public TransactionTemplate reportingTransactionTemplate() {
        final TransactionTemplate reportingTransactionTemplate = new TransactionTemplate();
        reportingTransactionTemplate.setTransactionManager(reportingTransactionManager());
        return reportingTransactionTemplate;
    }

    @Bean()
    public LocalSessionFactoryBean reportingSessionFactory() {
        final Class<?>[] annotatedClass = new Class<?>[]{Orders.class, OrderLineItem.class, OrderLineStatus.class,
            OrderAttributeValue.class, OrderLineItemAttribute.class, OrderLineItemTax.class, OrderLineItemFee.class,
            OrderLineShipmentNotification.class, OrderLineAdjustment.class, OrderLineStatusHistory.class};
        final LocalSessionFactoryBean reportingSessionFactory = new LocalSessionFactoryBean();
        reportingSessionFactory.setDataSource(dataSource());
        reportingSessionFactory.setHibernateProperties(hibernateProperties());
        reportingSessionFactory.setAnnotatedClasses(annotatedClass);
        return reportingSessionFactory;
    }

    @Bean()
    public DBVersionCheck dbVersionCheck() {
        return new DBVersionCheck();
    }

    private Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "com.b2s.apple.spring.H2DialectExtended");
        hibernateProperties.setProperty("hibernate.show_sql", "true");
        hibernateProperties.setProperty("hibernate.generate_statistics", "true");
        hibernateProperties.setProperty("connection.release_mode", "after_transaction");
        hibernateProperties.setProperty("transaction.auto_close_session", "true");
        hibernateProperties.setProperty("hibernate.hbm2ddl.import_files", "Test_schema_creation.sql");
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
