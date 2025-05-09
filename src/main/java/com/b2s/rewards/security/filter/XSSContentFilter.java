package com.b2s.rewards.security.filter;

import com.b2s.rewards.security.util.XSSRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class XSSContentFilter implements Filter {
  
    private FilterConfig config;

    private static final Logger log = LoggerFactory.getLogger(XSSContentFilter.class);

    public XSSContentFilter() {
        config = null;
    }

    public void init(FilterConfig filterconfig) throws ServletException {
        config = filterconfig;
    }

    public void destroy(){
        config = null;
    }

    public FilterConfig getFilterConfig() {
        return config;
    }

    public void doFilter(ServletRequest servletrequest, ServletResponse servletresponse, FilterChain filterchain)
        throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) servletrequest;
        String path = hreq.getServletPath();

        //certain resources must be ignored by this filter
        if (path.contains("/admin/")) {

            log.debug("Igoring XSSContentFilter for {}", path);
            filterchain.doFilter(servletrequest, servletresponse);
            return;
        }

        filterchain.doFilter(new XSSRequestWrapper((HttpServletRequest)servletrequest), servletresponse);
    }
}

