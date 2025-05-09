package com.b2s.rewards.apple.integration.model;

import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.common.util.CommonConstants;

import java.util.List;
import java.util.Map;

public class CarouselResponse implements Comparable<CarouselResponse>{

    private final String name;
    private final List<Product> products;
    private final Map<String, Object> config;

    private CarouselResponse(final Builder builder){
        this.name = builder.name;
        this.products = builder.products;
        this.config = builder.config;
    }

    public String getName() {
        return name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private String name;
        private List<Product> products;
        private Map<String, Object> config;

        public CarouselResponse build(){
            return new CarouselResponse(this);
        }

        public Builder withName(final String name){
            this.name = name;
            return this;
        }

        public Builder withProducts(final List<Product> products){
            this.products = products;
            return this;
        }

        public Builder withConfig(final Map<String, Object> config){
            this.config = config;
            return this;
        }
    }

    public int compareTo(CarouselResponse next) {
        return CommonConstants.CarouselType.get(this.name).getOrder()
            .compareTo(CommonConstants.CarouselType.get(next.name).getOrder());
    }

    @Override
    public String toString() {
        return "CarouselResponse{" +
            "name='" + name + '\'' +
            ", products=" + products +
            ", config=" + config +
            '}';
    }
}
