package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.PricingModelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.SUBSIDY;

/**
 * Created by rperumal on 2/11/2016
 */
@Repository("pricingModelConfigurationDao")
@Transactional
public interface PricingModelConfigurationDao extends JpaRepository<PricingModelConfiguration,Long> {

    List<PricingModelConfiguration> findByVarIdAndProgramId(String varId, String programId);
    default List<PricingModelConfiguration> getByVarIdProgramId(String varId, String programId){
        return findByVarIdAndProgramId(varId, programId);
    }

    List<PricingModelConfiguration> findByVarIdAndProgramIdAndPriceType(String varId, String programId, String priceType);
    default List<PricingModelConfiguration> getByVarIdProgramIdPriceType(String varId, String programId, String priceType) {
        return findByVarIdAndProgramIdAndPriceType(varId, programId, priceType);
    }

    PricingModelConfiguration findByVarIdAndProgramIdAndPriceKeyAndPriceType(String varId, String programId, String priceKey, String priceType);
    default PricingModelConfiguration getSubsidyByVarIdProgramIdPriceKey(String varId, String programId, String priceKey){
        return findByVarIdAndProgramIdAndPriceKeyAndPriceType(varId, programId, priceKey, SUBSIDY);
    }

    List<PricingModelConfiguration> findByVarIdAndProgramIdAndPriceTypeAndPriceKeyStartingWith(String varId, String programId, String priceType, String priceKey);
    default List<PricingModelConfiguration> getAllSubsidiesByVarIdProgramIdPriceKey(String varId, String programId, String priceKey) {
        return findByVarIdAndProgramIdAndPriceTypeAndPriceKeyStartingWith(varId, programId, SUBSIDY, priceKey);
    }

}
