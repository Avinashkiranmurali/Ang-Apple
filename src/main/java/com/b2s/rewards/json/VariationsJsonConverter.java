package com.b2s.rewards.json;

import com.b2s.common.json.JsonConverter;
import com.b2s.rewards.model.Product;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts a list of product variations to its json representation.
 * @author dmontoya
 * @version 1.0, 1/31/13 1:03 PM
 * @since b2r-rewardstep 5.3
 */
public final class VariationsJsonConverter implements JsonConverter<List<Product>> {

    @Override
    public String toJson(List<Product> variations) {
        if (variations == null || variations.isEmpty())
            return null;

        List<JSONifiedVariation> jsonifiedVariations = new ArrayList<JSONifiedVariation>();
        JSONifiedVariation jsonVar;
        for (Product variation : variations) {
            if (variation == null || !variation.getDefaultOffer().getAvailable()) continue;
            jsonVar = new JSONifiedVariation();
            jsonVar.productId = variation.getProductId();
            jsonVar.variationAttributes = variation.getVariationDimensionNameValues();
            jsonVar.thumbImageUrl = variation.getDefaultProductImage().getMediumImageURL();
            jsonVar.isAvailable = variation.getDefaultOffer().getAvailable().toString();
            jsonifiedVariations.add(jsonVar);
        }
        Gson jsonifier = new Gson();
        return jsonifier.toJson(jsonifiedVariations);
    }

    private static class JSONifiedVariation {
        public String productId;
        public Map<String, String> variationAttributes;
        public String thumbImageUrl;
        public String isAvailable;
    }
}