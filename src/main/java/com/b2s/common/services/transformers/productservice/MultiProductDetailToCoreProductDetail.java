package com.b2s.common.services.transformers.productservice;


import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.apple.entity.MerchantEntity;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.responses.productservice.CoreProductDetailResponse;
import com.b2s.common.services.transformers.Helper;
import com.b2s.common.services.transformers.Transformer;
import com.b2s.common.services.util.ImageObfuscatory;
import com.b2s.common.services.util.MerchantRepositoryHolder;
import com.b2s.rewards.model.*;
import com.b2s.rewards.model.Product;
import com.b2s.service.product.client.application.detail.MultiProductDetailResponse;
import com.b2s.service.product.common.domain.AvailabilityInformation;
import com.b2s.service.product.common.domain.Category;
import com.b2s.service.product.common.domain.OfferType;
import com.b2s.service.product.common.domain.ProductType;
import com.b2s.service.product.common.domain.response.*;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
/**
 * <p>
 * Used to transform multi product detail response from product service to core product model.
 @author sjonnalagadda
  * Date: 7/29/13
  * Time: 01:09 PM
 *
 */
@SuppressWarnings("all")
//TODO - remove the supresswarnings ALL and replace it with Avoid too complex class
public class MultiProductDetailToCoreProductDetail implements Transformer<MultiProductDetailResponse, CoreProductDetailResponse> {
//PLAN TO STOP REFERRING THIS CLASS

    private final Map<String, String> supplierProductMapping;
    private final Map<String, String> legacyMerchantMapper;
    private final ImageObfuscatory imageObfuscatory;

    @Autowired
    private MerchantRepositoryHolder merchantRepositoryHolder;

    private static final Logger logger = LoggerFactory.getLogger(MultiProductDetailToCoreProductDetail.class);

    /**
     * Constructs MultiProductDetailToCoreProductDetail transformer  from supplier product mapping, supplier pricing category
     * code,image obfuscatory and, legacy merchant mapper.
     *
     * @param supplierProductMapping holds mapping from supplier code (Example:"200") to product type (Example:"MERCHANDISE") and vice-versa.
     * @param imageObfuscatory used for obfuscating image URLs.
     * @throws IllegalArgumentException when any of the inputs is absent.
     */

    public  MultiProductDetailToCoreProductDetail(final Map<String, String> supplierProductMapping,
                                                  final ImageObfuscatory imageObfuscatory,
                                                  final Map<String, String> legacyMerchantMapper){
        validateInput(supplierProductMapping, ServiceExceptionEnums.SUPPLIER_PRODUCT_MAPPING_ABSENT);
        validateInput(imageObfuscatory, ServiceExceptionEnums.IMAGE_OBFUSCATORY_ABSENT);
        validateInput(legacyMerchantMapper, ServiceExceptionEnums.LEGACY_MERCHANT_MAPPER_ABSENT);
        this.supplierProductMapping = Collections.unmodifiableMap(supplierProductMapping);
        this.imageObfuscatory = imageObfuscatory;
        this.legacyMerchantMapper =  legacyMerchantMapper;
    }

    /**
     * Validate the inputs is present (or) not and throws exception with error message from <code>ServiceExceptionEnums</code>
     * @param input object being tested for existence.
     * @param serviceExceptionEnums enumerated service exception detail
     * @throws IllegalArgumentException when object is not present
     */

    private void validateInput(final Object input, final ServiceExceptionEnums serviceExceptionEnums){
        if(!Optional.ofNullable(input).isPresent()){
            throw new IllegalArgumentException(serviceExceptionEnums.getErrorMessage());
        }
    }

    @Override
    public CoreProductDetailResponse transform(final MultiProductDetailResponse from, final Helper helper, final com.b2s.rewards.apple.model.Program program) {

        validateInput(from, ServiceExceptionEnums.PRODUCT_DETAIL_RESPONSE_ABSENT);
        validateInput(helper, ServiceExceptionEnums.TRANSFORMER_HELPER_CAN_NOT_BE_NULL);


        Set<String> psidsFromDetailRequests = null;
        if(helper instanceof DetailResponseTransformerHelper){
            final DetailResponseTransformerHelper detailResponseTransformerHelper = (DetailResponseTransformerHelper)helper;
            psidsFromDetailRequests =  detailResponseTransformerHelper.getPsidsFromDetailRequests();
        }
        final CoreProductDetailResponse coreProductDetailResponse = new  CoreProductDetailResponse();
        if(Optional.ofNullable(from.getProducts()).isPresent() && !from.getProducts().isEmpty()){
            for(final com.b2s.service.product.common.domain.response.Product serviceProduct:from.getProducts()){
                final Product coreProduct = getProductDetails(serviceProduct);
                if(coreProduct.getHasVariations() && Optional.ofNullable(serviceProduct.getVariations()).isPresent()
                        && !serviceProduct.getVariations().isEmpty()) {
                    associateMainDocumentDetailsWithPsidFromRequest(serviceProduct.getVariations(),
                            psidsFromDetailRequests,coreProductDetailResponse,coreProduct);
                    final List<Variation> filteredVariations = new LinkedList<Variation>();
                    coreProduct.setVariations(getProductVariations(serviceProduct));
                }else{
                    // When  no product variations then there is nothing to compare with to adjust. It is one-one mapping.
                    // If detail request is for P1 as PSID then the main document PSID will be P1, when there are no variations.
                    coreProductDetailResponse.withProductDetailResponse(serviceProduct.getPsid(),coreProduct);
                }
            }
        }
        else   if(Optional.ofNullable(from.getFailures()).isPresent() && !from.getFailures().isEmpty()) {
            coreProductDetailResponse.setFailures(from.getFailures());
        }

        return coreProductDetailResponse;
    }

    /**
     * Let's say customer searched with PSID P1 and the response has a main document with PSID P2 and variants as P1, P2, P3 and P4
     * P1(searched with P1)-----
     * Response document as P2
     *                       |-----P2
     *                       |-----P1
     *                       |-----P3
     *                       |-----P4
     *
     * Then we  need to associate P1 from request to P2 in response. So, the clients does not need to explore in multiple
     * places.
     *
     */
    private void associateMainDocumentDetailsWithPsidFromRequest(final ImmutableList<Variation> productVariants, final Set<String> psidsFromRequest,
                                                                 final CoreProductDetailResponse coreProductDetailResponse, final Product mainDocument){
        for(final Variation variantProduct: productVariants){
            if(psidsFromRequest.contains(variantProduct.getPsid())
                    && !coreProductDetailResponse.getProductDetailByPsid(variantProduct.getPsid()).isPresent()){
                coreProductDetailResponse.withProductDetailResponse(variantProduct.getPsid(),mainDocument);
            }
        }
    }

    /**
     * Let's say customer searched with PSID P1 and the response has a main document with PSID P2 and variants as P1, P2, P3 and P4
     * P1(searched with P1)-----
     * Response document as P2
     *                       |-----P2
     *                       |-----P1
     *                       |-----P3
     *                       |-----P4
     * The response always has P2 as repeating structure as main document. Skipping P2 for variations logic will eliminate duplication.
     *
     */

    private void createFilteredVariations(final List<Variation> filteredVariations, final ImmutableList<Variation> productVariants, final String mainDocumentPsid){
        for(final Variation variantProduct: productVariants){
            //TODO: Find another way to not filter the variations
            //if(!variantProduct.getPsid().equals(mainDocumentPsid)){
            filteredVariations.add(variantProduct);
            //}
        }
    }

    private List<Product> getProductVariations(final com.b2s.service.product.common.domain.response.Product product) {
        final List<Variation> variantProducts = product.getVariations();
        final List<String> parentProductFeatures = product.getFeatures();
        final MerchantEntity
                merchant = merchantRepositoryHolder.getMerchantRepository().get(product.getMerchant().get()).get(0);
        final boolean shippingEligible = product.isShippingEligible();
        final boolean inStoreEligible = product.isInStoreEligible();
        final boolean reserveAndPickupEligible = product.isReserveAndPickupEligible();

        final List<Product> variantCoreProductList = new LinkedList<Product>();
        if(Optional.ofNullable(variantProducts).isPresent() && !variantProducts.isEmpty()){
            for(final Variation variantProduct: variantProducts){
                final com.b2s.rewards.model.Product coreVariationProduct = new com.b2s.rewards.model.Product();
                coreVariationProduct.setName(variantProduct.getName());
                coreVariationProduct.setProductId(variantProduct.getPsid());
                coreVariationProduct.setPsid(variantProduct.getPsid());

                if (variantProduct.getLongDescription() != null && variantProduct.getLongDescription().isPresent()){
                    coreVariationProduct.setDefaultProductDescription(variantProduct.getLongDescription().get());
                }


                coreVariationProduct.setCoreProductType(CoreProductType.merchandise);
                populateCategorySlug(coreVariationProduct, product.getCategories().get(0), 0);
                populateProductCategories(coreVariationProduct, product.getCategories());
                populateSupplierCategory(coreVariationProduct, CoreProductType.merchandise);


                final AvailabilityInformation availabilityInformation = variantProduct.getAvailabilityInformation();
                coreVariationProduct.setAvailable(AppleUtil.isAWPAvailableProduct(availabilityInformation));

                populateProductShortDescription(coreVariationProduct, variantProduct);
                populateProductFeatures(coreVariationProduct, parentProductFeatures);
                populateProductImageUrls(coreVariationProduct, variantProduct.getImageUrls(),merchant);

                final PricingInformation priceInfo = variantProduct.getPricingInformation();
                final List < Offer > productOffer = getOfferDetails(priceInfo, variantProduct, merchant, shippingEligible,isFreeShipping(variantProduct),
                        inStoreEligible, reserveAndPickupEligible, coreVariationProduct.getCoreProductType());
                coreVariationProduct.setOffers(productOffer);
                populateVariationDimensionNameValues(coreVariationProduct, convertVariantOptions(variantProduct));
                coreVariationProduct.setFlattenedAttributes(variantProduct.getAdditionalInfo());
                variantCoreProductList.add(coreVariationProduct);
            }
        }
        return variantCoreProductList;
    }

    private void populateCategorySlugForGiftCards(final Product coreVariationProduct, final ImmutableList<Category> categories) {
        if (Optional.ofNullable(categories).isPresent() && !categories.isEmpty()) {
            final Category productCategory = categories.get(0);
            coreVariationProduct.setCategorySlug(productCategory.getSlug());
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private List<Product> getProductVariations(final List<Variation> variantProducts, final List<String> parentProductFeatures,
                                               CoreProductType coreProductType, final MerchantEntity merchant,
                                               final boolean shippingEligible, final boolean inStoreEligible,
                                               final boolean reserveAndPickupEligible){
        final List<Product> variantCoreProductList = new LinkedList<Product>();
        if(Optional.ofNullable(variantProducts).isPresent() && !variantProducts.isEmpty()){
            for(final Variation variantProduct: variantProducts){
                final Product coreVariationProduct = new  Product();
                coreVariationProduct.setName(variantProduct.getName());
                coreVariationProduct.setProductId(variantProduct.getPsid());
                coreVariationProduct.setPsid(variantProduct.getPsid());
                coreVariationProduct.setCoreProductType(coreProductType);

                if (variantProduct.getLongDescription() != null && variantProduct.getLongDescription().isPresent()){
                    coreVariationProduct.setDefaultProductDescription(variantProduct.getLongDescription().get());
                }

                final AvailabilityInformation availabilityInformation = variantProduct.getAvailabilityInformation();
                coreVariationProduct.setAvailable(AppleUtil.isAWPAvailableProduct(availabilityInformation));

                populateProductShortDescription(coreVariationProduct, variantProduct);
                populateProductFeatures(coreVariationProduct, parentProductFeatures);
                populateProductImageUrls(coreVariationProduct, variantProduct.getImageUrls(),merchant);
                populateSupplierCategory(coreVariationProduct, coreProductType);

                final PricingInformation priceInfo = variantProduct.getPricingInformation();
                final List < Offer > productOffer = getOfferDetails(priceInfo, variantProduct, merchant, shippingEligible, isFreeShipping(variantProduct),
                        inStoreEligible, reserveAndPickupEligible, coreVariationProduct.getCoreProductType());
                coreVariationProduct.setOffers(productOffer);

                populateVariationDimensionNameValues(coreVariationProduct, convertVariantOptions(variantProduct));
                variantCoreProductList.add(coreVariationProduct);
            }
        }
        return variantCoreProductList;
    }

    public Product getProductDetails(final com.b2s.service.product.common.domain.response.Product serviceProduct){
        final Product coreProduct = new Product();
        populateProductGeneralInformation(coreProduct, serviceProduct);
        populateSupplierCategory(coreProduct, coreProduct.getCoreProductType());
        populateProductFeatures(coreProduct, serviceProduct.getFeatures());
        populateProductImageUrls(coreProduct,serviceProduct.getImageUrls(),merchantRepositoryHolder.getMerchantRepository().get(serviceProduct.getMerchant().get()).get(0));

        final AvailabilityInformation availabilityInformation = serviceProduct.getAvailabilityInformation();
        coreProduct.setAvailable(availabilityInformation.isAvailable());


        final PricingInformation priceInfo = serviceProduct.getPricingInformation();
        final List < Offer > offers = getOfferDetails(priceInfo, serviceProduct, merchantRepositoryHolder.getMerchantRepository().get(serviceProduct.getMerchant().get()).get(0),
                serviceProduct.isShippingEligible(), isFreeShipping(serviceProduct), serviceProduct.isInStoreEligible(),
                serviceProduct.isReserveAndPickupEligible(),
                coreProduct.getCoreProductType());

        coreProduct.setOffers(offers);
        populateVariationDimensionNames(coreProduct, serviceProduct.getOptions());
        return coreProduct;
    }

    private void populateVariationDimensionNameValues(final Product coreProduct, final Map<String, String> options){
        if(Optional.ofNullable(options).isPresent() && !options.isEmpty()){
            coreProduct.setVariationDimensionNameValues(options);
        }
    }

    private void populateVariationDimensionNames(final Product coreProduct, final Map<String, Option> options){
        if(Optional.ofNullable(options).isPresent() && !options.isEmpty()){
            coreProduct.setVariationDimensionNames(new ImmutableList.Builder<String>().addAll(options.keySet().iterator()).build());
            final List<String> optionNames = new LinkedList<String>();
            final List<String> optionValues = new LinkedList<String>();
            final Map<String, String> optionNamesAndValues = new HashMap<String,String>();
            for(final Map.Entry<String, Option> optionEntry :options.entrySet()){
                if(Optional.ofNullable(optionEntry.getValue()).isPresent() && Optional.ofNullable(optionEntry.getValue().getValue()).isPresent()){
                    optionNames.add(optionEntry.getKey());
                    optionValues.add(optionEntry.getValue().getValue());
                    optionNamesAndValues.put(optionEntry.getKey(),optionEntry.getValue().getValue());
                }
            }
            if(!optionNamesAndValues.isEmpty()){
                coreProduct.setVariationDimensionNames(optionNames);
                coreProduct.setVariationDimensionValues(optionValues);
                coreProduct.setVariationDimensionNameValues(optionNamesAndValues);

            }
        }
    }

    private void populateProductGeneralInformation(final Product coreProduct, final com.b2s.service.product.common.domain.response.Product serviceProduct){
        coreProduct.setProductId(serviceProduct.getPsid());
        coreProduct.setPsid(serviceProduct.getPsid());
        coreProduct.setBsin(serviceProduct.getBsin());

        if (serviceProduct.getUpc() != null && serviceProduct.getUpc().isPresent()) {
            coreProduct.setUpc(serviceProduct.getUpc().get());
        }

        if (serviceProduct.getBrand() != null && serviceProduct.getBrand().isPresent()) {
            coreProduct.setBrand(serviceProduct.getBrand().get());
        }
        if (serviceProduct.getManufacturer() != null && serviceProduct.getManufacturer().isPresent()) {
            coreProduct.setManufacturer(serviceProduct.getManufacturer().get());
        }
        populateCoreProductType(coreProduct, serviceProduct.getProductType());

        if(serviceProduct.getProductType().equals(ProductType.GIFT_CARD)) {
            coreProduct.setParentProductId(serviceProduct.getGroupId().get());
        }

        if (serviceProduct.getGiftCardType() != null && serviceProduct.getGiftCardType().isPresent()) {
            coreProduct.setFormat(serviceProduct.getGiftCardType().get());
        }

        if (serviceProduct.getLongDescription() != null && serviceProduct.getLongDescription().isPresent()){
            coreProduct.setDefaultProductDescription(serviceProduct.getLongDescription().get());
        }
        populateProductShortDescription(coreProduct, serviceProduct);

        if(serviceProduct.isHasVariations() && Optional.ofNullable(serviceProduct.getOptions()).isPresent() && !serviceProduct.getOptions().isEmpty()){
            coreProduct.setParentProductId(serviceProduct.getPsid());
            coreProduct.setHasVariations(Boolean.TRUE);
        } else {
            if (serviceProduct.getGroupId() != null && serviceProduct.getGroupId().isPresent()){
                coreProduct.setParentProductId(serviceProduct.getGroupId().get());
            }
            coreProduct.setHasVariations(Boolean.FALSE);
        }

        coreProduct.setName(serviceProduct.getName());

        if (!CoreProductType.giftcard.equals(coreProduct.getCoreProductType())) {
            populateModel(coreProduct, serviceProduct.getModel());
            // TODO:  chagne appropriate level, to support when accessories comes into picture
            populateCategorySlug(coreProduct, serviceProduct.getCategories().get(0), 1);
            populateProductCategories(coreProduct, serviceProduct.getCategories());
//            populateCategorySlug(coreProduct, serviceProduct.getCategories().get(0), 0);
        }

        if(serviceProduct.getAverageRating() != null && serviceProduct.getAverageRating().isPresent()){

            final Float averageRating = serviceProduct.getAverageRating().get();

            //Format the float to two decimal places
            final Double convertedRating = Double.valueOf(averageRating);
            coreProduct.setAverageRating(convertedRating);
        }
        coreProduct.setTotalReviews(serviceProduct.getTotalReviews());
        if(Optional.ofNullable(serviceProduct.getRelatedProducts()).isPresent() && !serviceProduct.getRelatedProducts().isEmpty()){
            coreProduct.setRelatedPsids(serviceProduct.getRelatedProducts());
        }
        if(serviceProduct.getMaxPurchaseQuantity().isPresent()){
            coreProduct.setMaxPurchaseQuantity(serviceProduct.getMaxPurchaseQuantity().get());
        }
        coreProduct.setFlattenedAttributes(serviceProduct.getAdditionalInfo());
    }

    private void populateCategorySlug(final Product coreProduct, final Category category, final Integer depth) {
        if (category.getAncestors().isEmpty() && category != null ) {
            coreProduct.setCategorySlug(category.getSlug());
        } else {
            for (Category ancestors : category.getAncestors() ) {
                //TODO: send proper depth.
//                if (ancestors.getDepth() == depth) {
//                    coreProduct.setCategorySlug(ancestors.getSlug());
//                }
                coreProduct.setCategorySlug(category.getSlug());
            }
        }
    }

    private void populateProductCategories(final Product coreProduct, final List<Category> categories) {
        if(coreProduct != null && CollectionUtils.isNotEmpty(categories)) {
            List<ProductCategory> productCategories = new ArrayList<>();
            for (Category category : categories) {
                ProductCategory productCategory = new ProductCategory();
                productCategory.setName(category.getSlug());
                productCategories.add(productCategory);
            }
            coreProduct.setProductCategories(productCategories);
        }
    }

    private void populateModel(final Product coreProduct, final Optional<String> model) {
        if (model.isPresent()) {
            coreProduct.setModel(model.get());
        }
    }

    private void populateCoreProductType(final Product coreProduct, final ProductType productType) {
        coreProduct.setCoreProductType(CoreProductType.merchandise);
        if (productType != null && CoreProductType.lookupCoreProductTypeByLabel(productType.name()) != null) {
            coreProduct.setCoreProductType(CoreProductType.lookupCoreProductTypeByLabel(productType.name()));
        }
    }

    private void populateProductShortDescription(final Product coreProduct, final AbstractProduct serviceProduct) {
        final ProductDescription productDescription;
        if (serviceProduct.getShortDescription().isPresent()) {
            productDescription = new ProductDescription(serviceProduct.getShortDescription().get());
        } else if (serviceProduct.getLongDescription().isPresent()){
            // this should be a rare case. We had to add it after PS3 made ShortDescription Optional
            productDescription = new ProductDescription(serviceProduct.getLongDescription().get());
            //noinspection deprecation
            productDescription.setContent(productDescription.getShortContent());
        } else {
            productDescription = null;
        }
        coreProduct.setShortDescription(productDescription);
    }

    private void populateProductFeatures(final Product coreProduct, final List<String> features){
        if(Optional.ofNullable(features).isPresent() && !features.isEmpty()){
            coreProduct.setFeatures(features);
        }
    }
    @SuppressWarnings("MethodWithTooManyParameters")

    private List<Offer> getOfferDetails(final PricingInformation priceInfo, final AbstractProduct serviceProduct,
                                        final MerchantEntity merchant,
                                        final boolean shippingEligible, final boolean freeShippingEligible, final boolean inStoreEligible,
                                        final boolean reserveAndPickupEligible, final CoreProductType coreProductType){

        final Offer offer = new Offer();
        setPricingInfo(offer, priceInfo.getFirstCalculatedPriceInfo(), serviceProduct, coreProductType);

        if (Optional.ofNullable(serviceProduct.getSku()).isPresent()) {
            offer.setSku(serviceProduct.getSku());
        }

        final com.b2s.rewards.model.Merchant productMerchant = new com.b2s.rewards.model.Merchant();
        if(Optional.ofNullable(merchant).isPresent()){
            final String productMerchantCode = String.valueOf(merchant.getMerchantId());
            final String convertedMerchantCode = Optional.ofNullable(legacyMerchantMapper.get(productMerchantCode)).isPresent() ?
                    legacyMerchantMapper.get(productMerchantCode) :  productMerchantCode;
            productMerchant.setMerchantId(convertedMerchantCode);
            productMerchant.setName(merchant.getSimpleName());
        }

        offer.setMerchant(productMerchant);

        offer.setEligibleForShipping(shippingEligible);
        offer.setEligibleForInStorePickup(inStoreEligible);
        offer.setEligibleForReserveAndPickup(reserveAndPickupEligible);
        offer.setEligibleForFreeShipping(freeShippingEligible);

        final AvailabilityInformation availabilityInformation = serviceProduct.getAvailabilityInformation();
        offer.setAvailable(AppleUtil.isAWPAvailableProduct(availabilityInformation));
        if (availabilityInformation.getAvailabilityMessage() != null && availabilityInformation.getAvailabilityMessage().indexOf("ERROR") == -1) {
            offer.setAvailabilityDescription(availabilityInformation.getAvailabilityMessage());
        }

        final List<Offer> offers = new ArrayList<>();
        offers.add(offer);
        return offers;
    }


    private void  populateSupplierCategory(final Product coreProduct, final CoreProductType productType){
        //TODO: pricingCategoryCode is needed to find var program category for calculator service.
        String pricingCategoryCode = null;
        if (CoreProductType.merchandise.equals(productType)) {
            pricingCategoryCode = coreProduct.getCategorySlug();
        }

        final Supplier supplier = new Supplier();
        final String supplierId = this.supplierProductMapping.get(productType.name());
        supplier.setSupplierId(Integer.valueOf(supplierId));
        coreProduct.setSupplier(supplier);

    }

    private void populateProductImageUrls(final Product coreProduct, final ProductImageUrls productImageUrls, final MerchantEntity merchant){
        if(Optional.ofNullable(productImageUrls).isPresent()){
            final ProductImage productImage = new ProductImage();

            if(productImageUrls.getThumbnail().isPresent()){
                productImage.setThumbnailImageURL(productImageUrls.getThumbnail().get());
            }
            if(productImageUrls.getSmall().isPresent()){
                productImage.setSmallImageURL(productImageUrls.getSmall().get());
            }
            if(productImageUrls.getMedium().isPresent()){
                productImage.setMediumImageURL(productImageUrls.getMedium().get());
            }
            if(productImageUrls.getLarge().isPresent()){
                productImage.setLargeImageURL(productImageUrls.getLarge().get());
            }
            Integer productMerchantCode = null;
            if(Optional.ofNullable(merchant).isPresent()){
                productMerchantCode = merchant.getMerchantId();
            }

            //TODO:pricingCategoryCode are needed for image obfuscation
            //Disable Obfuscation
//            this.imageObfuscatory.obfuscateImageUrls(productImage,String.valueOf(productMerchantCode), null);
            this.imageObfuscatory.resizeImageUrls(productImage);
            coreProduct.setDefaultProductImage(productImage);
        }
    }

    private boolean isFreeShipping (final AbstractProduct serviceProduct) {
        if (serviceProduct instanceof  com.b2s.service.product.common.domain.response.Product) {
            final Iterator<com.b2s.service.product.common.domain.Offer> productOfferTypesIterator = ((com.b2s.service.product.common.domain.response.Product) serviceProduct).getOffers().iterator();
            while (productOfferTypesIterator.hasNext()) {
                if (productOfferTypesIterator.next().getOfferType().equals(OfferType.FREE_SHIPPING)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setPricingInfo(final Offer offer, final CalculatedPriceInfo priceInfo, final AbstractProduct serviceProduct, final CoreProductType coreProductType) {
        OriginalPriceInfo origPriceInfo = null;
        if(serviceProduct instanceof Variation && ((Variation) serviceProduct).getPricingInformation().getOriginalPriceInfo()!=null) {
            origPriceInfo = ((Variation) serviceProduct).getPricingInformation().getOriginalPriceInfo();
        }

        if(serviceProduct instanceof com.b2s.service.product.common.domain.response.Product
                && ((com.b2s.service.product.common.domain.response.Product) serviceProduct).getPricingInformation().getOriginalPriceInfo()!=null) {
            origPriceInfo = ((com.b2s.service.product.common.domain.response.Product) serviceProduct).getPricingInformation().getOriginalPriceInfo();
        }
        if(origPriceInfo != null) {
            if (Optional.ofNullable(origPriceInfo.getBasePrice()).isPresent()) {
                offer.setItemPrice(origPriceInfo.getBasePrice().getAmount().doubleValue());
            }
            if (Optional.ofNullable(origPriceInfo.getShippingCost()).isPresent()) {
                offer.setShippingPrice(origPriceInfo.getShippingCost().getAmount().doubleValue());
            }


            if (origPriceInfo.getMsrp().isPresent() &&
                    Optional.ofNullable(priceInfo.getDisplayPrice()).isPresent()) {
                offer.setB2sPrice(
                        new Price(origPriceInfo.getMsrp().get(), priceInfo.getDisplayPrice().getAmount().intValue()));
            }

            if (Optional.ofNullable(origPriceInfo.getBasePrice()).isPresent() &&
                    Optional.ofNullable(priceInfo.getBasePrice()).isPresent()) {
                offer.setCurrency(priceInfo.getBasePrice().getCurrencyUnit());
                offer.setB2sItemPrice(
                        new Price(origPriceInfo.getBasePrice(), priceInfo.getBasePrice().getAmount().intValue()));
            }

            if (Optional.ofNullable(origPriceInfo.getShippingCost()).isPresent() &&
                    Optional.ofNullable(priceInfo.getShippingCost()).isPresent() &&
                    !CoreProductType.giftcard.equals(coreProductType)) {
                offer.setB2sShippingPrice(
                        new Price(origPriceInfo.getShippingCost(), priceInfo.getShippingCost().getAmount().intValue()));
            }
        }
    }

    private boolean isFreeShipping (final com.b2s.service.product.common.domain.response.Product serviceProduct) {
        Iterator<com.b2s.service.product.common.domain.Offer> offers = serviceProduct.getOffers().iterator();
        while (offers.hasNext()) {
            if (OfferType.FREE_SHIPPING.equals(offers.next().getOfferType())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> convertVariantOptions(Variation variantProduct) {
        Map<String, String> strOptions = new HashMap<>();
        for (String optionName : variantProduct.getOptionNames()) {
            strOptions.put(optionName, variantProduct.getOptions().get(optionName).getValue());
        }
        return strOptions;
    }
}
