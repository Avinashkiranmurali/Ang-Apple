package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by skither on 12/5/2018.
 */
@Repository("merchantListDao")
@Transactional("transactionManager")
public interface MerchantListDao extends JpaRepository<MerchantEntity,Integer> {

    MerchantEntity findBySupplierIdAndMerchantId(final Integer supplierId, final Integer merchantId);

    default List<MerchantEntity> getAll(){
        return findAll();
    }

    default MerchantEntity getMerchant(Integer supplierId, Integer merchantId){
        return findBySupplierIdAndMerchantId(supplierId, merchantId);
    }
}