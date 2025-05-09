package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramPaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rpillai on 6/24/2016.
 */
@Transactional
public interface VarProgramPaymentOptionDao extends JpaRepository<VarProgramPaymentOption, Long> {

    List<VarProgramPaymentOption> findByVarIdAndProgramId(String varId, String programId);

    default List<VarProgramPaymentOption> getVarProgramPaymentOption(String varId, String programId) {
        return findByVarIdAndProgramId(varId, programId);
    }
}
