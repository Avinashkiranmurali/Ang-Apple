package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgram;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rpillai on 6/24/2016.
 */
@Repository("varProgramDao1")
@Transactional
public interface VarProgramDao extends JpaRepository<VarProgram, Long> {

    VarProgram findByVarIdAndProgramIdAndActive(final String varId, final String programId, final String activeInd);

    default VarProgram getActiveVarProgram(String varId, String programId){
        return findByVarIdAndProgramIdAndActive(varId, programId, CommonConstants.YES_VALUE);
    }

}
