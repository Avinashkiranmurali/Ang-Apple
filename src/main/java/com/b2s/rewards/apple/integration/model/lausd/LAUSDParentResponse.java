package com.b2s.rewards.apple.integration.model.lausd;

import java.util.List;

public class LAUSDParentResponse {

    public List<ParentInfo> parentEmailList;

    public List<ParentInfo> getParentEmailList() {
        return parentEmailList;
    }

    public void setParentEmailList(List<ParentInfo> parentEmailList) {
        this.parentEmailList = parentEmailList;
    }
}
