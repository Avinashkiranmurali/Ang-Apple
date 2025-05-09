package com.b2s.rewards.apple.integration.model.UA;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by srukmagathan on 28-08-2018.
 */
public class PromotionalSubscription implements Serializable {

    private static final long serialVersionUID = 9164089448020483209L;
    private boolean displayCheckbox=false;
    private boolean isChecked=false;

    public boolean isDisplayCheckbox() {
        return displayCheckbox;
    }

    public void setDisplayCheckbox(final boolean displayCheckbox) {
        this.displayCheckbox = displayCheckbox;
    }

    @JsonProperty("isChecked")
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(final boolean checked) {
        isChecked = checked;
    }
}
