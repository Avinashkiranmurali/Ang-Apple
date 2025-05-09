package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramMessage;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by rpillai on 2/24/2017.
 */
@Repository("varProgramMessageDao")
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public class VarProgramMessageDaoImpl extends BaseDaoWrapper<VarProgramMessage, Long> implements VarProgramMessageDao {

    public static final String SELECT_FROM_VAR_PROGRAM_MESSAGE = "select * from var_program_message ";
    public static final String VAR_ID = "varId";
    public static final String PROGRAM_ID = "programId";
    public static final String LOCALE = "locale";

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public List<VarProgramMessage> getMessages(String varId, String programId, String locale) throws
        DataAccessException {
        final Query query = sessionFactory.getCurrentSession().createNativeQuery(SELECT_FROM_VAR_PROGRAM_MESSAGE +
            "WITH (NOLOCK) where var_id =:varId and program_id = :programId and locale = :locale", VarProgramMessage.class);

        query.setParameter(VAR_ID, varId);
        query.setParameter(PROGRAM_ID, programId);
        query.setParameter(LOCALE, locale);
        return query.list();
    }

    @Override
    public List<VarProgramMessage> getMessages(final List<String> varIds, final List<String> programIds,
        final List<String> locales) throws DataAccessException{
        final Query query = sessionFactory.getCurrentSession().createNativeQuery(SELECT_FROM_VAR_PROGRAM_MESSAGE +
                "WITH (NOLOCK) where var_id in (:varId) and program_id in (:programId) and locale in (:locale)",
            VarProgramMessage.class);

        query.setParameter(VAR_ID, varIds);
        query.setParameter(PROGRAM_ID, programIds);
        query.setParameter(LOCALE, locales);
        return query.list();
    }

    @Override
    public List<VarProgramMessage> getMessages(
        final List<String> varIds,
        final List<String> programIds,
        final List<String> locales,
        final String codeType) throws DataAccessException {
        final Query query = sessionFactory.getCurrentSession().createNativeQuery(SELECT_FROM_VAR_PROGRAM_MESSAGE +
                "WITH (NOLOCK) where var_id in (:varId) and program_id in (:programId) and locale in (:locale) and " +
                "code_type = :codeType",
            VarProgramMessage.class);

        query.setParameter(VAR_ID, varIds);
        query.setParameter(PROGRAM_ID, programIds);
        query.setParameter(LOCALE, locales);
        query.setParameter("codeType", codeType);
        return query.list();
    }
}