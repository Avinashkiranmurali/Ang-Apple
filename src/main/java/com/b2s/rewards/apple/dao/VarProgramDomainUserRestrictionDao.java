package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramDomainUserRestriction;
import com.b2s.rewards.dao.BaseDao;

/**
 * @author rjesuraj Date : 3/3/2017 Time : 12:52 PM
 */

public interface VarProgramDomainUserRestrictionDao extends BaseDao<VarProgramDomainUserRestriction,Integer> {

    boolean isUserOfAuthType(final String userId, final String varId, final String programId, final String loginType, final String authType);

}
