package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.ProductCarousalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProductCarouselImageDao extends JpaRepository<ProductCarousalImage, Long> {

    List<ProductCarousalImage> findByGroupNameAndIsActiveTrueOrderByImageOrderAsc(final String groupName);

    default List<ProductCarousalImage> getProductCarouselImageUrls(final String groupName) {
        return findByGroupNameAndIsActiveTrueOrderByImageOrderAsc(groupName);
    }
}
