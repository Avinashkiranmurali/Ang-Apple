package com.b2s.apple.services;

import com.b2s.apple.entity.GiftPromoEntity;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.rewards.apple.dao.GiftPromoDao;
import com.b2s.rewards.apple.model.GiftItem;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GiftPromoServiceTest {
    @InjectMocks
    private GiftPromoService giftPromoService;

    @Mock
    private GiftPromoDao giftPromoDao;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup()
        throws Exception {
        MockitoAnnotations.initMocks(this);
        MockMvcBuilders.standaloneSetup(giftPromoService).build();
    }

    @Test
    public void testGetGiftItem() throws ServiceException {
        final GiftPromoEntity entity = getEntity();
        entity.setQualifyingPsid("ABCDEF");
        entity.setGiftItemPsid("XYZ123");
        when(giftPromoDao.getGift(any(List.class), any(List.class), any(List.class), anyString(), anyString(), any(Date.class))).thenReturn(
            Arrays.asList(entity));

        final Optional<GiftItem> result = giftPromoService.getGiftItem(getUser(), "ABCDEF", "XYZ123", new Program());

        assertNotNull(result);
        assertEquals(result.get().getProductId(), entity.getGiftItemPsid());
        assertEquals(result.get().getDiscount(), entity.getDiscount());
        assertEquals(result.get().getDiscountType(), entity.getDiscountType());
    }

    @Test
    public void testDisableGiftItem() throws ServiceException {
        Program program = new Program();
        program.setConfig(Map.of("disableGwp", true));
        final Optional<GiftItem> result = giftPromoService.getGiftItem(getUser(), "ABCDEF", "XYZ123", program);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetGiftItemWithMultipleGiftsSuccess() throws ServiceException {
        final GiftPromoEntity entity1 = getEntity();
        entity1.setQualifyingPsid("ABCDEF");
        entity1.setGiftItemPsid("XYZ123");

        final GiftPromoEntity entity2 = getEntity();
        entity2.setQualifyingPsid("ABCDEF");
        entity2.setGiftItemPsid("XYZ123");
        entity2.setVarId("-1");

        List<GiftPromoEntity> entities = new ArrayList<>();
        entities.add(entity1);
        entities.add(entity2);
        when(giftPromoDao.getGift(any(List.class), any(List.class), any(List.class), anyString(), anyString(), any(Date.class))).thenReturn(entities);

        final Optional<GiftItem> result = giftPromoService.getGiftItem(getUser(), "ABCDEF", "XYZ123", new Program());

        assertNotNull(result);
        assertEquals(result.get().getProductId(), entities.get(0).getGiftItemPsid());
        assertEquals(result.get().getDiscount(), entities.get(0).getDiscount());
        assertEquals(result.get().getDiscountType(), entities.get(0).getDiscountType());
    }

    @Test
    public void testGetGiftItemWithMultipleGiftsFailure() throws ServiceException {
        final GiftPromoEntity entity1 = getEntity();
        entity1.setQualifyingPsid("ABCDEF");
        entity1.setGiftItemPsid("XYZ123");

        final GiftPromoEntity entity2 = getEntity();
        entity2.setQualifyingPsid("ABCDEF");
        entity2.setGiftItemPsid("XYZ123");

        List<GiftPromoEntity> entities = new ArrayList<>();
        entities.add(entity1);
        entities.add(entity2);
        when(giftPromoDao.getGift(any(List.class), any(List.class), any(List.class), anyString(), anyString(), any(Date.class))).thenReturn(entities);

        exceptionRule.expect(ServiceException.class);
        exceptionRule.expectMessage("Multiple promotions found for the selected qualifying product");

        final Optional<GiftItem> result = giftPromoService.getGiftItem(getUser(), "ABCDEF", "XYZ123", new Program());
    }

    private GiftPromoEntity getEntity() {
        GiftPromoEntity entity = new GiftPromoEntity();
        entity.setId(1L);
        entity.setVarId("Delta");
        entity.setLocale(Locale.US.toString());
        entity.setProgramId("PROG1");
        entity.setDiscount(60d);
        entity.setDiscountType(CommonConstants.DGWP_PERCENTAGE);

        return entity;
    }

    private User getUser() {
        User user = new User();
        user.setLocale(Locale.US);
        user.setVarId("Delta");
        user.setProgramId("PROG1");

        return user;
    }
}
