package com.b2s.rewards.security.util;

import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
public class SessionUtil {

    /**
     * Invalidates the current session and starts a new session.
     * @param request
     * @return
     */
    public static HttpSession restartSession(final HttpServletRequest request) {
        final HttpSession currentSession = request.getSession(false);
        if (currentSession != null){
            currentSession.invalidate();
        }
        return request.getSession(true);
    }
    /**
     * Find out is user HTTP session has timed out.
     * We try to display a custom timeout page here.
     *
     * @param request the request
     * @return true if the session has timed out
     */
    public static boolean isSessionTimeout(HttpServletRequest request){
        return request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT) == null;
    }

}
