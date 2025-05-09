package com.b2s.apple.services;

import com.b2s.apple.entity.GiftPromoEntity;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.dao.GiftPromoDao;
import com.b2s.rewards.apple.model.GiftItem;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.b2s.rewards.common.util.CommonConstants.*;

@Service
public class GiftPromoService {
    private static final Logger logger = LoggerFactory.getLogger(GiftPromoService.class);

    @Autowired
    private GiftPromoDao giftPromoDao;

    /**
     * Get Gift Item
     *
     * @param user
     * @param qualifyingPsid
     * @param giftPsid
     * @return
     */
    public Optional<GiftItem> getGiftItem(final User user, final String qualifyingPsid, final String giftPsid, final Program program)
        throws ServiceException {
        Optional<GiftItem> giftItem = Optional.empty();

        if (!AppleUtil.getProgramConfigValueAsBoolean(program, DISABLE_GWP)) {
            final List<GiftPromoEntity> giftAllPromoEntities = giftPromoDao.getGift(
                    List.of(DEFAULT_LOCALE, user.getLocale().toString()),
                    List.of(DEFAULT_VAR_PROGRAM, user.getVarId()),
                    List.of(DEFAULT_PROGRAM_KEY, user.getProgramId()),
                    qualifyingPsid,
                    giftPsid,
                    new Date());
            if (CollectionUtils.isNotEmpty(giftAllPromoEntities)) {
                List<GiftPromoEntity> giftList = filterGiftPromos(user.getLocale().toString(), user.getVarId(), user.getProgramId(), giftAllPromoEntities);

                // Promotion MUST be unique
                final GiftPromoEntity result = giftList.get(0);
                giftItem = Optional.of(getGiftItemModel(result));

                // if promotion is duplicated, then we throw an exception
                if (giftList.size() > 1) {
                    logger.error("Multiple active promotions found for : {}, {}, {}, {}, {}",
                            result.getLocale(),
                            result.getVarId(),
                            result.getProgramId(),
                            result.getQualifyingPsid(),
                            result.getGiftItemPsid());
                    logger.error("Please check the DB and keep only 1 promotion active");
                    throw new ServiceException(ServiceExceptionEnums.MULTIPLE_PROMOTIONS_FOUND);
                }
            }
        }

        return giftItem;
    }

    /**
     * Get Gift Products Entities for the given qualifying PSID
     *
     * @param user
     * @param qualifyingPsid
     * @return Gift Products PSID List or empty
     */
    private List<GiftPromoEntity> getListOfGiftsEntities(final User user, final String qualifyingPsid) {
        return giftPromoDao.getAllGiftPromos(
            List.of(DEFAULT_LOCALE, user.getLocale().toString()),
            List.of(DEFAULT_VAR_PROGRAM, user.getVarId()),
            List.of(DEFAULT_PROGRAM_KEY, user.getProgramId()),
            qualifyingPsid,
            new Date());
    }

    /**
     * Get Gift Products PSIDs for the given qualifying PSID
     *
     * @param user
     * @param qualifyingPsid
     * @return Gift Products PSID List or empty
     */
    public List<String> getGiftPsids(final User user, final String qualifyingPsid, final Program program) {
        return getGiftPsids(getGiftItemList(user, qualifyingPsid, program));
    }

    /**
     * Get Only Gift Product PSIDs from Gift Item list
     *
     * @return Gift Products PSID List or empty
     */
    public List<String> getGiftPsids(final List<GiftItem> giftItems) {
        return giftItems.stream()
            .map(GiftItem::getProductId)
            .collect(Collectors.toList());
    }


    /**
     * Get List of Gift Items for the given qualifying PSID
     *
     * @param user
     * @param qualifyingPsid
     * @return Gift Item List or empty
     */
    public List<GiftItem> getGiftItemList(final User user, final String qualifyingPsid, final Program program) {
        List<GiftItem> giftList = new ArrayList<>();
        if (!AppleUtil.getProgramConfigValueAsBoolean(program, DISABLE_GWP)) {
            final List<GiftPromoEntity> giftAllPromoEntities = getListOfGiftsEntities(user, qualifyingPsid);

            if (CollectionUtils.isNotEmpty(giftAllPromoEntities)) {
                giftList =
                        filterGiftPromos(user.getLocale().toString(), user.getVarId(), user.getProgramId(), giftAllPromoEntities)
                                .stream()
                                .map(this::getGiftItemModel)
                                .collect(Collectors.toList());
            }
        }

        return giftList;
    }

    /**
     * Get Get Item model from Gift Promo Entity
     *
     * @param giftPromoEntity
     * @return
     */
    private GiftItem getGiftItemModel(final GiftPromoEntity giftPromoEntity){
        final GiftItem giftItem = new GiftItem();
        giftItem.setProductId(giftPromoEntity.getGiftItemPsid());
        giftItem.setDiscount(giftPromoEntity.getDiscount());
        giftItem.setDiscountType(giftPromoEntity.getDiscountType());
        return giftItem;
    }

    /**
     * Retrieves relatively best match @GiftPromos List from the List of giftPromoEntities based on override precedence
     * override precedence ==> higher overrideValue overrides lower overrideValue
     * overrideValue = 3 ==> Specific Locale, Var and Program Ids
     * overrideValue = 2 ==> Specific Locale and Var Ids, Generic Program Id
     * overrideValue = 1 ==> Specific Locale Id, Generic Var and Program Ids
     * overrideValue = 0 ==> Generic Locale, Var and Program Ids
     *
     * @param locale
     * @param varId
     * @param programId
     * @param giftPromoEntities
     * @return GiftPromos List or null
     */
    private List<GiftPromoEntity> filterGiftPromos(final String locale, final String varId, final String programId,
        final List<GiftPromoEntity> giftPromoEntities) {
        final List<GiftPromoEntity> giftList = new ArrayList<>();

        int overrideValue = -1;
        for (GiftPromoEntity giftPromoEntity : giftPromoEntities) {
            if (giftPromoEntity.getLocale().equalsIgnoreCase(locale)) {
                if (giftPromoEntity.getVarId().equalsIgnoreCase(varId)) {
                    if (giftPromoEntity.getProgramId().equalsIgnoreCase(programId)) {
                        //highest precedence as Locale, Var and Program Ids matches
                        overrideValue = checkAndUpdateList(giftList, giftPromoEntity, 3, overrideValue);
                    } else if (overrideValue <= 2) {  //higher precedence as matching Specific Locale and Var Ids
                        overrideValue = checkAndUpdateList(giftList, giftPromoEntity, 2, overrideValue);
                    }
                } else if (overrideValue <= 1) {     //average precedence as only Locale Id matches
                    overrideValue = checkAndUpdateList(giftList, giftPromoEntity, 1, overrideValue);
                }
            } else if (overrideValue <= 0) {        //should be default configuration
                overrideValue = checkAndUpdateList(giftList, giftPromoEntity, 0, overrideValue);
            }
        }
        return giftList;
    }

    /**
     * Updates PSID in the giftList
     * Clear gift List and then add the new PSID if the precedence values are not matching
     *
     * @param giftList
     * @param giftPromoEntity
     * @param expectedPrecedence
     * @param currentPrecedence
     * @return currentPrecedence based on update logic
     */
    private int checkAndUpdateList(final List<GiftPromoEntity> giftList, final GiftPromoEntity giftPromoEntity,
        final int expectedPrecedence, int currentPrecedence) {
        if (currentPrecedence != expectedPrecedence) {
            giftList.clear();
            currentPrecedence = expectedPrecedence;
        }
        giftList.add(giftPromoEntity);
        return currentPrecedence;
    }
}