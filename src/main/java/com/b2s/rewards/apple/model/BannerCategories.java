package com.b2s.rewards.apple.model;

/**
 * Banner display details for a category
 *
 * Created by ssrinivasan on 2/19/2015.
 */
public class BannerCategories  {

    private String name;
    private String imageUrl;
    private String i18nText;
    private String title;
    private String titleImg;
    private String tagLine;  // tooltip
    private String shortDescription;
    private String position;  // possible values: left, right, centre

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getI18nText() {
        return i18nText;
    }

    public void setI18nText(String i18nText) {
        this.i18nText = i18nText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleImg() {
        return titleImg;
    }

    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
