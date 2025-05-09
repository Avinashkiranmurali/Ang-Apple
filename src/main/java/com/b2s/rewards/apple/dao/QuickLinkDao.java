package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.QuickLinkEntity;
import com.b2s.rewards.dao.BaseDao;

import java.util.List;

public interface QuickLinkDao extends BaseDao<QuickLinkEntity, Integer> {

    List<QuickLinkEntity> getByVarIdProgramIdLocaleLinkCode(String varId, String programId, String locale,boolean isAnon);
}
