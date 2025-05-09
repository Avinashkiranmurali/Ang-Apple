package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.rewards.apple.dao.MercSearchFilterDao;
import com.b2s.rewards.apple.model.MercSearchFilter;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.domain.CategoryNode;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rpillai on 8/19/2015.
 */
@RestController
@RequestMapping(value = "/category", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
@SessionAttributes(CommonConstants.USER_SESSION_OBJECT)
@ResponseBody
@EnableScheduling
public class CategoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
    public static final String INVALID_INPUT = "Invalid input";
    public static final String ERROR_CALLING_DISABLE_CATEGORIES_SERVICE = "Error calling disableCategories service";
    public static final String ERROR_CALLING_ENABLE_CATEGORIES_SERVICE = "Error calling enableCategories service";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    CategoryConfigurationService categoryConfigurationService;

    @Autowired
    @Qualifier("productServiceV3Service")
    ProductServiceV3 productServiceV3;

    @Autowired
    private MercSearchFilterDao mercSearchFilterDao;

    @Value("${jobs.categorySync.disable}")
    private boolean categorySyncDisable;

    /**
     * This method reloads the category repository for the given locale\
     * This is a scheduled method runs every 15 minutes to check if the database has been changed.
     * Initial Delay of 5 minutes is given to let the server start and load the categories initially.
     * The method will check every 60 minutes
     */
    @Scheduled(initialDelayString = "${scheduleReload.categories.initialDelay}", fixedDelayString = "${scheduleReload.categories.fixedDelay}")
    public void scheduleReloadCategories() {
        try {
            LOGGER.info("RELOAD CATEGORIES... BEGIN ");
            if(!categorySyncDisable) {
                final Set<Locale> supportedLocales = categoryConfigurationService.getSupportedLocales();
                assert supportedLocales != null;
                supportedLocales.forEach(locale -> reloadCategories(locale));
            }
            LOGGER.info("RELOAD CATEGORIES... COMPLETE");
        } catch (final Exception e) {
            LOGGER.error("Error calling scheduled reloadCategories service", e);
        }
    }

    private void reloadCategories(final Locale locale) {
        LOGGER.info("Reloaded the Categories for the locale ", locale);
        final CategoryRepository categoryRepository = categoryConfigurationService.getCategoryRepository(locale);
        Collection<CategoryNode> categoryNodes = getCategoryNodes(locale);
        Collection<String> categorySlugsFromPS = null;
        if(CollectionUtils.isNotEmpty(categoryNodes)){
            categorySlugsFromPS = categoryConfigurationService.getAllCategories(categoryNodes, new HashSet<String>());
        }
        int currentActiveCategoriesCount = 0;
        if(CollectionUtils.isNotEmpty(categorySlugsFromPS)) {
            currentActiveCategoriesCount = categorySlugsFromPS.size();
        }

        final Collection<String> categorySlugsFromCache =
            Objects.nonNull(categoryRepository) ? categoryRepository.getAllSlugs() : new ArrayList<>();
        final int currentActiveCategoriesInDB = (categoryRepository != null) ? categoryRepository.getTotalActiveCategoriesInDB() : 0;
        if (currentActiveCategoriesCount != currentActiveCategoriesInDB) {
            LOGGER.info("Categories size mismatch. Hence Reloading Categories...");
            LOGGER.info("Current Active Categories.. {} for locale {}", currentActiveCategoriesCount, locale);
            LOGGER.info("Active Categories in Repository.. {} for locale {}", currentActiveCategoriesInDB, locale);
            LOGGER.info("Triggering now.....RELOAD CATEGORIES>.. for locale {}", locale);
            categoryConfigurationService.reloadCategories(locale);
        }else {
           if (CollectionUtils.isNotEmpty(categorySlugsFromPS) && CollectionUtils.isNotEmpty(categorySlugsFromCache)) {
                List<String> filteredList = categorySlugsFromPS.stream()
                    .filter(slugFromPS -> categorySlugsFromCache.stream()
                            .noneMatch(slugFromCache -> slugFromPS.equals(slugFromCache)))
                                .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(filteredList)){
                    String filteredStr = filteredList.stream().collect(Collectors.joining(", "));
                    LOGGER.info("Current Active Categories..{} for locale {}", currentActiveCategoriesCount, locale);
                    LOGGER.info("Categories slug name mismatch for the categories {} .Hence Reloading Categories...",
                        filteredStr);
                    LOGGER.info("Triggering now.....RELOAD CATEGORIES>.. for locale {}", locale);
                    categoryConfigurationService.reloadCategories(locale);
                }
            }
        }
        LOGGER.info("Successfully reloaded the Categories ", locale);
    }

    private Collection<CategoryNode> getCategoryNodes(final Locale locale) {
        LOGGER.info("Get the categories nodes for the locale ", locale);
        final String catalogPrefix = applicationProperties.getProperty("PS3_DEFAULT_CATALOG");
        String catalogId = null;
        if (StringUtils.isNotBlank(catalogPrefix) && locale != null) {
            catalogId = AppleUtil.getCategoryIdByLocale(catalogPrefix, locale);
        }
        LOGGER.info("Successfully get the categories nodes for the locale ", locale);
        return categoryConfigurationService.getCategoryNodeCountFromPS(catalogId, locale);
    }

    /**
     * This method reloads the category repository for the given locale\
     */
    @ResponseBody
    @RequestMapping(value = {"/reloadRepository"})
    public ResponseEntity<String> reloadCategories(@RequestParam(required = false, value = "locale")  String locale) {
        try {
            LOGGER.info("reloadCategories... BEGIN ");
            // htmlEscape throws an exception if locale is blank. We need to handle that use case
            final String localeEscaped = StringUtils.isNotBlank(locale) ? HtmlUtils.htmlEscape(locale) : null;
            if(StringUtils.isNotBlank(localeEscaped)) {
                categoryConfigurationService.reloadCategories(LocaleUtils.toLocale(localeEscaped));
            } else {
                categoryConfigurationService.loadCategoryRepositories();
            }
            LOGGER.info("reloadCategories... COMPLETE ");
            return new ResponseEntity<>("Category repository for locale " + localeEscaped + " reloaded", HttpStatus.OK);
        } catch (final Exception e) {
            LOGGER.error("Error calling reloadCategories service", e);
            return new ResponseEntity<>("Error calling disableNonExistingCategories service", HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Add disabled Categories, to Merc Filter table for tracking purpose
     * @param user
     * @param categoryIds
     */
    private void addToMercFilter(User user, Set<String> categoryIds ) {

        LOGGER.info("Adding Categories to merc filter: {}", categoryIds);
        try {
            MercSearchFilter filter = new MercSearchFilter();
            filter.setVarId(user.getVarId());
            filter.setProgramId(user.getProgramId());
            filter.setFilterType(CommonConstants.EXCLUDE);
            filter.setFilterName(CommonConstants.CATEGORY);
            filter.setIsActive(CommonConstants.YES_VALUE);
            filter.setAddedBy(user.getFullName());
            filter.setComment("This Category is excluded");

            categoryIds.stream().forEach((categoryId) -> {
                filter.setFilterValue(categoryId);
                filter.setAddedDate(new Date());
                mercSearchFilterDao.add(filter);
            });
        } catch ( Exception ex) {
            LOGGER.error("Exception in Adding Category to merc filter: ", ex);
        }

        LOGGER.info("Successfully added Categories to merc filter: {}", categoryIds);
    }

    /**
     * Remove Categories , from Merc Filter table
     * @param user
     * @param categoryIds
     */
    private void removeMercFilter(User user, Set<String> categoryIds ) {

        LOGGER.info("Removing Filters set on Categories from Merc filter: {}", categoryIds);
        try {
            categoryIds.stream().forEach((categoryId) -> {
                MercSearchFilter filter = mercSearchFilterDao.getByPK(user.getVarId(), user.getProgramId(), CommonConstants.EXCLUDE, CommonConstants.CATEGORY, categoryId);

                if (filter != null) {
                    mercSearchFilterDao.delete(filter);
                }

            });
        } catch ( Exception ex) {
            LOGGER.error("Exception while Removing Filters set on Categories from Merc filter: ", ex);
        }

        LOGGER.info("Successfully Removed Filters set on Categories from Merc filter: {}", categoryIds);
    }


}


