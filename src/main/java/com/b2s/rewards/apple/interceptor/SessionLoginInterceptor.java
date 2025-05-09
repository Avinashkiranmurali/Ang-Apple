package com.b2s.rewards.apple.interceptor;

/**
 * To intercept/secure any external REST call to authenticate access
 *
 */

import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionLoginInterceptor implements HandlerInterceptor  {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        if(request.getRequestURI().contains("service/application/health")
                || request.getRequestURI().contains("service/application/info")
                || request.getRequestURI().contains("service/priceModelsByVarProgram")
                || request.getRequestURI().contains("service/publicMessages")){
            //Skip the health Controller.
            return true;
        }

        //Intercept for all these URL patterns
        if (!request.getRequestURI().contains("/service/notification/sendEmail") &&
            !request.getRequestURI().contains("/service/order/confirmPurchase") &&
            request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT) == null
            && (request.getRequestURI().contains("/service") || request.getRequestURI().contains(".json"))
            && !request.getRequestURI().contains("/service/cache")
            && !request.getRequestURI().contains("/service/order/status")
            && !request.getRequestURI().matches("/apple-gr/service/orders/[a-zA-Z0-9]*")
            && !request.getRequestURI().contains("/apple-gr/service/signOut")
            && !request.getRequestURI().contains("/services")
            && !request.getRequestURI().contains("/otp")
            && !request.getRequestURI().contains("/reloadRepository")
            && !request.getRequestURI().contains("/validate")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Unauthorized access. Please login with correct credentials");
            return false;
        }

        return true;
    }

}