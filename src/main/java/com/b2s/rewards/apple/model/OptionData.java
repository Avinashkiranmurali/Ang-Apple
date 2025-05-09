package com.b2s.rewards.apple.model;

/**
 * Options with  name value pairs for any product or any category.
 */
public class OptionData {
    /**
     * Name of the Option the value
     */
    private String imageUrl;
    private String description;

    // Empty constructor is required for JSON parsing
    public OptionData() {}

    public OptionData(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
