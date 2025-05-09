package com.b2s.apple.config;

import com.b2s.apple.services.AffordabilityService;
import com.b2s.apple.services.CarouselHolder;
import com.b2s.apple.services.CarouselServiceIF;
import com.b2s.apple.services.RecentlyViewedProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.b2s.rewards.common.util.CommonConstants.CarouselType;
import static com.b2s.rewards.common.util.CommonConstants.CarouselType.AFFORDABLE_PRODUCT;
import static com.b2s.rewards.common.util.CommonConstants.CarouselType.RECENTLY_VIEWED;

@Configuration
public class CarouselAppConfig {

    @Bean("carouselHolder")
    public CarouselHolder carouselHolder(
            @Autowired final RecentlyViewedProductsService recentlyViewedProductsService,
            @Autowired final AffordabilityService affordabilityService) {
        final CarouselHolder carouselHolder = new CarouselHolder();

        final Map<CarouselType, CarouselServiceIF> carouselMap = new HashMap<>();
        carouselMap.put(RECENTLY_VIEWED, recentlyViewedProductsService);
        carouselMap.put(AFFORDABLE_PRODUCT, affordabilityService);
        carouselHolder.setCarouselMap(carouselMap);

        return carouselHolder;
    }
}
