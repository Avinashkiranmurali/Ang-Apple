package com.b2s.apple.services;

import com.b2s.apple.entity.MerchantEntity;
import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.productservice.CategoryServiceV3;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.requests.productservice.CoreCategoryTaxonomyRequest;
import com.b2s.common.services.transformers.productservice.LocaleHelper;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.common.services.util.CategoryRepositoryHolder;
import com.b2s.common.services.util.ImageObfuscatory;
import com.b2s.common.services.util.MerchantRepositoryHolder;
import com.b2s.rewards.apple.dao.CategoryConfigurationDao;
import com.b2s.rewards.apple.dao.MerchantListDao;
import com.b2s.rewards.apple.dao.ProductAttributeConfigurationDao;
import com.b2s.rewards.apple.integration.model.ps.CategoryResponse;
import com.b2s.rewards.apple.model.Category;
import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.ProductAttributeConfiguration;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.domain.CategoryNode;
import com.b2s.service.product.common.domain.response.Facet;
import com.b2s.service.product.common.domain.response.ProductSearchResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by meddy on 7/7/2015.
 */
@Service
public class CategoryConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryConfigurationService.class);
    public static final String CATEGORY_NOT_ADDED_FOR_THE_SLUG_WITH_DEPTH =
            "Category not added for the slug {} with depth {}";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private CategoryConfigurationDao categoryConfigurationDao;

    @Autowired
    private ProductAttributeConfigurationDao productAttributeConfigurationDao;

    @Autowired
    private CategoryServiceV3 categoryServiceV3;

    @Autowired
    private CategoryRepositoryHolder categoryRepositoryHolder;

    @Autowired
    private MerchantRepositoryHolder merchantRepositoryHolder;

    @Autowired
    private ImageObfuscatory imageObfuscatory;

    @Autowired
    @Qualifier("productServiceV3Service")
    private ProductServiceV3 productServiceV3;

    @Autowired
    private MerchantListDao merchantDao;
    @Autowired
    private ProgramService programService;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private final Set<Locale> supportedLocales = null;

    @Value("#{'${accessoriesSlug}'.split(',')}")
    private List<String> accessoriesSlugs;

    public Set<Locale> getSupportedLocales() {
        Set<Locale> supportedLocales = this.supportedLocales;
        final String supportedLocalesStr = applicationProperties.getProperty("supportedLocales");
        if(StringUtils.isNotBlank(supportedLocalesStr)) {
            supportedLocales = Arrays.asList(supportedLocalesStr.split(",")).stream().map(LocaleUtils::toLocale).collect(Collectors.toSet());
        }
        return supportedLocales;
    }

    @Autowired
    @Qualifier("httpClientUtil")
    private HttpClientUtil httpClient;

    private static final Map<String, List<CategoryConfiguration>> CATEGORY_CONFIGURATION_MAP = new HashMap<>();

    private static final List<CategoryConfiguration> ALL_CATEGORY_CONFIGURATION = new ArrayList<>();

    @Autowired
    private VarProgramCatalogConfigService varProgramCatalogConfigService;

    public CategoryRepository getCategoryRepository(final Locale locale) {
        return categoryRepositoryHolder.getCategoryRepository(locale);
    }

    @PostConstruct
    public void loadCategoryRepositories() {
        ALL_CATEGORY_CONFIGURATION.clear();
        populateAllCategoryConfigurations();
        populateAllMerchant();
        final Set<Locale> supportedLocales = getSupportedLocales();
        if(CollectionUtils.isNotEmpty(supportedLocales)) {
            supportedLocales.forEach(this::loadCategoriesToRepository);
        }
    }

    /**
     * Loads either data from stub (or) invoking category service into repository.
     *
     * @throws ServiceException when category service did not return information for given locale (or) information is missing
     *                          while parsing category taxonomy response from category service (or) the repository size is not same when compared for
     *                          slug, browseNodeId and hierarchyFromRootNode
     */

    public void loadCategoriesToRepository(final Locale locale) {
        try {

            //For loading the category taxanomy use the default catalogId.CatalogId is required in the product
            //serviceV3.
            final String catalogPrefix = applicationProperties.getProperty("PS3_DEFAULT_CATALOG");
            final Map<String, CategoryInfo> categoriesBySlug = new HashMap<>();
            final Map<String, CategoryInfo> categoriesByHierarchyFromRootNode = new HashMap<>();
            final Map<String, Category> categoryHierarchyBySlug = new HashMap<>();
            final String psCatalogSuffix = applicationProperties.getProperty("PS_CATALOG_SUFFIX_"+locale.getLanguage()+"-"+locale.getCountry().toUpperCase());
            String catalogId = null;
            if(org.apache.commons.lang3.StringUtils.isNotBlank(psCatalogSuffix)){
                catalogId = catalogPrefix+psCatalogSuffix;
            }
            final LocaleHelper currentLocaleHelper = new LocaleHelper(Optional.ofNullable(locale));
            final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest
                    = new CoreCategoryTaxonomyRequest(locale, catalogId, true);
            // Setting default var for a catalog
            coreCategoryTaxonomyRequest.setVarId(categoryRepositoryHolder.getDefaultVarId(catalogId));
            final Collection<CategoryNode> categoryNodes = categoryServiceV3
                    .queryCategoryTaxonomyByLocale(coreCategoryTaxonomyRequest);
            if (Optional.ofNullable(categoryNodes).isPresent() && !categoryNodes.isEmpty()) {
                parseCategoryNodeTree(categoryNodes, currentLocaleHelper, null, categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug);
                addCategoryRepository(categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug, locale);
            } else {
                LOGGER.warn("No products in catalog {}", coreCategoryTaxonomyRequest.getDefaultCatalogId());
            }

        } catch (ServiceException se) {
            LOGGER.error("CATEGORY TAXONOMY NOT LOADED FROM SERVICE.", se);
            clearRepository(locale);
        } catch (Exception e) {
            LOGGER.error("CATEGORY TAXONOMY NOT LOADED.", e);
            clearRepository(locale);
        }
    }

    private void addCategoryRepository(Map<String, CategoryInfo> categoriesBySlug,
                                       Map<String, CategoryInfo> categoriesByHierarchyFromRootNode,
                                       Map<String, Category> categoryHierarchyBySlug, Locale locale) throws ServiceException {
        CategoryRepository  categoryRepository = new CategoryRepository(categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug);
        categoryRepositoryHolder.addCategoryRepository(locale, categoryRepository);
    }

    private void clearRepository(Locale locale) {
        CategoryRepository categoryRepository = categoryRepositoryHolder.getCategoryRepository(locale);
        if(categoryRepository != null) {
            categoryRepository.clearRepository();
        }
    }

    /**
     * This recursive method, Parses category taxonomy response from category service and populates to repository.
     *
     * @param categoryNodes       list of node from category service
     * @param currentLocaleHelper Holds a supported locale information.
     * @param parentCategory      holds parent category of current nodes.
     * @throws ServiceException when unable to parse response from service layer (or) unable to populate to repository.
     */

    private void parseCategoryNodeTree(final Collection<CategoryNode> categoryNodes,
                                       final LocaleHelper currentLocaleHelper,
                                       final CategoryInfo parentCategory,
                                       Map<String, CategoryInfo> categoriesBySlug,
                                       Map<String, CategoryInfo> categoriesByHierarchyFromRootNode,
                                       Map<String, Category> categoryHierarchyBySlug) throws ServiceException {
        if (Optional.ofNullable(categoryNodes).isPresent() && !categoryNodes.isEmpty()) {
            for (final CategoryNode categoryNode : categoryNodes) {
                if (Optional.ofNullable(parentCategory).isPresent()) {
                    // children
                    currentLocaleHelper.setHierarchyFromRootNode(parentCategory.getHierarchyFromRootNode() + '/' + categoryNode.getSlug());
                } else {
                    currentLocaleHelper.setHierarchyFromRootNode(categoryNode.getSlug());
                    currentLocaleHelper.setRootCategorySlug(categoryNode.getSlug());
                }
                final CategoryInfo categoryInfo = transformCurrentNode(categoryNode, currentLocaleHelper);
                addCategoryToRepository(categoryInfo, currentLocaleHelper, categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug);
                if (Optional.ofNullable(categoryNode.getChildren()).isPresent() && !categoryNode.getChildren().isEmpty()) {
                    //recursive call
                    parseCategoryNodeTree(categoryNode.getChildren(), currentLocaleHelper, categoryInfo, categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug);
                }
            }
        }
    }

    /**
     * Transforms current Node from category services to <code>CategoryInfo</code>
     *
     * @param categoryNode
     * @param currentLocaleHelper
     * @return
     * @throws ServiceException when unable to parse response from service layer
     */

    private CategoryInfo transformCurrentNode(final CategoryNode categoryNode, final LocaleHelper currentLocaleHelper) throws ServiceException {
        try {
            return categoryServiceV3.getResponseTransformer().transform(categoryNode, currentLocaleHelper, null);
        } catch (Exception e) {
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        }

    }

    /**
     * Adds category to repository by slug and browseNodeId
     *
     * @param categoryInfo category for adding into repository
     * @throws IllegalArgumentException when input is null.
     */

    private void addCategoryToRepository(final CategoryInfo categoryInfo, final LocaleHelper currentLocaleHelper,
                                         Map<String, CategoryInfo> categoriesBySlug,
                                         Map<String, CategoryInfo> categoriesByHierarchyFromRootNode,
                                         Map<String, Category> categoryHierarchyBySlug) {
        Locale locale = currentLocaleHelper.getUserLanguage().orElse(null);
        if (Optional.ofNullable(categoryInfo).isEmpty()) {
            throw new IllegalArgumentException(ServiceExceptionEnums.CATEGORY_INFO_ABSENT.getErrorMessage());
        }
        if (Optional.ofNullable(categoriesBySlug.get(categoryInfo.getSlug())).isPresent()) { // Already present in the repo, then it might be different local.
            final CategoryInfo categoryInfoFromRepo = categoriesBySlug.get(categoryInfo.getSlug());
            categoryInfo.getLocalizedName(locale).ifPresent(categoryName ->
                    categoryInfoFromRepo.addLocalizedNameForThisLocale(categoryName, locale));
        } else {
            categoriesBySlug.put(categoryInfo.getSlug(), categoryInfo);
            categoriesByHierarchyFromRootNode.put(categoryInfo.getHierarchyFromRootNode(), categoryInfo);
            addCategoryHierarchyBySlug(categoryInfo, locale, categoriesBySlug, categoryHierarchyBySlug);
        }
    }

    /**
     * From the parsed CategoryNodes, creates hierarchical structure for each category with parents.
     * Parents will have all subcategories, whereas each subcategory will not have parent information. Otherwise it
     * will become a cyclic reference
     *
     * @param categoryInfo
     * @param locale
     */
    private void addCategoryHierarchyBySlug(CategoryInfo categoryInfo, Locale locale, Map<String, CategoryInfo> categoriesBySlug, Map<String, Category> categoryHierarchyBySlug){

        //TODO Check to see if category slug is in the db and active

        if (categoryHierarchyBySlug.get(categoryInfo.getSlug()) == null) {

            Category newCategory = buildCategory(categoryInfo, locale, categoriesBySlug);
            String newCategorySlug = newCategory.getSlug();
            String parentSlug = getImmediateParentSlug(categoryInfo);
            List<CategoryConfiguration> categoryConfigurations = getCategoryConfigurationByCategoryName(newCategorySlug);
            boolean accessorySubCategory = false;

            if(newCategory.getDepth()==2){
                accessorySubCategory = true;
            }

            if(CollectionUtils.isNotEmpty(categoryConfigurations) || accessorySubCategory) {
                if (!parentSlug.equals(newCategorySlug)) {
                    createCategory(locale, categoryHierarchyBySlug, newCategory,
                            newCategorySlug, parentSlug, categoryConfigurations, accessorySubCategory);

                } else {
                    populateCategory(newCategory, categoryConfigurations);
                    // add to root category, if root category is active
                    if(newCategory.isActive()) {
                        categoryHierarchyBySlug.put(newCategorySlug, newCategory);
                    }
                }
            }else{
                LOGGER.warn(CATEGORY_NOT_ADDED_FOR_THE_SLUG_WITH_DEPTH, newCategorySlug, newCategory.getDepth());
            }
        }
    }

    private void createCategory(Locale locale, Map<String, Category> categoryHierarchyBySlug, Category newCategory, String newCategorySlug, String parentSlug, List<CategoryConfiguration> categoryConfigurations, boolean accessorySubCategory) {
        Category parentCategory = categoryHierarchyBySlug.get(parentSlug);
        // add parent information to new category and add to Map
        List<Category> parents = new ArrayList<>();
        parents.add(parentCategory);
        newCategory.setParents(parents);

        if(CollectionUtils.isNotEmpty(categoryConfigurations)){
            populateCategory(newCategory, categoryConfigurations);
        }else if(accessorySubCategory){
            additionalCategory(newCategory);
        }else{
            LOGGER.warn(CATEGORY_NOT_ADDED_FOR_THE_SLUG_WITH_DEPTH, newCategorySlug, newCategory.getDepth());
        }

        // add category if it is active
        if(newCategory.isActive()) {
            categoryHierarchyBySlug.put(newCategorySlug, newCategory);
        }

        //Now, add child information to parent and update Map
        addSubCategory(categoryHierarchyBySlug, newCategory, parentSlug, parentCategory);
    }

    private void addSubCategory(Map<String, Category> categoryHierarchyBySlug, Category newCategory, String parentSlug, Category parentCategory) {
        List<Category> subCategories = null;
        if(parentCategory != null) {
            subCategories = parentCategory.getSubCategories();
        }
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
        // To avoid nested reference remove parent information in subcategory, otherwise
        // parent information in a subcategory will have nested reference through its subcategories
        // add to sub categories if a sub category is active
        if(newCategory.isActive()) {
            Category subCategoryWithoutParent = new Category(newCategory);
            subCategoryWithoutParent.setParents(new ArrayList<>());
            subCategories.add(subCategoryWithoutParent);
        }
        if(parentCategory != null) {
            parentCategory.setSubCategories(subCategories);
            if (parentCategory.isActive()) {
                categoryHierarchyBySlug.put(parentSlug, parentCategory);
            }
        }
    }

    private Category additionalCategory(Category category){
        category.setTemplateType("CATEGORYLIST");
        category.setDisplayOrder(99);
        category.setDefaultImage("");
        category.setConfigurable(false);
        category.setImageUrl("");
        category.setEngraveBgImageLocation(null);
        category.setSummaryIconImage("");
        category.setIsNew(false);
        category.setIsActive(true);
        return category;

    }

    private void populateCategory(Category category, List<CategoryConfiguration> categoryConfigurations) {
        if(category != null && CollectionUtils.isNotEmpty(categoryConfigurations)) {
            for(CategoryConfiguration categoryConfiguration : categoryConfigurations) {
                category.setTemplateType(getTemplateType(categoryConfiguration, category));
                category.setDisplayOrder(categoryConfiguration.getOrderBy());
                category.setDefaultImage(categoryConfiguration.getDefaultProductImage());
                category.setConfigurable(categoryConfiguration.isConfigurable());
                category.setImageUrl(categoryConfiguration.getImageUrl());
                category.setEngraveBgImageLocation(categoryConfiguration.getEngraveBgImageLocation());
                category.setSummaryIconImage(categoryConfiguration.getSummaryImageIconUrl());
                category.setIsNew(categoryConfiguration.isNew());
                category.setIsActive(categoryConfiguration.isActive());
            }
        }
    }

    // Get immediate parent slug name
    private String getImmediateParentSlug(CategoryInfo categoryInfo) {
        String[] parentHierarchy = categoryInfo.getHierarchyFromRootNode().split("/");
        return (parentHierarchy.length > 1 ? parentHierarchy[parentHierarchy.length-2] : parentHierarchy[0] );
    }

    /**
     * This method gets the templatetype for a slug, if found. Otherwise it will get the default templateType of the parent
     *
     * @param currentCategory
     * @return templateType if found, otherwise get the  template type of from the parent
     */
    private String getTemplateType(CategoryConfiguration categoryConfiguration, Category currentCategory) {
        String templateType = "";
        //get template type from TemplateMap
        templateType = categoryConfiguration.getTemplate();

        //get Template from its Parent Category if template type is not available for current category
        if (StringUtils.isBlank(templateType)) {
            List<Category> parentCategory = currentCategory.getParents();
            if (CollectionUtils.isNotEmpty(parentCategory)) {
                CategoryConfiguration parentConfiguration =
                        getCategoryConfigurationByCategoryName(parentCategory.get(0).getSlug(), null);
                if (Objects.nonNull(parentConfiguration)) {
                    if (!StringUtils.isBlank(parentConfiguration.getDefaultTemplate())) {
                        templateType = parentConfiguration.getDefaultTemplate();
                    } else {
                        templateType = parentConfiguration.getTemplate();
                    }
                }
            }
        }
        return templateType;
    }

    /**
     * Construct Category
     *
     * @param categoryInfo
     * @param locale
     * @return
     */
    private Category buildCategory (CategoryInfo categoryInfo, Locale locale, Map<String, CategoryInfo> categoriesBySlug) {
        Category category = new Category();
        String slug = categoryInfo.getSlug();
        category.setSlug(slug);
        category.setDepth(categoryInfo.getDepth());
        Optional<String> localizedName = getCategoryDisplayNameBySlug(slug, locale, categoriesBySlug);
        localizedName.ifPresent(name -> {
                    category.setName(name);
                    category.setI18nName(name);
                }
        );
        return category;
    }

    private Optional<String> getCategoryDisplayNameBySlug(final String slug, final Locale displayNameLocale,  Map<String, CategoryInfo> categoriesBySlug) {
        if (Optional.ofNullable(slug).isEmpty()) {
            return Optional.empty();
        }
        if (Optional.ofNullable(displayNameLocale).isEmpty()) {
            return Optional.empty();
        }
        try {
            displayNameLocale.getISO3Language();
        } catch (MissingResourceException mre) {
            return Optional.empty();
        }
        if (Optional.ofNullable(categoriesBySlug.get(slug)).isEmpty()) {
            return Optional.empty();
        }
        return categoriesBySlug.get(slug).getLocalizedName(displayNameLocale);
    }

    public List<Category> getParentCategories(Program program, Locale locale) throws ServiceException {
        Map<String, String> categorySlugMap = new HashMap<>();
        CategoryRepository categoryRepository = getCategoryRepository(locale);
        List<Category> categories = null;
        List<String> brandFilteredCategory;
        if(categoryRepository != null) {
            categoryRepository.setImageObfuscatory(imageObfuscatory);
            categoryRepository.setProductServiceV3(productServiceV3);
            categoryRepository.setProgramService(programService);

            categories = categoryRepository.getParentCategories(program.getVarId(), program.getProgramId(),program, locale.toString());
        } else {
            LOGGER.error("{} is not present in supported locales", locale);
        }

        if(CollectionUtils.isNotEmpty(categories)) {
            CategoryResponse categoryResponse = null;
            // get the catalog id from program config, which is derived from var_program_config entry for the name 'catalog_id'
            final String catalogId = (String) program.getConfig().get(CommonConstants.CONFIG_CATALOG_ID);
            final String programId = (String) program.getConfig().getOrDefault(CommonConstants.DEFAULT_PS_PROGRAM, program.getProgramId());
            final String varId = (String) program.getConfig().getOrDefault(CommonConstants.DEFAULT_PS_VAR, program.getVarId());
            StringBuilder urlBuilder = new StringBuilder(applicationProperties.getProperty("PS3_HTTP_URL"))
                    .append("categories/")
                    .append(catalogId)
                    .append("?onlyWithProducts=true&varId=")
                    .append(varId)
                    .append("&programId=")
                    .append(programId);
            String shopName = (String) program.getConfig().get(CommonConstants.SHOP_NAME_KEY);
            if(StringUtils.isNotBlank(shopName)) {
                urlBuilder.append("&promoTag=").append(shopName);
            }
            try {
                categoryResponse = httpClient.getHttpResponse(urlBuilder.toString(), CategoryResponse.class, HttpMethod.GET, null);

                if(categoryResponse != null) {
                    // Accessories subcategory filtered by brand
                    brandFilteredCategory=filterAccessoriesCategoryByBrand(program,locale);

                    categories = filterCategories(categories, categoryResponse.getResponse(), locale, brandFilteredCategory);
                    List<String> excludedCategories = varProgramCatalogConfigService
                            .getListOfValue(catalogId, varId, programId, CommonConstants.EXCLUDE_CATEGORY);
                    categories = excludeCategories(categories, excludedCategories);

                    categorySlugMap = categories.stream()
                        .collect(Collectors.toMap(Category::getSlug, Category::getI18nName));

                    categorySlugMap.putAll(categories.stream()
                        .flatMap(category -> category.getSubCategories().stream())
                        .collect(Collectors.toMap(Category::getSlug, Category::getI18nName)));

                }
            } catch (Exception e) {
                LOGGER.error(
                        "Error while getting category nodes for var id: {}, program id: {}, shop name : {}. Exception: ",
                        program.getVarId(), program.getProgramId(), shopName, e);
            }
        }

        httpSession.setAttribute(CommonConstants.PS_CATEGORIES, categorySlugMap);
        return categories;
    }

    private List<Category> filterCategories(List<Category> categories, Collection<CategoryNode> categoryNodes,
                                            Locale locale, List<String> brandFilteredCategory) {
        List<Category> newCategories = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(categories) && CollectionUtils.isNotEmpty(categoryNodes)) {//check for new one
            for(CategoryNode categoryNode : categoryNodes) {
                addCategoryFromNode(categories, locale,
                        brandFilteredCategory, newCategories, categoryNode);
            }
        }
        if(CollectionUtils.isNotEmpty(newCategories)) {
            setNewDisplayOrder(newCategories, locale);
        }
        return newCategories;
    }

    private void addCategoryFromNode(List<Category> categories, Locale locale, List<String> brandFilteredCategory, List<Category> newCategories, CategoryNode categoryNode) {
        boolean isCategoryAdded = false;
        for(Category category : categories) {
            if(category.getSlug().equalsIgnoreCase(categoryNode.getSlug())) {
                category.setSubCategories(filterCategories(category.getSubCategories(),
                        categoryNode.getChildren(),locale,brandFilteredCategory));

                filterCategoryByBrand(brandFilteredCategory, category);
                newCategories.add(category);
                isCategoryAdded = true;
            }
        }
        if (!isCategoryAdded) {
            if (categoryNode.getDepth() == 2) {
                LOGGER.info("New category added {}", categoryNode.getSlug());
                newCategories.add(buildTempCategory(categoryNode));
            } else {
                LOGGER.warn("The category {} in depth {} is NOT considered", categoryNode.getSlug(),
                        categoryNode.getDepth());
            }
        }
    }

    // Filter accessories category by brand
    private void filterCategoryByBrand(List<String> brandFilteredCategory, Category category) {
        if (accessoriesSlugs.stream().anyMatch(s -> category.getSlug().startsWith(s)) && CollectionUtils.isNotEmpty(brandFilteredCategory)) {
            category.setSubCategories(category.getSubCategories().stream().filter(cat -> brandFilteredCategory.contains(cat.getSlug())).collect(
                    Collectors.toList()));
        }
    }

    /**
     * This method removes slugs at depth 1 and 2. It does a string comparison with a list from the
     * database table var_program_catalog_config.
     * It uses recursion. It starts at depth 0 and immediately invokes recursion with sub-categories.
     * It uses the same pattern as in the method filterCategories()
     *
     * @param categories The list of categories with hero products at depth 0.
     * @param excludedCategoryList The list of slug names which are to be removed from the categories
     * @return trimmed categories if match found
     */
    public static List<Category> excludeCategories(List<Category> categories, List<String> excludedCategoryList) {
        if(CollectionUtils.isNotEmpty(excludedCategoryList)) {
            List<Category> newCategories = new ArrayList<>();
            for(Category category : categories) {
                category.setSubCategories(excludeCategories(category.getSubCategories(), excludedCategoryList));
                category.setSubCategories(category.getSubCategories().stream().
                        filter(cat -> {
                            if(!(excludedCategoryList.contains(cat.getSlug()))) {
                                return true;
                            } else {
                                LOGGER.warn("excludeCategory for slug "
                                        + cat.getSlug() + " with depth " + cat.getDepth()
                                        + " found and hence removed");
                                return false;
                            }
                        }).
                        collect(Collectors.toList()));
                newCategories.add(category);
            }
            return newCategories;
        } else {
            return categories;
        }
    }

    private Category buildTempCategory (CategoryNode categoryNode) {
        Category category = new Category();
        String slug = categoryNode.getSlug();
        category.setSlug(slug);
        category.setDepth(categoryNode.getDepth());
        category.setName(categoryNode.getName());
        category.setI18nName(categoryNode.getName());
        return additionalCategory(category);
    }

    /**
     * Sort Categories in expected display order
     * Depth 0 and Depth 1 should be sorted based on DisplayOrder value
     * Depth 2 should be sorted based on Name
     *
     * @param subCategories
     */
    private void setNewDisplayOrder(List<Category> subCategories, Locale locale) {
        try {
            Collections.sort(subCategories, new Comparator<Category>() {
                @Override
                public int compare(Category a1, Category a2) {
                    if (Objects.nonNull(a1) && Objects.nonNull(a2) && Objects.nonNull(a1.getDepth()) &&
                            Objects.nonNull(a2.getDepth()) && a1.getDepth().equals(a2.getDepth())) {
                        Integer collator = compareCategoryByDepth(a1, a2, locale);
                        if (collator != null) {
                            return collator;
                        }
                    }
                    return 0;
                }
            });
        }
        catch (Exception ex){
            LOGGER.error("Error occurred while setting the display order. Exception: ",ex);
        }
    }

    private Integer compareCategoryByDepth(Category a1, Category a2, Locale locale) {
        if(a1.getDepth()==0 || a1.getDepth()==1){
            if(Objects.nonNull(a1.getDisplayOrder()) && Objects.nonNull(a2.getDisplayOrder())) {
                return a1.getDisplayOrder().compareTo(a2.getDisplayOrder());
            }
        }else if(a1.getDepth()==2){
            if(Objects.nonNull(a1.getI18nName()) && Objects.nonNull(a2.getI18nName())) {
                Collator collator = Collator.getInstance(locale);
                return collator.compare(a1.getI18nName(), a2.getI18nName());
            }
        }else{
            LOGGER.warn("Depth must be 0,1 or 2. But Actual depth is ({},{})", a1.getDepth(), a2.getDepth());
        }
        return null;
    }


    /**
     * Ths method gets all categoy configuration from database
     *
     * @return
     */
    public Collection<CategoryConfiguration> getCategoryConfigurations() {
        if(CollectionUtils.isEmpty(ALL_CATEGORY_CONFIGURATION)){
            final List<CategoryConfiguration> categoryConfigurations = categoryConfigurationDao.getAllCategoryConfiguration();
            if(CollectionUtils.isNotEmpty(categoryConfigurations)) {
                ALL_CATEGORY_CONFIGURATION.addAll(categoryConfigurations.stream()
                        .filter(CategoryConfiguration::isActive)
                        .collect(Collectors.toList()));
            }
        }
        return ALL_CATEGORY_CONFIGURATION;
    }

    public CategoryConfiguration getCategoryConfiguration(String categoryName) {
        return categoryConfigurationDao.getCategoryConfigurationByName(categoryName);
    }


    /**
     * Get the category configuration by name
     * @param categoryName
     * @return
     */
    public List<CategoryConfiguration> getCategoryConfigurationByCategoryName(String categoryName) {
        if(MapUtils.isEmpty(CATEGORY_CONFIGURATION_MAP) || !CATEGORY_CONFIGURATION_MAP.containsKey(categoryName)) {
            populateAllCategoryConfigurations();
        }
        return CATEGORY_CONFIGURATION_MAP.get(categoryName);
    }

    /**
     * Get the category configuration by name
     * @param categoryName
     * @return
     */
    public CategoryConfiguration getCategoryConfigurationByCategoryName(final String categoryName, final String psid) {
        CategoryConfiguration categoryConfiguration = null;
        if (MapUtils.isEmpty(CATEGORY_CONFIGURATION_MAP) || !CATEGORY_CONFIGURATION_MAP.containsKey(categoryName) ||
                ( StringUtils.isNotBlank(psid) && !CATEGORY_CONFIGURATION_MAP.containsKey(psid))) {
            populateAllCategoryConfigurations();
        }

        List<CategoryConfiguration> categoryConfigurations = null;
        if (CATEGORY_CONFIGURATION_MAP.containsKey(psid)) {
            categoryConfigurations = CATEGORY_CONFIGURATION_MAP.get(psid);
        } else {
            categoryConfigurations = CATEGORY_CONFIGURATION_MAP.get(categoryName);
        }

        if(CollectionUtils.isNotEmpty(categoryConfigurations)) {
            categoryConfiguration = categoryConfigurations.get(0);
        }
        return categoryConfiguration;
    }

    public void populateAllCategoryConfigurations() {
        CATEGORY_CONFIGURATION_MAP.clear();
        Collection<CategoryConfiguration> categoryConfigurations = getCategoryConfigurations();
        if (CollectionUtils.isNotEmpty(categoryConfigurations)) {
            CATEGORY_CONFIGURATION_MAP.putAll(categoryConfigurations.stream().collect(Collectors.groupingBy(CategoryConfiguration::getCategoryName)));
        }
    }

    public void reloadCategories(Locale locale){
        ALL_CATEGORY_CONFIGURATION.clear();
        populateAllCategoryConfigurations();
        populateAllMerchant();
        loadCategoriesToRepository(locale);
    }

    /**
     * Get the product attributes for the category
     * @param categoryName
     * @return
     */
    public Set<ProductAttributeConfiguration> getProductAttributes(String categoryName) {
        CategoryConfiguration categoryConfiguration = getCategoryConfigurationByCategoryName(categoryName, null
        );
        if (categoryConfiguration != null) {
            //FIX FOR Lazy Loading.
            return new HashSet(productAttributeConfigurationDao.findByCategoryConfiguration(categoryConfiguration));
        }
        return new HashSet<>();
    }

    public List<ProductAttributeConfiguration> getProductDetailsAttributesWithCategoryNull(String categoryName) {
        final CategoryConfiguration categoryConfiguration = getCategoryConfigurationByCategoryName(categoryName, null
        );

        return productAttributeConfigurationDao.findByCategoryConfigurationAndCategorySlugNullAndDetails(categoryConfiguration);

    }
    /**
     * This method returns flag to identify whether a product attribute is available for search or detail
     *
     * @param facetName
     * @param facetValue
     * @param isFacetForSearchPage
     * @return
     */
    public boolean showFacet(String facetName, String facetValue, boolean isFacetForSearchPage,
                             Collection<ProductAttributeConfiguration> productAttributes) {

        // By Default all facets will be displayed unless it is restricted in product_attribute_configuration DB table
        if (CollectionUtils.isEmpty(productAttributes)) {
            //Mapping must be present to control
            return true;
        }

        for (ProductAttributeConfiguration productAttribute : productAttributes) {
            if (StringUtils.isNotBlank(facetName) && StringUtils.isNotBlank(productAttribute.getAttributeType()) &&
                    CommonConstants.OPTIONS.equalsIgnoreCase(productAttribute.getAttributeType()) &&
                    StringUtils.isNotBlank(productAttribute.getAttributeName()) &&
                    (productAttribute.getAttributeName().equals(facetName) || (StringUtils.isNotBlank(facetValue) &&
                            productAttribute.getAttributeName().equalsIgnoreCase(facetName + "_" + facetValue)))) {
                if (isFacetForSearchPage) {
                    return productAttribute.isAvailableForSearch();
                } else {
                    return productAttribute.isAvailableForDetail();
                }
            }
        }
        return true;
    }

    public void populateAllMerchant() {
        merchantRepositoryHolder.getMerchantRepository().clear();
        final List<MerchantEntity> merchant = merchantDao.getAll();
        if (CollectionUtils.isNotEmpty(merchant)) {
            merchantRepositoryHolder.addMerchantRepository(merchant.stream().collect(Collectors.groupingBy(
                    MerchantEntity::getSimpleName)));
        }
    }

    public Collection<CategoryNode> getCategoryNodeCountFromPS(final String catalogId, final Locale locale) {
        try {
            final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest
                    = new CoreCategoryTaxonomyRequest(locale, catalogId, true);
            coreCategoryTaxonomyRequest.setVarId(categoryRepositoryHolder.getDefaultVarId(catalogId));
            return categoryServiceV3.queryCategoryTaxonomyByLocale(coreCategoryTaxonomyRequest);
        } catch(Exception e) {
            LOGGER.error("Error while getting category nodes from PS for catalogId: {} and locale: {}. Exception: ", catalogId, locale, e);
        }
        return null;
    }

    public Set<String> getAllCategories(Collection<CategoryNode> categoryNodes, Set<String> categorySlugs){
        if(CollectionUtils.isNotEmpty(categoryNodes)){
            for(CategoryNode categoryNode : categoryNodes) {
                if(CollectionUtils.isNotEmpty(categoryNode.getChildren())) {
                    categorySlugs.add(categoryNode.getSlug());
                    getAllCategories(categoryNode.getChildren(), categorySlugs);
                }else {
                    categorySlugs.add(categoryNode.getSlug());
                }
            }
        }
        return categorySlugs;
    }

    //To get the categories count from PS response
    public int getCategoryNodeCountFromPS(Collection<CategoryNode> categoryNodes) {
        int count = 0;
        if(CollectionUtils.isNotEmpty(categoryNodes)) {
            count = categoryNodes.size();
            for(CategoryNode categoryNode : categoryNodes) {
                if(CollectionUtils.isNotEmpty(categoryNode.getChildren())) {
                    count += getCategoryNodeCountFromPS(categoryNode.getChildren());
                }
            }
        }
        return count;
    }

    /**
     * Extract and populate leaf node categories
     *
     * @param categoryNodes
     * @param categories
     */
    public void flattenCategoryNodes(final Collection<CategoryNode> categoryNodes, final List<CategoryNode> categories) {
        if(categoryNodes == null || categories == null) {
            return;
        } else {
            for(CategoryNode categoryNode : categoryNodes) {
                categories.add(categoryNode);
                if(categoryNode.getChildren() != null && !categoryNode.getChildren().isEmpty()) {
                    flattenCategoryNodes(categoryNode.getChildren(), categories);
                }
            }
        }
    }

    private List<String> filterAccessoriesCategoryByBrand(Program program,Locale locale){

        final Set <String> categories=new HashSet<>(accessoriesSlugs);

        final ProductSearchRequest.Builder builder =productServiceV3.getProductSearchRequestBuilder(categories,
                null, null, null, locale, null, 0, null, program, null, false,false,null);

        ProductSearchResponse productSearchResponse = null;
        try {
            productSearchResponse = productServiceV3.searchProducts(builder.build());
        }catch (RuntimeException ex){
            LOGGER.error("filterAccessoriesCategoryByBrand :  Error while fetching PS response",ex);
        }

        if(Objects.isNull(productSearchResponse)){
            return null;
        }

        return productSearchResponse.getFacets().stream()
                .filter(facet -> facet.getName().equalsIgnoreCase(CommonConstants.CATEGORIES))
                .map(Facet::getEntries)
                .flatMap(Collection::stream)
                .map(facetEntry -> facetEntry.getValue().substring(facetEntry.getValue().lastIndexOf("/")+1))
                .collect(Collectors.toList());

    }

}