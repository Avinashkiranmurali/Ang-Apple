package com.b2s.rewards.security.filter;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter enable use to do couple of things.
 *  1.  Reuse existing domain mapping service for anonymous browsing
 *  2.  Skip SAML validation for anonymous browsing.
 *
 * Created by srukmagathan on 20-07-2017.
 */
public class AnonymousLoginFilter implements Filter {

    private static final String ONE_S="1";
    @Override
    public void init(final FilterConfig filterConfig)
        throws ServletException {
        //implementing init() method in filter interface
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
        final FilterChain filterChain)
        throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;

        final String anonymousFlag=httpServletRequest.getParameter(CommonConstants.ANONYMOUS_FLAG);

        if(ONE_S.equals(anonymousFlag)){
            User user=new User();
            // Setting dummy user in session will skip SAML validation
            httpServletRequest.getSession().setAttribute(CommonConstants.USER_SESSION_OBJECT,user);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }

    @Override
    public void destroy() {
        //implementing destroy() method in filter interface
    }
}
