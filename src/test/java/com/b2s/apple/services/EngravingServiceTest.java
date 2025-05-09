package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.EngraveConfigurationDao;
import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.Engrave;
import com.b2s.rewards.apple.model.EngraveConfiguration;
import com.b2s.shop.common.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * EngravingService has one method getEngravingConfiguration() and that is being tested in this class.
 * Two database tables are related to engraving are category_configuration CC and engraving_configuration EC
 * and the join is on EC.category_slug_id = CC.id.
 * One row of category_configuration can have multiple engraving_configuration for different locales.
 * category_configuration has engraving enabled or disabled via flag 'is_engravable' and is based on category names like 'ipad-air' or psid like '30001MU8F2AM/A'
 * engraving_configuration has engraving enabled or disabled via flag 'is_active' and can be switched of for different locales.
 *
 * The tests are based of category_configuration
 * 1. Given a CC, and no corresponding EC, the service should return null
 * 2. Given a CC but the field is_engraving is false, the service should return null
 * 3. Given a CC with active is_engraving, and matching EC but false is_active then it should return null
 * 4. Given a CC with active is_engraving, and matching EC with is_active true then it should return engraving object.
 * 5. Create a CC with categoryName=-1. Then skuBasedEngraving will be true.
 * 6. Confirm the Engrave object returned as image location.
 */
public class EngravingServiceTest {
    @InjectMocks
    private EngravingService engravingService;

    @Mock
    private CategoryConfigurationService categoryConfigurationService;

    @Mock
    private EngraveConfigurationDao engraveConfigurationDao;

    private String varId = "varId";
    private String programId = "programId";
    private Locale locale = new Locale("en_US");
    private String slugName = "slug";
    private String psid = "psid";
    private int categorySlugId = 100;
    private User user = null;
    private Engrave engrave = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        user = getUser();
        engravingService.engravingService = engravingService;
    }

    /**
     * 1. Given a CC, and no corresponding EC, the service should return null
     */
    @Test
    public void testNoEngravingConfiguration() {
        CategoryConfiguration cc = createCategoryConfiguration(true);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(null);
        engrave = engravingService.getEngravingConfiguration(user, slugName, psid, null);
        assertNull(engrave);
    }

    /**
     * 2. Given a CC but the field is_engraving is false, the service should return null
     */
    @Test
    public void testIsEngravingFalse() {
        CategoryConfiguration cc = createCategoryConfiguration(false);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        engrave = engravingService.getEngravingConfiguration(user, slugName, psid, null);
        assertNull(engrave);
    }

    /**
     * 3. Given a CC with active is_engraving, and matching EC but false is_active then it should return null
     */
    @Test
    public void testEngraveConfigurationIsActiveFalse() {
        CategoryConfiguration cc = createCategoryConfiguration(true);
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(false);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(ec);
        engrave = engravingService.getEngravingConfiguration(user, slugName, psid, null);
        assertNull(engrave);
    }

    /**
     * 4. Given a CC with active is_engraving, and matching EC with is_active true then it should return engraving object.
     */
    @Test
    public void testEngraveConfigurationNotNull() {
        CategoryConfiguration cc = createCategoryConfiguration(true);
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(true);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(ec);
        engrave = new Engrave();
        engravingService.getEngravingConfiguration(user, slugName, psid, engrave);
        assertNotNull(engrave);
    }

    /**
     *  5. Create a CC with categoryName=-1. Then skuBasedEngraving will be true.
     */
    @Test
    public void testSkuBasedEngravingIsTrue() {
        CategoryConfiguration cc = createCategoryConfiguration(true);
        cc.setCategoryName("-1"); // see CategoryConfiguration.getCategoryName() to understand -1
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(true);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(ec);
        engrave = new Engrave();
        engravingService.getEngravingConfiguration(user, slugName, psid, engrave);
        assertNotNull(engrave);
        assertTrue(engrave.getIsSkuBasedEngraving());
    }

    /**
     *  6. Confirm the Engrave object returned as image location.
     */
    @Test
    public void testEngraveImageLocationIsNotNull() {
        CategoryConfiguration cc = createCategoryConfiguration(true);
        cc.setCategoryName("-1"); // see CategoryConfiguration.getCategoryName() to understand -1
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(true);

        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
                thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(ec);
        engrave = new Engrave();
        engravingService.getEngravingConfiguration(user, slugName, psid, engrave);
        assertNotNull(engrave);
        assertNotNull(engrave.getEngraveBgImageLocation());
    }

    /**
     *  7. Confirm the Engrave enabled is False
     */
    @Test
    public void testEngraveEnabledIsFalse() {
        CategoryConfiguration cc = createCategoryConfiguration(false);
        cc.setCategoryName("-1");
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(true);


        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
            thenReturn(cc);
        boolean engravable = engravingService.isEngraveEnabled(getUser(), slugName, psid);
        assertEquals(false,engravable);
    }

    /**
     *  8. Confirm the Engrave enabled is True
     */
    @Test
    public void testActiveEngraveEnabledIsTrue() {
        CategoryConfiguration cc = createCategoryConfiguration(true);
        cc.setCategoryName("-1");
        cc.setEngravable(true);
        EngraveConfiguration ec = createEngravingConfiguration();
        ec.setActive(true);

        EngraveConfiguration engraveConfiguration = new EngraveConfiguration();
        engraveConfiguration.setActive(true);


        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid)).
            thenReturn(cc);
        when(engraveConfigurationDao.getByLocale(locale.toString(), categorySlugId)).
                thenReturn(engraveConfiguration);
        boolean engravable = engravingService.isEngraveEnabled(getUser(), slugName, psid);
        assertEquals(true,engravable);
    }

    private CategoryConfiguration createCategoryConfiguration( boolean isEngravable) {
        String imageLocation = "imageUrl";
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration(categorySlugId, slugName);
        categoryConfiguration.setPsid(psid);
        categoryConfiguration.setEngraveBgImageLocation(imageLocation);

        categoryConfiguration.setEngravable(isEngravable);
        return categoryConfiguration;
    }

    private EngraveConfiguration createEngravingConfiguration() {
        EngraveConfiguration engraveConfiguration = new EngraveConfiguration();
        engraveConfiguration.setLocale(locale.toString());
        engraveConfiguration.setCategorySlugId(categorySlugId);
        return engraveConfiguration;
    }

    private User getUser(){
        User user = new User();
        user.setLocale(locale);
        user.setVarId(varId);
        user.setProgramId(programId);
        return user;
    }
}
