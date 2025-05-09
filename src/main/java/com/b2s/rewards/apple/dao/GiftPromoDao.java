package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.GiftPromoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface GiftPromoDao extends JpaRepository<GiftPromoEntity, Integer> {

    List<GiftPromoEntity> findByLocaleInAndVarIdInAndProgramIdInAndQualifyingPsidAndActiveIsTrueAndStartDateIsLessThanEqualAndEndDateIsGreaterThanEqualOrderByLocaleDescVarIdDesc(
        final List<String> locales, final List<String> varIds, final List<String> programIds,
        final String qualifyingSku, final Date startTimeStamp, final Date endTimeStamp);

    List<GiftPromoEntity> findByLocaleInAndVarIdInAndProgramIdInAndQualifyingPsidAndGiftItemPsidAndActiveIsTrueAndStartDateIsLessThanEqualAndEndDateIsGreaterThanEqualOrderByLocaleDescVarIdDesc(
        final List<String> locales, final List<String> varIds, final List<String> programIds,
        final String qualifyingSku, final String giftItemSku, final Date startTimeStamp, final Date endTimeStamp);

    default List<GiftPromoEntity> getAllGiftPromos(final List<String> locales, final List<String> varIds,
        final List<String> programIds, final String qualifyingPsid, final Date currentDateTime) {
        final List<GiftPromoEntity> resultList =
            findByLocaleInAndVarIdInAndProgramIdInAndQualifyingPsidAndActiveIsTrueAndStartDateIsLessThanEqualAndEndDateIsGreaterThanEqualOrderByLocaleDescVarIdDesc(
                locales, varIds, programIds, qualifyingPsid, currentDateTime, currentDateTime);

        return resultList;
    }

    default List<GiftPromoEntity> getGift(final List<String> locales, final List<String> varIds,
        final List<String> programIds, final String qualifyingPsid, final String giftItemPsid,
        final Date currentDateTime) {
        return findByLocaleInAndVarIdInAndProgramIdInAndQualifyingPsidAndGiftItemPsidAndActiveIsTrueAndStartDateIsLessThanEqualAndEndDateIsGreaterThanEqualOrderByLocaleDescVarIdDesc(
            locales, varIds, programIds, qualifyingPsid, giftItemPsid, currentDateTime, currentDateTime);

    }
}
