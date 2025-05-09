package com.b2s.common.services.util;

import com.b2s.apple.services.ProgramService;
import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.model.Category;
import com.b2s.rewards.apple.model.ImageURLs;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.domain.SortField;
import com.b2s.service.product.client.domain.SortOrder;
import com.b2s.service.product.common.domain.response.ProductSearchDocument;
import com.b2s.service.product.common.domain.response.ProductSearchResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Repository for categories.
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 4:06 PM
 *
 */
public class CategoryRepository {


    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryRepository.class);
    public int getTotalActiveCategoriesInDB() {
        int count = 0;
        if(MapUtils.isNotEmpty(categoriesBySlug)) {
            count = categoriesBySlug.size();
        }
        return count;
    }

    private final Map<String, CategoryInfo> categoriesBySlug;
    private final Map<String, CategoryInfo> categoriesByHierarchyFromRootNode;

    private final Map<String, Category> categoryHierarchyBySlug;


    private final Map<String,Map<String, String>> imageURLCache = new ConcurrentHashMap<>();

    private ImageObfuscatory imageObfuscatory;

    private ProductServiceV3 productServiceV3;

    private ProgramService programService;

    public void setImageObfuscatory(final ImageObfuscatory imageObfuscatory) {
        this.imageObfuscatory = imageObfuscatory;
    }

    public void setProductServiceV3(final ProductServiceV3 productServiceV3) {
        this.productServiceV3 = productServiceV3;
    }

    public Set<String> getAllSlugs(){
        return categoriesBySlug.keySet();
    }

    public void setProgramService(final ProgramService programService) {
        this.programService = programService;
    }

    /**
     * Creates repository by querying category services for core supported locales.
     * This can load the data into repository from stub property file without querying category services.
     *
     * @param categoriesBySlug
     * @param categoriesByHierarchyFromRootNode
     * @param categoryHierarchyBySlug
     */
    public CategoryRepository(final Map<String, CategoryInfo> categoriesBySlug, final Map<String, CategoryInfo> categoriesByHierarchyFromRootNode, Map<String, Category> categoryHierarchyBySlug) throws ServiceException {
        this.categoriesBySlug = categoriesBySlug;
        this.categoriesByHierarchyFromRootNode = categoriesByHierarchyFromRootNode;
        this.categoryHierarchyBySlug = categoryHierarchyBySlug;
        validateRepository();
    }



    private void updateSubCategories(final Program program, final Locale locale, final Category category) {
        category.getSubCategories().forEach(subCategory -> {
            if(programService!=null && imageObfuscatory!= null && productServiceV3!=null) {
                final String imageURLKey = String.join("_", program.getVarId(), program.getProgramId(), subCategory.getSlug(),locale.toString());
                Map<String, String> imageURLMap = imageURLCache.get(imageURLKey);

                setSubCategoryDataFromProductsDetails(program, locale, subCategory, imageURLKey, imageURLMap);
            }

        });
    }

    private void setSubCategoryDataFromProductsDetails(final Program program, final Locale locale, final Category subCategory,
        final String imageURLKey, Map<String, String> imageURLMap) {
        if (Objects.isNull(imageURLMap)) {
            Set<String> categorySlugs = new HashSet<>();
            String sort = null;
            String sortOrder = null;
            if(StringUtils.isNotEmpty(subCategory.getSlug())){
                categorySlugs.add(subCategory.getSlug());
                if(AppleUtil.isAccessories(subCategory.getSlug())){
                    sort = SortField.SALES_RANK.name();
                    sortOrder = SortOrder.DESCENDING.name();
                }
            }
            final ProductSearchRequest.Builder builder =
                productServiceV3.getProductSearchRequestBuilder(categorySlugs,
                            null, sort, sortOrder, locale, null, 1, null, program, null, false,false,null);
            ProductSearchResponse productSearchResponse = null;
            try {
                productSearchResponse = productServiceV3.searchProducts(builder.build());
            }catch (RuntimeException ex){
                LOGGER.error("No product found",ex);
            }
            imageURLMap = createImageUrlMapFromProductSearchResponse(productSearchResponse);
            subCategory.setImages(imageURLMap);


            final String detailUrl = createProductDetailUrlFromProductSearchResponse(productSearchResponse);
            subCategory.setDetailUrl(detailUrl);
            imageURLCache.put(imageURLKey, imageURLMap);

        } else {
            subCategory.setImages(imageURLMap);
        }
    }

    private Map<String, String> createImageUrlMapFromProductSearchResponse(
        final ProductSearchResponse productSearchResponse) {
        final Map<String, String> imageURLMap = new ConcurrentHashMap<>();
        if(Objects.nonNull(productSearchResponse) &&Objects.nonNull(productSearchResponse.getProductSearchGroups()) && CollectionUtils
            .isNotEmpty(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments())) {
            final ImageURLs imageURLs = getProductImageRequest(productSearchResponse);
            imageObfuscatory.resizeImageUrls(imageURLs);

            if (Objects.nonNull(imageURLs)) {
                imageURLMap
                    .put(CommonConstants.PRODUCT_RESPONSE_IMAGE_URL_THUMBNAIL, imageURLs.getThumbnail());
                imageURLMap.put(CommonConstants.PRODUCT_RESPONSE_IMAGE_URL_SMALL, imageURLs.getSmall());
                imageURLMap.put(CommonConstants.PRODUCT_RESPONSE_IMAGE_URL_MEDIUM, imageURLs.getMedium());
                imageURLMap.put(CommonConstants.PRODUCT_RESPONSE_IMAGE_URL_LARGE, imageURLs.getLarge());

            }

        }
        return imageURLMap;
    }

    private String createProductDetailUrlFromProductSearchResponse(final ProductSearchResponse productSearchResponse) {
        String url = null;
        if(Objects.nonNull(productSearchResponse) && Objects.nonNull(productSearchResponse.getDefaultGroup())) {
            url = productSearchResponse.getDefaultGroup().map(productSearchDocumentGroup ->
                Optional.ofNullable(productSearchDocumentGroup)
                    .filter(p -> p.getTotalFound() == 1)
                    .map(product -> {
                        final ProductSearchDocument productSearchDocument = product.getProductSearchDocuments().get(0);
                        return new StringBuilder(productSearchDocument.getCategorySlugs().get(0))
                            .append("/")
                            .append(productSearchDocument.getPsid().replace(CommonConstants.SLASH, CommonConstants.HYPHEN))
                            .toString();
                    }).orElse(null)
            ).orElse(null);
        }

        return url;
    }


    private ImageURLs getProductImageRequest(final ProductSearchResponse productSearchResponse) {
        final ImageURLs imageURLs = new ImageURLs();
        if(CollectionUtils.isNotEmpty(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments())) {
            imageURLs.setThumbnail(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments().get(0).getImageUrls().getThumbnail().orElse(null));
            imageURLs.setSmall(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments().get(0).getImageUrls().getSmall().orElse(null));
            imageURLs.setMedium(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments().get(0).getImageUrls().getMedium().orElse(null));
            imageURLs.setLarge(productSearchResponse.getProductSearchGroups().get(CommonConstants.PRODUCT_RESPONSE_DEFAULT_GROUP).getProductSearchDocuments().get(0).getImageUrls().getLarge().orElse(null));
        }
        return imageURLs;
    }



    /**
     * Makes sure the the category repository by slug, browseNodeId and hierarchyFromRootNode are having same size.
     */

    private void validateRepository() throws ServiceException {
        final int sizeBySlug = this.categoriesBySlug.size();
        if (sizeBySlug != this.categoriesByHierarchyFromRootNode.size()) {
            throw new ServiceException(ServiceExceptionEnums.CATEGORIES_REPO_SIZE_NOT_MATCHING);
        }
    }

    public void clearRepository() {
        this.categoriesBySlug.clear();
        this.imageURLCache.clear();
        this.categoriesByHierarchyFromRootNode.clear();
    }

    public Category getCategoryDetailsByHierarchy(String slugName) {
        return this.categoryHierarchyBySlug.get(slugName);
    }

    public List<Category> getParentCategories(String varId, String programId, final Program program, String locale) throws ServiceException {
        List<Category> categories = new ArrayList<>();
        final Locale localeObj = LocaleUtils .toLocale(locale);
        try {
            for(Category category : categoryHierarchyBySlug.values()) {
                if(category.getParents()!=null && category.getParents().size()==0){
                    Category categoryCopy = (Category)BeanUtils.cloneBean(category);


                    updateSubCategories(program , localeObj, categoryCopy);
                    categories.add(categoryCopy);
                }
            }

            if (CollectionUtils.isEmpty(categories)) {
                throw new ServiceException(ServiceExceptionEnums.CATEGORIES_NOT_FOUND_EXCEPTION);
            }
        } catch(Exception e) {
            LOGGER.error("Error while getting parent categories for var id: {}, program id: {}, locale: {}. Exception: {}", varId, programId, locale, e);
        }
        return Collections.unmodifiableList(categories);
    }

}