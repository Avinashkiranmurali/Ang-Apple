package com.b2s.rewards.apple.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Banner structure for each category for all landing pages, wherever applicable
 *
 * Created by ssrinivasan on 2/19/2015.
 */
public class Banner {

    private String name;
    private List<BannerCategories> subcat = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BannerCategories> getSubcat() {
        return subcat;
    }

    public void setSubcat(List<BannerCategories> subcat) {
        this.subcat = subcat;
    }

}
