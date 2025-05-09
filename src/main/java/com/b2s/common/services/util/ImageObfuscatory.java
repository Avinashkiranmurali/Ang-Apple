package com.b2s.common.services.util;

import com.b2s.imageproxy.ImageRequest;
import com.b2s.imageproxy.ImageSize;
import com.b2s.rewards.apple.model.ImageURLs;
import com.b2s.rewards.model.Merchant;
import com.b2s.rewards.model.ProductImage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This class is used to obfuscate image URLS provided by product services layer,
 * and point the URL to image proxy server.
 *
 @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
 *
 */
public class ImageObfuscatory {


    private static final Logger logger = LoggerFactory.getLogger(ImageObfuscatory.class);
    private static final String URL_EMPTY_SPACE_ENCODING = "%20";
    public static final String S_WID_D_HEI_D = "%s?wid=%d&hei=%d";
    public static final String S_WID_D_HEI_D1 = "%s&wid=%d&hei=%d";

    private final Map<String, String> legacyMerchantMapper;
    private final Pattern imageProxyUrlPattern;
    private final Pattern backwardSlashPattern;
    private final Pattern spacePattern;
    private final Pattern lastIndexOfForwardSlashPattern;

    /**
     * Constructs a legacy merchant mapper and image proxy URL pattern.
     * Legacy merchant mapper is used to support backward compatibility for merchant codes.
     * Image proxy pattern is used to identify whether URL is eligible for obfuscation (or) not.
     * @param legacyMerchantMapper name of the property file.
     * @param legacyMerchantMapper name of the property file.
     * @throws IllegalArgumentException if unable to locate the resource
     */

    public ImageObfuscatory(final Map<String, String> legacyMerchantMapper,
                             final String imageProxyUrl){
        if(!Optional.ofNullable(legacyMerchantMapper).isPresent() || legacyMerchantMapper.isEmpty()){
            throw new IllegalArgumentException("Legacy merchant mapper is missing");
        }
        if(!Optional.ofNullable(imageProxyUrl).isPresent() || imageProxyUrl.isEmpty()){
            throw new IllegalArgumentException("Image proxy URL is missing");
        }
        this.legacyMerchantMapper = legacyMerchantMapper;
        this.imageProxyUrlPattern = Pattern.compile("(^("+imageProxyUrl+"){1})");
        this.backwardSlashPattern = Pattern.compile("\\+");
        this.spacePattern = Pattern.compile("( )+");
        this.lastIndexOfForwardSlashPattern = Pattern.compile("((.*)(/))(.*)");
    }

    /**
     * If the merchant code is found in legacy mapping, then convert it to new one.
     * If the merchant code is of <ul>BEST BUY</ul> and pricing category code is either <ul>Movies</ul> (or) <ul>Music</ul> then
     * large image URL is set as medium URL. For other merchants, medium URL is not altered.
     * Image proxy pattern is used to identify whether URL is eligible for obfuscation (or) not.
     * @param merchantId name of the property file.
     * @param pricingCategoryCode name of the property file.
     * @return false when large URL is needed for medium otherwise true.
     */

    private boolean isMerchantAndPriceCategoryNeedLargeUrlForMedium(final String merchantId, final String pricingCategoryCode){
        final String convertedMerchantCode = Optional.ofNullable(legacyMerchantMapper.get(merchantId)).isPresent() ?
                                                    legacyMerchantMapper.get(merchantId) :  merchantId;
        if (Integer.toString(Merchant.MerchantId.BESTBUY.getMerchantId()).equals(convertedMerchantCode) &&
            (Optional.ofNullable(pricingCategoryCode).isPresent() && ("Movies".equals(pricingCategoryCode) ||
                "Music".equals(pricingCategoryCode)))) {
            return false;
        }

        return true;
    }

    /**
     * If the Large URL can be obfuscated then does URL encoding on large image URL.
     * Sets <code>ProductImage</code> with obfuscated large image URL.
     * Uses merchant code and pricingCategory code  to find the whether medium should size URL should point to large
     * image URL (or) not.
     * If medium URL can be obfuscated, sets <code>ProductImage</code> with obfuscated medium image URL based on large image URL.
     * If small URL can be obfuscated, sets <code>ProductImage</code> with obfuscated small image URL based on large image URL.
     * If thumbnail  URL can be obfuscated, sets <code>ProductImage</code> with obfuscated small image URL based on large image URL.
     * Set <code>ProductImage</code> with obfuscated thumbnail URL based on large image URL.
     * @param productImage holds URLs for large, medium, small and thumbnail images of search product.
     * @param merchantId merchant code of the product.
     * @param pricingCategoryCode category code associated to product.
     */

    public void obfuscateImageUrls(final ProductImage productImage, final String merchantId, final String pricingCategoryCode) {
        if (Optional.ofNullable(productImage).isPresent()) {
            final String noiseFreeLargeImageUrl = removeNoiseFromLargeUrl(productImage .getLargeImageURL());
            //for Large URL
            productImage.setLargeImageURL(findObfuscatedUrlForLargeUrl(noiseFreeLargeImageUrl));
            //For Medium URL
            final ImageSize imageSizeMedium = isMerchantAndPriceCategoryNeedLargeUrlForMedium(merchantId, pricingCategoryCode) ?
                                               ImageSize.Medium : ImageSize.Original;
            productImage.setMediumImageURL(findObfuscatedUrlForCurrentUrlBasedOnLargeUrl(
                    noiseFreeLargeImageUrl,imageSizeMedium ,productImage.getMediumImageURL()));
            //for small URL
            productImage.setSmallImageURL(findObfuscatedUrlForCurrentUrlBasedOnLargeUrl(
                    noiseFreeLargeImageUrl,ImageSize.Small ,productImage.getSmallImageURL()));
            //For Thumbnail
            productImage.setThumbnailImageURL(findObfuscatedUrlForCurrentUrlBasedOnLargeUrl(
                    noiseFreeLargeImageUrl,ImageSize.Thumbnail ,productImage.getThumbnailImageURL()));
        }
    }
    /**
     * If the Large URL can be obfuscated then does URL encoding on large image URL.
     * Sets <code>ProductImage</code> with obfuscated large image URL.
     * Uses merchant code and pricingCategory code  to find the whether medium should size URL should point to large
     * image URL (or) not.
     * If medium URL can be obfuscated, sets <code>ProductImage</code> with obfuscated medium image URL based on large image URL.
     * If small URL can be obfuscated, sets <code>ProductImage</code> with obfuscated small image URL based on large image URL.
     * If thumbnail  URL can be obfuscated, sets <code>ProductImage</code> with obfuscated small image URL based on large image URL.
     * Set <code>ProductImage</code> with obfuscated thumbnail URL based on large image URL.
     * @param productImage holds URLs for large, medium, small and thumbnail images of search product.
     */

    public void resizeImageUrls(final ProductImage productImage) {
        if (Optional.ofNullable(productImage).isPresent()) {
            final String noiseFreeLargeImageUrl = removeNoiseFromLargeUrl(productImage .getLargeImageURL());
            productImage.setLargeImageURL(noiseFreeLargeImageUrl);
            productImage.setMediumImageURL(
                String.format(S_WID_D_HEI_D,
                    noiseFreeLargeImageUrl,
                    ImageSize.Medium.getWidth(),
                    ImageSize.Medium.getHeight()));
            productImage.setSmallImageURL(
                String.format(S_WID_D_HEI_D,
                    noiseFreeLargeImageUrl,
                    ImageSize.Small.getWidth(),
                    ImageSize.Small.getHeight()));
            productImage.setThumbnailImageURL(
                String.format(S_WID_D_HEI_D,
                    noiseFreeLargeImageUrl,
                    ImageSize.Thumbnail.getWidth(),
                    ImageSize.Thumbnail.getHeight()));
        }
    }


    /**
     * If the Large URL can be obfuscated then does URL encoding on large image URL.
     * Sets <code>ProductImage</code> with obfuscated large image URL.
     * Uses merchant code and pricingCategory code  to find the whether medium should size URL should point to large
     * image URL (or) not.
     * If medium URL can be obfuscated, sets <code>ImageURLs</code> with obfuscated medium image URL based on large image URL.
     * If small URL can be obfuscated, sets <code>ImageURLs</code> with obfuscated small image URL based on large image URL.
     * If thumbnail  URL can be obfuscated, sets <code>ImageURLs</code> with obfuscated small image URL based on large image URL.
     * Set <code>ProductImage</code> with obfuscated thumbnail URL based on large image URL.
     * @param imageUrl holds URLs for large, medium, small and thumbnail images of search product.
     */

    public ImageURLs resizeImageUrls(final ImageURLs imageUrl) {
        if (Optional.ofNullable(imageUrl).isPresent()) {
            imageUrl.setLarge(removeNoiseFromLargeUrl(imageUrl.getLarge()));
            resizeMediumImage(imageUrl);
            resizeSmallImage(imageUrl);
            resizeThumbnailImage(imageUrl);

        }
        return imageUrl;
    }

    private void resizeThumbnailImage(final ImageURLs imageUrl) {
        String thumbnailImage = removeNoiseFromLargeUrl(imageUrl.getThumbnail());
        if(StringUtils.isNotBlank(thumbnailImage) && (thumbnailImage.contains("wid=") || thumbnailImage.contains("hei="))) {
            imageUrl.setThumbnail(thumbnailImage + "&wid=" + ImageSize.Thumbnail.getWidth() + "&hei=" + ImageSize.Thumbnail.getHeight());
        } else {
            imageUrl.setThumbnail(thumbnailImage + "?wid=" + ImageSize.Thumbnail.getWidth() + "&hei=" + ImageSize.Thumbnail.getHeight());
        }
    }

    private void resizeSmallImage(final ImageURLs imageUrl) {
        String smallImage = removeNoiseFromLargeUrl(imageUrl.getSmall());
        if(StringUtils.isNotBlank(smallImage) && (smallImage.contains("wid=") || smallImage.contains("hei="))) {
            imageUrl.setSmall(
                String.format(S_WID_D_HEI_D1,
                    smallImage,
                    ImageSize.Small.getWidth(),
                    ImageSize.Small.getHeight()));
        } else {
            imageUrl.setSmall(
                String.format(S_WID_D_HEI_D,
                    smallImage,
                    ImageSize.Small.getWidth(),
                    ImageSize.Small.getHeight()));
        }
    }

    private void resizeMediumImage(final ImageURLs imageUrl) {
        String mediumImage = removeNoiseFromLargeUrl(imageUrl.getMedium());
        if(StringUtils.isNotBlank(mediumImage) && (mediumImage.contains("wid=") || mediumImage.contains("hei="))) {
            imageUrl.setMedium(
                String.format(S_WID_D_HEI_D1,
                    mediumImage,
                    ImageSize.Medium.getWidth(),
                    ImageSize.Medium.getHeight()));
        } else {
            imageUrl.setMedium(
                String.format(S_WID_D_HEI_D,
                    mediumImage,
                    ImageSize.Medium.getWidth(),
                    ImageSize.Medium.getHeight()));
        }
    }

    /**
     * If the large URL can be obfuscated then, take part of the URL after context path, encode spaces and backslash
     * with URL encoding characters. otherwise returns same as input.
     * @param largeImageUrl large image URL.
     * @return String encoded large image URL.
     */
    private String removeNoiseFromLargeUrl(final String largeImageUrl){
        if (canThisUrlBeObfuscated(largeImageUrl)) {
            final Matcher lastIndexMatcher = lastIndexOfForwardSlashPattern.matcher(largeImageUrl);
            if(lastIndexMatcher.find()){
                final String contentBeforeLastForwardSlash =  lastIndexMatcher.group(1);
                String contentAfterLastForwardSlash = lastIndexMatcher.group(4);
                contentAfterLastForwardSlash =  this.backwardSlashPattern.matcher(contentAfterLastForwardSlash).replaceAll(URL_EMPTY_SPACE_ENCODING);
                contentAfterLastForwardSlash =  this.spacePattern.matcher(contentAfterLastForwardSlash).replaceAll(URL_EMPTY_SPACE_ENCODING);
                return  contentBeforeLastForwardSlash + contentAfterLastForwardSlash;
            }
        }
        return  largeImageUrl;
    }

    /**
     * If the large URL can be obfuscated then provide obfuscated URL, otherwise returns same as input.
     * @param largeImageUrl large image URL.
     * @return String Obfuscated URL.
     */

    private String findObfuscatedUrlForLargeUrl(final String largeImageUrl){
        if (canThisUrlBeObfuscated(largeImageUrl)) {
            try {
                return new ImageRequest(largeImageUrl). asProxiedUrl();
            } catch (Exception ex) {
                logger.debug("Cannot proxy large image URL");
            }
        }
        return largeImageUrl;
    }

    /**
     * If the URL can be obfuscated then use large image URL and provide URL based on image size, otherwise returns
     * same as input.
     * @param largeImageUrl large image URL.
     * @param imageSize size of the image.
     * @param currentUrl obfuscation URL.
     * @return String Obfuscated URL.
     */

    private String findObfuscatedUrlForCurrentUrlBasedOnLargeUrl(final String largeImageUrl,final ImageSize imageSize,
                                                                 final String currentUrl){
        if (canThisUrlBeObfuscated(currentUrl)) {
            try {
               return new ImageRequest(largeImageUrl, imageSize).asProxiedUrl();
            } catch (Exception ex) {
                logger.debug("Cannot proxy large image URL to {}",imageSize.name());
            }
        }
        return currentUrl;
    }

    /**
     * If url input is null (or) less than seven characters (or) pointing to image proxy server
     * then not eligible for obfuscation. For rest of the cases it is eligible for obfuscation.
     * @param url image URL.
     * @return true when image URL can be obfuscated otherwise false.
     */

    private boolean canThisUrlBeObfuscated(final String url) {
        if(!Optional.ofNullable(url).isPresent() || url.isEmpty() || url.trim().length() <7){
            return false;
        }
        final Matcher patternMatcher = this.imageProxyUrlPattern.matcher(url.trim());
        if(patternMatcher.find(0)){
            return false;
        }
        return true;
    }

}
