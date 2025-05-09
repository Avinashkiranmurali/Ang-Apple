package com.b2s.rewards.security.filter;

import com.b2s.rewards.security.util.XSSRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JspSecurityFilter implements Filter {

    private static final String MARKER = "JspSecurityFilter.marker";
    private static final String WELCOME = "index.jsp";

    /**
    * Stop direct access of jsp pages. This is a no-no. Must use Struts actions
    * for absolutely everything. The only exception is the welcome-page which
    * cannot be an action (index.jsp exception in code below).
    *
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request.getAttribute(MARKER) == null && request instanceof HttpServletRequest) {

            // mark this request as the "outer" request, so we only log it once
            request.setAttribute(MARKER, Boolean.TRUE);

            XSSRequestWrapper httpRequest = new XSSRequestWrapper((HttpServletRequest) request);

            String name = httpRequest.getRequestURI();

            if (name.endsWith(".jsp") && !name.endsWith(WELCOME)) {
                String msg = "Prohibited direct access of jsp attempted: " + name;
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        return;
    }

    public void destroy() {
        return;
    }
}