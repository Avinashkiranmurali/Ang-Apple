package com.b2s.rewards.security.filter;

import com.b2s.rewards.common.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.b2s.rewards.apple.util.AppleUtil.getClientIpAddress;

public class XSRFValidationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(XSRFValidationFilter.class);
    private static final String[] ALLOWED_API_PATTERN = {
            "/apple-gr/service/orders/[a-zA-Z0-9]*"
    };
    private static final String[] CHECK_API = {
            "/service",
            ".json"
    };

    final String[] NO_XSRF_CHECK_URLS = {
            "application/health",
            "application/info",
            "/order/confirmPurchase",
            "/program",
            "/configData",
            "/order/status",
            "/template",
            "/messages",
            "/fraudcheck/redirectlogohtm",
            "/timedOut",
            "/kount/data-collector",
            "/validSession",
            "/category/reloadRepository",
            "/getXSRFToken",
            "/log/errors.json",
            "/signOut",
            "/keepAlive",
            "/publicMessages"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //overriding init() method in filter interface
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        /**
         * REST end points like /program, /messages, /template are called from Payment Server BE.
         * REST end points like /confirmPurchase, /order/status, /orders/{orders} are called by other vars/partners like Vitality, PPC, Grassroot etc
         * So we need to exclude these REST end points from XSRF token validation.
         */
        if (isXsrfValidationRequired(httpServletRequest)
                && !isValidXsrfTokenPresent(httpServletRequest)) {
            log.warn("Unauthorized access from ip address {} for url {}",
                    getClientIpAddress(httpServletRequest),
                    httpServletRequest.getContextPath()
                            + httpServletRequest.getServletPath()
                            + httpServletRequest.getPathInfo()
            );
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Method to skip XSRF validation based on below 2 conditions
     * 1. Mandatory - URLs should contain either "/service" or ".json"
     * 2.1. URL should either contain anyOne of the string as mentioned in 'excludeUrls' list or
     * 2.2. URL should match regex pattern "/apple-gr/service/orders/[0-9]*"
     *
     * @param httpServletRequest
     * @return <code>true</code>: when the XSRF Validation is required; <code>false</code> otherwise
     */
    private boolean isXsrfValidationRequired(final HttpServletRequest httpServletRequest) {
        final String requestUri = httpServletRequest.getRequestURI();

        // URLs should contain any of these values
        final boolean containsUrl = Stream.of(CHECK_API)
                                          .anyMatch(url -> requestUri.contains(url));

        // These are URLs that is not protected
        final boolean isExcludeUrl = Stream.of(NO_XSRF_CHECK_URLS)
                                           .anyMatch(url -> requestUri.contains(url));

        // These are URL patterns that are allowed for special API access
        final boolean isAllowed = Stream.of(ALLOWED_API_PATTERN)
                                        .anyMatch(pattern -> requestUri.matches(pattern));

        // If the given URI is allowed to proceed without check for XSRF token, then allow it.
        return !(containsUrl && (isAllowed || isExcludeUrl));
    }

    /**
     * Validate XSRF Token
     * Returns false if XSRF Token in Session not matches with the Token in the in-coming request
     *
     * @param request
     * @return true/false
     */
    private boolean isValidXsrfTokenPresent(final HttpServletRequest request) {
        final Optional<String> csrfTokenFromRequest =
            Optional.ofNullable(request.getHeader(CommonConstants.XSRF_TOKEN_REQUEST_HEADER_KEY));
        final Optional<String> csrfTokenInSession =
            Optional.ofNullable((String) request.getSession().getAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY));

        return csrfTokenFromRequest.equals(csrfTokenInSession);
    }

    @Override
    public void destroy() {
        //overriding destroy() method in filter interface
    }
}

