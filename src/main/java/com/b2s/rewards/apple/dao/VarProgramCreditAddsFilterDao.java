package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.VarProgramCreditAddsFilterEntity;
import com.b2s.rewards.dao.BaseDao;

import java.util.List;

public interface VarProgramCreditAddsFilterDao extends BaseDao<VarProgramCreditAddsFilterEntity, VarProgramCreditAddsFilterEntity.VarProgramFilterId> {


    List<VarProgramCreditAddsFilterEntity> findByVarProgram(String varId, String programId);
}
