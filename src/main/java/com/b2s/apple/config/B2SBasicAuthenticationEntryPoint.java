package com.b2s.apple.config;

import com.b2s.rewards.common.util.CommonConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class B2SBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(B2SBasicAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.warn("AUDIT - Authentication failure ... Bad credentials");
        response.setContentType(CommonConstants.APPLICATION_JSON);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new JSONObject()
                .put(CommonConstants.MESSAGE, "Authentication failed - Bad credentials")
                .toString());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("APPLEGR");
        super.afterPropertiesSet();
    }
}
