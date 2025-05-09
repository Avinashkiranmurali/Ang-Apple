package com.b2s.apple.services;

import com.b2s.apple.entity.DemoUserEntity;
import com.b2s.rewards.apple.dao.DemoUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemoUserService {

    @Autowired
    private DemoUserDao demoUserDao;

    public DemoUserEntity selectUser(final String varid, final String programid, final String userid) {
        final DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId();
        demoUserId.setProgramId(programid);
        demoUserId.setVarId(varid);
        demoUserId.setUserId(userid);
        return demoUserDao.get(demoUserId);
    }

    public void updateUser(final DemoUserEntity demoUserEntity) {
        demoUserDao.save(demoUserEntity);
    }

}
