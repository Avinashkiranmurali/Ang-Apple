package com.b2s.apple.services;

import com.b2s.apple.entity.RecentlyViewedProductsEntity;
import com.b2s.apple.mapper.RecentlyViewedProductsMapper;
import com.b2s.apple.model.CarouselConfig;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.RecentlyViewedProductsDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.b2s.rewards.common.util.CommonConstants.CarouselType;

/**
 * Created by ssundaramoorthy on 08/30/2021.
 */
@Service
public class RecentlyViewedProductsService implements CarouselServiceIF{

    private static final Logger logger = LoggerFactory.getLogger(RecentlyViewedProductsService.class);

    @Autowired
    private RecentlyViewedProductsDao recentlyViewedProductsDao;

    @Autowired
    private RecentlyViewedProductsMapper recentlyViewedProductsMapper;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    @Qualifier("productServiceV3Service")
    private ProductServiceV3 productServiceV3;

    /**
     * Retrieves Recently viewed products configuration for the program
     *
     * @param program
     * @return
     */
    private Optional<CarouselConfig> getRecentlyViewedProductsConfig(final Program program) {
        if (MapUtils.isNotEmpty(program.getCarouselConfig())) {
            return program.getCarouselConfig().values()
                .parallelStream()
                .filter(carouselTypeConfigMap -> carouselTypeConfigMap.containsKey(CarouselType.RECENTLY_VIEWED))
                .map(carouselTypeConfigMap -> carouselTypeConfigMap.get(CarouselType.RECENTLY_VIEWED))
                .findAny();
        }
        return Optional.empty();
    }

    /**
     * Get Recently Viewed Products PSIDs
     *
     * @param user
     * @return
     */
    private List<String> getProductPsids(final User user) {
        return recentlyViewedProductsDao.getProducts(user.getVarId(), user.getProgramId(), user.getUserId())
            .stream()
            .map(RecentlyViewedProductsEntity::getProductId)
            .collect(Collectors.toList());
    }

    /**
     * Get Recently Viewed Products
     *
     * @param user
     * @return
     */
    public List<RecentlyViewedProduct> getProducts(final User user) {
        List<RecentlyViewedProduct> productList = new ArrayList<>();
        try {
            productList = recentlyViewedProductsMapper.getProducts(
                recentlyViewedProductsDao.getProducts(user.getVarId(), user.getProgramId(), user.getUserId()));
        } catch (final RuntimeException ex) {
            logger.error("Failed to get recently viewed products for the user: {} ", user.getUserId(), ex);
        }
        return productList;
    }

    /**
     * Get Recently Viewed Carousel Products
     *
     * @param user
     * @param program
     * @param maxProductCount
     * @return
     */
    @Override
    public List<Product> getCarouselProducts(final User user, final Program program, final Integer maxProductCount) {
        final List<String> sortedProductIds = getProductPsids(user);
        if (CollectionUtils.isNotEmpty(sortedProductIds)) {
            logger.info("getRecentlyViewedProducts() -> PS call started");
            final List<Product> products =
                productServiceV3.getAppleMultiProductDetail(sortedProductIds, program, false, user, false, false);
            //Sorting the resulting products based on Recently viewed timestamp
            if (CollectionUtils.isNotEmpty(products)) {
                logger.info("getRecentlyViewedProducts() -> PS call completed");
                return sortedProductIds.stream()
                    .map(productId -> products.parallelStream()
                        .filter(product -> product.getPsid().equalsIgnoreCase(productId) && product.isAvailable())
                        .findAny()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    /**
     * Logic to update Recently Viewed Products table
     * Inserts new record, if product not already exist in the table
     * Updates viewed date time column if product already exist
     * If Max count reached, deletes older records and then inserts new record
     *
     * @param user
     * @param productId
     * @throws ServiceException
     */
    public void updateProducts(final User user, final Program program, final String productId)
        throws ServiceException {
        try {
            final Optional<CarouselConfig> carouselConfig = getRecentlyViewedProductsConfig(program);
            if (carouselConfig.isPresent()) {
                logger.info("Recently viewed configuration enabled");
                final List<RecentlyViewedProduct> recentlyViewedProductsList = getProducts(user);

                final Optional<RecentlyViewedProduct> currentlyViewingProduct = recentlyViewedProductsList.stream()
                    .filter(product -> product.getProductId().equalsIgnoreCase(productId))
                    .findAny();

                if (currentlyViewingProduct.isPresent()) {
                    recentlyViewedProductsDao
                        .updateProductWithCurrentTime(user.getVarId(), user.getProgramId(), user.getUserId(),
                            productId);
                } else {
                    final int maxCount = AppleUtil.getMinCount(carouselConfig.get().getMaxProductCount(),
                        applicationProperties.getProperty(CommonConstants.CAROUSEL_RECENTLY_VIEWED_MAX_COUNT));

                    if (recentlyViewedProductsList.size() >= maxCount) {
                        deleteRecentlyViewedProducts(user, recentlyViewedProductsList, maxCount);
                    }
                    recentlyViewedProductsDao.insert(createEntity(user, productId));
                }
                logger.info("Updated Recently viewed product {}", productId);
            }
        } catch (RuntimeException ex) {
            logger.error("Failed to load the recently viewed products for the user: {}", user.getUserId());
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    /**
     * Deletes older records based on max count. If the total products count in DB is greater than Max count, deletes
     * the exceeded products based on last viewed timestamp
     *
     * @param user
     * @param recentlyViewedProductsList
     * @param maxCount
     */
    private void deleteRecentlyViewedProducts(final User user, List<RecentlyViewedProduct> recentlyViewedProductsList,
        final int maxCount) {
        final List<String> productIdsToDelete = recentlyViewedProductsList.stream()
            .skip(maxCount - 1)
            .map(RecentlyViewedProduct::getProductId)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(productIdsToDelete)) {
            final int deletedCount = recentlyViewedProductsDao
                .deleteProducts(user.getVarId(), user.getProgramId(), user.getUserId(), productIdsToDelete);
            if (deletedCount > 1) {
                logger.info("Recently Viewed Products Count in DB: {}, is greater than MAX count {}. So deleted {}.",
                    recentlyViewedProductsList.size(), maxCount, deletedCount);
            }
        }
    }

    private RecentlyViewedProductsEntity createEntity(final User user, final String productId) {
        final RecentlyViewedProductsEntity productsEntity = new RecentlyViewedProductsEntity();
        productsEntity.setUserId(user.getUserId());
        productsEntity.setProductId(productId);
        productsEntity.setProgramId(user.getProgramId());
        productsEntity.setVarId(user.getVarId());
        productsEntity.setViewedDateTime(new Date());
        return productsEntity;
    }
}