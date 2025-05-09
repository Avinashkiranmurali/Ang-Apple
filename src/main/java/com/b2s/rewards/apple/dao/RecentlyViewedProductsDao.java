package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.RecentlyViewedProductsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public interface RecentlyViewedProductsDao extends JpaRepository<RecentlyViewedProductsEntity, Long> {

    List<RecentlyViewedProductsEntity> findByVarIdAndProgramIdAndUserIdOrderByViewedDateTimeDesc(final String varId,
        final String programId, final String userId);

    int deleteByVarIdAndProgramIdAndUserIdAndProductIdIn(final String varId, final String programId,
        final String userId, final List<String> productIds);

    @Modifying
    @Query(value = "update RecentlyViewedProductsEntity set viewedDateTime = :currentDate where varId=:varId " +
        "and programId=:programId and userId=:userId and productId=:productId")
    int updateByVarIdAndProgramIdAndUserIdAndProductIdAndViewedDateTime(
        @Param("varId") final String varId, @Param("programId") final String programId,
        @Param("userId") final String userId, @Param("productId") final String productId,
        @Param("currentDate") final Date currentDate);

    default void insert(RecentlyViewedProductsEntity recentlyViewedProductsEntity) {
        save(recentlyViewedProductsEntity);
    }

    default List<RecentlyViewedProductsEntity> getProducts(final String varId, final String programId,
        final String userId) {
        return findByVarIdAndProgramIdAndUserIdOrderByViewedDateTimeDesc(varId, programId, userId);
    }

    default int updateProductWithCurrentTime(final String varId, final String programId, final String userId,
        final String productId) throws RuntimeException{
        return updateByVarIdAndProgramIdAndUserIdAndProductIdAndViewedDateTime(varId, programId, userId, productId,
            new Date());
    }

    default int deleteProducts(final String varId, final String programId, final String userId,
        final List<String> productIds) {
        return deleteByVarIdAndProgramIdAndUserIdAndProductIdIn(varId, programId, userId, productIds);
    }

}