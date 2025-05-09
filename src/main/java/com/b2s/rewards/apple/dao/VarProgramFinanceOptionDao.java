package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramFinanceOption;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface VarProgramFinanceOptionDao extends BaseDao<VarProgramFinanceOption, Long> {

    List<VarProgramFinanceOption> getVarProgramFinanceOption(final String varId, final String programId);

}
