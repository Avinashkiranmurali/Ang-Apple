package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rkumar 2020-02-10
 */
@Repository("VarProgramTemplateDao")
@Transactional
public interface VarProgramTemplateDao extends JpaRepository<VarProgramTemplate, Long> {

    VarProgramTemplate findByVarIdAndProgramIdAndIsActive(String varId, String programId, Boolean isActive);

    default VarProgramTemplate getByVarIdProgramId(String varId, String programId) {
        return findByVarIdAndProgramIdAndIsActive(varId, programId, Boolean.TRUE);
    }
}
