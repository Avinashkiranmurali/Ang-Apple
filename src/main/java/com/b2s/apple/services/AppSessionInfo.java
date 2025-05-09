package com.b2s.apple.services;

import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_SESSION)
public class AppSessionInfo {
    @Autowired
    private HttpSession httpSession;

    public User currentUser() {
        return (User) httpSession.getAttribute(CommonConstants.USER_SESSION_OBJECT);
    }

    public Cart getSessionCart() {
        return (Cart) httpSession.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
    }

    public Map<String, String> getCategories() {
        return (Map<String, String>) httpSession.getAttribute(CommonConstants.PS_CATEGORIES);
    }
}