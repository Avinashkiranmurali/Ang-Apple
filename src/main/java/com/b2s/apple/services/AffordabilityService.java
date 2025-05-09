package com.b2s.apple.services;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.ProductResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class AffordabilityService implements CarouselServiceIF {

    private static final Logger logger = LoggerFactory.getLogger(AffordabilityService.class);

    @Autowired
    private Properties applicationProperties;

    @Autowired
    @Qualifier("productServiceV3Service")
    private ProductServiceV3 productServiceV3;

    /**
     * Get Affordable Carousel Products
     *
     * @param user
     * @param program
     * @param configMaxProdCount
     * @return
     */
    @Override
    public List<Product> getCarouselProducts(final User user, final Program program, final Integer configMaxProdCount) {
        try {
            final Integer[] pointsRange = {0, user.getBalance()};
            final int maxCount = AppleUtil.getMinCount(configMaxProdCount,
                applicationProperties.getProperty(CommonConstants.CAROUSEL_AFFORDABILITY_MAX_COUNT));

            ProductSearchRequest.Builder searchRequestBuilder =
                productServiceV3.getAffordableProductSearchRequestBuilder(
                    user.getLocale(),
                    pointsRange,
                    maxCount,
                    0,
                    program);

            logger.info("AffordabilityService : Searching for affordable product. balance = {}", user.getBalance());
            ProductResponse products =
                productServiceV3.getProducts(searchRequestBuilder, user.getLocale(), program, user, true);
            return products.getProducts();
        } catch (ServiceException e) {
            logger.error("AffordabilityService : Error calling product search service", e);
            return new ArrayList<>();
        }
    }
}
