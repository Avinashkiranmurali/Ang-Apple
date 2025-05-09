package com.b2s.rewards.apple.config.listener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * copies attributes from META-INF/MANIFEST.MF to ServletContext
 */
public class ManifestContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(ManifestContextListener.class);

    private String manifestLocation = "/META-INF/MANIFEST.MF";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        try(InputStream inputStream = servletContext.getResourceAsStream(manifestLocation)) {
            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);
                Attributes attributes = manifest.getMainAttributes();
                for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
                    servletContext.setAttribute(entry.getKey().toString(), entry.getValue().toString());
                }
            } else {
                log.warn("Unable to load manifest file. ServletContext attributes will not be initialized: {}" , manifestLocation);
            }

            String b2sVersion = (String) servletContext.getAttribute("B2S-Version");
            String buildNumber = (String) servletContext.getAttribute("Build-Number");
            if (b2sVersion != null) {
                if(!StringUtils.isNumeric(buildNumber)){
                    //Set some integer value for the build number this is for local
                    buildNumber = String.valueOf(System.currentTimeMillis());
                }
                servletContext.setAttribute("buildId", b2sVersion + "-" + buildNumber);
            }

        } catch (Exception e) {
            log.warn("Error reading manifest file from {}", manifestLocation, e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Enumeration<String> attrNames = servletContextEvent.getServletContext().getAttributeNames();
        while(attrNames.hasMoreElements()){
            servletContextEvent.getServletContext().removeAttribute(attrNames.nextElement());
        }
    }
}
