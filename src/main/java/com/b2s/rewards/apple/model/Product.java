package com.b2s.rewards.apple.model;

/**
 * Created by rperumal on 3/20/2015.
 */

import com.b2s.rewards.model.ProductError;
import com.b2s.rewards.model.ProductErrorType;
import com.b2s.rewards.model.ProductImage;
import com.b2s.rewards.model.Supplier;
import com.b2s.service.product.common.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Specification for a product
 */
public class Product implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long serialVersionUID = 918646069062438444L;
    private String psid;
    private String sku;
    private String name;
    private String shortDescription;
    private String longDescription;
    private String brand;
    private String manufacturer;
    private Float averageRating = 0F;
    private String upc;
    private String shippingAvailabilityMessage;
    private String shipmentQuoteDate;
    // Added suppress warnings as we are deserializing in CartController > copy() method. Hence transient will not be
    // applicable
    @SuppressWarnings("squid:S1948")
    private Optional<Promotion> promotion;
    private List<Category> categories = new ArrayList<Category>();
    private List<Offer> offers = new ArrayList<>();
    private List<Option> options = new ArrayList<>();
    @SuppressWarnings("squid:S1948")
    private Map<String, Object> additionalInfo = new HashMap<>();
    private ImageURLs images = new ImageURLs();
    private List<Product> variations = new ArrayList<>();
    @SuppressWarnings("squid:S1948")
    private Map<ProductErrorType, ProductError> productErrors;
    @SerializedName("isAvailable")
    private boolean available;
    private String appleSku;
    @SuppressWarnings("squid:S1948")
    private Map<String, JsonNode> merchantSpecificData = new HashMap<>();
    private boolean accessoryItem;
    private Map<String,Set<Option>> optionsConfigurationData;
    private boolean hasVariations;
    private AddOns addOns = new AddOns();
    private String learnMore;
    private ProductType productType;
    private boolean isEngravable;
    private boolean hasRelatedProduct;
    private List<Product> relatedProducts = new ArrayList<>();
    private List<String> carouselImages = new ArrayList<>();

    private Engrave engrave;
    private Set<String> ampSubscriptionConfig;

    private SmartPrice smartPrice;

    public Set<String> getAmpSubscriptionConfig() {
        return ampSubscriptionConfig;
    }

    public void setAmpSubscriptionConfig(final Set<String> ampSubscriptionConfig) {
        this.ampSubscriptionConfig = ampSubscriptionConfig;
    }

    public boolean isHasVariations() {
        return hasVariations;
    }

    public void setHasVariations(final boolean hasVariations) {
        this.hasVariations = hasVariations;
    }

    public boolean isHasRelatedProduct() {
        return hasRelatedProduct;
    }

    public void setHasRelatedProduct(final boolean hasRelatedProduct) {
        this.hasRelatedProduct = hasRelatedProduct;
    }

    public List<Product> getRelatedProducts() {
        return relatedProducts;
    }

    public void setRelatedProducts(final List<Product> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    public List<String> getCarouselImages() {
        return carouselImages;
    }

    public void setCarouselImages(final List<String> carouselImages) {
        this.carouselImages = carouselImages;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public Product() {
        promotion = Optional.empty();
    }

    public void addProductError(ProductError productError) {
        if (productErrors == null) {
            productErrors = new HashMap<>();
        }
        this.productErrors.put(productError.getErrorType(), productError);
    }

    public boolean hasProductErrors() {
        return !this.productErrors.isEmpty();
    }
    //Data access: getter/setter
    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Float getAverageRating() {
        return (averageRating != null? averageRating : 0F);
    }

    public void setAverageRating(Float averageRating) {
        this.averageRating = averageRating;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getShippingAvailabilityMessage() {
        return shippingAvailabilityMessage;
    }

    public void setShippingAvailabilityMessage(String shippingAvailabilityMessage) {
        this.shippingAvailabilityMessage = shippingAvailabilityMessage;
    }

    public String getShipmentQuoteDate() {
        return shipmentQuoteDate;
    }

    public void setShipmentQuoteDate(final String shipmentQuoteDate) {
        this.shipmentQuoteDate = shipmentQuoteDate;
    }

    public Optional<Promotion> getPromotion() {
        return promotion;
    }

    public void setPromotion(Optional<Promotion> promotion) {
        this.promotion = promotion;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public ImageURLs getImages() {
        return images;
    }

    public void setImages(ImageURLs images) {
        this.images = images;
    }

    public List<Product> getVariations() {
        return (variations==null? new ArrayList<Product>() : variations);
    }

    public void setVariations(List<Product> variations) {
        this.variations = variations;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategory(List<Category> categories) {
        this.categories = categories;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Offer getDefaultOffer(){
        return (getHasOffers()) ? offers.get(0): null ;
    }
    public Boolean getHasOffers() {
        return offers != null && !offers.isEmpty();
    }

    public String getAppleSku() {
        return appleSku;
    }

    public Product setAppleSku(String appleSku) {
        this.appleSku = appleSku;
        return this;
    }

    public String getLearnMore() {
        return learnMore;
    }

    public void setLearnMore(final String learnMore) {
        this.learnMore = learnMore;
    }

    public boolean isAccessoryItem() {
        return accessoryItem;
    }

    public void setAccessoryItem(final boolean accessoryItem) {
        this.accessoryItem = accessoryItem;
    }

    /**
     * Converts core model product to apple product.
     * @param coreProduct
     * @return
     */
    public Product transform(com.b2s.rewards.model.Product coreProduct){
        Product appleProduct = new Product();
        try {
            BeanUtils.copyProperties(appleProduct, coreProduct);
            appleProduct.setShortDescription(coreProduct.getShortDescription() != null ? coreProduct.getShortDescription().getContent() : "");
            if (coreProduct.getProductDescriptions() != null && coreProduct.getProductDescriptions().size() > 0) {
                String longDescription = coreProduct.getProductDescriptions().get(0).getContent();
                appleProduct.setLongDescription(longDescription != null ? longDescription : "");
            }

            setAdditionalInfo(appleProduct, coreProduct);
            populateCategories(appleProduct, coreProduct);
            List<Offer> offers = new ArrayList();
            appleProduct.setImages(ImageURLs.transform(coreProduct.getDefaultProductImage()));
            appleProduct.setOffers(offers);
        }catch(IllegalAccessException iAE){
            LOG.error("Property access Exception while getting Apple Product ", iAE);
        }catch(InvocationTargetException iTE){
            LOG.error("Property write Exception while setting Apple Product ", iTE);
        }
        return appleProduct;
    }

    private void populateCategories(Product appleProduct, com.b2s.rewards.model.Product coreProduct) {
        if(appleProduct != null  && coreProduct != null && CollectionUtils.isNotEmpty(coreProduct.getProductCategories())) {
            appleProduct.setCategory(coreProduct.getProductCategories().stream().map(productCategory -> new Category(productCategory.getName())).collect(Collectors.toList()));
        }
    }

    // Convert additionalInfo to object map
    private void setAdditionalInfo(Product appleProduct, com.b2s.rewards.model.Product coreProduct) {
        Map<String, Object> additionalInfo = new HashMap<>();
        if (coreProduct.getFlattenedAttributes() != null) {
            additionalInfo.putAll(coreProduct.getFlattenedAttributes());
        }
        appleProduct.setAdditionalInfo(additionalInfo);
    }

    public String getProductBasedPricingKey(){
        if(CollectionUtils.isNotEmpty(categories) && Objects.nonNull(categories.get(0))) {
            return String.format("%s|%s", categories.get(0).getSlug(), getOptionValue("caseSize").replace(" ", ""));
        }
        return "";
    }
    public String getCategoryBasedPricingKey(String pricingKey) {
        String slug = "";
        if (CollectionUtils.isNotEmpty(categories) && Objects.nonNull(categories.get(0))) {
            slug = categories.get(0).getSlug();
        }
        return StringUtils.join(pricingKey, "|", slug);
    }

    public String getOptionValue(final String type){
        for (final Option option : options) {
            if ( type.equalsIgnoreCase(option.getName()) ) {
                return option.getValue();
            }
        }
        return  "";
    }

    public Map<String, JsonNode> getMerchantSpecificData() {
        return merchantSpecificData;
    }

    public void setMerchantSpecificData(final Map<String, JsonNode> merchantSpecificData) {
        this.merchantSpecificData = merchantSpecificData;
    }

    public Map<String, Set<Option>> getOptionsConfigurationData() {
        return optionsConfigurationData;
    }

    public void setOptionsConfigurationData(Map<String, Set<Option>> optionsConfigurationData) {
        this.optionsConfigurationData = optionsConfigurationData;
    }

    public AddOns getAddOns() {
        return addOns;
    }

    public void setAddOns(final AddOns addOns) {
        this.addOns = addOns;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(final ProductType productType) {
        this.productType = productType;
    }

    public boolean getIsEngravable() {
        return isEngravable;
    }

    public void setEngravable(final boolean engravable) {
        isEngravable = engravable;
    }

    public Engrave getEngrave() {
        return engrave;
    }

    public void setEngrave(final Engrave engrave) {
        this.engrave = engrave;
    }

    private String format;
    private Map<String, String> variationDimensionNameValues;
    private String productId;
    private transient List<List<ProductImage>> productImages;
    @JsonIgnore
    private transient Supplier supplier;
    private String parentProductId;

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public Map<String, String> getVariationDimensionNameValues() {
        return variationDimensionNameValues;
    }

    public void setVariationDimensionNameValues(final Map<String, String> variationDimensionNameValues) {
        this.variationDimensionNameValues = variationDimensionNameValues;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public List<List<ProductImage>> getProductImages() {
        return productImages;
    }

    public void setProductImages(final List<List<ProductImage>> productImages) {
        this.productImages = productImages;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(final Supplier supplier) {
        this.supplier = supplier;
    }

    public String getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(final String parentProductId) {
        this.parentProductId = parentProductId;
    }

    public SmartPrice getSmartPrice() {
        return smartPrice;
    }

    public void setSmartPrice(SmartPrice smartPrice) {
        this.smartPrice = smartPrice;
    }

    public void setDefaultProductImage(ProductImage productImage) {
        List<ProductImage> defaultSet = new ArrayList<>();
        defaultSet.add(productImage);
        if (Objects.isNull(productImages) || productImages.size() == 0) {
            productImages = new ArrayList<>();
            productImages.add(defaultSet);
        } else {
            productImages.set(0, defaultSet);
        }
    }
}
