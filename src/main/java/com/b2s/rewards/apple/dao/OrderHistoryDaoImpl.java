package com.b2s.rewards.apple.dao;


import com.b2s.rewards.apple.model.Orders;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by vmurugesan on 10/3/2016.
 */

@Repository("orderHistoryDao")
@Transactional("reportingTransactionManager")
public class OrderHistoryDaoImpl implements OrderHistoryDao {

    @Autowired
    @Qualifier("reportingSessionFactory")
    private SessionFactory reportingSessionFactory;

    @Autowired
    private Properties applicationProperties;

    public List<Orders> getOrderHistory(User user, final Integer days, final boolean allPrograms) throws
        ServiceException {
        Session session = reportingSessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Orders> cq = cb.createQuery(Orders.class);
        Root<Orders> root = cq.from(Orders.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.USER_ID_CAMEL_CASE), user.getUserId()));
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ID_CAMEL_CASE), user.getVarId()));
        if (!allPrograms) {
            predicates.add(cb.equal(root.get(CommonConstants.PROGRAM_ID), user.getProgramId()));
        }

        final long daysLimit = fetchOrderHistoryDaysLimit(days);
        if (daysLimit > 0) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(CommonConstants.ORDER_DATE_CAMEL_CASE),
                Timestamp.valueOf(LocalDate.now().minus(daysLimit, ChronoUnit.DAYS).atStartOfDay())));
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root).where(predicateArray);

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        @SuppressWarnings("unchecked")
        List<Orders> returnList = query.getResultList();
        return returnList;
    }

    /*
    * This method will fetch Order History limit by the number of days. It will either set the value if sent by UI
    * or it will set the value from environment property "orderHistory.default.day" if available
    * */
    private long fetchOrderHistoryDaysLimit(final Integer days) {
        final String defaultDays = applicationProperties.getProperty(CommonConstants.ORDER_HISTORY_DEFAULT_DAYS);
        long daysLimit = 0;
        if (Objects.nonNull(days) && days > 0) {
            daysLimit = days;
        } else if (StringUtils.isNotBlank(defaultDays)) {
            daysLimit = Long.valueOf(defaultDays);
        }
        return daysLimit;
    }

    public Orders getOrderHistoryDetails(final User user, final Integer orderId, final boolean allPrograms) throws ServiceException {
        Session session = reportingSessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Orders> cq = cb.createQuery(Orders.class);
        Root<Orders> root = cq.from(Orders.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.ORDER_ID), orderId));
        predicates.add(cb.equal(root.get(CommonConstants.USER_ID_CAMEL_CASE), user.getUserId()));
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ID_CAMEL_CASE), user.getVarId()));
        if (!allPrograms) {
            predicates.add(cb.equal(root.get(CommonConstants.PROGRAM_ID), user.getProgramId()));
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root)
            .where(predicateArray)
            .orderBy(cb.desc(root.get(CommonConstants.ORDER_DATE_CAMEL_CASE)));

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        return (Orders) query.getSingleResult();
    }

    @Override
    public Orders getOrderHistoryDetails(User user, String varOrderId, boolean allPrograms) throws ServiceException {
        Session session = reportingSessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Orders> cq = cb.createQuery(Orders.class);
        Root<Orders> root = cq.from(Orders.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ORDER_ID), varOrderId));
        predicates.add(cb.equal(root.get(CommonConstants.USER_ID_CAMEL_CASE), user.getUserId()));
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ID_CAMEL_CASE), user.getVarId()));
        if (!allPrograms) {
            predicates.add(cb.equal(root.get(CommonConstants.PROGRAM_ID), user.getProgramId()));
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root)
            .where(predicateArray)
            .orderBy(cb.desc(root.get(CommonConstants.ORDER_DATE_CAMEL_CASE)));

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        return (Orders) query.getSingleResult();
    }

    @Override
    public Orders getOrderHistoryDetails(final String orderId, final String email, final String varId, final String programId, final Locale locale) throws ServiceException {
        Session session = reportingSessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Orders> cq = cb.createQuery(Orders.class);
        Root<Orders> root = cq.from(Orders.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.EMAIL_LOWER_CASE), email));
        predicates.add(cb.equal(root.get(CommonConstants.ORDER_ID), orderId));
        predicates.add(cb.equal(root.get(CommonConstants.PROGRAM_ID), programId));
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ID_CAMEL_CASE), varId));
        predicates.add(cb.equal(root.get(CommonConstants.COUNTRY_CODE_CAMEL_CASE), locale.getCountry()));
        predicates.add(cb.equal(root.get(CommonConstants.LANGUAGE_CODE_CAMEL_CASE), locale.getLanguage()));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root)
                .where(predicateArray)
                .orderBy(cb.desc(root.get(CommonConstants.ORDER_DATE_CAMEL_CASE)));

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);
        query.setMaxResults(1);

        return (Orders) query.getSingleResult();
    }
}
