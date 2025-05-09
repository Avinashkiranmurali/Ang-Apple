package com.b2s.apple.services;

import org.apache.commons.collections.MapUtils;

import java.util.Map;

import static com.b2s.rewards.common.util.CommonConstants.CarouselType;

public class CarouselHolder {
    private Map<CarouselType, CarouselServiceIF> carouselMap;

    public CarouselServiceIF getCarouselService(final CarouselType carouselType) {
        CarouselServiceIF carouselManager = null;
        if(MapUtils.isNotEmpty(carouselMap)) {
            carouselManager = carouselMap.get(carouselType);
        }
        return carouselManager;
    }

    public Map<CarouselType, CarouselServiceIF> getCarouselMap() {
        return carouselMap;
    }

    public void setCarouselMap(Map<CarouselType, CarouselServiceIF> carouselMap) {
        this.carouselMap = carouselMap;
    }
}
