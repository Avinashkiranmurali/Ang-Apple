package com.b2s.rewards.apple.model;

import java.util.List;

/**
 * Created by rpillai on 8/25/2015.
 */
public class OptionChangeData {

    private String optionName;
    private List<String> newOptionValues;
    private List<String> removedOptionValues;

    public OptionChangeData() {}

    public OptionChangeData(String optionName, List<String> newOptionValues) {
        this.optionName = optionName;
        this.newOptionValues = newOptionValues;
    }

    public OptionChangeData(String optionName, List<String> newOptionValues, List<String> removedOptionValues) {
        this.optionName = optionName;
        this.newOptionValues = newOptionValues;
        this.removedOptionValues = removedOptionValues;
    }

    public List<String> getNewOptionValues() {
        return newOptionValues;
    }

    public void setNewOptionValues(List<String> newOptionValues) {
        this.newOptionValues = newOptionValues;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public List<String> getRemovedOptionValues() {
        return removedOptionValues;
    }

    public void setRemovedOptionValues(List<String> removedOptionValues) {
        this.removedOptionValues = removedOptionValues;
    }

    @Override
    public String toString() {
        return "OptionChangeData{" +
                "optionName='" + optionName + '\'' +
                ", newOptionValues=" + newOptionValues +
                ", removedOptionValues=" + removedOptionValues +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionChangeData that = (OptionChangeData) o;

        if (!optionName.equals(that.optionName)) return false;
        if (newOptionValues != null ? !newOptionValues.equals(that.newOptionValues) : that.newOptionValues != null)
            return false;
        return !(removedOptionValues != null ? !removedOptionValues.equals(that.removedOptionValues) : that.removedOptionValues != null);

    }

    @Override
    public int hashCode() {
        int result = optionName.hashCode();
        result = 31 * result + (newOptionValues != null ? newOptionValues.hashCode() : 0);
        result = 31 * result + (removedOptionValues != null ? removedOptionValues.hashCode() : 0);
        return result;
    }

}
