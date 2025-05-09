package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.integration.model.lausd.ParentInfo;
import com.b2s.shop.common.User;

import java.util.List;

public class UserLAUSD extends User {

    private List<ParentInfo> parents;

    public List<ParentInfo> getParents() {
        return parents;
    }

    public void setParents(final List<ParentInfo> parents) {
        this.parents = parents;
    }
}
