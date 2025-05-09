package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.ProductCarouselImageDao;
import com.b2s.rewards.apple.dao.ProductOptionsConfigDao;
import com.b2s.rewards.apple.model.Option;
import com.b2s.rewards.apple.model.ProductCarousalImage;
import com.b2s.rewards.apple.model.ProductOptionsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCarouselImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCarouselImageService.class);

    @Autowired
    ProductOptionsConfigDao productOptionsConfigDao;

    @Autowired
    ProductCarouselImageDao productCarouselImageDao;

    //@Cacheable(value = CommonConstants.CACHE_CONF, key = "{#locale, #psid}")
    @SuppressWarnings("unchecked")
    public List<String> getImageUrls(final String locale, final String categorySlug, final String psid,
        final List<Option> options) {
        ProductOptionsConfig psidConfig = productOptionsConfigDao.getConfigBasedOnPsid(locale, psid);
        String imageGroupName = null;
        if (Objects.nonNull(psidConfig)) {
            imageGroupName = psidConfig.getGroupName();
        } else {
            imageGroupName = getGroupNameBasedOnCategory(locale, categorySlug, options);
        }
        return getImageUrls(imageGroupName);
    }

    private String getGroupNameBasedOnCategory(final String locale, final String categorySlug,
        final List<Option> options) {
        List<ProductOptionsConfig> categoryConfigs = productOptionsConfigDao.getConfigBasedOnCategoryName(locale,
            categorySlug);

        if (CollectionUtils.isNotEmpty(categoryConfigs)) {
            for (ProductOptionsConfig config : categoryConfigs) {
                Map<String, String> optionMap = new HashMap<>();

                try {
                    optionMap = new ObjectMapper().readValue(config.getOptions(), Map.class);
                } catch (JsonProcessingException e) {
                    LOGGER.warn("Error occurred while reading carousel image options {}:{}.", config.getId(),
                        config.getOptions());
                }

                if (isAllMatching(options, optionMap)) {
                    return config.getGroupName();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param options
     * @param optionMap
     * @return
     */
    private boolean isAllMatching(List<Option> options, Map<String, String> optionMap) {
        boolean allMatching = true;
        if (CollectionUtils.isNotEmpty(options)) {
            //Mapping PS option Name with DB key & PS option key with DB value
            for (Map.Entry<String, String> optionEntry : optionMap.entrySet()) {
                boolean matching = options.stream()
                    .anyMatch(psOption -> psOption.getName().equalsIgnoreCase(optionEntry.getKey())
                        && psOption.getKey().equalsIgnoreCase(optionEntry.getValue()));
                if (!matching) {
                    allMatching = false;
                    break;
                }
            }
        }
        return allMatching;
    }

    private List<String> getImageUrls(final String imageGroupName) {
        List<String> imageUrls = new ArrayList<>();
        if (StringUtils.isNotBlank(imageGroupName)) {
            List<ProductCarousalImage> imageConfigs =
                productCarouselImageDao.getProductCarouselImageUrls(imageGroupName);
            imageUrls = imageConfigs.stream()
                .sorted(Comparator.comparingInt(ProductCarousalImage::getImageOrder))
                .map(ProductCarousalImage::getImageUrl)
                .collect(Collectors.toList());
        }
        return imageUrls;
    }
}
