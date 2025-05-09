package com.b2s.rewards.apple.model;

import java.util.List;

/**
 * Created by rpillai on 8/25/2015.
 */
public class CategoryChangeData {

    private String categoryName;
    private Reason reason;
    private List<OptionChangeData> optionChanges;

    private boolean isCategoryDisabled = true;

    public CategoryChangeData() {}

    public CategoryChangeData(String categoryName, Reason reason, List<OptionChangeData> optionChanges) {
        this.categoryName = categoryName;
        this.reason = reason;
        this.optionChanges = optionChanges;
    }

    public CategoryChangeData(String categoryName, Reason reason) {
        this.categoryName = categoryName;
        this.reason = reason;
    }


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public List<OptionChangeData> getOptionChanges() {
        return optionChanges;
    }

    public void setOptionChanges(List<OptionChangeData> optionChanges) {
        this.optionChanges = optionChanges;
    }

    public boolean isCategoryDisabled() {
        return isCategoryDisabled;
    }

    public void setIsCategoryDisabled(boolean isCategoryDisabled) {
        this.isCategoryDisabled = isCategoryDisabled;
    }

    @Override
    public String toString() {
        return "CategoryChangeData{" +
                "categoryName='" + categoryName + '\'' +
                ", reason='" + reason + '\'' +
                ", optionChanges=" + optionChanges +
                ", isCategoryDisabled=" + isCategoryDisabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryChangeData that = (CategoryChangeData) o;

        if (!categoryName.equals(that.categoryName)) return false;
        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        return !(optionChanges != null ? !optionChanges.equals(that.optionChanges) : that.optionChanges != null);

    }

    @Override
    public int hashCode() {
        int result = categoryName.hashCode();
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (optionChanges != null ? optionChanges.hashCode() : 0);
        return result;
    }

    public enum Reason {
        NEW_CATEGORY(4001, "New Category"),
        REMOVED_CATEGORY(4002, "Removed Category"),
        OPTION_CHANGES(4003, "Options Changed"),
        LEAF_NODE_CATEGORY_WITHOUT_PRODUCTS(4004, "Leaf node category without any products");

        Integer reasonCode;
        String reasonDesc;
        private Reason(Integer reasonCode, String reasonDesc) {
            this.reasonCode = reasonCode;
            this.reasonDesc = reasonDesc;
        }

        public String getReasonDesc() {
            return reasonDesc;
        }

        public Integer getReasonCode() {
            return reasonCode;
        }

    }
}
