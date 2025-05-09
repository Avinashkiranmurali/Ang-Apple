package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.PricingLogEntity;
import com.b2s.rewards.dao.BaseDao;

/**
 * @author rkumar 2019-12-06
 */
public interface PricingLogDao extends BaseDao<PricingLogEntity, Long> {

    void insert(PricingLogEntity pricingLogEntity);

}