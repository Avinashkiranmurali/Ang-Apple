package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

/**
 * @author kmurchison  7/8/2014 3:14 PM
 */
public class UpdatedUserProfileRequest {
    private final User user;

    private UpdatedUserProfileRequest(final User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public static class Builder {
        private User user;

        public Builder withUser(final User withUser) {
            this.user = withUser;
            return this;
        }

        public UpdatedUserProfileRequest build() {
            return new UpdatedUserProfileRequest(user);
        }
    }
}
