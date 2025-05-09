package com.b2s.rewards.apple.model;

import java.util.List;

/**
 * Created by rpillai on 8/27/2015.
 */
public class CategoryRefreshResponse {

    private List<CategoryChangeData> categoryChanges;

    private boolean isSuccessFul;

    public CategoryRefreshResponse () {}

    public CategoryRefreshResponse(List<CategoryChangeData> categoryChanges, boolean isSuccessFul) {
        this.categoryChanges = categoryChanges;
        this.isSuccessFul = isSuccessFul;
    }

    public List<CategoryChangeData> getCategoryChanges() {
        return categoryChanges;
    }

    public void setCategoryChanges(List<CategoryChangeData> categoryChanges) {
        this.categoryChanges = categoryChanges;
    }

    public boolean isSuccessFul() {
        return isSuccessFul;
    }

    public void setIsSuccessFul(boolean isSuccessFul) {
        this.isSuccessFul = isSuccessFul;
    }
}
