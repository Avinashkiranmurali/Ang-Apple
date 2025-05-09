package com.b2s.rewards.apple.dao;

import com.b2s.rewards.dao.BaseDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

public abstract class BaseDaoWrapper<MODEL, ID extends Serializable> implements BaseDao<MODEL, ID> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDaoWrapper.class);
    private SessionFactory localSessionFactory;

    public SessionFactory getSessionFactory() {
        return localSessionFactory;
    }

    @Override
    public void setDaoSessionFactory(final SessionFactory daoSessionFactory) {
        if (localSessionFactory == null) {
            localSessionFactory = daoSessionFactory;
        }
    }

    private final Class<MODEL> persistentClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    @Transactional(
        readOnly = true
    )

    @Override
    public MODEL get(final ID id) {
        return localSessionFactory.getCurrentSession().get(this.persistentClass, id);
    }

    @Transactional(
        readOnly = true
    )

    @Override
    public List<MODEL> getAll(final Integer maxResults) {
        final Query query = localSessionFactory.getCurrentSession().createQuery("from " + this.persistentClass.getName());
        if (maxResults != null && maxResults != -1) {
            query.setMaxResults(maxResults);
        }

        try {
            return query.list();
        } catch (Exception e){
            LOGGER.error("Error occurred while retrieving data : {}", e.getMessage());
            throw  e;
        }
    }

    @Override
    public void create(final MODEL toSave) {
        localSessionFactory.getCurrentSession().save(toSave);
    }

    @Override
    public void delete(final MODEL toDelete) {
        localSessionFactory.getCurrentSession().delete(toDelete);
    }

    @Override
    public void update(final MODEL toUpdate) {
        localSessionFactory.getCurrentSession().saveOrUpdate(toUpdate);
    }

    @Override
    public void merge(final MODEL toUpdate) {
        localSessionFactory.getCurrentSession().merge(toUpdate);
    }

    @Override
    public void save(final MODEL toUpdate) {
        localSessionFactory.getCurrentSession().save(toUpdate);
    }

    public void saveAll(final Collection<MODEL> toUpdateAll){
        toUpdateAll.forEach(model -> save(model));
    }

    @Override
    public void refresh(final MODEL toRefresh) {
        localSessionFactory.getCurrentSession().refresh(toRefresh);
    }

    @Override
    public void saveOrUpdate(final MODEL toSaveOrUpdate) {
        localSessionFactory.getCurrentSession().saveOrUpdate(toSaveOrUpdate);
    }

    @Override
    public void flush() {
        localSessionFactory.getCurrentSession().flush();
    }

    protected BaseDaoWrapper() {
    }
}
