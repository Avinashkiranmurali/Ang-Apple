package com.b2s.rewards.apple.dao;


import com.b2s.apple.entity.RelevantLanguageEntity;
import com.b2s.rewards.dao.BaseDao;

/**
 * Created by rperumal on 9/9/2015.
 */

public interface RelevantLanguageDao extends BaseDao<RelevantLanguageEntity, Long> {

    RelevantLanguageEntity getByLocale(final String locale);

}

