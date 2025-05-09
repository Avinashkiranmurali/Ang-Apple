package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramNotification;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rpillai on 6/24/2016.
 */
@Transactional
public interface VarProgramNotificationDao extends JpaRepository<VarProgramNotification, Long> {

    List<VarProgramNotification> findByVarIdInAndProgramIdInAndLocaleInAndIsActiveAndType(final List<String> varIds,
        final List<String> programIds, final List<String> locales, final boolean isActive, final String type);

    default List<VarProgramNotification> getActiveEmailNotifications(final List<String> varIds,
        final List<String> programIds, final List<String> locales) {
        return findByVarIdInAndProgramIdInAndLocaleInAndIsActiveAndType(varIds, programIds, locales, true,
            CommonConstants.EMAIL);
    }

}
