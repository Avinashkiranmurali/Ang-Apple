package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.MaintenanceMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MaintenanceMessageDao extends JpaRepository<MaintenanceMessageEntity, Integer> {

    List<MaintenanceMessageEntity> findByVarIdInAndProgramIdInAndActive(final List<String> varIds,
        final List<String> programIds, final boolean activeInd);

    default List<MaintenanceMessageEntity> getVarProgramMessageValue(final List<String> varIds, final List<String> programIds) {
        return findByVarIdInAndProgramIdInAndActive(varIds, programIds, true);
    }
}
