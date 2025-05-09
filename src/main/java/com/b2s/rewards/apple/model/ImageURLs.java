package com.b2s.rewards.apple.model;

import com.b2s.rewards.model.ProductImage;
import com.b2s.service.product.common.domain.response.ProductImageUrls;

import java.io.Serializable;

/**
 * Created by rperumal on 2/19/2015.
 */
public class ImageURLs implements Serializable {

    private static final long serialVersionUID = 4034699324950648249L;
    private String thumbnail;
    private String small;
    private String medium;
    private String large;

    public ImageURLs() {
    }

    public ImageURLs(String thumbnail, String small, String medium, String large) {
        this.thumbnail = thumbnail;
        this.small = small;
        this.medium = medium;
        this.large = large;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public static ImageURLs transform(ProductImage productImage){
        ImageURLs imageURLs = new ImageURLs();
        imageURLs.setSmall(productImage.getSmallImageURL());
        imageURLs.setMedium(productImage.getMediumImageURL());
        imageURLs.setLarge(productImage.getLargeImageURL());
        imageURLs.setThumbnail(productImage.getThumbnailImageURL());
        return imageURLs;
    }

    /**
     * For Browse Products
     * @param productImage
     * @return  Apple ImageURLs
     */
    public static ImageURLs transformProductImages(ProductImageUrls productImage){
        ImageURLs imageURLs = new ImageURLs();
        imageURLs.setSmall( productImage.getSmall().orElse(""));
        imageURLs.setMedium(productImage.getMedium().orElse(""));
        imageURLs.setLarge(productImage.getLarge().orElse(""));
        imageURLs.setThumbnail(productImage.getThumbnail().orElse(""));

        return imageURLs;
    }



}
