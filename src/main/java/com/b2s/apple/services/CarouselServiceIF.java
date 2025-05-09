package com.b2s.apple.services;

import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.Program;
import com.b2s.shop.common.User;

import java.util.List;

public interface CarouselServiceIF {

    List<Product> getCarouselProducts(final User user, final Program program, final Integer configMaxProductCount);
}
