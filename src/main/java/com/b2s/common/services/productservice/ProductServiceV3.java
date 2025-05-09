package com.b2s.common.services.productservice;

import com.b2s.apple.entity.RelevantLanguageEntity;
import com.b2s.apple.mapper.ProductMapper;
import com.b2s.apple.services.*;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.ClientRequest;
import com.b2s.common.services.requests.productservice.CoreProductDetailRequest;
import com.b2s.common.services.requests.productservice.MultiProductDetailRequest;
import com.b2s.common.services.responses.productservice.CoreProductDetailResponse;
import com.b2s.common.services.transformers.Helper;
import com.b2s.common.services.transformers.TransformersHolder;
import com.b2s.common.services.transformers.productservice.DetailResponseTransformerHelper;
import com.b2s.common.services.util.CategoryPriceHolder;
import com.b2s.rewards.apple.dao.RelevantLanguageDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.model.Option;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.PricingUtil;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.api.ManagementServiceClient;
import com.b2s.service.product.client.api.ProductServiceClient;
import com.b2s.service.product.client.application.changes.SaveChangeRequest;
import com.b2s.service.product.client.application.detail.MultiProductDetailResponse;
import com.b2s.service.product.client.application.detail.ProductDetailRequest;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.application.terms.TermsRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.Audience;
import com.b2s.service.product.client.domain.SortField;
import com.b2s.service.product.client.domain.SortOrder;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import com.b2s.service.product.client.exception.RequestValidationException;
import com.b2s.service.product.common.domain.WrappedFacetName;
import com.b2s.service.product.common.domain.attributes.CommonAttributes;
import com.b2s.service.product.common.domain.change.AppliedChange;
import com.b2s.service.product.common.domain.change.Payload;
import com.b2s.service.product.common.domain.change.PayloadType;
import com.b2s.service.product.common.domain.response.*;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.GiftItemManager;
import com.b2s.shop.common.order.var.VAROrderManagerIF;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.common.util.CommonConstants.*;
import static com.b2s.rewards.apple.util.AppleUtil.*;

/**
 * <p/>
 * This class is used to product service client connection factory and execute requests on product service layer.
 *
 * @author sjonnalagadda Date: 7/11/13 Time: 4:13 PM
 */
public class ProductServiceV3 extends AbstractProductService {


    public static final String INVALID_REQUEST_IN_BROWSE_PRODUCTS = "Invalid request in browse products...";
    public static final String NO_SUCH_PRODUCT_AVAILABLE = "No such product available";
    public static final String EXCEPTION_WHILE_SEARCH_FOR_PRODUCTS = "Exception while search for products";
    public static final String CASE_SIZE = "caseSize";
    public static final String EXCEPTION_WHILE_RETRIEVING_PRODUCT_DETAIL = "Exception while retrieving product detail";
    public static final String SALES_RANK = "SALES_RANK";
    public static final String DESCENDING = "DESCENDING";

    @Value("#{'${sortFilterOptionsLocales}'.split(',')}")
    private List<String> sortFilterOptionsLocales;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryConfigurationService categoryConfigurationService;

    @Autowired
    private VarOrderManagerHolder varOrderManagerHolder;

    @Autowired
    private CategoryPriceHolder categoryPriceHolder;

    @Autowired
    private RelevantLanguageDao relevantLanguageDao;

    @Autowired
    private ProgramService programService;

    @Autowired
    private VarProgramCatalogConfigService varProgramCatalogConfigService;

    @Autowired
    private GiftPromoService giftPromoService;

    @Autowired
    private EngravingService engravingService;

    @Autowired
    private GiftItemManager giftItemManager;

    @Autowired
    private ProductCarouselImageService productCarouselImageService;

    @Autowired
    private CartService cartService;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceV3.class);

    private final TransformersHolder<MultiProductDetailRequest, Set<ProductDetailRequest>,
            MultiProductDetailResponse, CoreProductDetailResponse>
            detailTransformersHolder;


    public ProductServiceV3(final ProductServiceFactoryWrapper productServiceFactoryWrapper,
                            final TransformersHolder<MultiProductDetailRequest, Set<ProductDetailRequest>, MultiProductDetailResponse,
                                    CoreProductDetailResponse> detailTransformersHolder
    )
        throws ServiceException {
        super(productServiceFactoryWrapper);

        validateDetailTransformerHolder(detailTransformersHolder);
        this.detailTransformersHolder = detailTransformersHolder;
    }

    private <A, B, C, D> void validateDetailTransformerHolder(final TransformersHolder<A, B, C, D> transformersHolder)
        throws ServiceException {
        if (!Optional.ofNullable(transformersHolder).isPresent()) {
            throw new ServiceException(ServiceExceptionEnums.DETAIL_TRANSFORMERS_HOLDER_ABSENT);
        }

        if (!Optional.ofNullable(transformersHolder.getRequestTransformer()).isPresent()) {
            throw new ServiceException(ServiceExceptionEnums.DETAIL_REQUEST_TRANSFORMER_ABSENT);
        }

        if (!Optional.ofNullable(transformersHolder.getResponseTransformer()).isPresent()) {
            throw new ServiceException(ServiceExceptionEnums.DETAIL_RESPONSE_TRANSFORMER_ABSENT);
        }
    }



    public void populateCategoryPrices() throws ServiceException {
        Locale locale = LocaleUtils.toLocale("en_US");
        Program program = programService.getProgram("BSWIFT", "EPP", locale);
        if(Objects.nonNull(program)) {
            List<CategoryPrice> categoryPrices = getCategoryPrices(program, locale);
            if (CollectionUtils.isNotEmpty(categoryPrices)) {
                categoryPrices.forEach(categoryPrice ->
                    categoryPriceHolder.addCategoryPrice(categoryPrice.getCategoryName(), categoryPrice)
                );
            }
        }
    }

    /**
     * Get the list of category prices for the categories available for provided program
     * CategoryPrice contains the starting from price among the category
     * @param program
     * @param locale
     * @return
     * @throws ServiceException
     */
    public List<CategoryPrice> getCategoryPrices(Program program, Locale locale) throws ServiceException {
        List<CategoryPrice> categoryPrices = new ArrayList<>();
        List<Category> categories = categoryConfigurationService.getParentCategories(program, locale);
        // get all child categories from the parent child categories
        categories = getChildCategories(categories);

        if(CollectionUtils.isNotEmpty(categories)) {
            for(Category category : categories) {
                Set<String> categorySlugs = new HashSet<>();
                if(StringUtils.isNotEmpty(category.getSlug())){
                    categorySlugs.add(category.getSlug());
                }
                // get products for each categorues
                final ProductSearchRequest.Builder builder = getProductSearchRequestBuilder(categorySlugs, null,
                    SortField.DISPLAY_PRICE.name(), SortOrder.DESCENDING.name(), locale, null, null, null, program,
                    null,false, false,null);
                final ProductSearchResponse productSearchResponse = searchProducts(builder.build());
                if (productSearchResponse != null && Optional.ofNullable(productSearchResponse.getDefaultGroup()).isPresent()) {
                    categoryPrices.addAll(getCategoryPrices(program, null, productSearchResponse));
                }
            }
        }
        return categoryPrices;
    }

    public List<CategoryPrice> getCategoryPrices(Program program, List<Option> options,
                                                 ProductSearchResponse productSearchResponse) {
        List<CategoryPrice> categoryPrices = new ArrayList<>();
        if (Objects.nonNull(productSearchResponse)) {
           final ProductSearchDocumentGroup group = productSearchResponse.getDefaultGroup().orElse(null);
            if (Objects.nonNull(group) && CollectionUtils.isNotEmpty(group.getProductSearchDocuments())) {
                List<ProductSearchDocument> products = group.getProductSearchDocuments();
                // get the max priced product and min priced product from the product list
                ProductSearchDocument maxPricedProduct = products.get(0);
                ProductSearchDocument minPricedProduct = products.get(products.size() - 1);
                String categoryName = null;
                // get the category name from the product
                if (minPricedProduct != null && CollectionUtils.isNotEmpty(minPricedProduct.getCategorySlugs())) {
                    categoryName = minPricedProduct.getCategorySlugs().get(0);
                }
                // if options are not empty, get the min priced product and max priced product for each option
                // to decide the 'starting from' price for each option
                getMinMaxPricedProducts(program, options, categoryPrices, products, maxPricedProduct,
                        minPricedProduct, categoryName);
            }
        }
        return categoryPrices;
    }

    private void getMinMaxPricedProducts(Program program, List<Option> options, List<CategoryPrice> categoryPrices, List<ProductSearchDocument> products, ProductSearchDocument maxPricedProduct, ProductSearchDocument minPricedProduct, String categoryName) {
        // if options are not empty, get the min priced product and max priced product for each option
        // to decide the 'starting from' price for each option
        if (CollectionUtils.isNotEmpty(options)) {
            for (Option option : options) {
                List<ProductSearchDocument> productsWithOption = getSortedProducts(option, products);
                if (CollectionUtils.isNotEmpty(productsWithOption)) {
                    maxPricedProduct = productsWithOption.get(0);
                    minPricedProduct = productsWithOption.get(productsWithOption.size() - 1);
                    CategoryPrice categoryPrice = getCategoryPrice(program, categoryName, maxPricedProduct, minPricedProduct);
                    categoryPrice.setOption(option);
                    categoryPrices.add(categoryPrice);
                }
            }
        } else {
            CategoryPrice categoryPrice = getCategoryPrice(program, categoryName, maxPricedProduct, minPricedProduct);
            categoryPrices.add(categoryPrice);
        }
    }

    /**
     * Get display price sorted product list with provided option.
     *
     * @param option
     * @param products
     * @return
     */
    private List<ProductSearchDocument> getSortedProducts(Option option, List<ProductSearchDocument> products) {
        List<ProductSearchDocument> newProducts = new ArrayList<>();
        if (option != null && CollectionUtils.isNotEmpty(products)) {
            newProducts = products.stream()
                    .filter(product -> product != null && MapUtils.isNotEmpty(product.getOptions()) &&
                            product.getOptions().get(option.getName()) != null &&
                            StringUtils.isNotBlank(product.getOptions().get(option.getName()).getValue()) &&
                            product.getOptions().get(option.getName()).getValue().equals(option.getValue())
                    ).collect(Collectors.toList());
        }
        Collections.sort(newProducts, new Comparator<ProductSearchDocument>() {
            @Override
            public int compare(ProductSearchDocument o1, ProductSearchDocument o2) {
                if(o1 != null && o2 != null) {
                    return o2.getPricingInformation().getFirstCalculatedPriceInfo().getDisplayPrice().getAmount().compareTo(o1.getPricingInformation().getFirstCalculatedPriceInfo().getDisplayPrice().getAmount());
                }
                return 0;
            }
        });
        return newProducts;
    }

    public CategoryPrice getCategoryPrice(Program program, String categoryName, final ProductSearchDocument maxPricedProduct, final ProductSearchDocument minPricedProduct) {
        CategoryPrice categoryPrice = null;
        if(Objects.nonNull(minPricedProduct ) && Objects.nonNull(minPricedProduct.getPricingInformation().getFirstCalculatedPriceInfo().getDisplayPrice())) {
           final Money displayPrice = minPricedProduct.getPricingInformation().getFirstCalculatedPriceInfo().getDisplayPrice();

            // get the display price and un promoted price from min priced product to get the starting from price
            final Optional<Price> minUnpromotedDisplayPrice = minPricedProduct.getPricingInformation().getFirstCalculatedPriceInfo().
                    getUnpromotedDisplayPrice().map(unPromotedPrice -> {
                if (Objects.nonNull(unPromotedPrice.getAmount())) {
                    return new Price(minPricedProduct.getPricingInformation().
                            getOriginalPriceInfo().getBasePrice(), unPromotedPrice.getAmount().intValue());
                }
                return null;
            });

           categoryPrice =  createCategoryPrice(program, categoryName, maxPricedProduct, minPricedProduct, displayPrice,
                   minUnpromotedDisplayPrice.orElse(null));
        }
        return categoryPrice;
    }

    private CategoryPrice createCategoryPrice(final Program program, final String categoryName,
                                              final ProductSearchDocument maxPricedProduct, final ProductSearchDocument minPricedProduct,
                                              final Money displayPrice, final Price minUnpromotedDisplayPrice) {
        final CategoryPrice categoryPrice;
        Price minDisplayPrice = getMinDisplayPrice(minPricedProduct, displayPrice, minUnpromotedDisplayPrice);
        if (minDisplayPrice != null && minDisplayPrice.getAmount() > 0d) {
            categoryPrice = new CategoryPrice(categoryName, false, minDisplayPrice.getAmount());
            categoryPrice.setIsFree(false);
        } else {
            categoryPrice = new CategoryPrice(categoryName, true, 0d);
            categoryPrice.setIsFree(true);
        }
        // set the min, max display price and unpromoted display price
        categoryPrice.setMinDisplayPrice(minDisplayPrice);
        categoryPrice.setMinUnpromotedDisplayPrice(minUnpromotedDisplayPrice);
        final Money money =
                maxPricedProduct.getPricingInformation().getFirstCalculatedPriceInfo().
                        getUnpromotedDisplayPrice().orElse(null);
        Price maxUnpromotedDisplayPrice =
                new Price(maxPricedProduct.getPricingInformation().getOriginalPriceInfo().getBasePrice(),
                        Objects.nonNull(money)?money.getAmount().intValue() : null);
        Price maxDisplayPrice = getMaxDisplayPrice(maxPricedProduct, maxUnpromotedDisplayPrice);
        categoryPrice.setMaxDisplayPrice(maxDisplayPrice);
        categoryPrice.setMaxUnpromotedDisplayPrice(maxUnpromotedDisplayPrice);

        // get the highest price in category after adding max display price and highest tax amount
        if(categoryPrice != null && categoryPrice.getActualStartingFromPrice() > 0d) {
            Object estimatedMaxTaxRateObj = program.getConfig().get(CommonConstants.ESTIMATED_MAX_TAX_RATE);
            Integer estimatedMaxTaxRate = (estimatedMaxTaxRateObj != null && StringUtils.isNotBlank(estimatedMaxTaxRateObj.toString())) ? new Integer(estimatedMaxTaxRateObj.toString()) : null;
            BigDecimal taxAmount=BigDecimal.valueOf(0.0);
            if(estimatedMaxTaxRate!=null) {
                taxAmount = new BigDecimal(estimatedMaxTaxRate).multiply(new BigDecimal(categoryPrice.getMaxDisplayPrice().getAmount())).divide(new BigDecimal(100)).setScale(2, RoundingMode.FLOOR);
            }
            Double highestPriceInCategory = categoryPrice.getMaxDisplayPrice().getAmount() + taxAmount.doubleValue();
            categoryPrice.setHighestPriceInCategory(highestPriceInCategory);
        }
        return categoryPrice;
    }

    private Price getMaxDisplayPrice(final ProductSearchDocument maxPricedProduct,
                                     final Price maxUnpromotedDisplayPrice) {
        Price maxDisplayPrice = new Price(maxPricedProduct.getPricingInformation().getOriginalPriceInfo().getBasePrice(), maxPricedProduct.getPricingInformation().getFirstCalculatedPriceInfo().getDisplayPrice().getAmount().intValue());
        if(maxDisplayPrice != null && maxUnpromotedDisplayPrice != null && maxDisplayPrice.getPoints() != maxUnpromotedDisplayPrice.getPoints()) {
            if(maxDisplayPrice.getAmount() > 0) {
                maxDisplayPrice = new Price(new BigDecimal(maxDisplayPrice.getPoints()).divide(new BigDecimal(100)).setScale(2, RoundingMode.FLOOR).doubleValue(), "", maxDisplayPrice.getPoints());
            } else {
                maxDisplayPrice = new Price(0d, "", 0);
            }
        }
        return maxDisplayPrice;
    }

    private Price getMinDisplayPrice(final ProductSearchDocument minPricedProduct, final Money displayPrice,
                                     final Price minUnpromotedDisplayPrice) {
        Price minDisplayPrice = null;
        if(displayPrice != null && displayPrice.getAmount() != null) {
            minDisplayPrice = new Price(minPricedProduct.getPricingInformation().getOriginalPriceInfo().getBasePrice(), displayPrice.getAmount().intValue());
        }
        // Since product service does not apply promotion in dollar prices, check the different of display price and un promoted price
        // and see whether promotion is applied or not
        if(minDisplayPrice != null && minUnpromotedDisplayPrice != null && minDisplayPrice.getPoints() != minUnpromotedDisplayPrice.getPoints()) {
            if(minDisplayPrice.getAmount() > 0) {
                minDisplayPrice = new Price(new BigDecimal(minDisplayPrice.getPoints()).divide(new BigDecimal(100)).setScale(2, RoundingMode.FLOOR).doubleValue(), "", minDisplayPrice.getPoints());
            } else {
                minDisplayPrice = new Price(0d, "", 0);
            }
        }
        return minDisplayPrice;
    }

    private void categoryPricesetFromPriceMessage(CategoryPrice categoryPrice, MessageSource messageSource,
                                                  final User user, boolean isSearch, final String code1, final String code2, Object[] args) {
        if (isSearch) {
            categoryPrice.setFromPriceMessage(messageSource.getMessage(code1, args, user.getLocale()));
        } else {
            categoryPrice.setFromPriceMessage(messageSource.getMessage(code2, args, user.getLocale()));
        }
    }

    public void validateForPayrollEligibility(final User user, final List<CategoryPrice> categoryPrices, final Program program, boolean isSearch) {
        if(CollectionUtils.isNotEmpty(categoryPrices) && user != null && program != null) {
            MessageSource messageSource = (MessageSource) AppContext.getApplicationContext().getBean("messageSource" + user.getVarId());
            categoryPrices.forEach(categoryPrice -> {
                if(categoryPrice.getIsFree()) {
                    categoryPricesetFromPriceMessage(categoryPrice, messageSource, user, isSearch, "config-Free", "model-Free", null);
                } else {
                    boolean isEligibleForPayrollDeduction = PricingUtil.isEligibleForPayrollDeduction(user, program, new Price(new Double(categoryPrice.getStartingFromPrice()), "", 0));
                    int price = categoryPrice.getStartingFromPrice();
                    if (isEligibleForPayrollDeduction && MapUtils.isNotEmpty(program.getConfig()) &&
                            Objects.nonNull(program.getConfig().get(CommonConstants.PAY_PERIODS))) {
                        final int payPeriods =
                                Integer.parseInt(program.getConfig().get(CommonConstants.PAY_PERIODS).toString());
                        price = new BigDecimal(price).divide(new BigDecimal(payPeriods), RoundingMode.CEILING)
                                .setScale(0, RoundingMode.CEILING).intValue();
                        categoryPricesetFromPriceMessage(categoryPrice, messageSource, user, isSearch,
                                "startingFrom-config-pd", "startingFrom-model-pd", new String[]{String.valueOf(price)});
                    } else {
                        categoryPricesetFromPriceMessage(categoryPrice, messageSource, user, isSearch,
                                "startingFrom-config", "startingFrom-model", new String[]{String.valueOf(price)});
                    }
                }
            });
        }
    }

    private List<Category> getChildCategories(List<Category> subCategories) {
        List<Category> childCategories = new ArrayList<>();
        if(subCategories != null) {
            for(Category category : subCategories) {
                if(CollectionUtils.isNotEmpty(category.getSubCategories())) {
                    childCategories.addAll(getChildCategories(category.getSubCategories()));
                } else {
                    childCategories.add(category);
                }
            }
        }
        return childCategories;
    }

    public Collection<CategoryPrice> getCategoryPrices() {
        return categoryPriceHolder.getCategoryPrices();
    }

    @Deprecated
    public CoreProductDetailResponse productDetail(final ClientRequest request, final String productType,  final Program program, final boolean checkDRP)
        throws ServiceException {
        try {
            validateSearchInputs(request);
            //Transform it to server request
            final Set<ProductDetailRequest> serverRequest =
                this.detailTransformersHolder.getRequestTransformer()
                    .transform((MultiProductDetailRequest) request, null, program);

            if (Optional.ofNullable(serverRequest).isPresent() && !serverRequest.isEmpty()) {

                final Set<String> psidsFromDetailRequests = getPsidsFromDetailRequests(serverRequest);

                final ProductServiceClient productServiceClient =
                    this.productServiceFactoryWrapper.getProductServiceClient();
                //Send request to server
                final MultiProductDetailResponse multiProductDetailResponse =
                    productServiceClient.detail(serverRequest);
                if (Optional.ofNullable(multiProductDetailResponse).isPresent()) {
                    final Helper responseTransformerHelper = new DetailResponseTransformerHelper(
                        Optional.ofNullable(productType), psidsFromDetailRequests, request.getUserLanguage());
                    //Transform response from server to domain model
                    final CoreProductDetailResponse coreProductDetailResponse = this.detailTransformersHolder.getResponseTransformer().transform(multiProductDetailResponse,
                        responseTransformerHelper, program);
                    if(checkDRP) {
                        multiProductDetailResponse.getProducts().forEach(productAPIResponse -> {
                            final com.b2s.rewards.model.Product product =
                                coreProductDetailResponse.getProductDetailByPsid(productAPIResponse.getPsid())
                                        .orElse(null);
                            if (Objects.nonNull(product)) {
                                product.setAvailable(AppleUtil.isAWPAvailableProduct(program, productAPIResponse.getAvailabilityInformation()));
                            }
                        });
                    }
                    return coreProductDetailResponse;
                }
            }
            return null;
        } catch (ServiceException se) {
            logger.error(EXCEPTION_WHILE_RETRIEVING_PRODUCT_DETAIL, se);
            throw se;
        } catch (Exception e) {
            logger.error(EXCEPTION_WHILE_RETRIEVING_PRODUCT_DETAIL, e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        }
    }

    private Set<String> getPsidsFromDetailRequests(final Set<ProductDetailRequest> serverRequest) {
        final Set<String> psidsFromDetailRequest = new HashSet<>();
        if (Optional.ofNullable(serverRequest).isPresent() && !serverRequest.isEmpty()) {
            for (final ProductDetailRequest productDetailRequest : serverRequest) {
                psidsFromDetailRequest.add(productDetailRequest.getPsid());
            }
        }
        return psidsFromDetailRequest;
    }

    public List<String> autoCompleteTerms(final TermsRequest request) {
        final ProductServiceClient productServiceClient = this.productServiceFactoryWrapper.getProductServiceClient();
        return productServiceClient.autoCompleteTerms(request);
    }

    /**
     * @param serverRequest
     * @return
     */
    public ProductSearchResponse searchProducts(final ProductSearchRequest serverRequest) {
        final ProductServiceClient productServiceClient = this.productServiceFactoryWrapper.getProductServiceClient();
        return productServiceClient.search(serverRequest);
    }

    /**
     * @param serverRequest
     * @return
     */
    public void saveCatalogChanges(final SaveChangeRequest serverRequest) {
        final ManagementServiceClient managementServiceClient =
            this.productServiceFactoryWrapper.getManagementServiceClient();
        managementServiceClient.saveChange(serverRequest);
    }

    /**
     * @param serverRequest
     * @return
     * @throws ServiceException
     */
    public ProductResponse appleSearch(final ProductSearchRequest serverRequest, final Locale locale, final Program
        program, final User user, final List<DiscountCode> discounts, final boolean withProducts)
        throws ServiceException {
        try {
            //Send request to server
            final ProductSearchResponse productSearchResponse = searchProducts(serverRequest);
            if (Optional.ofNullable(productSearchResponse).isPresent()) {
                final List<String> productTilesOptions = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
                    program.getVarId(), program.getProgramId(), PRODUCT_TILES_OPTIONS);
                ProductResponse productResponse = new ProductResponse();
                List<Product> appleProducts = new ArrayList<>();
                if (withProducts) {
                    updateAppleProducts(serverRequest, locale, program, user, productSearchResponse,
                        productTilesOptions,
                        productResponse,
                        appleProducts);

                    if (program != null && CollectionUtils.isNotEmpty(discounts)) {
                        updateProductCategoryPrice(program, user, discounts, productSearchResponse, productResponse);
                    }
                    productResponse.setFacetsFilters(getFacetsFilters(productSearchResponse, serverRequest, program, locale));
                }

                productResponse.setProducts(appleProducts);
                return productResponse;
            } else {
                throw new EntityNotFoundException(NO_SUCH_PRODUCT_AVAILABLE);
            }
        } catch (EntityNotFoundException enfe) {
            logger.error("Product Search Request {}", serverRequest);
            logger.error("No products available ", enfe);
            throw new EntityNotFoundException("No products available");
        } catch (RequestValidationException rve) {
            logger.error(INVALID_REQUEST_IN_BROWSE_PRODUCTS, rve);
            throw new RequestValidationException(INVALID_REQUEST_IN_BROWSE_PRODUCTS + rve.getMessage());
        } catch (Exception e) {
            logger.error(EXCEPTION_WHILE_SEARCH_FOR_PRODUCTS, e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        }
    }

    private void updateAppleProducts(final ProductSearchRequest serverRequest, final Locale locale,
        final Program program, final User user, final ProductSearchResponse productSearchResponse,
        final List<String> productTilesOptions, final ProductResponse productResponse,
        final List<Product> appleProducts) {
        for (Map.Entry<String, ProductSearchDocumentGroup> productSearchDocumentGroup : productSearchResponse
            .getProductSearchGroups().entrySet()) {
            for (ProductSearchDocument productSearchDocument : productSearchDocumentGroup.getValue()
                .getProductSearchDocuments()) {

                updateAppleProduct(serverRequest, locale, program, user, productTilesOptions, productResponse,
                    appleProducts,
                    productSearchDocument);

            }
            if (productResponse.getTotalFound()==0){
                // set total number of products
                productResponse.setTotalFound(productSearchDocumentGroup.getValue().getTotalFound());
            }
        }
    }

    private void updateProductCategoryPrice(final Program program, final User user, final List<DiscountCode> discounts,
        final ProductSearchResponse productSearchResponse, final ProductResponse productResponse) {
        Object showFromPriceObj = program.getConfig().get(CommonConstants.SHOW_FROM_PRICE);
        Boolean showFromPrice = showFromPriceObj != null ? Boolean.parseBoolean(showFromPriceObj.toString()) : false;
        if(showFromPrice) {
            List<Option> options = new ArrayList<>();
            Object showCaseSizeFromPriceObj = program.getConfig().get(CommonConstants.SHOW_CASE_SIZE_FROM_PRICE);
            Boolean showCaseSizeFromPrice = (showCaseSizeFromPriceObj != null && StringUtils.isNotBlank(showCaseSizeFromPriceObj.toString())) ? new Boolean(showCaseSizeFromPriceObj.toString()) : false;
            if(showCaseSizeFromPrice) {
                options.add(new Option(CASE_SIZE, "38mm", Optional.empty()));
                options.add(new Option(CASE_SIZE, "42mm", Optional.empty()));
            }
            if(CollectionUtils.isNotEmpty(options)) {
                List<CategoryPrice> categoryPrices = applySubsidyDiscountToCategoryPrices(getCategoryPrices(program, options, productSearchResponse), discounts);
                boolean hidePrice = categoryPrices.stream().allMatch(categoryPrice -> categoryPrice.getIsFree() == true);
                if(!hidePrice) {
                    hidePrice = categoryPrices.stream().allMatch(categoryPrice -> categoryPrice.getIsFree() == false);
                }
                if(!hidePrice) {
                    // Check for payroll eligibility based on user, program or price
                    validateForPayrollEligibility(user, categoryPrices, program, true);
                    productResponse.setCategoryPrices(categoryPrices);
                }

            }
        }
    }

    private void updateAppleProduct(final ProductSearchRequest serverRequest, final Locale locale,
        final Program program, final User user, final List<String> productTilesOptions,
        final ProductResponse productResponse, final List<Product> appleProducts,
        final ProductSearchDocument productSearchDocument) {
        final Product appleProduct;
        Set<VariationSearchDocument> variations = productSearchDocument.getVariations();
        ImmutableList<String> categorySlugs = productSearchDocument.getCategorySlugs();

        if (CollectionUtils.isNotEmpty(categorySlugs) &&
            categorySlugs.get(0).contains(CommonConstants.ACCESSORIES)
            && AppleUtil.collectionSizeIsGreaterThan(variations, 1)
            && productSearchDocument.getOptions().containsKey(CommonConstants.LANGUAGE)) {

            Optional<VariationSearchDocument> variationProduct =
                getVariationProductByLanguagePreference(variations, serverRequest, user);

            if (variationProduct.isPresent()) {
                ProductSearchDocument variationProductSearchDocument =
                    productMapper.variation2ProductSearchDocument(productSearchDocument, variationProduct.get());

                appleProducts.add(productMapper.from(variationProductSearchDocument, locale, program, user, productTilesOptions));
            }
        } else {
            final String[] supplierTypesStr =
                {SUPPLIER_TYPE_GIFTCARD_STR.toLowerCase(), SUPPLIER_TYPE_GIFT_CARDS_STR.toLowerCase()};
            if (CollectionUtils.isNotEmpty(categorySlugs) &&
                AppleUtil.stringContainsWord(categorySlugs.get(0), supplierTypesStr) &&
                CollectionUtils.isNotEmpty(variations)) {
                for (VariationSearchDocument variationSearchDocument : variations) {
                    ProductSearchDocument variationProductSearchDocument = productMapper
                        .variation2ProductSearchDocument(productSearchDocument, variationSearchDocument);

                    Product variationAppleProduct =
                        productMapper.from(variationProductSearchDocument, locale, program, user, productTilesOptions);
                    final VAROrderManagerIF varOrderManager =
                        varOrderManagerHolder.getVarOrderManager(program.getVarId());
                    varOrderManager.computePricingModel(variationAppleProduct, user, program);
                    appleProducts.add(variationAppleProduct);

                }
                productResponse.setTotalFound(variations.size());
            } else {

                // Get apple product
                appleProduct = productMapper.from(productSearchDocument, locale, program, user, productTilesOptions);

                //add pricing model structure
                final VAROrderManagerIF varOrderManager =
                    varOrderManagerHolder.getVarOrderManager(program.getVarId());
                varOrderManager.computePricingModel(appleProduct, user, program);
                appleProducts.add(appleProduct);
            }
        }
    }

    /**
     * Apple discount code in category prices
     *
     * @param categoryPrices
     * @param discounts
     * @return
     */
    public List<CategoryPrice> applySubsidyDiscountToCategoryPrices(Collection<CategoryPrice> categoryPrices, List<DiscountCode> discounts) {
        List<CategoryPrice> categoryPrices1 = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(categoryPrices)) {
            for(CategoryPrice categoryPrice : categoryPrices) {
                categoryPrices1.add(addSubsidyDiscountToCategoryPrice(discounts, categoryPrice));
            }
        }
        return categoryPrices1;
    }

    /**
     * Apply discount code in category from price
     *N
     * @param discounts
     * @param categoryPrice
     * @return
     */
    public CategoryPrice addSubsidyDiscountToCategoryPrice(List<DiscountCode> discounts, CategoryPrice categoryPrice) {
        CategoryPrice categoryPriceClone = new CategoryPrice();
        try {
            // get a cloned category price, just not to corrupt the original object
            categoryPriceClone = (CategoryPrice)BeanUtils.cloneBean(categoryPrice);
            if(categoryPriceClone != null && categoryPriceClone.getActualStartingFromPrice() > 0d) {

                // get the discount code from user, if exists
                DiscountCode discount = (CollectionUtils.isNotEmpty(discounts))?discounts.get(0):null;
                if(categoryPriceClone.getHighestPriceInCategory() > 0) {
                    // check whether highest price in category is less than discount amount. If yes, mark the products as free
                    calculateCategoryPriceActualStartingFromPrice(categoryPriceClone, discount);
                } else {
                    categoryPriceClone.setIsFree(true);
                    categoryPriceClone.setActualStartingFromPrice(0);
                }
            }
        } catch (Exception e) {
            logger.error("Error while applying subsidy discount in category prices ", e);
        }
        return categoryPriceClone;
    }

    private void calculateCategoryPriceActualStartingFromPrice(final CategoryPrice categoryPriceClone,
        final DiscountCode discount) {
        BigDecimal startingFromPrice;
        if(discount != null && categoryPriceClone.getHighestPriceInCategory() <= discount.getDiscountAmount()) {
            categoryPriceClone.setIsFree(true);
            categoryPriceClone.setActualStartingFromPrice(0);
        } else {
            // since products are not free, get the min priced product to decide starting from price
            if(categoryPriceClone.getMinDisplayPrice() != null) {
                // get the min display price
                Price minDisplayPrice = categoryPriceClone.getMinDisplayPrice();
                // apply discount
                if(discount != null) {
                    minDisplayPrice = getDiscountedPrice(minDisplayPrice, discount);
                }
                startingFromPrice = BigDecimal.valueOf(minDisplayPrice.getAmount());
                if(startingFromPrice.intValue() < 0) {
                    startingFromPrice = new BigDecimal(0);
                }
                categoryPriceClone.setIsFree(false);
                categoryPriceClone.setActualStartingFromPrice(startingFromPrice.intValue());
            }
        }
    }

    private Price getDiscountedPrice(Price newPrice, DiscountCode discountCode) {
        if(discountCode != null && discountCode.getDiscountType().equals("dollar")) {
            Double discountedAmount = new BigDecimal(newPrice.getAmount()).subtract(new BigDecimal(discountCode.getDiscountAmount())).setScale(2, RoundingMode.HALF_UP).doubleValue();
            int discountedPoints = new BigDecimal(newPrice.getPoints()).subtract(new BigDecimal(discountCode.getDiscountAmount())).intValue();
            if(discountedAmount < 0) {
                discountedAmount = 0d;
                discountedPoints = 0;
            }
            newPrice = new Price(discountedAmount, "", discountedPoints);
        }
        return newPrice;
    }

    /**
     * Construct ProductSearchRequestBuilder
     *
     * @param program
     * @param locale
     * @return
     */
    public ProductSearchRequest.Builder getProductSearchRequestBuilder(final Program program, final Locale locale) {

        final CatalogRequestContext requestContext = getCatalogRequestContext(program, locale);

        final ProductSearchRequest.Builder builder = ProductSearchRequest.builder();
        builder.withRequestContext(requestContext);

        final String shopName = (String) program.getConfig().get(CommonConstants.SHOP_NAME_KEY);
        if (StringUtils.isNotBlank(shopName)) {
            builder.withPromoTag(shopName);
        }

        final String brandsList = (String) program.getConfig().get(CommonConstants.BRAND_LIST_KEY);
        if(StringUtils.isNotEmpty(brandsList)){
            final String[] brands = brandsList.split(",");
            builder.withBrands(new HashSet<>(Arrays.asList(brands)));
        }

        return builder;
    }

    public CatalogRequestContext getCatalogRequestContext(Program program, Locale locale) {
        final CatalogRequestContext.Builder requestContextBuilder = CatalogRequestContext.builder();
        final Object defaultProgramIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_PROGRAM);
        final Object defaultVarIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_VAR);
        final String programId = (defaultProgramIdObj != null && StringUtils.isNotBlank(defaultProgramIdObj.toString())) ?
                defaultProgramIdObj.toString() : program.getProgramId();
        final String varId = (defaultVarIdObj != null && StringUtils.isNotBlank(defaultVarIdObj.toString())) ?
            defaultVarIdObj.toString() : program.getVarId();

        if (StringUtils.isNotBlank(varId) && StringUtils.isNotBlank(programId)) {
            final Audience audience = Audience.builder()
                .withVarIdAndProgramId(varId, programId)
                .withPricingTier(program.getPricingTier())
                .withCountryCode(locale.getCountry())
                .build();
            requestContextBuilder.withAudience(audience);
                 /* Catalogs are monolingual. Language need not to be passed. Commenting if in case required later
                 .withLanguage(Language.forLocale(locale))
                */
        }
        requestContextBuilder.withCatalogId(program.getConfig().get(CommonConstants.CONFIG_CATALOG_ID).toString());
        requestContextBuilder.withPartnerCode(applicationProperties.getProperty(APEX_HEADER_PARTNERCODE));
        return requestContextBuilder.build();
    }

    /**
     * For Product Detail Page response
     *
     * @param psid
     * @param program
     * @param user
     * @param withEngraveConfig
     * @param withRelatedProduct
     * @return product for PDP
     */
    public Product getDetailPageProduct(final String psid, final Program program, final User user,
                                        final boolean withEngraveConfig, final boolean withRelatedProduct) {

        final Product product = getAppleProductDetail(psid, program, false, user,
                true, true, withEngraveConfig, withRelatedProduct);

        if (Objects.nonNull(product)) {
            final boolean isDisplayProductPageCarousel = (Boolean) program.getConfig()
                    .getOrDefault(CommonConstants.DISPLAY_PRODUCT_PAGE_CAROUSEL, Boolean.TRUE);
            if (isDisplayProductPageCarousel) {
                final String categorySlug = CollectionUtils.isNotEmpty(product.getCategories()) ?
                        product.getCategories().get(0).getSlug() : "";
                product.setCarouselImages(productCarouselImageService.getImageUrls(user.getLocale().toString(),
                        categorySlug, psid, product.getOptions()));
            }

            //Keeping enableSmartPricingOverride VPC as a fallback logic
            //If enableSmartPricingOverride VPC is True, get pricing info based on P$ call
            if (getProgramConfigValueAsBoolean(program, ENABLE_SMART_PRICING_OVERRIDE)) {
                final Cart cart = cartService.generateCartWithPriceInfo(user, program, product);
                final RedemptionPaymentLimit redemptionPaymentLimit = cart.getRedemptionPaymentLimit();

                if (Objects.nonNull(redemptionPaymentLimit)) {
                    SmartPrice smartPrice = productMapper.getSmartPrice(redemptionPaymentLimit, program);
                    product.setSmartPrice(smartPrice);
                }
            }
        }
        return product;
    }

    /**
     * Get the product detail information using the psid
     *
     * @param psid
     * @param program
     * @param isForSearch
     * @param user
     * @param applyDiscount
     * @return
     */
    public Product getAppleProductDetail(final String psid, final Program program, final boolean isForSearch,
                                         final User user, boolean applyDiscount, final boolean withVariations,
                                         final boolean withEngraveConfig, final boolean withRelatedProduct){
        final CoreProductDetailRequest coreProductDetailRequest = CoreProductDetailRequest.builder()
                .withPsid(psid)
                .withNeedVariationsInfo(withVariations)
                .withNeedRealTimeInfo(false)
                .withTargetCurrencies(getTargetCurrencies(program))
                .build();
        final MultiProductDetailRequest multiProductDetailRequest = new MultiProductDetailRequest(Optional.ofNullable(user.getLocale()),
                program, applicationProperties.getProperty(APEX_HEADER_PARTNERCODE));
        multiProductDetailRequest.withProductDetailRequest(coreProductDetailRequest);
        Product product = null;
        try {
            validateSearchInputs(multiProductDetailRequest);
            //Transform it to server request
            final Set<ProductDetailRequest> serverRequest =
                    this.detailTransformersHolder.getRequestTransformer()
                            .transform(multiProductDetailRequest, null, program);

            if (Optional.ofNullable(serverRequest).isPresent() && !serverRequest.isEmpty()) {

                final ProductServiceClient productServiceClient =
                        this.productServiceFactoryWrapper.getProductServiceClient();
                //Send request to server
                final MultiProductDetailResponse multiProductDetailResponse =
                        productServiceClient.detail(serverRequest);
                if (Optional.ofNullable(multiProductDetailResponse).isPresent() && multiProductDetailResponse.getProducts().get(0).getAvailabilityInformation().isAvailable()) {
                    final Map<String, Product> productMap = productMapper.from(multiProductDetailResponse,
                            user.getLocale(), program, user, isForSearch, applyDiscount);
                    product = productMap.get(psid);
                    //concatenating giftcards name
                    productMapper.productName2CartName(product);
                    //add pricing model structure
                    final VAROrderManagerIF varOrderManager = varOrderManagerHolder.getVarOrderManager(program.getVarId());
                    varOrderManager.computePricingModel(product, user, program);

                    //R-04828 - APL-215 - Fetch Related Products & Accessories for interstitial Page
                    final List<String> relatedProductPSIds =
                        multiProductDetailResponse.getProducts().get(0).getRelatedProducts();
                    if (CollectionUtils.isNotEmpty(relatedProductPSIds)) {
                        processRelatedProducts(program, user, withRelatedProduct, product, relatedProductPSIds);
                    }

                    if (!withRelatedProduct) {
                        //R-03024 - Support AppleCare offering
                        fetchAppleCareOffering(program, user, withEngraveConfig, product, multiProductDetailResponse);

                        //R-04626 - AMP Phase 2 - Fetch Available AMP  from PS
                        fetchAMPSubscriptions(product, multiProductDetailResponse.getProducts().get(0).getAppliedChanges());
                    }
                }
                if (!withRelatedProduct) {
                    //Set Product with Engrave and Gift configurations
                    setEngraveInfo(user, program, product, psid, withEngraveConfig);
                    setGiftInfo(user, program, product, psid);
                }
            }

        } catch (final ServiceException se) {
            logger.error(EXCEPTION_WHILE_RETRIEVING_PRODUCT_DETAIL, se);
        } catch (final Exception e) {
            logger.error(EXCEPTION_WHILE_RETRIEVING_PRODUCT_DETAIL, e);
        }
        return product;
    }

    private void processRelatedProducts(final Program program, final User user, final boolean withRelatedProduct,
        final Product product, final List<String> relatedProductPSIds) {

        final boolean relatedProductsEnabled =
            (Boolean) program.getConfig().getOrDefault(IS_RELATED_PRODUCTS_ENABLED, Boolean.FALSE);
        product.setHasRelatedProduct(
            relatedProductsEnabled && CollectionUtils.isNotEmpty(relatedProductPSIds));

        if (withRelatedProduct && product.isHasRelatedProduct()) {
            logger.info("Found Related items for the product {}...Retrieval of its product details has " +
                    "started..",
                product.getName());

            //Make additional product detail call to fetch information for Related products
            final List<Product> relatedProductList =
                getAppleMultiProductDetail(relatedProductPSIds, program, false, user, false, true);

            if (CollectionUtils.isNotEmpty(relatedProductList)) {
                product.getRelatedProducts().addAll(relatedProductList.stream()
                    .filter(Product::isAvailable)
                    .collect(Collectors.toList()));
                logger.info("Retrieval of product details for Related items has ended..");
            } else {
                logger.info("No product details found for Related items");
            }
        }
    }

    private void fetchAMPSubscriptions(final Product product, final List<AppliedChange> appliedChanges) {
        if (CollectionUtils.isNotEmpty(appliedChanges)) {
            Set<String> ampSubscriptionConfig = new HashSet<>();
            appliedChanges.stream()
                .filter(appliedChange -> Objects.nonNull(appliedChange.getPayload()))
                .forEach(appliedChange -> {
                    final Payload payload = appliedChange.getPayload();
                    if (payload.getPayloadType().equals(PayloadType.SUBSCRIPTION) &&
                        payload.getSubscriptionType().isPresent()) {
                        ampSubscriptionConfig.add(payload.getSubscriptionType().get());
                    }
                });

            product.setAmpSubscriptionConfig(ampSubscriptionConfig);
        }
    }

    private void fetchAppleCareOffering(final Program program, final User user, final boolean withEngraveConfig,
        final Product product, final MultiProductDetailResponse multiProductDetailResponse) {
        final boolean appleCareServiceEnabled = getProgramConfigValueAsBoolean(program,
            ENABLE_APPLE_CARE_SERVICE_PLAN);
        if (appleCareServiceEnabled && !withEngraveConfig &&
            CollectionUtils.isNotEmpty(multiProductDetailResponse.getProducts().get(0).getServicePlanInfo())) {

            final List<String> servicePlanInfoPSIds =
                multiProductDetailResponse.getProducts().get(0).getServicePlanInfo()
                    .stream().map(ServicePlanInfo::getPsid).collect(Collectors.toList());

            populateAppleCareServiceInfo(program, user, product, servicePlanInfoPSIds);
        }
    }

    private void populateAppleCareServiceInfo(final Program program, final User user, final Product product,
        final List<String> servicePlanInfoPSIds ) {
            logger.info("Found AppleCare item for the product {}...Retrieval of its product details has started..",
                product.getName());

            //Make additional product detail call to fetch information for AppleCare products
            final List<Product> servicePlanProduct =
                getAppleMultiProductDetail(servicePlanInfoPSIds, program, false, user, false, false);

            if (CollectionUtils.isNotEmpty(servicePlanProduct)) {
                product.getAddOns().getServicePlans().addAll(servicePlanProduct);
                logger.info("Retrieval of product details for AppleCare items has ended..");
            } else {
                logger.info("No product details found for AppleCare items");
            }
        }

    /**
     * Get the list of products for the given psids
     *
     * @param psids
     * @param program
     * @param isForSearch
     * @param user
     * @param applyDiscount
     * @param withVariations
     * @return List of products or null for exception
     */
    public List<Product> getAppleMultiProductDetail(final List<String> psids, final Program program,
        final boolean isForSearch, final User user, boolean applyDiscount, final boolean withVariations) {

        final MultiProductDetailRequest multiProductDetailRequest =
                new MultiProductDetailRequest(Optional.ofNullable(user.getLocale()),
                        program, applicationProperties.getProperty(APEX_HEADER_PARTNERCODE));

        Set<CurrencyUnit> targetCurrencies = getTargetCurrencies(program);

        psids.forEach(psid ->
            multiProductDetailRequest.withProductDetailRequest(CoreProductDetailRequest.builder()
                .withPsid(psid)
                .withNeedVariationsInfo(withVariations)
                .withNeedRealTimeInfo(false)
                .withTargetCurrencies(targetCurrencies)
                .build())
        );

        List<Product> products = new ArrayList<>();
        try {
            validateSearchInputs(multiProductDetailRequest);
            //Transform it to server request
            final Set<ProductDetailRequest> serverRequest =
                this.detailTransformersHolder.getRequestTransformer().transform(multiProductDetailRequest, null, program);

            if (Optional.ofNullable(serverRequest).isPresent() && !serverRequest.isEmpty()) {

                final ProductServiceClient productServiceClient =
                    this.productServiceFactoryWrapper.getProductServiceClient();
                //Send request to server
                final MultiProductDetailResponse multiProductDetailResponse =
                    productServiceClient.detail(serverRequest);

                if (Optional.ofNullable(multiProductDetailResponse).isPresent()
                    && CollectionUtils.isNotEmpty(multiProductDetailResponse.getProducts())
                    && multiProductDetailResponse.getProducts().get(0).getAvailabilityInformation().isAvailable()) {

                    final Map<String, Product> productMap = productMapper.from(multiProductDetailResponse,
                        user.getLocale(), program, user, isForSearch, applyDiscount);

                    productMap.entrySet().stream().forEach(stringProductEntry -> {
                        final Product product = stringProductEntry.getValue();

                        //concatenating giftcards name
                        productMapper.productName2CartName(product);
                        //add pricing model structure
                        final VAROrderManagerIF varOrderManager =
                            varOrderManagerHolder.getVarOrderManager(program.getVarId());
                        varOrderManager.computePricingModel(product, user, program);

                        //Set Product with Engrave and Gift configurations
                        setEngraveInfo(user, program, product, product.getPsid(), false);

                        products.add(product);
                    });
                }
            }
        } catch (final ServiceException se) {
            logger.error("Exception while retrieving Multi product detail {}", se);
        } catch (final Exception e) {
            logger.error("Exception while retrieving Multi product detail {}", e);
        }
        return products;
    }

    public Map<String, Map<String,String>> getAllActivationFees(final String varId, final String programId) {
        final VAROrderManagerIF varOrderManager = varOrderManagerHolder.getVarOrderManager(varId);
        return varOrderManager.getAllActivationFees(varId, programId);
    }

    public ProductResponse getProducts(ProductSearchRequest.Builder builder, Locale locale, Program program, User
        user, final boolean withProducts) throws
        ServiceException {
        try {
            ProductResponse productResponse = appleSearch(builder.build(), locale, program, user, (user!=null)?user
                .getDiscounts():null, withProducts);
            return productResponse;
        } catch (EntityNotFoundException enfe) {
            logger.error(NO_SUCH_PRODUCT_AVAILABLE, enfe);
            throw new EntityNotFoundException(NO_SUCH_PRODUCT_AVAILABLE);
        } catch (RequestValidationException rve) {
            logger.error(INVALID_REQUEST_IN_BROWSE_PRODUCTS, rve);
            throw new RequestValidationException(INVALID_REQUEST_IN_BROWSE_PRODUCTS + rve.getMessage());
        } catch (Exception e) {
            logger.error(EXCEPTION_WHILE_SEARCH_FOR_PRODUCTS, e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        }
    }

    public ProductSearchRequest.Builder getProductSearchRequestBuilder(Set<String> categorySlugs, String keyword,
                                                                       String sort, String sortOrder, Locale locale, Integer[] pointsRange, Integer pageSize, Integer resultOffSet,
                                                                       Program program,String promoTag, boolean withVariations, boolean withFacets, Map<String, List<Option>> facetsFilters) {
        return getProductSearchRequestBuilder(categorySlugs, keyword, sort, sortOrder, locale, pointsRange, pageSize,
                resultOffSet, program, promoTag, withVariations, withFacets, facetsFilters, false);

    }

    public ProductSearchRequest.Builder getAffordableProductSearchRequestBuilder(Locale locale, Integer[] pointsRange, Integer pageSize, Integer resultOffSet, Program program) {
        return getProductSearchRequestBuilder(null, null, SALES_RANK, DESCENDING, locale,
                pointsRange, pageSize, resultOffSet, program, null, true, false,
                null, true);

    }

    private ProductSearchRequest.Builder getProductSearchRequestBuilder(Set<String> categorySlugs, String keyword,
        String sort, String sortOrder, Locale locale, Integer[] pointsRange, Integer pageSize, Integer resultOffSet,
        Program program,String promoTag, boolean withVariations, boolean withFacets, Map<String, List<Option>> facetsFilters, final Boolean isAffordabilitySearch) {
        //Construct ProductSearchRequestBuilder
        final String searchResultLimit = applicationProperties.getProperty("searchResultLimit");
        final ProductSearchRequest.Builder builder= getProductSearchRequestBuilder(program, locale);

        if (withFacets && MapUtils.isNotEmpty(facetsFilters)) {
            builder.withDynamicFilters(facetsFilters.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().map(i->i.getValue()).collect(Collectors.toSet())
                    )));

        }
        if(CollectionUtils.isNotEmpty(categorySlugs)){
            builder.withCategories(categorySlugs);
        }
        builder.withVariations(true);

        if(StringUtils.isNotBlank(promoTag)){
            builder.withPromoTag(promoTag);
        }

        buildSearchRequestWithSort(keyword, sort, sortOrder, builder);

        if(!StringUtils.isBlank(keyword)) {
            builder.withQueryPhrase(AppleUtil.skuSearch(keyword));
        }
        if (pageSize != null) {
            builder.withResultLimit(pageSize);
        } else {
            builder.withResultLimit((searchResultLimit == null) ? 2000 : Integer.parseInt(searchResultLimit));
        }
        if (resultOffSet != null) {
            builder.withOffset(resultOffSet);
        }
        builder.withTargetCurrencies(getTargetCurrencies(program));
        //Get all products for the available points
        //Filter products based on points range from request parameter
        if (pointsRange != null) {
            CurrencyUnit currency = CommonAttributes.POINTS_CURRENCY_UNIT;
            final Range<Money> priceRange =
                    Range.closed(Money.of(currency, pointsRange[0], RoundingMode.UNNECESSARY), Money.of(currency, pointsRange[1], RoundingMode.UNNECESSARY));
            builder.withPriceRangeFilter(priceRange);
        }

        //Set Smart Pricing based on VPC - enableSmartPricing
        if (getProgramConfigValueAsBoolean(program, ENABLE_SMART_PRICING)) {
            SplitTender splitTender = getSplitTenderConfig(program);
            splitTender.getMaxCashAmount().ifPresent(builder::withMaxCashAmount);
            splitTender.getMaxPointsPercentage().ifPresent(builder::withMaxPointsPercentage);
        }

        if(withFacets){
            getAdditionalFacets(builder,categorySlugs,program, keyword);
        }

        if (isAffordabilitySearch) {
            applyAffordabilitySearchCriteria(pointsRange, program, builder);
        }

        return builder;
    }

    private void applyAffordabilitySearchCriteria(Integer[] pointsRange, Program program, ProductSearchRequest.Builder builder) {
        /**********************
         * 1- affordability feature only available when payment Options is POINTS.
         *  in this case redemption options will contain POINTSONLY or POINTSFIXED in addition to splitpay
         * 2- Payment Options contains "POINTS"
         *  affordability range is [0 .. userBalance] (parameter 'pointsRange' coming from Controller)
         *  2.1 - if splitPay contains "percentage" ==> maxPointsPercentage = paymentMaxLimit
         *  2.2 - if splitPay contains "dollar" ==> maxCashAmount = paymentMaxLimit
         *      it's possible to send both "maxPointsPercentage" and "maxCashAmount" in the same request
         *********************/
        if (program.getPayments().stream()
                .anyMatch(payment -> CommonConstants.PaymentOption.POINTS.name().equals(payment.getPaymentOption()))) {

            /*********************
             * in case of split pay,
             * # the paymentMaxLimit of 'dollar' is MaxCashAmount
             * # the paymentMaxLimit of 'percentage' is MaxPointsPercentage
             * ********************/
            SplitTender splitTender = getSplitTenderConfig(program);
            splitTender.getMaxCashAmount().ifPresent(builder::withMaxCashAmount);
            splitTender.getMaxPointsPercentage().ifPresent(builder::withMaxPointsPercentage);

            builder.withApplyEhf(true);
            if (StringUtils.isNotBlank(program.getPricingTier())) {
                builder.withPricingTier(program.getPricingTier());
            }
        }
    }

    private void buildSearchRequestWithSort(final String keyword, final String sort, final String sortOrder,
        final ProductSearchRequest.Builder builder) {
        if(StringUtils.isNotBlank(sortOrder)) {
            if(StringUtils.isBlank(sort) && StringUtils.isBlank(keyword)){
                builder.withSort(SortField.NAME, Optional.of(SortOrder.valueOf(sortOrder)));
            } else if(!StringUtils.isBlank(sort)){
                builder.withSort(SortField.valueOf(sort), Optional.of(SortOrder.valueOf(sortOrder)));
            }
        } else {
            if(StringUtils.isBlank(sort) && StringUtils.isBlank(keyword)){
                builder.withSort(SortField.NAME);
            } else if(!StringUtils.isBlank(sort)){
                builder.withSort(SortField.valueOf(sort));
            }
        }
    }

    public Set<CurrencyUnit> getTargetCurrencies(Program program) {
        final Set<CurrencyUnit> targetCurrencies = new HashSet<>();
        targetCurrencies.add(CurrencyUnit.of("PNT"));
        targetCurrencies.add(program.getTargetCurrency());
        return targetCurrencies;
    }


    private Optional<VariationSearchDocument> getVariationProductByLanguagePreference(Set<VariationSearchDocument> variations, ProductSearchRequest serverRequest, User user){
        //case 1:  if user search for specific language products.
        Optional<VariationSearchDocument> variationProduct =
            variations.stream().filter(variationSearchDocument -> isOptionsContainLanguageFromQueryPhrase(serverRequest,
                variationSearchDocument)
            ).findAny();

        //case 2: Show localized product based on user locale.
        //Case 3: Show relevant localized product if case 1 & case 2 not matched.
        if (variationProduct.isEmpty()) {
            List<String> localeStrings =new ArrayList<>();
            localeStrings.addAll(getRelatedLanguage(user));
            for (String locale : localeStrings) {
                variationProduct = variations.parallelStream()
                    .filter(variationSearchDocument -> {
                        com.b2s.service.product.common.domain.response.Option option =
                            variationSearchDocument.getOptions().get(CommonConstants.LANGUAGE);
                        return Objects.nonNull(option) && option.getKey().get().equalsIgnoreCase(locale);
                    }).findAny();
                if(variationProduct.isPresent()) {
                    break;
                }
            }
        }

        if(variationProduct.isEmpty()) {
            variationProduct = variations.stream()
                .filter(v->v.getOptions().get(CommonConstants.LANGUAGE).getKey().isPresent())//Remove items from the list that are not having key
                .sorted(Comparator.comparing(v->v.getOptions().get(CommonConstants.LANGUAGE).getKey().get()))
                .findFirst();
        }
        return variationProduct;
    }

    private boolean isOptionsContainLanguageFromQueryPhrase(final ProductSearchRequest serverRequest,
        final VariationSearchDocument variationSearchDocument) {
        com.b2s.service.product.common.domain.response.Option option =
            variationSearchDocument.getOptions().get(CommonConstants.LANGUAGE);
        if (Objects.nonNull(option) && serverRequest.getQueryPhrase().isPresent()) {
            String[] searchKey = serverRequest.getQueryPhrase().orElse("").split(" ");

            for (String key : searchKey) {
                if (option.getValue().toLowerCase().contains(key.toLowerCase())) {
                    return true;
                }
            }

        }
        return false;
    }


    // Adds user local as default locale
    public List<String> getRelatedLanguage(final User user){
        final RelevantLanguageEntity relevantLanguageEntity = relevantLanguageDao.getByLocale(user.getLocale().toString());
        List<String> languageList=new ArrayList<>();
        languageList.add(user.getLocale().toString());
        if(Objects.nonNull(relevantLanguageEntity)){
            languageList.addAll(Arrays.asList(relevantLanguageEntity.getRelevantLanguage().split("\\,")));
        }
        return languageList;
    }

    public  ProductSearchRequest.Builder getAdditionalFacets(ProductSearchRequest.Builder builder,
        Set<String> categorySlugs, final Program program, final String keyword) {

        final List<String> slugsWithFacetsFilter = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
            program.getVarId(), program.getProgramId(), SLUGS_WITH_FACETS_FILTER);
        String facetFilterSlug= slugsWithFacetsFilter.stream().filter(s -> categorySlugs.stream().anyMatch(s1 -> s1.contains(s))).findFirst().orElse(null);

        List<String> facets = null;
        if(StringUtils.isNotBlank(keyword)) {
            facets = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
                    program.getVarId(), program.getProgramId(), SEARCH + DOT_FACETS);
        } else if(StringUtils.isNotBlank(facetFilterSlug)) {
            facets = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
                    program.getVarId(), program.getProgramId(), facetFilterSlug + DOT_FACETS);
        }

        if(CollectionUtils.isNotEmpty(facets)) {
            Set<WrappedFacetName> additionalFacets = new HashSet<>();
            facets.forEach(facet -> additionalFacets.add(WrappedFacetName.builder().withFacetName(facet).withCollapseResults(true).build()));

            builder.withAdditionalFacets(additionalFacets);
        }
        return builder;
    }

    public  Map<String,List<Option>> getFacetsFilters(final ProductSearchResponse productSearchResponse,
        final ProductSearchRequest request, final Program program, final Locale locale){

        final List<String> slugsWithFacetsFilter = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
            program.getVarId(), program.getProgramId(), SLUGS_WITH_FACETS_FILTER);
        Map<String,List<Option>> sortedMap = new LinkedHashMap<>();
        String facetFilterSlug= slugsWithFacetsFilter.stream()
                .filter(s -> request.getCategories().stream().anyMatch(s1 -> s1.contains(s)))
                .findFirst().orElse(null);

        List<String> facets = null;
        if(request.getQueryPhrase().isPresent()) {
            facets = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
                    program.getVarId(), program.getProgramId(), SEARCH + DOT_FACETS);
        } else if(StringUtils.isNotBlank(facetFilterSlug)) {
            facets = varProgramCatalogConfigService.getListOfValue(program.getCatalogId(),
                    program.getVarId(), program.getProgramId(), facetFilterSlug + DOT_FACETS);
        }

        if(CollectionUtils.isNotEmpty(facets)) {
            List<String> finalFacets = facets;
            Map<String, List<Option>> facetFilterMap = productSearchResponse.getFacets().stream()
                .filter(facet -> finalFacets.contains(facet.getName()))
                .collect(Collectors.toMap(Facet::getName,
                    o -> {
                        Stream<Option> facetsOptions = o.getEntries().stream()
                                .map(facetEntry -> new Option(facetEntry.getParentName(),
                                        facetEntry.getValue(), Optional.empty(), o.getLabel()));

                        // if the locale exists (ignore case) in the locales white list to sort filter options
                        if (sortFilterOptionsLocales.stream().anyMatch(locale.toString()::equalsIgnoreCase)) {
                            facetsOptions = facetsOptions.sorted(Comparator.comparing(Option::getValue, String.CASE_INSENSITIVE_ORDER));
                        }
                        return facetsOptions.collect(Collectors.toList());
                }));

            if (MapUtils.isNotEmpty(facetFilterMap)) {
                facets.stream().forEach(
                    facetKey -> {
                        if (facetFilterMap.containsKey(facetKey)) {
                            sortedMap.put(facetKey, facetFilterMap.get(facetKey));
                        }
                    }
                );
                return sortedMap;
            }
        }
        return null;
    }

    /**
     * Get Gift Products List for the qualifying PSID based on PS response
     * if applyDiscount flag is True, call P$ to get updated discount amount for the products
     *
     * @param user
     * @param program
     * @param qualifyingPsid
     * @param applyDiscount
     * @return
     */
    public List<Product> getGiftItem(final User user, final Program program, final String qualifyingPsid,
        final boolean applyDiscount) {
        List<Product> products = null;
        final List<GiftItem> giftItems = giftPromoService.getGiftItemList(user, qualifyingPsid, program);
        if (CollectionUtils.isNotEmpty(giftItems)) {
            logger.info("DGwP --> Qualifying Product: {}, Available Gift Items: {}", qualifyingPsid, giftItems);
            products =
                getAppleMultiProductDetail(giftPromoService.getGiftPsids(giftItems), program, false, user, true, false);

            //Set Product details with Offers for Single gift Item for a Cart Item
            if (applyDiscount || giftItems.size() == 1) {
                giftItemManager.addGiftItemPricingInfo(user, program, products, giftItems);
            }
        }
        return products;
    }

    /**
     * Set Product with the below attributes
     * isEligibleForGift flag --> Gift products are mapped with the qualifying psid - refer var_program_gift_promo
     * isMultiGiftAvailable flag --> More gift products are mapped with the qualifying psid
     * availableGiftItems list --> list of gift products with its engrave configuration
     *
     * @param user
     * @param program
     * @param product
     * @param psid
     */
    private void setGiftInfo(final User user, final Program program, final Product product, final String psid){
        final List<Product> giftProducts = getGiftItem(user, program, psid, false);
        //Checks whether the qualifying item has gift item
        if (CollectionUtils.isNotEmpty(giftProducts)) {
            //Checks whether the selected gift item is Engrave enabled to redirect to corresponding Engrave page
            product.getAddOns().setAvailableGiftItems(giftProducts);
        }
    }

    /**
     * Set Product with the below attributes
     * engravable flag --> VPC engrave enabled and configured in Category configuration table
     * engrave object --> sets Engrave configuration based on DB configuration and withEngraveConfig flag
     *
     * @param user
     * @param program
     * @param product
     * @param psid
     * @param withEngraveConfig
     */
    private void setEngraveInfo(final User user, final Program program, final Product product, final String psid,
        final boolean withEngraveConfig) {
        final String categorySlug = AppleUtil.getCategorySlug(product);

        final boolean engraveDisabled =
            (Boolean) program.getConfig().getOrDefault(CommonConstants.ENGRAVE_DISABLED, Boolean.FALSE);

        //Sets Engrave details only if it is not disabled in VPC level - Used to redirect to Engrave page
        if (!engraveDisabled) {
            product.setEngravable(engravingService
                .isEngraveEnabled(user, categorySlug, psid));

            //Sets Engrave object only if withEngraveConfig from UI is set as true to load Engrave page
            if (withEngraveConfig) {
                product.setEngrave(engravingService.getEngravingConfiguration(user, categorySlug, psid, null));
            }
        }
    }
}