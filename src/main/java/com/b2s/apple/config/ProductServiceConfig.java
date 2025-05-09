package com.b2s.apple.config;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.productservice.CategoryServiceV3;
import com.b2s.common.services.productservice.ProductServiceFactoryWrapper;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.transformers.TransformersHolder;
import com.b2s.common.services.transformers.productservice.CategoryNodeToCategoryInfo;
import com.b2s.common.services.transformers.productservice.CoreCategoryToCategoryTaxonomy;
import com.b2s.common.services.transformers.productservice.MultiProductDetailToCoreProductDetail;
import com.b2s.common.services.transformers.productservice.MultiProductDetailToProductDetail;
import com.b2s.common.services.util.ImageObfuscatory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan("com.b2s.apple.config")
public class ProductServiceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceConfig.class);

    @Bean("coreDetailRequestToProductDetailRequestTransformer")
    public MultiProductDetailToProductDetail coreDetailRequestToProductDetailRequestTransformer() {
        return new MultiProductDetailToProductDetail();
    }

    @Bean("productDetailsServiceToCoreProductDetailTransformer")
    public MultiProductDetailToCoreProductDetail productDetailsServiceToCoreProductDetailTransformer(
        @Autowired @Qualifier("supplierProductMapping") final Map<String, String> supplierProductMapping,
        @Autowired @Qualifier("imageObfuscatory") final ImageObfuscatory imageObfuscatory,
        @Autowired @Qualifier("legacyMerchantCodeMappings") final Map<String, String> legacyMerchantCodeMappings) {
        return new MultiProductDetailToCoreProductDetail(supplierProductMapping, imageObfuscatory, legacyMerchantCodeMappings);
    }

    @Bean("detailTransformersHolder")
    public TransformersHolder detailTransformersHolder(
        @Autowired @Qualifier("coreDetailRequestToProductDetailRequestTransformer")
        final MultiProductDetailToProductDetail requestTransformer,
        @Autowired @Qualifier("productDetailsServiceToCoreProductDetailTransformer")
        final MultiProductDetailToCoreProductDetail responseTransformer) {
        return new TransformersHolder(requestTransformer, responseTransformer);
    }

    @Bean("coreCategoryToCategoryTaxonomy")
    public CoreCategoryToCategoryTaxonomy coreCategoryToCategoryTaxonomy() {
        return new CoreCategoryToCategoryTaxonomy();
    }

    @Bean("categoryNodeToCategoryInfo")
    public CategoryNodeToCategoryInfo categoryNodeToCategoryInfo() {
        return new CategoryNodeToCategoryInfo();
    }

    @Bean("categoryTransformersHolder")
    public TransformersHolder categoryTransformersHolder(
        @Autowired @Qualifier("coreCategoryToCategoryTaxonomy") final CoreCategoryToCategoryTaxonomy requestTransformer,
        @Autowired @Qualifier("categoryNodeToCategoryInfo") final CategoryNodeToCategoryInfo responseTransformer) {
        return new TransformersHolder(requestTransformer, responseTransformer);
    }

    @Bean("productServiceFactoryWrapper")
    public ProductServiceFactoryWrapper productServiceFactoryWrapper(@Value("${PS3_HTTP_URL}") final String url,
        @Value("${PS3_SERVICE_TIMEOUT}") final Integer timeOut,
        @Value("${PS3_CONNECTION_POOL_SIZE}") final Integer poolSize) {
        ProductServiceFactoryWrapper productServiceFactoryWrapper = null;
        try {
            productServiceFactoryWrapper = new ProductServiceFactoryWrapper(url, timeOut, poolSize);
        } catch (ServiceException se) {
            LOGGER.error("ServiceException occurred in productServiceFactoryWrapper. Error Message: ",se);
        }
        return productServiceFactoryWrapper;
    }

    @Bean("productServiceV3Service")
    public ProductServiceV3 productServiceV3Service(
        @Autowired @Qualifier("productServiceFactoryWrapper")
        final ProductServiceFactoryWrapper productServiceFactoryWrapper,
        @Autowired @Qualifier("detailTransformersHolder") final TransformersHolder detailTransformersHolder) {
        ProductServiceV3 productServiceV3Service = null;
        try {
            productServiceV3Service = new ProductServiceV3(productServiceFactoryWrapper, detailTransformersHolder);
        } catch (ServiceException se) {
            LOGGER.error("ServiceException occurred in productServiceV3Service. Error Message: ",se);
        }
        return productServiceV3Service;
    }

    @Bean("categoryServiceV3Service")
    public CategoryServiceV3 categoryServiceV3Service(
        @Autowired @Qualifier("productServiceFactoryWrapper")
        final ProductServiceFactoryWrapper productServiceFactoryWrapper,
        @Autowired @Qualifier("categoryTransformersHolder") final TransformersHolder categoryTransformersHolder) {
        CategoryServiceV3 categoryServiceV3Service = null;
        try {
            categoryServiceV3Service = new CategoryServiceV3(productServiceFactoryWrapper, categoryTransformersHolder);
        } catch (ServiceException se) {
            LOGGER.error("ServiceException occurred in categoryServiceV3Service. Error Message: ",se);
        }
        return categoryServiceV3Service;
    }

    @Bean("imageObfuscatory")
    public ImageObfuscatory imageObfuscatory(
        @Autowired @Qualifier("legacyMerchantCodeMappings") final Map<String, String> legacyMerchantCodeMappings,
        @Value("${imageProxyServerUrl}") final String imageProxyUrl) {
        return new ImageObfuscatory(legacyMerchantCodeMappings, imageProxyUrl);
    }
}
