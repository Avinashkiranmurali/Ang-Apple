package com.b2s.apple.mapper;

import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.apple.services.EngravingService;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.common.services.util.ImageObfuscatory;
import com.b2s.common.services.util.MerchantRepositoryHolder;
import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.PricingUtil;
import com.b2s.rewards.apple.util.ShipmentQuoteUtil;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.model.ProductImage;
import com.b2s.rewards.model.Supplier;
import com.b2s.service.product.client.application.detail.MultiProductDetailResponse;
import com.b2s.service.product.common.domain.AvailabilityInformation;
import com.b2s.service.product.common.domain.DiscountPriceInfo;
import com.b2s.service.product.common.domain.ProductType;
import com.b2s.service.product.common.domain.change.AppliedChange;
import com.b2s.service.product.common.domain.change.PayloadType;
import com.b2s.service.product.common.domain.response.Option;
import com.b2s.service.product.common.domain.response.*;
import com.b2s.shop.common.User;
import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by meddy on 7/13/2015.
 */
@Component
public class ProductMapper {

    @Autowired
    private OptionMapper optionMapper;

    @Autowired
    private ImageObfuscatory imageObfuscatory;

    @Autowired
    private CategoryConfigurationService categoryConfigurationService;

    @Autowired
    private EngravingService engravingService;

    @Autowired
    private ShipmentQuoteUtil shipmentQuoteUtil;

    @Autowired
    private Map<String, String> supplierProductMapping;

    @Autowired
    private MerchantRepositoryHolder merchantRepositoryHolder;

    @Autowired
    @Qualifier("legacyMerchantCodeMappings")
    private Map<String, String> legacyMerchantCodeMappings;

    private static final Logger logger = LoggerFactory.getLogger(ProductMapper.class);

    public Product from(final ProductSearchDocument productSearchDocument, final Locale locale,
        final Program program, final User user, final List<String> productTilesOptions) {

        final Product appleProduct = new Product();
        appleProduct.setPsid(productSearchDocument.getPsid());
        appleProduct.setSku(productSearchDocument.getSku());
        appleProduct.setName(productSearchDocument.getName());

        final List<String> categorySlugs = productSearchDocument.getCategorySlugs();
        //Set Category based on Search Response
        setCategory(getCategoryForSearch(categorySlugs, appleProduct, locale), appleProduct, locale);

        appleProduct.setAvailable(AppleUtil.isAWPAvailableProduct(productSearchDocument.getAvailabilityInformation()));
        appleProduct.setUpc(productSearchDocument.getUpc().orElse(null));
        appleProduct.setAppleSku(productSearchDocument.getModel().orElse(null));

        appleProduct.setShortDescription(productSearchDocument.getShortDescription().orElse(""));
        appleProduct.setBrand(productSearchDocument.getBrand().orElse(""));
        appleProduct.setManufacturer(productSearchDocument.getManufacturer().orElse(Optional.ofNullable(productSearchDocument.getMerchant()).orElse("")));
        appleProduct.setAverageRating(productSearchDocument.getAverageRating().orElse(0f));
        appleProduct.setImages(this.imageObfuscatory.resizeImageUrls(ImageURLs.transformProductImages(productSearchDocument.getImageUrls())));
        appleProduct.setOptions(optionMapper.getOptions(categorySlugs, productSearchDocument.getOptions(), locale,
            true, program.getVarId(), program.getProgramId()));
        final boolean hasVariations = productSearchDocument.hasVariations();
        ImmutableSet<VariationSearchDocument> variationSearchDocuments = productSearchDocument.getVariations();

        appleProduct.setHasVariations(hasVariations && CollectionUtils.isNotEmpty(variationSearchDocuments) && variationSearchDocuments.size() > 1);

        //To set OptionsConfigurationData
        if(appleProduct.isHasVariations()){
            appleProduct.setOptionsConfigurationData(getOptionsConfigurationData(variationSearchDocuments, productTilesOptions));
        }

        appleProduct.setOffers(
            List.of(getOffer(appleProduct, productSearchDocument.getPricingInformation(), true, program, user, true)));
        return appleProduct;
    }

    /**
     * Get Category based on PS Search response
     *
     * @param categorySlugs
     * @param appleProduct
     * @param locale
     * @return
     */
    private Category getCategoryForSearch(final List<String> categorySlugs, final Product appleProduct,
        final Locale locale) {
        Category tempCategory = null;
        // set Category on parent level and not for Variations
        for (final String categorySlug : categorySlugs) {
            final Category category = getCategory(locale, categorySlug);
            if (Objects.nonNull(category)) {
                if (isParentCategoryAvailable(category)) {
                    tempCategory = category;

                    //Check for Non-Accessory or Hero Category Slug
                    if (isHeroCategory(category)) {
                        return category; //returns only if it is Hero Category
                    }
                } else {
                    logger.info("PS-Search:Parent Category not found in Repository for slug {}", categorySlug);
                    tempCategory = getCategory(tempCategory, category);
                }
            } else {
                logger.warn("PS-Search:Category not found in Repository for slug {}", categorySlug);
            }
        }

        //Set Accessory Item to TRUE as it is not a Hero product
        appleProduct.setAccessoryItem(true);
        return tempCategory;
    }

    private Category getCategory(Category tempCategory, final Category category) {
        if (Objects.isNull(tempCategory)) {
            tempCategory = category;
        }
        return tempCategory;
    }

    private boolean isParentCategoryAvailable(final Category category) {
        return CollectionUtils.isNotEmpty(category.getParents()) &&
            Objects.nonNull(category.getParents().get(0));
    }

    /**
     * Set Category based on Parent Category
     *
     * @param category
     * @param appleProduct
     * @param locale
     */
    private void setCategory(final Category category, final Product appleProduct, final Locale locale) {
        if (Objects.nonNull(category)) {
            if (isParentCategoryAvailable(category)) {
                appleProduct.getCategories().add(category);
            } else {
                logger.warn("Parent Category not found for psid - [{}], Name - [{}].", appleProduct.getPsid(),
                    appleProduct.getName());
                appleProduct.getCategories().add(setDefaultParentCategory(locale, category));
            }
        } else {
            logger.error("No category found for psid - [{}], Name - [{}].", appleProduct.getPsid(),
                appleProduct.getName());
        }
    }

    /**
     * Checks whether the Category is not Accessory
     *
     * @param category
     * @return true only if category slug and parent category slug are not accessory
     */
    private boolean isHeroCategory(final Category category) {
        if (!AppleUtil.isAccessories(category.getSlug())) {
            return category.getParents().stream()
                .filter(Objects::nonNull)
                //Checks Parent Category also NON Accessory
                .anyMatch(parent -> !AppleUtil.isAccessories(parent.getSlug()));
        }
        return false;
    }

    public Map<String, Product> from(final MultiProductDetailResponse multiProductDetailResponse, final Locale
        locale, final Program program, final User user, final boolean isForSearch, final boolean applyDiscount) {
        Map<String, Product> productMap = new HashMap<>();
        if(Optional.ofNullable(multiProductDetailResponse).isPresent() && CollectionUtils.isNotEmpty(multiProductDetailResponse.getProducts())) {
            multiProductDetailResponse.getProducts().forEach(product ->
                productMap.put(product.getPsid(), from(product, locale, program, user, isForSearch, applyDiscount))
            );
        }
        return productMap;
    }

    private Map<String, Object> filterAdditionalInfoForDisplay(final String categorySlug, final String psid,
                                                               final Map<String, String> additionalInfo, final User user, final Program program) {
        final Map<ProductAttributeConfiguration, Object> rawFilteredAdditionalInfoForDisplay =  new HashMap<>();
        final Map<String, Object> filteredAdditionalInfoForDisplay = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(additionalInfo)) {
            boolean isEngraveActive = false;
            final CategoryConfiguration categoryConfiguration =
                    categoryConfigurationService.getCategoryConfigurationByCategoryName(categorySlug, psid);
            if (Objects.nonNull(categoryConfiguration)) {
                final EngraveConfiguration engraveConfiguration =
                        engravingService.getEngraveConfiguration(user.getLocale().toString(), categoryConfiguration.getId());
                isEngraveActive = Objects.nonNull(engraveConfiguration) && engraveConfiguration.isActive();
            } else {
                logger.debug("Category Slug {} does not have any product attributes in category Configuration ",
                        categorySlug);
            }

            List<ProductAttributeConfiguration> productAttributes =
                    categoryConfigurationService.getProductDetailsAttributesWithCategoryNull(categorySlug);


            productAttributes
                .stream()
                .filter(ob -> Objects.nonNull(ob.getCategoryConfiguration()))
                .forEach(paob -> populateAdditionalInfoForDisplay(additionalInfo, rawFilteredAdditionalInfoForDisplay, paob));

            productAttributes
                .stream()
                .filter(ob -> Objects.isNull(ob.getCategoryConfiguration()))
                .forEach( paob -> populateAdditionalInfoForDisplay(additionalInfo, rawFilteredAdditionalInfoForDisplay, paob));


            rawFilteredAdditionalInfoForDisplay
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(ob -> Objects.nonNull(ob.getKey().getOrderBy())?ob.getKey().getOrderBy(): Integer.MAX_VALUE))
                .forEach(ob -> filteredAdditionalInfoForDisplay.put(ob.getKey().getAttributeName(),ob.getValue()));

            // overriding engravable flag as per category configuration
            updateEngravableFlag(categoryConfiguration, program, filteredAdditionalInfoForDisplay, isEngraveActive);
        }
        return filteredAdditionalInfoForDisplay;
    }

    // overriding engravable flag as per category configuration
    private void updateEngravableFlag(final CategoryConfiguration categoryConfiguration, final Program program,
        final Map<String, Object> filteredAdditionalInfoForDisplay, final boolean isEngraveActive) {
        if (Objects.nonNull(categoryConfiguration) && categoryConfiguration.isEngravable()) {
            filteredAdditionalInfoForDisplay.put(CommonConstants.ENGRAVABLE, "true");
            final Object objEngraveDisabled = program.getConfig().get(CommonConstants.ENGRAVE_DISABLED);
            final boolean engraveDisabled = objEngraveDisabled != null ? (boolean) objEngraveDisabled : false;
            if (engraveDisabled || !isEngraveActive) {
                filteredAdditionalInfoForDisplay.put(CommonConstants.ENGRAVABLE, "false");
            }
        }
    }

    private void populateAdditionalInfoForDisplay(final Map<String, String> additionalInfo,
        final Map<ProductAttributeConfiguration, Object> rawFilteredAdditionalInfoForDisplay, final ProductAttributeConfiguration productAttributeConfiguration) {
        if(StringUtils.isNotBlank(productAttributeConfiguration.getAttributeName()) && !rawFilteredAdditionalInfoForDisplay
            .keySet()
            .stream()
            .anyMatch(ob -> ob.getAttributeName().equals(productAttributeConfiguration.getAttributeName()))) {
            final Optional<Map.Entry<String, String>> value = additionalInfo
                .entrySet()
                .stream()
                .filter(ob -> productAttributeConfiguration.getAttributeName().equals(ob.getKey() ))
                .findFirst();
            if(value.isPresent()){
                rawFilteredAdditionalInfoForDisplay.put(productAttributeConfiguration, value.get().getValue() );
            }
        }
    }


    public Product from(com.b2s.service.product.common.domain.response.Product productDetail, Locale locale, final
    Program program, final User user, boolean isForSearch, final boolean applyDiscount) {
        Product appleProduct = new Product();
        String psid = productDetail.getPsid();

        appleProduct.setProductId(psid);
        setAttributesBasedOnProductDetail(appleProduct, productDetail);

        appleProduct.setPsid(psid);
        appleProduct.setSku(productDetail.getSku());
        appleProduct.setAppleSku(productDetail.getModel().orElse(null));
        appleProduct.setName(productDetail.getName());
        appleProduct.setAverageRating(productDetail.getAverageRating().orElse(0f));
        appleProduct.setLongDescription(productDetail.getLongDescription().orElse(StringUtils.EMPTY));
        appleProduct.setUpc(productDetail.getUpc().orElse(StringUtils.EMPTY));
        appleProduct.setShortDescription(productDetail.getShortDescription().orElse(StringUtils.EMPTY));
        appleProduct.setBrand(productDetail.getBrand().orElse(StringUtils.EMPTY));
        appleProduct.setProductType(productDetail.getProductType());
        appleProduct.setManufacturer(
            productDetail.getManufacturer().orElse(productDetail.getMerchant().orElse(StringUtils.EMPTY)));
        appleProduct.setImages(
            this.imageObfuscatory.resizeImageUrls(ImageURLs.transformProductImages(productDetail.getImageUrls())));
        appleProduct.setAvailable(AppleUtil.isAWPAvailableProduct(productDetail.getAvailabilityInformation()));
        if (Objects.nonNull(productDetail) && CollectionUtils.isNotEmpty(productDetail.getCategories())) {
            List<Category> categoriesForDetail = new ArrayList<>();

            final Category firstCategoryTemp =
                getCategoryForDetail(categoriesForDetail, productDetail, locale, isForSearch);

            appleProduct.setAccessoryItem(productDetail.getCategories().stream()
                .anyMatch(c -> (AppleUtil.isAccessories(c.getSlug())) ||
                    (Objects.nonNull(c.getAncestors()) && c.getAncestors()
                        .stream()
                        .filter(Objects::nonNull)
                        .anyMatch(p -> AppleUtil.isAccessories(p.getSlug())))));

            setAppleProductCategory(productDetail, locale, program, appleProduct, categoriesForDetail,
                firstCategoryTemp);


        }

        appleProduct.setOptions(optionMapper.getOptions(productDetail.getCategories().stream().map(category -> category.getSlug()).collect(Collectors.toList()), productDetail.getOptions(), locale, isForSearch, program.getVarId(), program.getProgramId()));
        final String categorySlug = CollectionUtils.isNotEmpty(appleProduct.getCategories())?
            appleProduct.getCategories().get(0).getSlug():
            CollectionUtils.isNotEmpty(productDetail.getCategories())?
                productDetail.getCategories().get(0).getSlug():null;

        appleProduct.setAdditionalInfo(filterAdditionalInfoForDisplay(categorySlug, appleProduct.getPsid(),
            productDetail.getAdditionalInfo(), user, program));

        setMerchantSpecificData(appleProduct, productDetail.getMerchantSpecificData());

        final Offer offer =
            getOffer(appleProduct, productDetail.getPricingInformation(), true, program, user, applyDiscount);
        setMerchant(offer, productDetail);
        appleProduct.setOffers(List.of(offer));

        setShipmentInformation(appleProduct, productDetail.getSku(), categorySlug,
            productDetail.getAvailabilityInformation(), locale, program.getVarId());

        setPromotion(appleProduct, productDetail.getAppliedChanges());

        if(appleProduct.isAccessoryItem()){
            setVariationsInfo(appleProduct, productDetail.getVariations(), program, user, applyDiscount);
        }

        if (productDetail.isHasVariations() && CollectionUtils.isNotEmpty(appleProduct.getVariations()) &&
            appleProduct.getVariations().size() > 1) {
            appleProduct.setHasVariations(true);
        } else {
            appleProduct.setHasVariations(false);
        }

        setLearnMoreModal(appleProduct);

        return appleProduct;
    }

    private void setLearnMoreModal(final Product appleProduct) {
        if (MapUtils.isNotEmpty(appleProduct.getAdditionalInfo())
            && Objects.nonNull(appleProduct.getAdditionalInfo().get(CommonConstants.MANUFACTURE_NOTE))) {
            String learnMore = (String) appleProduct.getAdditionalInfo().get(CommonConstants.MANUFACTURE_NOTE);
            appleProduct.setLearnMore(learnMore.substring(1, learnMore.length() - 1));
        }
    }

    private void setAppleProductCategory(final com.b2s.service.product.common.domain.response.Product productDetail,
        final Locale locale, final Program program,
        final Product appleProduct, final List<Category> categoriesForDetail,
        final Category firstCategoryTemp) {
        if (CollectionUtils.isNotEmpty(categoriesForDetail)) {
            Category firstCategory = categoriesForDetail.get(0);
            categoriesForDetail.clear();
            categoriesForDetail.add(firstCategory);
            appleProduct.setCategory(categoriesForDetail);
        } else {
            logger.warn("No category mapped for productDetail [{}] , name {}",productDetail.getPsid(),productDetail.getName());
            if(CollectionUtils.isNotEmpty(productDetail.getCategories())){
                if(appleProduct.isAccessoryItem()){
                    if(Objects.nonNull(firstCategoryTemp)){
                        categoriesForDetail.add(setDefaultParentCategory(locale, firstCategoryTemp));
                        appleProduct.setCategory(categoriesForDetail);
                    }else{
                        logger.error("Category slug not set for the Accessory Item {} name {} ",
                                productDetail.getPsid(),productDetail.getName());
                    }
                } else if (ProductType.SERVICE_PLAN.equals(appleProduct.getProductType())) {
                    Category category = buildServicePlanCategory(productDetail);

                    categoriesForDetail.clear();
                    categoriesForDetail.add(category);
                    appleProduct.setCategory(categoriesForDetail);
                }
                else{
                    logger.error("Category is not mapped for Hero Product {} name {}", productDetail.getPsid(),productDetail.getName());
                }
            }else{
                logger.error("Category is empty in PS response for the product {} name {}",productDetail.getPsid(),productDetail.getName());
            }
        }
    }

    private Category buildServicePlanCategory(com.b2s.service.product.common.domain.response.Product productDetail) {
        com.b2s.service.product.common.domain.Category servicePlanCategory = productDetail.getCategories().get(0);
        Category category = new Category();
        category.setSlug(servicePlanCategory.getSlug());
        category.setName(servicePlanCategory.getName());
        category.setDepth(Integer.valueOf(servicePlanCategory.getDepth()));
        return category;
    }

    /**
     * Get Category specific to Locale, if not exist set default US locale
     *
     * @param locale
     * @param categorySlug
     * @return
     */
    private Category getCategory(final Locale locale, final String categorySlug) {
        Category category = categoryConfigurationService.getCategoryRepository(locale)
            .getCategoryDetailsByHierarchy(categorySlug);
        if (Objects.isNull(category)) {
            CategoryRepository categoryRepo = categoryConfigurationService.getCategoryRepository(Locale.US);
            if (Objects.nonNull(categoryRepo)) {
                category = categoryRepo.getCategoryDetailsByHierarchy(categorySlug);
            }
        }
        return category;
    }

    /**
     * Iterate all the categories and add only needed categories
     *
     * @param productDetail
     * @param locale
     * @param isForSearch
     * @return first Category
     */
    private Category getCategoryForDetail(final List<Category> categoriesForDetail,
        com.b2s.service.product.common.domain.response.Product productDetail, final Locale locale,
        final boolean isForSearch){

        Category firstCategory = null;
        for (com.b2s.service.product.common.domain.Category category : productDetail.getCategories()) {
            // set Category on parent level and not for Variations
            Category categoryTemp = getCategory(locale, category.getSlug());

            if (Objects.nonNull(categoryTemp)) {
                if (!AppleUtil.isAccessories(categoryTemp.getSlug())) {
                    firstCategory = categoryTemp;
                    break;
                } else if (Objects.isNull(firstCategory)) {
                    firstCategory = categoryTemp;
                }
            } else {
                logger.info("Category not found in Repository for slug {}", category.getSlug());
            }
        }

        //Remove parent's subcategory information for detail service
        if (Objects.nonNull(firstCategory) && CollectionUtils.isNotEmpty(firstCategory.getParents()) &&
            Objects.nonNull(firstCategory.getParents().get(0))) {
            if (!isForSearch) {
                //For Product Details, no need to show sub-categories for parent
                Category detailCategory = new Category();
                detailCategory = detailCategory.transformCloning(firstCategory, firstCategory.getParents());
                categoriesForDetail.add(detailCategory);
            }else {
                //For browse products
                categoriesForDetail.add(firstCategory);
            }
        }
        return firstCategory;
    }

    public Category setDefaultParentCategory(final Locale locale, final Category firstCategoryTemp){
        Category categoryParent = categoryConfigurationService.getCategoryRepository(locale)
            .getCategoryDetailsByHierarchy(
                CommonConstants.ALL_ACCESSORIES);
        if(Objects.isNull(categoryParent)){
            categoryParent=categoryConfigurationService.getCategoryRepository(locale)
                .getCategoryDetailsByHierarchy(
                    CommonConstants.ACC_ACCESSORIES);
        }

        final List<Category> parentCategories = new ArrayList<>();
        parentCategories.add(categoryParent);
        final Category detailCategory = new Category();
        return detailCategory.transformCloning(firstCategoryTemp, parentCategories);
    }

    private void setMerchantSpecificData(final Product appleProduct, ImmutableMap<String, JsonNode> merchantSpecificData){
        Map<String,JsonNode> convertedHashMap = new HashMap<>();
        for (Map.Entry<String,JsonNode> entry : merchantSpecificData.entrySet()){
            convertedHashMap.put(entry.getKey(),
                Objects.nonNull(entry.getValue())? entry.getValue():entry.getValue().deepCopy());
        }
        appleProduct.setMerchantSpecificData(convertedHashMap);
    }

    public void setShipmentInformation(final Product appleProduct, final String sku, final String slug,
        AvailabilityInformation availabilityInformation, final Locale locale, final String varId){
        final MessageSource messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean("messageSource"+varId);
        final Locale defaultLocale = Locale.forLanguageTag(CommonConstants.EN_US_TAG);
        String shippingAvailMessage = getShippingAvailMessage(sku, slug, locale, messageSource, defaultLocale);

        if(StringUtils.isEmpty(shippingAvailMessage) && availabilityInformation != null && StringUtils.isNotBlank(availabilityInformation.getAvailabilityMessage())) {
            logger.debug("Message category level not present for sku[{}] and category[{}]",sku, slug);
            final String shippingAvailMessageFromPS = availabilityInformation.getAvailabilityMessage();
                // extract key from message
                final java.util.Optional<String> shippingMessageKey = java.util.Optional.ofNullable(StringUtils.substringBetween(shippingAvailMessageFromPS, "[", "]"));
                if(shippingMessageKey.isPresent()) {
                    // if key is present, look up message for the key
                    shippingAvailMessage = messageSource.getMessage(shippingMessageKey.get(), null, null, locale);
                    if(StringUtils.isNotEmpty(shippingAvailMessage)) {
                        //Shipment Time frame mapping is stored in var_program_message table with 'en' as default.
                        //Hence setting English as default locale for retrieving shipmentTimeFrame
                        final String shipmentTimeFrame = messageSource.getMessage(CommonConstants
                            .SHIPMENT_KEY_FORMAT + shippingMessageKey
                            .get(), null, null, Locale.ENGLISH);
                        if(StringUtils.isNotEmpty(shipmentTimeFrame)) {
                            setShipmentQuoteDate(appleProduct, shipmentTimeFrame);
                        }
                        else {
                            shippingAvailMessage = getShippingAvailabilityMessage(shippingAvailMessageFromPS, appleProduct.getPsid(), locale);
                        }
                    }
                    else {
                        shippingAvailMessage = getShippingAvailabilityMessage(shippingAvailMessageFromPS, appleProduct.getPsid(), locale);
                    }

                } else {
                    // if key is no present, use message from PS
                    shippingAvailMessage = shippingAvailMessageFromPS;
                    logger.warn("Key is missing in shipping availability message, {}, for product_psid_{}, locale: {}",
                        shippingAvailMessageFromPS, appleProduct.getPsid(), locale);
                }
            appleProduct.setAvailable(AppleUtil.isAWPAvailableProduct(availabilityInformation));
        }
        if(StringUtils.isBlank(shippingAvailMessage)) {
            // still shipping availability message is null, use default shipping availability message
            shippingAvailMessage = messageSource.getMessage(CommonConstants.DEFAULT_SHIPPING_AVAILABILITY_KEY, null, null, locale);
        }
        appleProduct.setShippingAvailabilityMessage(shippingAvailMessage.trim());
    }

    private void setShipmentQuoteDate(final Product appleProduct, final String shipmentTimeFrame) {
        final String shipmentQuoteDate =
            shipmentQuoteUtil.getShipmentQuoteDate(shipmentTimeFrame, LocalDate.now());
        if (StringUtils.isNotBlank(shipmentQuoteDate)) {
            appleProduct.setShipmentQuoteDate(shipmentQuoteDate);
        }
    }

    private String getShippingAvailMessage(final String sku, final String slug, final Locale locale,
        final MessageSource messageSource, final Locale defaultLocale) {
        //For getting Message in SKU level, we have set to default en_US locale
        String shippingAvailMessage = messageSource.getMessage(sku, null, null, defaultLocale);
        if(StringUtils.isEmpty(shippingAvailMessage) && StringUtils.isNotEmpty(slug)) {
            logger.debug("Message sku level not present for sku[{}]",sku);
            shippingAvailMessage = messageSource.getMessage(slug, null, null, locale);
        }
        return shippingAvailMessage;
    }

    private String getShippingAvailabilityMessage(final String shippingAvailMessageFromPS, final String psid,
        final Locale locale) {
        // if no message exists for the key, use message from PS removing brackets portion
        String shippingAvailMessage = StringUtils.substringBefore(shippingAvailMessageFromPS, "[");
        logger.warn(
            "Key in shipping availability message, {}, for product_psid_{}, locale: {} not available in " +
                "platform database",
            shippingAvailMessage, psid, locale);
        return shippingAvailMessage;
    }

    public void setPromotion(final Product appleProduct, final ImmutableList<AppliedChange> appliedChanges){
        if(CollectionUtils.isNotEmpty(appliedChanges)) {
            final java.util.Optional<BigDecimal> discountPercentage = appliedChanges.stream()
                .filter(appliedChange -> PayloadType.PROMOTION.equals(appliedChange.getPayload().getPayloadType()))
                .filter(appliedChange -> appliedChange.getPayload().getDiscountPercentage().isPresent())
                .map(appliedChange -> appliedChange.getPayload().getDiscountPercentage().get())
                .findFirst();
            if(discountPercentage.isPresent()) {
                appleProduct.setPromotion(java.util.Optional.ofNullable(Promotion
                    .builder()
                    .withDiscountPercentage(discountPercentage.get())
                    .build()));
            }

            final java.util.Optional<Money> fixedPointValue = appliedChanges.stream()
                .filter(appliedChange -> PayloadType.PROMOTION.equals(appliedChange.getPayload().getPayloadType()))
                .filter(appliedChange -> appliedChange.getPayload().getFixedPointPrice().isPresent())
                .map(appliedChange -> appliedChange.getPayload().getFixedPointPrice().get().getValue())
                .findFirst();
            if(fixedPointValue.isPresent()) {
                appleProduct.setPromotion(java.util.Optional.ofNullable(Promotion
                    .builder()
                    .withFixedPointPrice(fixedPointValue.get())
                    .build()));
            }

            final java.util.Optional<BigDecimal> costPerPoint = appliedChanges.stream()
                .filter(appliedChange -> PayloadType.PROMOTION.equals(appliedChange.getPayload().getPayloadType()))
                .filter(appliedChange -> appliedChange.getPayload().getCostPerPoint().isPresent())
                .map(appliedChange -> appliedChange.getPayload().getCostPerPoint().get())
                .findFirst();
            if (costPerPoint.isPresent()) {
                appleProduct.setPromotion(java.util.Optional.ofNullable(Promotion
                    .builder()
                    .withCostPerPoint(costPerPoint.get())
                    .build()));
            }
        }
    }



    public Product from(final Variation variation,final Product standardProduct, final Program program, final User
        user, final boolean applyDiscount, final boolean isAccessories){
        Product appleProduct = new Product();
        appleProduct.setPsid(variation.getPsid());
        appleProduct.setSku(variation.getSku());
        appleProduct.setAppleSku(null);
        appleProduct.setName(variation.getName());
        appleProduct.setShortDescription(variation.getShortDescription().orElse(StringUtils.EMPTY));
        appleProduct.setLongDescription(variation.getLongDescription().orElse(StringUtils.EMPTY));
        appleProduct.setImages(this.imageObfuscatory.resizeImageUrls(ImageURLs.transformProductImages(variation.getImageUrls())));
        appleProduct.setOptions(optionMapper.getVariationsOptions(variation));
        appleProduct.setAccessoryItem(isAccessories);

        final String shipmentCategorySlug = CollectionUtils.isNotEmpty (standardProduct.getCategories())?
            standardProduct.getCategories().get(0).getSlug():StringUtils.EMPTY;
        setShipmentInformation(appleProduct, appleProduct.getSku(), shipmentCategorySlug, variation
            .getAvailabilityInformation(), user.getLocale(), program.getVarId());

        appleProduct.setOffers(
            List.of(getOffer(appleProduct, variation.getPricingInformation(), true, program, user, applyDiscount)));

        setPromotion(appleProduct, variation.getAppliedChanges());

        appleProduct.setBrand(variation.getBrand().orElse(StringUtils.EMPTY));
        appleProduct.setManufacturer(variation.getMerchant().orElse(StringUtils.EMPTY));

        final Map<String, Object> filteredAdditionalInfoForDisplay = new HashMap<>();
        if(Objects.nonNull(variation.getAdditionalInfo())){
            for (final Map.Entry<String, String> option : variation.getAdditionalInfo().entrySet()) {
                filteredAdditionalInfoForDisplay.put(option.getKey(),
                    Objects.nonNull(option.getValue())? option.getValue():StringUtils.EMPTY);
            }
        }
        appleProduct.setAdditionalInfo(filteredAdditionalInfoForDisplay);
        appleProduct.setMerchantSpecificData(standardProduct.getMerchantSpecificData());
        setLearnMoreModal(appleProduct);
        return appleProduct;
    }

    private void setVariationsInfo(final Product appleProduct, ImmutableList<Variation> variations, final Program program,
        final User user, final boolean applyDiscount){
        List<Product> variationsProduct = new ArrayList<>();
        List<com.b2s.rewards.apple.model.Option> variationOptions = new ArrayList<>();
        if(Objects.nonNull(variations)){
            for(Variation variation:variations){
                variationOptions.addAll(optionMapper.getVariationsOptions(variation));
                variationsProduct.add(from(variation, appleProduct, program, user, applyDiscount, appleProduct.isAccessoryItem()));
            }
        }
        appleProduct.setVariations(variationsProduct);

        final Map<String, Set<com.b2s.rewards.apple.model.Option>> mapNameOptions = new LinkedHashMap<>();
        appleProduct.setOptionsConfigurationData(getMapOptions(mapNameOptions, variationOptions));
    }

    private Offer getOffer(final Product appleProduct,
                                 final PricingInformation pricingInformation,
                                 boolean freeShipping,
                                 final Program program,
                                 final User user,
                                 final boolean applyDiscount) {
        final CalculatedPriceInfo pntPriceInfo = pricingInformation.getFirstCalculatedPriceInfo(CurrencyUnit.of("PNT"));
        final CalculatedPriceInfo currencyPriceInfo = pricingInformation.getFirstCalculatedPriceInfo(program.getTargetCurrency());
        final OriginalPriceInfo originalPriceInfo = pricingInformation.getOriginalPriceInfo();
        final CalculatedPriceInfo calculatedPriceInfo = pricingInformation.getFirstCalculatedPriceInfo(program.getTargetCurrency());
        final java.util.Optional<DiscountPriceInfo> discountPriceInfo = pricingInformation.getDiscountPriceInfo();

        final Offer offer = new Offer();
        offer.setAppleSku(appleProduct.getAppleSku());
        Optional.ofNullable(originalPriceInfo).ifPresent(opi ->
            Optional.ofNullable(opi.getBasePrice()).ifPresent(basePrice ->
                offer.setOrgItemPrice(basePrice.getAmount().doubleValue())
            ));

        Optional<SplitTenderEstimate> splitTenderEstimateOpt = pntPriceInfo.getSplitTenderEstimate();
        if (splitTenderEstimateOpt.isPresent()) {
            SplitTenderEstimate splitTenderEstimate = splitTenderEstimateOpt.get();

            SmartPrice smartPrice = new SmartPrice();
            smartPrice.setAmount(splitTenderEstimate.getCcBuyInAmountLimit().getAmount().doubleValue());
            smartPrice.setPoints(splitTenderEstimate.getRemainingPointPrice());
            smartPrice.setCurrencyCode(splitTenderEstimate.getCcBuyInAmountLimit().getCurrencyUnit().getCode());
            smartPrice.setIsCashMaxLimitReached(isCashMaxLimitReached(program.getRedemptionOptions(),
                            splitTenderEstimate.getCcBuyInAmountLimit().getAmount().intValue()));
            appleProduct.setSmartPrice(smartPrice);
        }

        Optional.ofNullable(pntPriceInfo.getBasePrice()).ifPresent(pntBasePrice -> {
                final Price price = Optional.ofNullable(currencyPriceInfo.getBasePrice())
                    .map(currencyBasePrice -> new Price(currencyBasePrice, pntBasePrice.getAmount().intValue()))
                    .orElse(new Price(0d, CommonConstants.POINT_CURRENCY_STRING, pntBasePrice.getAmount().intValue()));
                offer.setBasePrice(price);
            }
        );

        Optional.ofNullable(originalPriceInfo).ifPresent(opi -> offer.setSupplierSalesTax(opi.getSupplierSalesTax()));

        discountPriceInfo.ifPresent(dpi -> {
            offer.setDiscountPrice(dpi.getBasePrice());
            offer.setDiscountSupplierSalesTax(dpi.getSupplierSalesTax().get());
        });

        //changes
        if (pntPriceInfo.getDisplayPrice()!=null) {
            if (currencyPriceInfo.getDisplayPrice() != null) {
                offer.setTotalPrice(new Price(currencyPriceInfo.getDisplayPrice(),
                    pntPriceInfo.getDisplayPrice().getAmount().intValue()));
                offer.setUnitTotalPrice(
                    new Price(currencyPriceInfo.getDisplayPrice(), pntPriceInfo.getDisplayPrice().getAmount()
                        .intValue()));
                offer.setB2sItemPrice(new Price(currencyPriceInfo.getDisplayPrice(),
                    pntPriceInfo.getDisplayPrice().getAmount().intValue()));
            } else {
                offer.setTotalPrice(new Price(0d, CommonConstants.POINT_CURRENCY_STRING,
                        pntPriceInfo.getDisplayPrice().getAmount().intValue()));
                offer.setUnitTotalPrice(new Price(0d, CommonConstants.POINT_CURRENCY_STRING,
                        pntPriceInfo.getDisplayPrice().getAmount().intValue()));
                offer.setB2sItemPrice(new Price(0d, CommonConstants.POINT_CURRENCY_STRING,
                        pntPriceInfo.getDisplayPrice().getAmount().intValue()));
            }
        }

        Optional.ofNullable(pntPriceInfo.getUnpromotedDisplayPrice()).ifPresent(pntUnpromotedDisplayPrice -> {
            final Price price =
                Optional.ofNullable(currencyPriceInfo.getUnpromotedDisplayPrice()).map(currencyUnpromotedDisplayPrice ->
                    new Price(currencyUnpromotedDisplayPrice.get(),
                        pntUnpromotedDisplayPrice.get().getAmount().intValue())
                ).orElse(new Price(0d, CommonConstants.POINT_CURRENCY_STRING,
                    pntUnpromotedDisplayPrice.get().getAmount().intValue()));
            offer.setUnpromotedDisplayPrice(price);
        });

        Optional.ofNullable(pntPriceInfo.getShippingCost()).ifPresent(pntShoppingCost -> offer.setB2sShippingPrice(
            new Price(0d, CommonConstants.POINT_CURRENCY_STRING, pntShoppingCost.getAmount().intValue())));

        pntPriceInfo.getMsrp().ifPresent(pntMsrp -> offer.setMsrpPrice(pntMsrp.getAmount().doubleValue()));

        offer.setOrgSupplierTaxPrice(pntPriceInfo.getSupplierSalesTax().getAmount().doubleValue());

        //Setting Estimated Earn Points from PS
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);
        final Integer estimatedEarnedPoint = pntPriceInfo.getEstimatedEarnedPoints().orElse(null);
        if (showEarnPoints && Objects.nonNull(estimatedEarnedPoint)) {
            offer.setEarnPoints(estimatedEarnedPoint.intValue());
        }
        // Now apply discount codes, if any
        if(applyDiscount) {
            offer.setB2sItemPrice(getDiscountedPrice(offer.getB2sItemPrice(), user.getDiscounts(), java.util.Optional.of(offer)));
            offer.setTotalPrice(getDiscountedPrice(offer.getTotalPrice(), user.getDiscounts(), java.util.Optional.empty()));
            offer.setUnitTotalPrice(getDiscountedPrice(offer.getTotalPrice(), user.getDiscounts(), java.util.Optional.empty()));

        }

        applyPayrollDeductionData(program, user, offer);


        if (!freeShipping && Optional.ofNullable(pntPriceInfo.getShippingCost()).isPresent()) {
            offer.setOrgShippingPrice(pntPriceInfo.getShippingCost().getAmount().doubleValue());
        }
        offer.setSku(appleProduct.getSku());
        calculateDisplayPrice(appleProduct, program, offer);
        return offer;
    }

    private void calculateDisplayPrice(final Product appleProduct, final Program program, final Offer offer) {
        // Add display price
        offer.setDisplayPrice(offer.getB2sItemPrice());
        if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())) {
            offer.setDisplayPrice(offer.getTotalPrice());
        }
        if(CollectionUtils.isNotEmpty(program.getPricingModels())) {
            final Optional<PricingModel> pricingModelOptional = PricingUtil.getPricingModel(appleProduct, program.getPricingModels());
            if(pricingModelOptional.isPresent()) {
                offer.setDisplayPrice(new Price(pricingModelOptional.get().getPaymentValue(),program.getTargetCurrency().getCode(), 0));
            }
        }
    }

    private void applyPayrollDeductionData(final Program program, final User user, final Offer offer) {
        // Now add payroll reduction related calculation and values
        if (offer.getTotalPrice() != null) {
            if (MapUtils.isNotEmpty(program.getConfig()) &&
                (Objects.nonNull((program.getConfig().get(CommonConstants.INSTALLMENT))) ||
                    (Objects.nonNull(program.getConfig().get(CommonConstants.EPP)) &&
                        Boolean.TRUE.equals(program.getConfig().get(CommonConstants.EPP))))) {
                boolean applyMinLimit = false;
                // set the is eligibile for payroll deduction flag based on program config
                offer.setIsEligibleForPayrollDeduction(user.getIsEligibleForPayrollDeduction());
                if(Objects.nonNull(program.getConfig().get(CommonConstants.SINGLE_ITEM_PURCHASE))) {
                    applyMinLimit = (Boolean) program.getConfig().get(CommonConstants.SINGLE_ITEM_PURCHASE);
                }
                applyIsEligibleForPayrollDeduction(program, user, offer, applyMinLimit);
                applyPayPeriodData(program, offer);
            } else {
                offer.setIsEligibleForPayrollDeduction(false);
            }
        }
    }

    private void applyPayPeriodData(final Program program, final Offer offer) {
        //set payPerPeriod for EPP
        if (Objects.nonNull(program.getConfig().get(CommonConstants.PAY_PERIODS))) {
            final int payPeriods =  Integer.parseInt(program.getConfig().get(CommonConstants.PAY_PERIODS).toString());
            final Double payPerPeriod;
            if (payPeriods > 0) {
                payPerPeriod = getPayPerPeriodPrice(program, offer.getTotalPrice().getAmount(), payPeriods);
                offer.setPayPerPeriodPrice(payPerPeriod);
                offer.setPayPeriods(payPeriods);
                if(Objects.nonNull(program.getConfig().get(CommonConstants.PAY_DURATION))){
                    offer.setPayDuration(program.getConfig().get(CommonConstants.PAY_DURATION).toString());
                }
            }
        }
    }

    private void applyIsEligibleForPayrollDeduction(final Program program, final User user, final Offer offer,
        final boolean applyMinLimit) {
        if(applyMinLimit) {
            // set the is eligibile for payroll deduction flag based on price and payment option min limit
            boolean isEligibleForPayrollDeduction = PricingUtil
                .isEligibleForPayrollDeduction(user, program, offer.getTotalPrice());
            if(isEligibleForPayrollDeduction) {
                offer.setIsEligibleForPayrollDeduction(true);
            } else {
                offer.setIsEligibleForPayrollDeduction(false);
            }
        }
    }

    public static Double getPayPerPeriodPrice(Program program, Double price, int payPeriods) {
        Double payPerPeriod = null;
        if(program != null && price != null) {
            final boolean showExactPayPerPeriod = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EXACT_PAY_PER_PERIOD, Boolean.FALSE);
            if (showExactPayPerPeriod) {
                // This will show exact payPerPeriod including decimal places
                payPerPeriod = new BigDecimal(price)
                        .divide(new BigDecimal(payPeriods), CommonConstants.TWO, RoundingMode.CEILING)
                        .doubleValue();
            } else {
                // This will round payPerPeriod to dollars
                payPerPeriod = new BigDecimal(price)
                        .divide(new BigDecimal(payPeriods), CommonConstants.ZERO, RoundingMode.CEILING)
                        .doubleValue();
            }
        }
        return payPerPeriod;
    }


    private Price getDiscountedPrice(Price price, List<DiscountCode> discounts, final java.util.Optional<Offer> optionalOffer) {
        Price newPrice = price;
        if(CollectionUtils.isNotEmpty(discounts)) {
            for(DiscountCode discountCode : discounts) {
                newPrice = getDiscountedPrice(newPrice, discountCode, optionalOffer);
            }
        }
        return newPrice;
    }

    private Price getDiscountedPrice(Price newPrice, DiscountCode discountCode, final java.util.Optional<Offer> optionalOffer) {
        if(discountCode != null && discountCode.getDiscountType().equals("dollar")) {
            Double discountedAmount = new BigDecimal(newPrice.getAmount()).subtract(new BigDecimal(discountCode.getDiscountAmount())).setScale(2, RoundingMode.HALF_UP).doubleValue();
            int discountedPoints = new BigDecimal(newPrice.getPoints()).subtract(new BigDecimal(discountCode.getDiscountAmount())).intValue();
            if(optionalOffer.isPresent()){

                final Offer offer = optionalOffer.get();
                final double discountAmount = offer.getDiscountApplied() + discountCode.getDiscountAmount();
                offer.setDiscountApplied(discountAmount);
            }
            if(discountedAmount < 0) {
                discountedAmount = 0d;
                discountedPoints = 0;
            }
            newPrice = new Price(discountedAmount, "", discountedPoints);
        }
        return newPrice;
    }

    public ProductSearchDocument variation2ProductSearchDocument(ProductSearchDocument productSearchDocument,
        VariationSearchDocument variationSearchDocument) {
        //update option value name
        ImmutableMap<String, Option> options = variationSearchDocument.getOptions();
        Map<String, com.b2s.service.product.common.domain.response.Option> newOptions = new HashedMap();

        for (Map.Entry<String, com.b2s.service.product.common.domain.response.Option> entry : options.entrySet()) {
            com.b2s.service.product.common.domain.response.Option option = entry.getValue();
            if (CommonConstants.GIFTCARDS_PHYSICAL.equals(option.getValue())) {
                option = com.b2s.service.product.common.domain.response.Option.builder()
                    .withValue(CommonConstants.GIFTCARDS_SEND_BY_MAIL)
                    .withLabel(option.getLabel())
                    .build();
            } else if (CommonConstants.GIFTCARDS_ECARD.equals(option.getValue())) {
                option = com.b2s.service.product.common.domain.response.Option.builder()
                    .withValue(CommonConstants.GIFTCARDS_SEND_BY_EMAIL)
                    .withLabel(option.getLabel())
                    .build();
            }
            newOptions.put(entry.getKey(), option);
        }

        //update giftcards name
        StringBuffer giftCardName = new StringBuffer();
        if (newOptions.get(CommonConstants.GIFTCARDS_DENOMINATION) != null &&
            newOptions.get(CommonConstants.GIFTCARDS_DENOMINATION).getValue() != null) {
            giftCardName.append(newOptions.get(CommonConstants.GIFTCARDS_DENOMINATION).getValue()).append(" ");
        }
        giftCardName.append(variationSearchDocument.getName());
        if (newOptions.get(CommonConstants.GIFTCARDS_DELIVERYMETHOD) != null &&
            newOptions.get(CommonConstants.GIFTCARDS_DELIVERYMETHOD).getValue() != null &&
            newOptions.get(CommonConstants.GIFTCARDS_DELIVERYMETHOD).getValue().length() > 3) {
            giftCardName.append(newOptions.get(CommonConstants.GIFTCARDS_DELIVERYMETHOD).getValue().substring(4));
        }

        return ProductSearchDocument
            .builder()
            .withName(giftCardName.toString())
            .withPsid(variationSearchDocument.getPsid())
            .withBsin(variationSearchDocument.getBsin())
            .withSku(variationSearchDocument.getSku())
            .withShortDescription(variationSearchDocument.getShortDescription().orElse(null))
            .withProductImageUrls(variationSearchDocument.getImageUrls())
            .withAppliedChanges(variationSearchDocument.getAppliedChanges())
            .withOptions(newOptions)
            .withPurchasableByAgentsOnly(variationSearchDocument.isPurchasableByAgentsOnly())
            .withShippingInformation(variationSearchDocument.getShippingInformation())
            .withPricingInformation(variationSearchDocument.getPricingInformation())
            .withAvailabilityInformation(variationSearchDocument.getAvailabilityInformation())
            .withSourceCountry(productSearchDocument.getSourceCountry())
            .withMerchant(productSearchDocument.getMerchant())
            .withProductType(productSearchDocument.getProductType())
            .withCategorySlugs(productSearchDocument.getCategorySlugs())
            .withHasVariations(productSearchDocument.hasVariations())
            .withHasOffer(productSearchDocument.hasOffer())
            .withOfferTypes(productSearchDocument.getOfferTypes())
            .withUpc(productSearchDocument.getUpc().orElse(""))
            .withGroupId(productSearchDocument.getGroupId().orElse(""))
            .withGroupName(productSearchDocument.getGroupName().orElse(""))
            .withBrand(productSearchDocument.getBrand().orElse(""))
            .withModel(productSearchDocument.getModel().orElse(""))
            .withManufacturer(productSearchDocument.getManufacturer().orElse(""))
            .withGiftCardInfo(productSearchDocument.getGiftCardInfo().orElse(null))
            .withGiftCardType(productSearchDocument.getGiftCardType().orElse(""))
            .withVendor(productSearchDocument.getVendor().orElse(""))
            .withAverageRating(productSearchDocument.getAverageRating().orElse(0f))
            .withTotalReviews(productSearchDocument.getTotalReviews())
            .withWebRedeemable(productSearchDocument.getWebRedeemable().orElse(false))
            .withMaxPurchaseQuantity(productSearchDocument.getMaxPurchaseQuantity().orElse(0))
            .withDebugInfo(productSearchDocument.getDebugInfo().orElse(null))
            .withVariations(productSearchDocument.getVariations())
            .build();
    }

    public String productName2CartNameForGiftCard(final Product newProduct) {
        final StringBuilder productNameSb = new StringBuilder();
        if (CommonConstants.GIFTCARDS_APPLEMUSIC.equalsIgnoreCase(newProduct.getBrand()) ||
            CommonConstants.GIFTCARDS_ITUNES.equalsIgnoreCase(newProduct.getBrand())) {
            if (newProduct.getVariationDimensionNameValues() != null &&
                newProduct.getVariationDimensionNameValues().get(CommonConstants.GIFTCARDS_DENOMINATION) != null) {
                productNameSb
                    .append(newProduct.getVariationDimensionNameValues().get(CommonConstants.GIFTCARDS_DENOMINATION));
            }
            productNameSb.append(" ")
                .append(newProduct.getName())
                .append(CommonConstants.GIFTCARDS_PHYSICAL.equalsIgnoreCase(newProduct.getFormat()) ?
                    CommonConstants.GIFTCARDS_BY_MAIL : CommonConstants.GIFTCARDS_BY_EMAIL);
        } else {
            productNameSb.append(newProduct.getName());
        }
        return productNameSb.toString();
    }

    public void productName2CartName(final Product detailProduct) {
        final List<Category> categories = detailProduct.getCategories();
        final String categorySlug = getCategorySlug(categories);
        final String productName = detailProduct.getName();
        if (StringUtils.isNotEmpty(categorySlug)
            && isValidCategorySlug(categorySlug, productName)) {
            final StringBuilder productNameDetailSb = new StringBuilder();

            final List<com.b2s.rewards.apple.model.Option> options = detailProduct.getOptions();
            final String denomination =
                options
                    .stream()
                    .filter(option -> CommonConstants.GIFTCARDS_DENOMINATION.equals(option.getName()))
                    .findFirst()
                    .orElse(new com.b2s.rewards.apple.model.Option("")).getKey();
            final String deliveryMethod =
                options
                    .stream()
                    .filter(option -> CommonConstants.GIFTCARDS_DELIVERYMETHOD.equals(option.getName()))
                    .findFirst()
                    .orElse(new com.b2s.rewards.apple.model.Option("")).getKey();
            productNameDetailSb.append(denomination)
                .append(" ")
                .append(detailProduct.getName())
                .append(CommonConstants.GIFTCARDS_PHYSICAL.equalsIgnoreCase(deliveryMethod) ?
                    CommonConstants.GIFTCARDS_BY_MAIL : CommonConstants.GIFTCARDS_BY_EMAIL);
            detailProduct.setName(productNameDetailSb.toString());
        }
    }

    private boolean isValidCategorySlug(final String categorySlug, final String productName) {
        return (categorySlug.contains(CommonConstants.SUPPLIER_TYPE_GIFTCARD_STR.toLowerCase())
            || categorySlug.contains(CommonConstants.SUPPLIER_TYPE_GIFT_CARDS_STR.toLowerCase()))
            && !productName.contains(CommonConstants.GIFTCARDS_BY_EMAIL)
            && !productName.contains(CommonConstants.GIFTCARDS_BY_MAIL);
    }

    private String getCategorySlug(final List<Category> categories) {
        if (CollectionUtils.isNotEmpty(categories)) {
            final Category category = categories.get(0);
            if (category != null) {
                return category.getSlug();
            }
        }

        return "";
    }

    private Map<String, Set<com.b2s.rewards.apple.model.Option>> getOptionsConfigurationData(
                                             final ImmutableSet<VariationSearchDocument> variationSearchDocuments,
                                             final List<String> productTilesOptions) {
        final Map<String, Set<com.b2s.rewards.apple.model.Option>> mapNameOptions = new LinkedHashMap<>();
        variationSearchDocuments.forEach(variationSearchDocument -> getMapOptions(mapNameOptions, getVariationsOptions(variationSearchDocument, productTilesOptions)));
        return mapNameOptions;
    }

    private Map<String, Set<com.b2s.rewards.apple.model.Option>> getMapOptions(
            final Map<String, Set<com.b2s.rewards.apple.model.Option>> mapNameOptions,
            List<com.b2s.rewards.apple.model.Option> variations) {
        for (com.b2s.rewards.apple.model.Option option : variations) {
            if (mapNameOptions.containsKey(option.getName())) {
                mapNameOptions.get(option.getName()).add(option);
            } else {
                final Set<com.b2s.rewards.apple.model.Option> optionsList = new TreeSet<>();
                optionsList.add(option);
                mapNameOptions.put(option.getName(), optionsList);
            }
        }
        return mapNameOptions;
    }

    private List<com.b2s.rewards.apple.model.Option> getVariationsOptions(final VariationSearchDocument variation,
                                                                          final List<String> productTilesOptions){
        List<com.b2s.rewards.apple.model.Option> variationOptions = new ArrayList<>();
        for(Map.Entry<String, com.b2s.service.product.common.domain.response.Option> mapOption:
                variation.getOptions().entrySet()){
            if(productTilesOptions.contains(mapOption.getKey())) {
                com.b2s.service.product.common.domain.response.Option option = mapOption.getValue();
                //add to Option list
                optionMapper.addOption(variationOptions, mapOption.getKey(), variation.getOptions(), option.getKey(),
                        option.getValue(), option.getLabel(), variation.getPsid());
            }
        }
        return variationOptions;
    }

    /**
     * Set Apple Product attributes based on PS response
     *
     * @param appleProduct
     * @param productDetail
     */
    private void setAttributesBasedOnProductDetail(final Product appleProduct,
        com.b2s.service.product.common.domain.response.Product productDetail) {
        final AvailabilityInformation availabilityInformation = productDetail.getAvailabilityInformation();
        appleProduct.setAvailable(availabilityInformation.isAvailable());

        productDetail.getGiftCardInfo()
            .ifPresent(giftCardInfo -> appleProduct.setFormat(giftCardInfo.getGiftCardType().name()));

        if (productDetail.getProductType().equals(ProductType.GIFT_CARD)) {
            productDetail.getGroupId().ifPresent(appleProduct::setParentProductId);
        }
        if (productDetail.isHasVariations() && MapUtils.isNotEmpty(productDetail.getOptions())) {
            appleProduct.setParentProductId(productDetail.getPsid());
        } else {
            productDetail.getGroupId().ifPresent(appleProduct::setParentProductId);
        }

        appleProduct.setProductType(productDetail.getProductType());
        populateSupplierCategory(productDetail, appleProduct);
        populateProductImageUrls(appleProduct, productDetail.getImageUrls());
    }

    /**
     * Set Merchant Specific Information
     *
     * @param offer
     * @param productDetail
     */
    private void setMerchant(final Offer offer,
                             com.b2s.service.product.common.domain.response.Product productDetail) {

        productDetail.getMerchant().ifPresent(merchant -> {
            final MerchantEntity merchantEntity =
                    merchantRepositoryHolder.getMerchantRepository().get(merchant).get(0);
            final com.b2s.rewards.model.Merchant productMerchant = new com.b2s.rewards.model.Merchant();

            if (Optional.ofNullable(merchantEntity).isPresent()) {
                final String productMerchantCode = String.valueOf(merchantEntity.getMerchantId());
                final String convertedMerchantCode =
                        Optional.ofNullable(legacyMerchantCodeMappings.get(productMerchantCode)).isPresent() ?
                                legacyMerchantCodeMappings.get(productMerchantCode) : productMerchantCode;
                productMerchant.setMerchantId(convertedMerchantCode);
                productMerchant.setName(merchantEntity.getSimpleName());
            }

            offer.setMerchant(productMerchant);
        });
    }

    /**
     * Populate Supplier Category
     *
     * @param productDetail
     * @param appleProduct
     */
    private void populateSupplierCategory(final com.b2s.service.product.common.domain.response.Product productDetail,
        final Product appleProduct){
        final Supplier supplier = new Supplier();
        final String supplierId = this.supplierProductMapping.get(productDetail.getProductType().name());
        supplier.setSupplierId(Integer.parseInt(supplierId));
        appleProduct.setSupplier(supplier);
    }

    /**
     * Populate Product Image Urls
     *
     * @param appleProduct
     * @param productImageUrls
     */
    private void populateProductImageUrls(final Product appleProduct, final ProductImageUrls productImageUrls) {
        if (Optional.ofNullable(productImageUrls).isPresent()) {
            final ProductImage productImage = new ProductImage();
            productImageUrls.getThumbnail().ifPresent(productImage::setThumbnailImageURL);
            productImageUrls.getSmall().ifPresent(productImage::setSmallImageURL);
            productImageUrls.getMedium().ifPresent(productImage::setMediumImageURL);
            productImageUrls.getLarge().ifPresent(productImage::setLargeImageURL);
            this.imageObfuscatory.resizeImageUrls(productImage);
            appleProduct.setDefaultProductImage(productImage);
        }
    }

    /**
     * Get Smart Price details
     *
     * @param redemptionPaymentLimit
     * @param program
     * @return Smart Price
     */
    public SmartPrice getSmartPrice(RedemptionPaymentLimit redemptionPaymentLimit, Program program){
        final SmartPrice smartPrice = new SmartPrice();
        smartPrice.setPoints(redemptionPaymentLimit.getUseMinPoints().getPoints());
        smartPrice.setCurrencyCode(redemptionPaymentLimit.getUseMinPoints().getCurrencyCode());
        smartPrice.setAmount(redemptionPaymentLimit.getUseMinPoints().getAmount());
        smartPrice.setIsCashMaxLimitReached(isCashMaxLimitReached(program.getRedemptionOptions(),
                redemptionPaymentLimit.getUseMinPoints().getAmount().intValue()));
        return smartPrice;
    }

    /**
     * If Max cash configured in dollar level and cart/product reaches the limit, returns True
     *
     * @param redemptionOptions - var program redemption option configuration
     * @param splitTenderAmount - amount expected to be paid
     * @return true if cash max limit reached else false
     */
    private boolean isCashMaxLimitReached(Map<String, List<VarProgramRedemptionOption>> redemptionOptions, int splitTenderAmount) {
        Optional<VarProgramRedemptionOption> varProgramRedemptionOption = Optional.empty();
        if (MapUtils.isNotEmpty(redemptionOptions)) {
            for (final Map.Entry<String, List<VarProgramRedemptionOption>> options : redemptionOptions.entrySet()) {
                if (options.getKey().equalsIgnoreCase(PaymentOptions.SPLITPAY.getPaymentOption())) {
                    varProgramRedemptionOption = options.getValue().stream()
                            .filter(redemptionOption ->
                                    redemptionOption.getLimitType().equalsIgnoreCase(CommonConstants.DOLLAR) &&
                                            redemptionOption.getPaymentMaxLimit() > 0 &&
                                            redemptionOption.getPaymentMaxLimit()
                                                    .compareTo(splitTenderAmount) == 0)
                            .findAny();
                }
            }
        }
        return varProgramRedemptionOption.isPresent();
    }
}