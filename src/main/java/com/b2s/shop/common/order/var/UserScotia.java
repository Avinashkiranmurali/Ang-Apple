package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

/**
 * Created by hranganathan on 9/13/2016.
 */
public class UserScotia extends User {

    private String agentId;
    private String userReturnToken;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(final String agentId) {
        this.agentId = agentId;
    }

    public String getUserReturnToken() {
        return userReturnToken;
    }

    public void setUserReturnToken(final String userReturnToken) {
        this.userReturnToken = userReturnToken;
    }

}
