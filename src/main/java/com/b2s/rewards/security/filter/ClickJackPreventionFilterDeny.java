package com.b2s.rewards.security.filter;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by skither on 1/24/2019.
 */
public class ClickJackPreventionFilterDeny implements Filter
{
    private String mode = "DENY";

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse)response;
        HttpServletRequest req = (HttpServletRequest)request;

        if(isDRPLoginPageController(req.getRequestURI())){
                res.addHeader("X-FRAME-OPTIONS",mode);
        }else{
            final Program program = (Program)req.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            if(Objects.nonNull(program)){
                final String showExperience = (String)program.getConfig().getOrDefault(CommonConstants.SHOP_EXPERIENCE, "");
                if(showExperience.equalsIgnoreCase(CommonConstants.EXPERIENCE_DRP)) {
                    res.addHeader("X-FRAME-OPTIONS",mode);
                }
            }
        }
        chain.doFilter(request, response);
    }
    public void destroy() {
        mode = "DENY";
    }

    public void init(FilterConfig filterConfig) {
        final String configMode = filterConfig.getInitParameter("mode");
        if ( configMode != null ) {
            mode = configMode;
        }
    }

    public boolean isDRPLoginPageController(final String path){
        final String ignorePath[] = {"/apple-gr/service/awp/","/apple-gr/service/publicMessages","/apple-gr/pages/login-agreement-modal.html",
                "/apple-gr/service/otp/generateOTPAWP","/apple-gr/service/otp/validateOTPAWP"};
        final List mylist = Arrays.asList(ignorePath);
        return mylist.stream().anyMatch(p->path.startsWith(p.toString()));
    }
}