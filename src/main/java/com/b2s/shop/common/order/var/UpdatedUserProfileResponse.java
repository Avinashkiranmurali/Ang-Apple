package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

/**
 * @author kmurchison  7/8/2014 3:15 PM
 */
public class UpdatedUserProfileResponse {
    private final User user;
    private final boolean success;

    private UpdatedUserProfileResponse(final User user, final boolean success) {
        this.user = user;
        this.success = success;
    }

    public User getUser() {
        return user;
    }

    public boolean isSuccess() {
        return success;
    }

    public static class Builder {
        private User user;
        private boolean success;

        public Builder withUser(final User withUser) {
            this.user = withUser;
            return this;
        }

        public Builder withSuccess(final boolean withSuccess) {
            this.success = withSuccess;
            return this;
        }

        public UpdatedUserProfileResponse build() {
            return new UpdatedUserProfileResponse(user, success);
        }
    }
}
