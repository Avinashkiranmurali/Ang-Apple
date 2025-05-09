package com.b2s.apple.mapper;

import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.service.product.common.domain.response.Variation;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.QUALITY_SWATCH_IMAGE_URL;

/**
 * Created by meddy on 7/13/2015.
 */
@Component
public class OptionMapper {
    private static final Logger logger = LoggerFactory.getLogger(OptionMapper.class);

    @Autowired
    private CategoryConfigurationService categoryConfigurationService;

    public List<Option> getOptions(final List<String> categorySlugs,
        ImmutableMap<String, com.b2s.service.product.common.domain.response.Option> documentOptions, Locale locale,
        boolean isFacetForSearchPage, String varId, String programId) {
        List<Option> options = new ArrayList<>();
        if(documentOptions!=null && documentOptions.size()>0) {
            CategoryConfiguration categoryConfiguration = categoryConfigurationService
                .getCategoryConfigurationByCategoryName(categorySlugs.get(0), null);
            Set<ProductAttributeConfiguration> productAttributes = new HashSet<>();
            if (categoryConfiguration != null) {
                productAttributes = categoryConfigurationService.getProductAttributes(categorySlugs.get(0));
            }
            for (Map.Entry<String, com.b2s.service.product.common.domain.response.Option> entry : documentOptions
                .entrySet()) {
                String optionName = entry.getKey();
                com.b2s.service.product.common.domain.response.Option documentValueOption = entry.getValue();

                final String optionValueKey = documentValueOption.getKey().orElse(documentValueOption.getValue());

                if (categoryConfigurationService
                    .showFacet(optionName, optionValueKey, isFacetForSearchPage, productAttributes
                    )) {
                    //add to Option list
                    addOption(options,optionName,documentOptions,documentValueOption.getKey(),documentValueOption
                        .getValue(),documentValueOption.getLabel(),categorySlugs.get(0));
                }
            }
        }
        return options;
    }

    public List<Option> getVariationsOptions(final Variation variation){
        List<Option> variationOptions = new ArrayList<>();

        for(Map.Entry<String, com.b2s.service.product.common.domain.response.Option> mapOption:
            variation.getOptions().entrySet()){
            com.b2s.service.product.common.domain.response.Option option = mapOption.getValue();
            //add to Option list
            addOption(variationOptions, mapOption.getKey(), variation.getOptions(), option.getKey(),
                    option.getValue(), option.getLabel(), variation.getPsid());

        }
        return variationOptions;
    }

    public void addOption(final List<Option> options, final String optionName, ImmutableMap<String,com.b2s.service
        .product.common.domain.response.Option> documentOptions, final Optional<String> documentKey, final String
        documentValue, final String documentLabel, final String slugOrPsid){
        if(!CommonConstants.SWATCH_IMAGE_URL.equalsIgnoreCase(optionName)){
            String swatchImageUrl = null;
            if(CommonConstants.COLOR.equalsIgnoreCase(optionName)) {
                if(documentOptions.containsKey(CommonConstants.SWATCH_IMAGE_URL)){
                    swatchImageUrl = documentOptions.get(CommonConstants.SWATCH_IMAGE_URL).getValue();
                    options.add(createOption(optionName, documentValue, documentKey, documentLabel, swatchImageUrl));
                }else{
                    logger.debug("swatchImageUrl not added for {} with color {}",  slugOrPsid ,documentValue);
                }
            }else{
                options.add(createOption(optionName, documentValue, documentKey, documentLabel, swatchImageUrl));
            }
        }
    }

    public Option createOption(final String optionName, final String documentValue, final Optional<String> documentKey,
        final String documentLabel, final String swatchImageUrl){
        final Option option = new Option(optionName, documentValue, documentKey);
        option.setI18Name(documentLabel); //Document Label refers to i18Name
        option.setSwatchImageUrl(CommonConstants.COLOR.equalsIgnoreCase(optionName) ? swatchImageUrl + QUALITY_SWATCH_IMAGE_URL : swatchImageUrl);
        return option;
    }
}