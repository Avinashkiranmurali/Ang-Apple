package com.b2s.apple.services;

import com.b2s.apple.entity.EngraveFontConfigurationEntity;
import com.b2s.rewards.apple.dao.EngraveConfigurationDao;
import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.Engrave;
import com.b2s.rewards.apple.model.EngraveConfiguration;
import com.b2s.rewards.apple.model.EngraveFontConfiguration;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by srukmagathan on 23-05-2017.
 */
@Service
public class EngravingService {

    @Autowired
    CategoryConfigurationService categoryConfigurationService;

    @Autowired
    EngraveConfigurationDao engraveConfigurationDao;

    @Lazy
    @Autowired
    EngravingService engravingService;

    private static final Logger logger = LoggerFactory.getLogger(EngravingService.class);

    /**
     * Method to get Engrave configuration
     *
     * @param user
     * @param slugName
     * @param psid
     * @param engrave
     * @return
     */
    public Engrave getEngravingConfiguration(final User user, final String slugName, final String psid,
        Engrave engrave) {

        final CategoryConfiguration catConfig = getCategoryConfiguration(slugName, psid);
        if (Objects.nonNull(catConfig)) {
            final EngraveConfiguration engraveConfiguration =
                engravingService.getEngraveConfiguration(user.getLocale().toString(), catConfig.getId());
            if (Objects.nonNull(engraveConfiguration) && engraveConfiguration.isActive()) {
                if (Objects.isNull(engrave)) {
                    engrave = new Engrave();
                }
                updateEngraveConfiguration(catConfig, engraveConfiguration, engrave);
            }
        }
        return engrave;
    }

    /**
     * Method to update Engrave Configuration
     *
     * @param catConfig
     * @param engraveConfiguration
     * @param engrave
     */
    private void updateEngraveConfiguration(final CategoryConfiguration catConfig,
        final EngraveConfiguration engraveConfiguration, final Engrave engrave){
        try {
            cloneEngraveConfigurations(engrave, engraveConfiguration);

            // For Sku based engraving, PSID and Category Name should be same
            if (catConfig.getCategoryName().equals(catConfig.getPsid())) {
                engrave.setIsSkuBasedEngraving(Boolean.TRUE);
            }
            engrave.setEngraveBgImageLocation(catConfig.getEngraveBgImageLocation());
        } catch (final Exception e) {
            logger.error("Error while copying engraving information");
        }
    }

    /**
     * Deep clone Engrave Configurations from Entity to Model object
     *
     * @param engrave              Model object
     * @param engraveConfiguration Entity object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void cloneEngraveConfigurations(final Engrave engrave,
        final EngraveConfiguration engraveConfiguration) throws IllegalAccessException, InvocationTargetException {
        BeanUtils.copyProperties(engrave, engraveConfiguration);

        if (CollectionUtils.isNotEmpty(engraveConfiguration.getEngraveFontConfigurations())) {
            final List<EngraveFontConfiguration> fontConfigurations = new ArrayList<>();
            EngraveFontConfiguration engraveFontConfigObj = null;
            for (EngraveFontConfigurationEntity engraveFontConfig : engraveConfiguration
                .getEngraveFontConfigurations()) {
                engraveFontConfigObj = new EngraveFontConfiguration();
                BeanUtils.copyProperties(engraveFontConfigObj, engraveFontConfig);
                fontConfigurations.add(engraveFontConfigObj);
            }
            engrave.setEngraveFontConfigurations(fontConfigurations);
        }
    }

    /**
     * Method to check engrave enabled or disabled based on Category Configuration table
     *
     * @param slugName
     * @param psid
     * @param user
     * @return
     */
    public boolean isEngraveEnabled(final User user, final String slugName, final String psid) {
        final CategoryConfiguration catConfig = getCategoryConfiguration(slugName, psid);
        if (Objects.nonNull(catConfig)) {
            final EngraveConfiguration engraveConfiguration =
                engravingService.getEngraveConfiguration(user.getLocale().toString(), catConfig.getId());
            return catConfig.isEngravable()
                && Objects.nonNull(engraveConfiguration) && engraveConfiguration.isActive();
        }
        return false;
    }

    /**
     * Method to check Engrave enabled or disabled
     *
     * @param slugName
     * @param psid
     * @return boolean flag with engrave enabled or disabled
     */
    private CategoryConfiguration getCategoryConfiguration(final String slugName, final String psid) {
        return categoryConfigurationService.getCategoryConfigurationByCategoryName(slugName, psid);
    }

    /**
     * Fetch Engrave Configuration either from DB or from Cache
     *
     * @param userLocale
     * @param categorySlugId
     * @return EngraveConfiguration
     */
    @Cacheable(value = CommonConstants.CACHE_ENGRAVE_CONF, key = "{#userLocale, #categorySlugId}")
    public EngraveConfiguration getEngraveConfiguration(final String userLocale, final Integer categorySlugId){
        return engraveConfigurationDao.getByLocale(userLocale, categorySlugId);
    }
}
