package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.VarProgramAccountActivityEntity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Repository
@Transactional
public class VarProgramAccountActivityDaoImpl extends BaseDaoWrapper<VarProgramAccountActivityEntity, Long> implements VarProgramAccountActivityDao  {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

}
