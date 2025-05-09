package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.DemoUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author rjesuraj Date : 8/20/2019 Time : 12:40 PM
 */
@Repository
@Transactional
public interface DemoUserDao extends JpaRepository<DemoUserEntity, DemoUserEntity.DemoUserId> {
    DemoUserEntity findByDemoUserIdAndPassword(final DemoUserEntity.DemoUserId demoUserId, final String password);

    default DemoUserEntity get(final DemoUserEntity.DemoUserId demoUserId) {
        Optional<DemoUserEntity> demoUserEntity = findById(demoUserId);
        return demoUserEntity.orElse(null);
    }

}
