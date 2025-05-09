package com.b2s.rewards.apple.controller;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.apple.services.DetailService;
import com.b2s.apple.services.SearchRedirectService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.MercSearchFilterDao;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.changes.SaveChangeRequest;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.Audience;
import com.b2s.service.product.client.domain.SortField;
import com.b2s.service.product.client.domain.SortOrder;
import com.b2s.service.product.client.domain.change.ChangeEntry;
import com.b2s.service.product.client.domain.change.Filter;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import com.b2s.service.product.client.exception.RequestValidationException;
import com.b2s.service.product.common.domain.change.PayloadType;
import com.b2s.service.product.common.domain.change.PayloadWithTranslations;
import com.b2s.shop.common.User;
import com.b2s.shop.util.VarProgramConfigHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * All Search, Browse, Detail of products are in this controller
 *
 * @author Ssrinivasan
 */
@RestController
@RequestMapping(value="/", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
@ResponseBody
public class ProductsController {

    private static final Logger logger = LoggerFactory.getLogger(ProductsController.class);

    @Value("${disableCustomJSONFormatting}")
    private String disableCustomJSONFormatting;

    @Autowired
    private DetailService detailService;

    @Autowired
    @Qualifier("productServiceV3Service")
    ProductServiceV3 productServiceV3;

    @Autowired
    CategoryConfigurationService categoryConfigurationService;

    @Autowired
    private MercSearchFilterDao mercSearchFilterDao;

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    @Autowired
    private SearchRedirectService searchRedirectService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    @Autowired
    private Properties applicationProperties;

    @ResponseBody
    @RequestMapping(value = {"/reloadCategoryPrice"})
    public ResponseEntity<String> loadCategoryPrices() {
        try {
            productServiceV3.populateCategoryPrices();
            return new ResponseEntity<>("Category price reloaded", HttpStatus.OK);
        } catch(Exception e) {
            logger.error("Error calling loadCategoryPrices service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error calling loadCategoryPrices service");
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/categories"})
    public ResponseEntity<List<Category>> getCategories(HttpServletRequest servletRequest) {
        List<Category> categories = new ArrayList<>();
        final User user = appSessionInfo.currentUser();
        try {
            Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            categories = categoryConfigurationService.getParentCategories(program, user.getLocale());
            if(logger.isDebugEnabled()) {
                logger.debug("Printing categories in getCategories method:{}", new Gson().toJson(categories));
            }
            return new ResponseEntity<>(categories, HttpStatus.OK);
        }catch (final ServiceException se) {
            logger.error("Error calling getCategories service", se);
            return new ResponseEntity<>(categories, HttpStatus.NO_CONTENT);
        }

    }

    /**
     * Request Method -- POST
     * Get Products based on Multiple Category slugs
     * Created this method(POST) to retrieve products based on Multiple category slugs
     * If UI modified all the existing requests to this newly introduced POST call, we will deprecate existing GET call
     *
     * @param productRequest - @ProductRequest object
     * @param user - @User object
     * @param servletRequest - @HttpServletRequest object
     * @return - Products based on Multiple Category slugs
     */
    @ResponseBody
    @RequestMapping(value = {"/filterProducts"}, method = RequestMethod.POST)
    public ResponseEntity<Object> filterProducts(@RequestBody final ProductsRequest productRequest,
        final HttpServletRequest  servletRequest){
        final User user = appSessionInfo.currentUser();

        return getProducts(productRequest.getCategorySlugs(), productRequest.getMinPoints(),
                productRequest.getMaxPoints(),productRequest.getPageSize(), productRequest.getResultOffSet(),
                productRequest.getKeyword(), productRequest.getPromoTag(), productRequest.getSort(),
                productRequest.isWithVariations(), user, servletRequest,
                productRequest.getOrder(),productRequest.isWithFacets(),productRequest.getFacetsFilters(),productRequest.isWithProducts());
    }

    /**
     * Created this common method getProducts for both GET and POST calls to refer
     * @return - Products based on Multiple Category slugs
     */
    public ResponseEntity<Object> getProducts(final Set<String> categorySlugs,final Integer minPoints,
                                         final Integer maxPoints, final Integer pageSize, final Integer resultOffSet, final String keyword,
                                         final String promoTag, final String sort, final boolean withVariations, final User user,
                                         final HttpServletRequest servletRequest, String order,
                                         final boolean withFacets, Map<String, List<Option>> facetsFilters, final boolean withProducts){
        ProductResponse productResponse = null;
        Integer[] pointsRange = null;

        try {
            //validate request
            if(CollectionUtils.isEmpty(categorySlugs)
                && StringUtils.isBlank(keyword)
                && StringUtils.isBlank(promoTag)
                && (minPoints == null && maxPoints == null)) {
                return ResponseEntity.badRequest().body(
                    "Invalid request in browse products...one must be present Keyword(q), Category Slug(categorySlug)" +
                        ", Points Range (minPoints & maxPoints) )...  ");
            }
            final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);


            if (minPoints != null || maxPoints != null) {
                pointsRange = getPointsRange(minPoints, maxPoints);
                if (pointsRange == null) {
                    return ResponseEntity.badRequest()
                        .body("Min and Max points range is incorrect. Please enter valid values for both. ");
                }
            }

            order = getSortOrder(sort, order);

            return processProductResponse(categorySlugs, pageSize, resultOffSet, keyword, promoTag, sort,
                withVariations, user, order, withFacets, facetsFilters, pointsRange, program, withProducts);
        } catch (EntityNotFoundException enfe) {
            logger.error("No such product available", enfe);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No such product available for the given slugs: "+ categorySlugs.stream().collect(Collectors.joining(",")));
        } catch (RequestValidationException rve) {
            logger.error("Invalid request in browse products...", rve);
            return ResponseEntity.badRequest().body("Invalid request in browse products... "+ rve.getMessage());
        } catch (final ServiceException se) {
            logger.error("Error calling product search service", se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Object> processProductResponse(final Set<String> categorySlugs, final Integer pageSize,
        final Integer resultOffSet, final String keyword, final String promoTag, final String sort,
        final boolean withVariations, final User user, final String order,final boolean withFacets,
        final Map<String, List<Option>> facetsFilters, final Integer[] pointsRange, final Program program, boolean withProducts)
        throws ServiceException {
        final ProductResponse productResponse;
        String searchKeyword = StringUtils.isNotBlank(keyword) ? keyword.trim() : null;
        final SearchRedirectModel.Builder searchRedirectBuilder = SearchRedirectModel.builder();
        SearchRedirect searchRedirect = null;

        if (StringUtils.isNotBlank(searchKeyword)) {
            final String catalogId = program.getCatalogId();
            searchRedirect = searchRedirectService.getSearchRedirect(user, catalogId, keyword);

            if (Objects.nonNull(searchRedirect)) {
                if (CommonConstants.ALTERNATE.equalsIgnoreCase(searchRedirect.getActionType())) {
                    searchKeyword = searchRedirect.getValue();
                    logger.info("SEARCH_ALTERNATE : Original keyword {}, alternate keyword {}", searchKeyword, keyword);
                } else if (CommonConstants.REDIRECT.equalsIgnoreCase(searchRedirect.getActionType())) {
                    productResponse = new ProductResponse();
                    searchRedirectBuilder.withRedirectURL(searchRedirect.getValue());
                    productResponse.setSearchRedirect(searchRedirectBuilder.build());
                    logger.info("SEARCH_REDIRECT : Search Keyword {} redirects the user to {}", searchKeyword,
                        searchRedirect.getValue());
                    return ResponseEntity.ok(Boolean.valueOf(disableCustomJSONFormatting) ? productResponse :
                        toJson(productResponse));
                } else if (CommonConstants.REDIRECT_ON_NO_RESULT.equalsIgnoreCase(searchRedirect.getActionType())) {
                    withProducts = true;
                }
            }
        }

        final ProductSearchRequest.Builder builder = productServiceV3.getProductSearchRequestBuilder
            (categorySlugs, searchKeyword, sort, order, user.getLocale(), pointsRange, pageSize,
                            resultOffSet, program, promoTag, withVariations,withFacets, facetsFilters);
        //get products
        productResponse = productServiceV3.getProducts(builder, user.getLocale(), program, user, withProducts);

        setProductSearchRedirect(productResponse, searchKeyword, searchRedirectBuilder, searchRedirect);

        return ResponseEntity.ok(Boolean.valueOf(disableCustomJSONFormatting)?productResponse:toJson(productResponse));
    }

    private void setProductSearchRedirect(final ProductResponse productResponse, final String searchKeyword,
        final SearchRedirectModel.Builder searchRedirectBuilder, final SearchRedirect searchRedirect) {
        if (StringUtils.isNotBlank(searchKeyword) && Objects.nonNull(searchRedirect)) {
            if (CollectionUtils.isEmpty(productResponse.getProducts()) &&
                CommonConstants.REDIRECT_ON_NO_RESULT.equalsIgnoreCase(searchRedirect.getActionType())) {
                logger.info("SEARCH_REDIRECT_ON_NO_RESULT : Search keyword {} redirects the user to {}",searchKeyword, searchRedirect.getValue());

                searchRedirectBuilder.withRedirectURL(searchRedirect.getValue());
                productResponse.setSearchRedirect(searchRedirectBuilder.build());
            } else if (CommonConstants.ALTERNATE.equalsIgnoreCase(searchRedirect.getActionType())) {

                searchRedirectBuilder.withAlternateSearchText(searchRedirect.getValue());
                productResponse.setSearchRedirect(searchRedirectBuilder.build());
            }
        }
    }

    private String getSortOrder(final String sort, String order) {
        if(StringUtils.isEmpty(order) && StringUtils.isNotEmpty(sort) ) {
            if(SortField.SALES_RANK.equals(SortField.valueOf(sort))){
                order = SortOrder.DESCENDING.name();
            }
            if(SortField.DISPLAY_PRICE.equals(SortField.valueOf(sort))){
                order = SortOrder.ASCENDING.name();
            }
            if(SortField.NAME.equals(SortField.valueOf(sort))){
                order = SortOrder.ASCENDING.name();
            }
        }
        return order;
    }

    private Object toJson(Object pojos) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(JsonNode.class, new InstanceCreator(){
                public JsonNode createInstance(Type type) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.createObjectNode();
                }
            })
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return (fieldAttributes.getName().equals("subCategories")
                        || fieldAttributes.getName().equals("minPricedProduct")
                        || fieldAttributes.getName().equals("maxPricedProduct"));
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }).create();
        final String resultJson = gson.toJson(pojos);
        return StringUtils.isBlank(resultJson) ||  CommonConstants.NULL_VALUE.equalsIgnoreCase(resultJson) ?resultJson :gson.fromJson(resultJson,pojos.getClass());
    }

    /**
     *
     * @param categorySlug
     * @param user
     * @return
     * <p>
     *
     * </p>
     */
    @ResponseBody
    @RequestMapping(value = {"/productsWithConfiguration"})
    public ResponseEntity<Object> getProductsWithConfiguration(@RequestParam(value = "categorySlug") String categorySlug,
        @RequestParam(required = false, value = "minPoints") Integer minPoints,
        @RequestParam(required = false, value = "maxPoints") Integer maxPoints,
        @RequestParam(required = false, value = "q") String keyword,
        @RequestParam(required = false, value = "sort") String sort,
        @RequestParam(required = false, value = "pageSize") Integer pageSize,
        @RequestParam(required = false, value = "resultOffSet") Integer resultOffSet,
        HttpServletRequest servletRequest) {
        logger.info("getProductsWithConfiguration with categorySlug: {}, minPoints: {}, maxPoints: {}, " +
            "pageSize: {}, " +
            "resultOffSet: {}, sort: {}, q: {} - ENTRY", categorySlug, minPoints, maxPoints, pageSize, resultOffSet, keyword, sort);
        final User user = appSessionInfo.currentUser();
        //validate request
        if(StringUtils.isBlank(categorySlug)
            && StringUtils.isBlank(keyword)
            && (minPoints == null && maxPoints == null)) {
            return ResponseEntity.badRequest().body("Invalid request in browse products...one must be present Keyword(q)," +
                " Category Slug(categorySlug)," +
                " Points Range (minPoints & maxPoints) )...  ");
        }

        Integer[] pointsRange = null;
        if (minPoints != null || maxPoints != null) {
            pointsRange = getPointsRange(minPoints, maxPoints);
            if (pointsRange == null) {
                return ResponseEntity.badRequest()
                    .body("Min and Max points range is incorrect. Please enter valid values for both. ");
            }
        }

        ProductResponse productResponse = null;
        Map<String,List<Option>> sortedMap = new LinkedHashMap<>();
        Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        Set<String> categorySlugs = new HashSet<>();
        if(StringUtils.isNotEmpty(categorySlug)){
            categorySlugs.add(categorySlug);
        }
        final ProductSearchRequest.Builder builder = productServiceV3.getProductSearchRequestBuilder(categorySlugs,
                keyword, SortField.DISPLAY_PRICE.name(), SortOrder.DESCENDING.name(), user.getLocale(), pointsRange,
                pageSize, resultOffSet, program, null, false,false, null);
        try {
            // get products
            productResponse = getProductResponseWithConfiguration(user, sortedMap, program, builder);
        } catch(ServiceException se) {
            logger.error("Error calling product search service", se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if(productResponse != null) {
            productResponse.setOptionsConfigurationData(sortedMap);
        }

        if(logger.isDebugEnabled()) {
            logger.debug("Printing response in getProductsWithConfiguration method: {}", new Gson().toJson(productResponse));
            logger.debug("getProductsWithConfiguration with categorySlug: {}, minPoints: {}, maxPoints: {}, pageSize: {}, resultOffSet: {}, sort: {}, q: {} - EXIT", categorySlug, minPoints, maxPoints, pageSize, resultOffSet, keyword, sort);
        }
        if(Objects.nonNull(productResponse)) {
            return ResponseEntity.ok(Boolean.valueOf(disableCustomJSONFormatting) ? productResponse : toJson(productResponse));
        }else{
            return ResponseEntity.badRequest()
                    .body("Product response is empty ");
        }
    }

    private ProductResponse getProductResponseWithConfiguration(
        final User user,
        final Map<String, List<Option>> sortedMap, final Program program, final ProductSearchRequest.Builder builder)
        throws ServiceException {
        final ProductResponse productResponse;
        productResponse = productServiceV3.getProducts(builder, user.getLocale(), program, user, true);
        boolean showGridView=false;
        if(Objects.nonNull(program.getConfig().get(CommonConstants.SHOW_GRID_VIEW))){
            showGridView=Boolean.parseBoolean(program.getConfig().get(CommonConstants.SHOW_GRID_VIEW).toString());
        }
        if(!showGridView) {
            final Map<String, Set<Option>> mapNameOptions = new HashMap<>();
            if (productResponse != null && CollectionUtils.isNotEmpty(productResponse.getProducts())) {
                buildNameOptionsList(productResponse, mapNameOptions);
            }
            updateProductPoints(user, program, sortedMap, productResponse, mapNameOptions);
        }
        return productResponse;
    }

    private void updateProductPoints(
        final User user, final Program program,
        final Map<String, List<Option>> sortedMap, final ProductResponse productResponse,
        final Map<String, Set<Option>> mapNameOptions) {
        final String optionNameStr = applicationProperties.getProperty(CommonConstants.OPTION_NAME_KEY);
        if (optionNameStr != null) {
            String[] optionNames = optionNameStr.split(",");
            for (String optionName : optionNames) {
                final Set<Option> options = mapNameOptions.get(optionName);
                if (CollectionUtils.isNotEmpty(options)) {
                    updateSortedMap(sortedMap, productResponse, optionName, options);
                }
            }
        }
    }

    private void updateSortedMap(final Map<String, List<Option>> sortedMap, final ProductResponse productResponse,
        final String optionName, final Set<Option> options) {
        final List<Option> optionsList = new ArrayList<>(options);
        final List<Option> customSortOrder = getCustomSortOrder(optionName);
        if (CommonConstants.GIFTCARDS_DENOMINATION.equals(optionName) && productResponse != null &&
            CollectionUtils.isNotEmpty(productResponse.getProducts())) {
            setOptionsPoint(productResponse, optionsList);
        }
        optionsList.sort((lhs, rhs) -> {
            final Integer lhsIdx = customSortOrder.indexOf(lhs);
            final Integer rhsIdx = customSortOrder.indexOf(rhs);
            if(lhsIdx.equals(rhsIdx)) {
                return lhs.compareTo(rhs);
            } else {
                return lhsIdx.compareTo(rhsIdx);
            }
        });
        sortedMap.put(optionName, optionsList);
    }

    private void setOptionsPoint(final ProductResponse productResponse, final List<Option> optionsList) {
        for (Product product : productResponse.getProducts()) {
            for (Option prodOption : product.getOptions()) {
                if (prodOption.getName().equals(CommonConstants.GIFTCARDS_DENOMINATION)) {
                    for (Option option : optionsList) {
                        if (prodOption.getKey().equals(option.getKey())) {
                            option.setPoints(
                                product.getDefaultOffer().getDisplayPrice().getPoints());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void buildNameOptionsList(final ProductResponse productResponse,
        final Map<String, Set<Option>> mapNameOptions) {
        for (Product product : productResponse.getProducts()) {
            for (Option option : product.getOptions()) {
                if (mapNameOptions.containsKey(option.getName())) {
                    mapNameOptions.get(option.getName()).add(option);
                } else {
                    final Set<Option> optionsList = new HashSet<>();
                    optionsList.add(option);
                    mapNameOptions.put(option.getName(), optionsList);
                }
            }
        }
    }

    private List<Option> getCustomSortOrder(final String optionName) {
        final VarProgramConfigEntity varProgramConfig = varProgramConfigDao.getVarProgramConfigByVarProgramName("-1", "-1", "options.value." + optionName.toLowerCase() + ".displayOrderBy");
        if(varProgramConfig != null) {
            final String sortOrderStr = varProgramConfig.getValue();
            if(StringUtils.isNotBlank(sortOrderStr)) {
                final List<String> sortOrderStrList = Arrays.asList(sortOrderStr.split(","));
                return sortOrderStrList.stream().map(key -> new Option(key)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    /**
     * This method enables the category names provided as part of request param 'category' in product Service
     *
     * @param psids : comma separated psids
     * @return http status code
     */
    @ResponseBody
    @RequestMapping(value = {"/products/disable"})
    public ResponseEntity<Object> disablePsids(
        @RequestParam(required = true, value = "psids") Set<String> psids, HttpServletRequest servletRequest) {
        try {
            if (psids == null || psids.isEmpty()) {
                return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
            }
            Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final User user = appSessionInfo.currentUser();
            logger.info("Disabling PSIDs on Product Service side: {}", psids);
            final ChangeEntry changeEntry = ChangeEntry.builder()
                .withAudiences(ImmutableSet.of(Audience.builder().build()))
                .withFilter(Filter.builder().withPsids(psids).build())
                .withReferenceId("DISABLE_PSIDS_REST_ENDPOINT")
                .withName(user.getFullName())
                .withSubmitter(user.getFullName())
                .withApprover(user.getFullName())
                .withCreatedDatetime(new DateTime())
                .withOverride(true)
                .withApprovedDatetime(new DateTime())
                .withPayloadWithTranslations(PayloadWithTranslations.builder().withPayloadType(PayloadType.SUPPRESSION)
                    .withSuppressed(true)
                    .build()).build();
            CatalogRequestContext catalogRequestContext = CatalogRequestContext.builder()
                .withCatalogId(program.getCatalogId())
                .withAudience(Audience.builder().build())
                .build();

            SaveChangeRequest saveChangeRequest = SaveChangeRequest.builder()
                .withRequestContext(catalogRequestContext)
                .withChangeEntry(changeEntry)
                .build();
            //Disable psids on Product Service side
            productServiceV3.saveCatalogChanges(saveChangeRequest);
            logger.info("Successfully Disabled PSIDs on Product Service side: {}", psids);

            //Create MERC filter entries for all PSDIs
            addToMercFilter(user, psids);
        } catch (final Exception se) {
            logger.error("Error calling disablePsids  service", se);
            return new ResponseEntity<>("Error calling disablePsids service", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>("PSIDs Disabled: "+ psids, HttpStatus.OK);
    }

    /**
     * This method enables the category names provided as part of request param 'category' in product Service
     *
     * @param psids : comma separated psids
     * @return http status code
     */
    @ResponseBody
    @RequestMapping(value = {"/products/enable"})
    public ResponseEntity<Object> enablePsids(
        @RequestParam(required = true, value = "psids") Set<String> psids, HttpServletRequest servletRequest) {
        try {
            if (psids == null || psids.isEmpty()) {
                return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
            }
            Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final User user = appSessionInfo.currentUser();
            logger.info("Enabling PSIDs on Product Service side: {}", psids);
            final ChangeEntry changeEntry = ChangeEntry.builder()
                .withAudiences(ImmutableSet.of(Audience.builder().build()))
                .withFilter(Filter.builder().withPsids(psids).build())
                .withReferenceId("ENABLE_PSIDS_REST_ENDPOINT")
                .withName(user.getFullName())
                .withSubmitter(user.getFullName())
                .withApprover(user.getFullName())
                .withCreatedDatetime(new DateTime())
                .withOverride(true)
                .withApprovedDatetime(new DateTime())
                .withPayloadWithTranslations(PayloadWithTranslations.builder().withPayloadType(PayloadType.SUPPRESSION)
                    .withSuppressed(false)
                    .build()).build();
            CatalogRequestContext catalogRequestContext = CatalogRequestContext.builder()
                .withCatalogId(program.getCatalogId())
                .withAudience(Audience.builder().build())
                .build();

            SaveChangeRequest saveChangeRequest = SaveChangeRequest.builder()
                .withRequestContext(catalogRequestContext)
                .withChangeEntry(changeEntry)
                .build();

            //Enable psids on Product Service side
            productServiceV3.saveCatalogChanges(saveChangeRequest);

            //Create MERC filter entries for all PSDIs
            removeMercFilter(user, psids);

        } catch (final Exception se) {
            logger.error("Error calling enablePsids  service", se);
            return new ResponseEntity<>("Error calling enablePsids service", HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>("PSIDs Enabled: "+ psids, HttpStatus.OK);
    }

    /**
     * Sorts the Options based on the comma separated sort string passed
     * @param options
     * @param optionValues
     * @return
     */
    private List<Option> sortOptions(List<Option> options, List<ProductAttributeValue> optionValues){
        List<Option> sortedOptions = new ArrayList<>();

        Set<String> alreadyAdded = new HashSet<>();
        for(ProductAttributeValue value : optionValues){
            for(Option option:options){
                if (org.apache.commons.lang.StringUtils.isNotBlank(option.getValue())
                    && org.apache.commons.lang.StringUtils.isNotBlank(value.getAttributei18nValue())
                    && option.getValue().replace('\u00A0', ' ').trim()
                    .equalsIgnoreCase(value.getAttributei18nValue().replace('\u00A0', ' ').trim()) &&
                    !alreadyAdded.contains(option.getValue().replace('\u00A0', ' ').trim())) {
                    option.setKey(value.getAttributeValue());
                    option.setOrderBy(value.getOrderBy());
                    sortedOptions.add(option);
                    alreadyAdded.add(option.getValue().replace('\u00A0',' ').trim());
                }
            }
        }
        return  sortedOptions;
    }

    @ResponseBody
    @RequestMapping(value={"/productAttributeConfiguration"})
    public ResponseEntity<List<ProductAttributeConfiguration>> getAllProductAttributeConfiguration() {
        try {
            return new ResponseEntity<>(detailService.getAllProductAttributeConfiguration(), HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity("Unexpected error occurred while fetching Product Attribute Configuration",
                HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @ResponseBody
    @RequestMapping(value ={"/categoryConfigurations"})
    public ResponseEntity<List<CategoryConfiguration>> getAllCategoryConfigurations(){
        try {
            return new ResponseEntity<>(detailService.getAllCategoryConfigurations(), HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity("Unexpected error occurred while fetching Category Configuration",
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Validate points range. Both are mandatory. Keeping it independent of User point.
     * @param minPoints
     * @param maxPoints
     * @return
     */
    private Integer[] getPointsRange(Integer minPoints, Integer maxPoints) {
        Integer[] pointsRange = new Integer[2];
        pointsRange[0] = minPoints == null ? 0 : minPoints;
        pointsRange[1] = maxPoints == null ? 0 : maxPoints;

        //min value must be less than max value
        if (pointsRange[1] == 0 || pointsRange[0] > pointsRange[1]) {
            logger.error("Incorrect Min/Max point value");
            return null;
        }

        return pointsRange;
    }

    /**
     *
     * @return
     * <p>
     *
     * </p>
     */
    @ResponseBody
    @RequestMapping(value = {"/loadProductDetailCache"})
    public void loadProductDetailCache(){
        logger.info("Starting Product Detail Cache Load");
        //TODO load and cache the products.
        logger.info("Completed Product Detail Cache Load");
    }

    /**
     * Add PSIDs that were disabled in Product Service side , to Merc Filter table for tracking purpose
     * @param user
     * @param psids
     */
    private void addToMercFilter(User user, Set<String> psids ) {

        logger.info("Adding Product PSDIs to merc filter: {}", psids);
        try {
            MercSearchFilter filter = new MercSearchFilter();
            filter.setVarId(user.getVarId());
            filter.setProgramId(user.getProgramId());
            filter.setFilterType(CommonConstants.EXCLUDE);
            filter.setFilterName(CommonConstants.PSID);
            filter.setIsActive(CommonConstants.YES_VALUE);
            filter.setAddedBy(user.getFullName());
            filter.setComment("This PSID is excluded in Product Service Side");

            psids.stream().forEach((psid) -> {
                filter.setFilterValue(psid);
                filter.setAddedDate(new Date());
                mercSearchFilterDao.add(filter);
            });
        } catch ( Exception ex) {
            logger.error("Exception in Adding Product PSDIs to merc filter: ", ex);
        }

        logger.info("Successfully added Product PSDIs to merc filter: {}", psids);
    }

    /**
     * Remove PSIDs that were disabled in Product Service side , from Merc Filter table
     * @param user
     * @param psids
     */
    private void removeMercFilter(User user, Set<String> psids ) {

        logger.info("Removing Filters set on Product PSDIs from Merc filter: {}", psids);
        try {
            psids.stream().forEach((psid) -> {
                MercSearchFilter filter = mercSearchFilterDao.getByPK(user.getVarId(), user.getProgramId(), CommonConstants.EXCLUDE, CommonConstants.PSID, psid);

                if (filter != null) {
                    mercSearchFilterDao.delete(filter);
                }

            });
        } catch ( Exception ex) {
            logger.error("Exception while Removing Filters set on Product PSDIs from Merc filter: ", ex);
        }

        logger.info("Successfully Removed Filters set on Product PSDIs from Merc filter: {}", psids);
    }


}
