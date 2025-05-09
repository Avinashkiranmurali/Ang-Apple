package com.b2s.apple.config;

import com.b2s.rewards.common.util.CommonConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class B2SAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(B2SAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("AUDIT - User unauthorized ...Bad credentials");
        response.setContentType(CommonConstants.APPLICATION_JSON);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(new JSONObject()
                .put(CommonConstants.MESSAGE, "User unauthorized - Bad credentials" )
                .toString());
    }
}
