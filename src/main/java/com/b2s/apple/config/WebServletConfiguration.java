package com.b2s.apple.config;

import com.b2s.monitor.filter.P3PResponseFilter;
import com.b2s.rewards.apple.config.listener.ManifestContextListener;
import com.b2s.rewards.security.filter.*;
import com.b2s.security.saml.listener.SAMLBootstrapListener;
import org.apache.axis.transport.http.AxisHTTPSessionListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.HttpSessionMutexListener;

import javax.servlet.*;

public class WebServletConfiguration implements WebApplicationInitializer {

    public static final String HTM = "*.htm";
    public static final String JSON = "*.json";
    public static final String SERVICE = "/service/*";
    public static final String SERVICES = "/services/*";

    @Override
    public void onStartup(ServletContext servletContext) {

        //I18N multi lingual support
        servletContext.setInitParameter("javax.servlet.jsp.jstl.fmt.localizationContext","ApplicationResources");
        servletContext.setInitParameter("javax.servlet.jsp.jstl.fmt.fallbackLocale","en");
        servletContext.setInitParameter("javax.servlet.jsp.jstl.fmt.locale","en");

        final AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
        webApplicationContext.register(ApplicationConfig.class);//BasicAuthConfiguration.class
        webApplicationContext.setServletContext(servletContext);

        final ServletRegistration.Dynamic rewardstep = servletContext.addServlet("rewardstep", new DispatcherServlet(webApplicationContext));
        rewardstep.setLoadOnStartup(1);
        rewardstep.addMapping(HTM, JSON,"*.do", SERVICE, SERVICES,"/citi/SAML/POST");

        final ServletRegistration.Dynamic servlet = servletContext.addServlet("saml", new DispatcherServlet(webApplicationContext));
        servlet.setLoadOnStartup(2);
        servlet.addMapping("/SAML/POST","/SAML/login");

        servletContext.addListener(AxisHTTPSessionListener.class);
        servletContext.addListener(RequestContextListener.class);
        servletContext.addListener(HttpSessionMutexListener.class);
        servletContext.addListener(ManifestContextListener.class);
        servletContext.addListener(SAMLBootstrapListener.class);

        //Register Filters
        FilterRegistration.Dynamic springSecurityFilterChainFilter = servletContext.addFilter(
            "springSecurityFilterChain", new DelegatingFilterProxy());
        //make sure springSecurityFilterChain is matched first
        springSecurityFilterChainFilter.addMappingForUrlPatterns(null, false, "/service/merchants/create",
            "/service/order/confirmPurchase/*",
            "/service/order/status/*",
            "/service/orders/*",
            "/service/notification/*",
            SERVICES);

        FilterRegistration.Dynamic clickJackPreventionFilter = servletContext.addFilter("clickjackprevention",
            new ClickJackPreventionFilterDeny());
        clickJackPreventionFilter.addMappingForUrlPatterns(null, false, "/*");
        clickJackPreventionFilter.setInitParameter("mode", "DENY");

        //Anonymous login filter for Core-Apple clients
        FilterRegistration.Dynamic anonymousFilter = servletContext.addFilter("anonymousFilter",
            new AnonymousLoginFilter());
        anonymousFilter.addMappingForUrlPatterns(null, false, "/DomainLogin.do","/login.do");

        FilterRegistration.Dynamic samlProtectionFilter = servletContext.addFilter("samlProtectionFilter",
            new DelegatingFilterProxy());
        samlProtectionFilter.addMappingForUrlPatterns(null, false, "/", "/index.jsp");

        //Log4J MDC Filter
        FilterRegistration.Dynamic log4JMDCFilter = servletContext.addFilter("LoggerMDCFilter",
            new LoggerMDCFilter());
        log4JMDCFilter.addMappingForUrlPatterns(null, false, "*");

        //XSRF Check filter
        FilterRegistration.Dynamic xsrfValidationFilter = servletContext.addFilter("XSRFValidationFilter",
            new XSRFValidationFilter());
        xsrfValidationFilter.addMappingForUrlPatterns(null, false, JSON, SERVICE);

        //Travel: Luxury servlets END
        FilterRegistration.Dynamic xssContentFilter = servletContext.addFilter("XSSContentFilter",
            new XSSContentFilter());
        xssContentFilter.addMappingForUrlPatterns(null, false, HTM,
            JSON,
            "*.do",
            SERVICE,
            SERVICES);

        //Travel: Luxury servlets END
        FilterRegistration.Dynamic securityInterceptorFilter = servletContext.addFilter("SecurityInterceptorFilter",
            new SecurityInterceptorFilter());
        securityInterceptorFilter.addMappingForUrlPatterns(null, false, HTM,
            JSON,
            "*.do",
            SERVICE);

        //Filter to insert P3P policy header
        FilterRegistration.Dynamic staticResponseHeaderFilter = servletContext.addFilter("StaticResponseHeaderFilter",
            new P3PResponseFilter());
        staticResponseHeaderFilter.addMappingForUrlPatterns(null, false, "/*");
        staticResponseHeaderFilter.setInitParameter("headername", "P3P");
        staticResponseHeaderFilter.setInitParameter("headervalue", "CP=\"CAO PSA OUR\"");
    }
}
