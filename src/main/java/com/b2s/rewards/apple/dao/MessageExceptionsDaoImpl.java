package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.MessageExceptionsEntity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 12:06 PM
 */

@Repository
@Transactional
public class MessageExceptionsDaoImpl extends BaseDaoWrapper<MessageExceptionsEntity, Long>implements MessageExceptionsDao{
    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }
}
