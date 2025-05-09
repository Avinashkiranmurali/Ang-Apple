package com.b2s.monitor.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Helps to insert P3P policy header into http response header.
 * This can help resolve cookie lost issue with IE7.0
 * 
 */
public class P3PResponseFilter implements Filter
{
	private final static String HEADERNAME_INIT_PARAM = "headername";
	private final static String HEADERVALUE_INIT_PARAM = "headervalue";

	private String headername = null;
	private String headervalue = null;

	public void init(FilterConfig filterConfig) throws ServletException {
		headername = filterConfig.getInitParameter(HEADERNAME_INIT_PARAM);
		headervalue = filterConfig.getInitParameter(HEADERVALUE_INIT_PARAM);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
	ServletException {
		HttpServletResponse httpResp = (HttpServletResponse) response;
		httpResp.setHeader(headername, headervalue);
		filterChain.doFilter(request, response);
	}

	public void destroy() {
		headername = null;
		headervalue = null;
	}
}
