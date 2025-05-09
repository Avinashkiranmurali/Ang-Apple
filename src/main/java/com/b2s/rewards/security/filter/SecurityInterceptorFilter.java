package com.b2s.rewards.security.filter;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Servlet Filter implementation class BaseFilter
 */
public class SecurityInterceptorFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SecurityInterceptorFilter.class);


    /**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		return;
	}


	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest hreq = (HttpServletRequest) request;
		HttpServletResponse hresp = (HttpServletResponse) response;
		String path = hreq.getServletPath();
        String referer = hreq.getHeader("Referer");

        //legacy resources this filter needs to ignore
        if (path.contains("/admin/")) {
            log.debug("Igoring SecurityFilter for {}", path);
            chain.doFilter(request, response);
            return;
        }

        User user = (User) hreq.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        
        //context information for logging
        if (user != null && user.getUserId() != null) {
            MDC.put("userid", "[" + user.getUserId() + "]");
        }
        MDC.put("transactionid", "[" + new Date().getTime() + "]");

        chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		return;
	}

}

