package com.b2s.apple.mapper;

import com.b2s.apple.entity.RecentlyViewedProductsEntity;
import com.b2s.rewards.apple.model.RecentlyViewedProduct;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by ssundaramoorthy on 8/30/2021.
 */
@Component
public class RecentlyViewedProductsMapper {

    /**
     * Get Recently Viewed Products based on Entities
     *
     * @param productsEntities
     * @return
     */
    public List<RecentlyViewedProduct> getProducts(final List<RecentlyViewedProductsEntity> productsEntities) {
        if (CollectionUtils.isNotEmpty(productsEntities)) {
            return productsEntities
                .stream()
                .filter(Objects::nonNull)
                .map(this::getProduct)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get Recently Viewed Product based on Entity
     *
     * @param productsEntity
     * @return
     */
    private RecentlyViewedProduct getProduct(final RecentlyViewedProductsEntity productsEntity) {
        return RecentlyViewedProduct.builder()
            .withProductId(productsEntity.getProductId())
            .withProgramId(productsEntity.getProgramId())
            .withUserId(productsEntity.getUserId())
            .withVarId(productsEntity.getVarId())
            .withViewedDateTime(productsEntity.getViewedDateTime())
            .build();
    }
}