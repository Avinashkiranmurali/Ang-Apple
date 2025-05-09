package com.b2s.apple.services

import com.b2s.rewards.apple.model.ProductAttributeConfiguration
import spock.lang.Specification

class CategoryConfigurationServiceSpec extends Specification{

    def "test ShowFacet values case"() {
        setup:
        CategoryConfigurationService categoryConfigurationService = new CategoryConfigurationService()
        def productAttributes = getProductAttributeConfigurations()

        boolean showFacet = categoryConfigurationService.showFacet(optionName, optionValue, isSearchPage, productAttributes);

       expect:
       showFacet == display

        where:
        optionName      | optionValue      | isSearchPage || display
        "color"         | "space_gray"     | true         || true
        "color"         | "space_gray"     | false        || false
        "color"         | "test"           | true         || true
        "color"         | "test"           | false        || true

        "storage"       | "128gb"          | true         || true
        "storage"       | "128gb"          | false        || false
        "storage"       | "test"           | true         || true
        "storage"       | "test"           | false        || false

        "processor"     | "test"           | true         || false
        "processor"     | "test"           | false        || true
        "memory"        | "test"           | true         || false
        "memory"        | "test"           | false        || false
        "graphics"      | "test"           | true         || true
        "graphics"      | "test"           | false        || true
        "communication" | "test"           | true         || true
        "communication" | "test"           | false        || true

    }

    private Set<ProductAttributeConfiguration> getProductAttributeConfigurations() {
        Set<ProductAttributeConfiguration> productAttributes = new HashSet<>();
        //productAttributes.add(getProductAttributeConfiguration("options", "color", true, true));
        productAttributes.add(getProductAttributeConfiguration("options", "color_space_gray", true, false));
        productAttributes.add(getProductAttributeConfiguration("options", "storage", true, false));
        productAttributes.add(getProductAttributeConfiguration("options", "processor", false, true));
        productAttributes.add(getProductAttributeConfiguration("options", "memory", false, false));
        productAttributes.add(getProductAttributeConfiguration("options", "graphics", true, true));
        return productAttributes;
    }

    private ProductAttributeConfiguration getProductAttributeConfiguration(String attributeType, String attributeName,
                                                                           boolean availableForSearch, boolean availableForDetail) {
        ProductAttributeConfiguration productAttributeConfiguration = new ProductAttributeConfiguration();
        productAttributeConfiguration.setAttributeType(attributeType);
        productAttributeConfiguration.setAttributeName(attributeName);
        productAttributeConfiguration.setAvailableForSearch(availableForSearch);
        productAttributeConfiguration.setAvailableForDetail(availableForDetail);
        return productAttributeConfiguration;
    }
}
