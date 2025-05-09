package com.b2s.apple.services;

import com.b2s.apple.entity.BannerConfigEntity;
import com.b2s.rewards.apple.dao.BannerConfigDao;
import com.b2s.rewards.apple.integration.model.BannerConfigResponse;
import com.b2s.rewards.apple.util.VarProgramLocaleComparator;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

@Service
public class BannerConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BannerConfigService.class);

    @Autowired
    private BannerConfigDao bannerConfigDao;

    @Autowired
    private AppSessionInfo appSessionInfo;

    public static final String WHATS_NEW = "whatsnew";
    public static final String FAMILY = "family";
    public static final String LANDING = "landing";
    public static final String PRODUCT = "product";
    public static final String DEFAULT_CATEGORY = "-1";
    public static final String DISABLE = "disable";
    public static final String DISPLAY_ORDER = "displayOrder";
    public static final String CAROUSEL_ORDER = "carouselOrder";

    /***
     * Get BannerConfigResponses
     *
     * @param user
     * @return
     */
    public Map<String, List<BannerConfigResponse>> getBanners(final User user) {

        final Map<String, List<BannerConfigResponse>> bannerResponseMap = new HashMap<>();
        final List<BannerConfigResponse> familyConfigs = new ArrayList<>();
        final List<BannerConfigResponse> whatsNewConfigs = new ArrayList<>();
        final List<BannerConfigResponse> landingConfigs = new ArrayList<>();

        final Map<String, Map<String, Map<String, List<BannerConfigEntity>>>> bannerConfigs =
            getBannerConfigEntities(user.getVarId(),
                user.getProgramId(), user.getLocale().toString());

        final Map<String, List<BannerConfigEntity>> bannerConfigEntitiesByConfigType = new HashMap<>();

        //Fetch Var, Program & Locale specific configs if present
        if (MapUtils.isNotEmpty(bannerConfigs)) {
            bannerConfigs.forEach((configType, configTypeEntities) -> {
                final List<BannerConfigEntity> bannerConfigEntities = new ArrayList<>();
                configTypeEntities.forEach((category, categoryEntities) ->
                    categoryEntities.forEach((name, nameEntities) ->
                        nameEntities.stream()
                            .min(VarProgramLocaleComparator::compare)
                            .ifPresent(bannerConfigEntities::add)));
                bannerConfigEntitiesByConfigType.put(configType, bannerConfigEntities);
            });
        }

        populateBannerConfigs(user, familyConfigs, whatsNewConfigs, landingConfigs, bannerConfigEntitiesByConfigType);
        bannerResponseMap.put(FAMILY, familyConfigs);
        bannerResponseMap.put(WHATS_NEW, whatsNewConfigs);
        bannerResponseMap.put(LANDING, getCarouselOrderSorted(landingConfigs));
        return bannerResponseMap;
    }

    private void populateBannerConfigs(final User user, final List<BannerConfigResponse> familyConfigs,
                                       final List<BannerConfigResponse> whatsNewConfigs, final List<BannerConfigResponse> landingConfigs,
        final Map<String, List<BannerConfigEntity>> bannerConfigEntitiesByConfigType) {
        if (MapUtils.isNotEmpty(bannerConfigEntitiesByConfigType)) {
            bannerConfigEntitiesByConfigType.forEach((configType, bannerConfigEntities) -> {
                if (CollectionUtils.isNotEmpty(bannerConfigEntities)) {
                    final List<BannerConfigEntity> disabledEntities = bannerConfigEntities.stream()
                        .filter(bannerConfigEntity -> DISABLE.equalsIgnoreCase(bannerConfigEntity.getName()) &&
                            BooleanUtils.toBoolean(bannerConfigEntity.getValue()))
                        .collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(disabledEntities)) {
                        //if disabled banner config is present
                        LOGGER
                            .info(
                                "Banner configuration is disabled for this VAR:{} Program:{} Locale:{} & ConfigType:{}",
                                user.getVarId(), user.getProgramId(), user.getLocale(), configType);
                    } else {
                        addBannerConfigResponseByCategory(familyConfigs, whatsNewConfigs, landingConfigs,
                                configType, bannerConfigEntities);
                    }
                }
            });
        }
    }

    private void addBannerConfigResponseByCategory(List<BannerConfigResponse> familyConfigs, List<BannerConfigResponse> whatsNewConfigs, List<BannerConfigResponse> landingConfigs, String configType, List<BannerConfigEntity> bannerConfigEntities) {
        if (FAMILY.equalsIgnoreCase(configType)) {
            addBannerConfigResponseByCategory(familyConfigs, bannerConfigEntities, configType);
        } else if (WHATS_NEW.equalsIgnoreCase(configType)) {
            addBannerConfigResponseByCategory(whatsNewConfigs, bannerConfigEntities, configType);
        } else {
            addBannerConfigResponseByCategory(landingConfigs, bannerConfigEntities, configType);
        }
    }

    /***
     *
     * Sort BannerConfigurations in expected carousel Order
     *
     * @param bannerConfigResponses
     */
    private List<BannerConfigResponse> getCarouselOrderSorted(List<BannerConfigResponse> bannerConfigResponses) {
        if (CollectionUtils.isNotEmpty(bannerConfigResponses)) {
            final List<BannerConfigResponse> orderedBannerConfigResponses = new ArrayList<>();
            try {
                List<BannerConfigResponse> defaultBannerConfigResponses = bannerConfigResponses.stream()
                    .filter(config -> config.getCategoryId().equalsIgnoreCase(DEFAULT_CATEGORY))
                    .sorted(Comparator.comparing(
                        bannerConfigResponse -> bannerConfigResponse.getConfig().get(CAROUSEL_ORDER).toString()))
                    .collect(Collectors.toList());

                orderedBannerConfigResponses.addAll(defaultBannerConfigResponses);
                orderedBannerConfigResponses.addAll(bannerConfigResponses.stream()
                    .filter(config -> !config.getCategoryId().equalsIgnoreCase(DEFAULT_CATEGORY))
                    .collect(Collectors.toList()));
            } catch (Exception ex) {
                LOGGER.error("Error occurred while setting the Carousel Order. Exception: ", ex);
            }

            return orderedBannerConfigResponses;
        } else {
            return bannerConfigResponses;
        }
    }

    /**
     * Add BannerConfigResponse by it's category
     *
     * @param configs
     * @param bannerConfigEntities
     * @param configType
     */
    private void addBannerConfigResponseByCategory(final List<BannerConfigResponse> configs,
        final List<BannerConfigEntity> bannerConfigEntities, final String configType) {

        final Map<String, String> categoriesMap = appSessionInfo.getCategories();
        Map<String, List<BannerConfigEntity>> configEntitiesByCategory = bannerConfigEntities
            .parallelStream()
            .collect(Collectors.groupingBy(BannerConfigEntity::getCategory));

        //The PRODUCT & FAMILY configType configs should get populated with the
        // displayOrder's order in BannerConfigResponse list
        if (MapUtils.isNotEmpty(configEntitiesByCategory) &&
                (PRODUCT.equalsIgnoreCase(configType) || FAMILY.equalsIgnoreCase(configType) || WHATS_NEW.equalsIgnoreCase(configType))) {
            //To get categories sorted by their 'displayOrder'
            final Map<String, List<BannerConfigEntity>> displayOrderedEntitiesByCategory =
                    getDisplayOrderSorted(bannerConfigEntities, configEntitiesByCategory);
            sortCategoriesByDisplayOrder(configs, displayOrderedEntitiesByCategory, configType, categoriesMap);
        }
    }

    //To get categories sorted by their 'displayOrder'
    private void sortCategoriesByDisplayOrder(final List<BannerConfigResponse> configs,
        final Map<String, List<BannerConfigEntity>> displayOrderedEntitiesByCategory, final String configType,
        final Map<String, String> categoriesMap) {

        if (MapUtils.isNotEmpty(displayOrderedEntitiesByCategory)) {
            displayOrderedEntitiesByCategory.forEach((category, configEntities) -> {
                if (PRODUCT.equalsIgnoreCase(configType) || WHATS_NEW.equalsIgnoreCase(configType) ||
                    StringUtils.isNotBlank(categoriesMap.get(category))) {
                    configs.add(buildBannerConfigResponse(category, configEntities.get(0).getConfigType(),
                        configEntities, categoriesMap));
                }
            });
        }
    }

    /**
     * Build BannerConfigResponse by it's category, config type, config entities & categoriesMap
     *
     * @param category
     * @param bannerType
     * @param bannerConfigEntities
     * @param categoriesMap
     * @return
     */
    private BannerConfigResponse buildBannerConfigResponse(final String category, final String bannerType,
                                                           final List<BannerConfigEntity> bannerConfigEntities,
                                                           final Map<String, String> categoriesMap) {
        return BannerConfigResponse.builder()
                .withCategoryId(category)
                .withCategoryName(categoriesMap.get(category))
                .withConfig(buildConfigs(bannerType, bannerConfigEntities))
                .withBannerType(bannerType)
                .build();
    }

    /**
     * Build configs for the BannerConfigResponse
     *
     * @param bannerType
     * @param bannerConfigEntities
     * @return
     */
    private Map<String, Object> buildConfigs(final String bannerType, final List<BannerConfigEntity> bannerConfigEntities) {
        try {
            if (CollectionUtils.isNotEmpty(bannerConfigEntities)) {
                final Map<String, Object> loadedConfigs = CollectionUtils.isEmpty(bannerConfigEntities) ? Map.of() :
                        bannerConfigEntities.stream()
                                .collect(Collectors.toMap(BannerConfigEntity::getName, BannerConfigEntity::getValue));
                return updateBooleanValue(loadedConfigs);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while building Configs for the Config Type:{} ", bannerType);
        }
        return Map.of();
    }

    //Check if the value is type of boolean, then convert it to value of boolean
    private Map<String, Object> updateBooleanValue(final Map<String, Object> loadedConfigs) {
        return Stream.of(loadedConfigs)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, v ->
                    //Check if the value is type of boolean, then convert it to value of boolean
                    v.getValue() instanceof String && BooleanUtils.toBooleanObject((String) v.getValue()) != null ?
                        Boolean.valueOf((String) v.getValue()) : v.getValue(),
                (v1, v2) -> null != v2 ? v2 : v1));
    }

    /**
     * To get the sorted configs based on their display Order
     *
     * @param bannerConfigEntities
     * @param configEntitiesByCategory
     * @return
     */
    private Map<String, List<BannerConfigEntity>> getDisplayOrderSorted(
        final List<BannerConfigEntity> bannerConfigEntities,
        final Map<String, List<BannerConfigEntity>> configEntitiesByCategory) {
        final Map<String, List<BannerConfigEntity>> displayOrderedEntitiesByCategory = new LinkedHashMap<>();

        if (CollectionUtils.isNotEmpty(bannerConfigEntities) && MapUtils.isNotEmpty(configEntitiesByCategory)) {
            final Optional<BannerConfigEntity> categoriesList = bannerConfigEntities.stream()
                .filter(bannerConfigEntity -> DISPLAY_ORDER.equalsIgnoreCase(bannerConfigEntity.getName()))
                .findAny();

            if (categoriesList.isPresent() && StringUtils.isNotBlank(categoriesList.get().getValue())) {
                for (String category : categoriesList.get().getValue().split(",")) {
                    if (configEntitiesByCategory.containsKey(category)) {
                        displayOrderedEntitiesByCategory.put(category, configEntitiesByCategory.get(category));
                    }
                }
            } else {
                LOGGER
                    .info("Banner's display order is not present or null for this banner type {}",
                        bannerConfigEntities.get(0).getConfigType());
            }
        }
        return displayOrderedEntitiesByCategory;
    }

    /**
     * Get Banner Config Entities grouping by Config Type, Category & by it's Name
     *
     * @param varId
     * @param programId
     * @param locale
     * @return
     */
    private Map<String, Map<String, Map<String, List<BannerConfigEntity>>>> getBannerConfigEntities(final String varId,
        final String programId, final String locale) {
        final List<String> varIds = List.of(DEFAULT_VAR_PROGRAM, varId);
        final List<String> programIds = List.of(DEFAULT_VAR_PROGRAM, programId);
        final List<String> locales = List.of(DEFAULT_VAR_PROGRAM, locale);

        return bannerConfigDao.findByVarProgramLocale(varIds, programIds, locales)
            .stream()
            .collect(Collectors.groupingBy(BannerConfigEntity::getConfigType,
                Collectors.groupingBy(BannerConfigEntity::getCategory,
                    Collectors.groupingBy(BannerConfigEntity::getName))));
    }

}