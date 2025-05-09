package com.b2s.apple.services;

import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by vkrishnan on 4/21/2019.
 */
@Service
public class VersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionService.class);

    private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";

    private static final String SELECT_QUERY = "SELECT 1";

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private DataSource dataSource;

    /**
     * Returns Application status based on DB connectivity
     *
     * @param timeout
     * @return { "status": "UP" }
     */
    public AppVersion getWebAppHealth(Integer timeout) {
        int timeoutInSeconds = 3 * 1000;
        //set timeout limitation
        if (Objects.nonNull(timeout) && timeout > 0) {
            timeoutInSeconds = timeout * 1000;
        }

        final AppVersion appInfo = new AppVersion();
        try {
            if (checkDBConnectivity(timeoutInSeconds)) {
                appInfo.setStatus(CommonConstants.APP_UP);
            } else {
                appInfo.setStatus(CommonConstants.APP_DOWN);
            }
        } catch (RuntimeException ex) {
            appInfo.setStatus(CommonConstants.APP_DOWN);
        }
        return appInfo;
    }


    public AppVersion getWebAppDetails() {
        Attributes attr = new Attributes();
        //Collect Manifest attributes
        try(InputStream in = servletContext.getResourceAsStream(MANIFEST_FILE)) {
            if (Objects.nonNull(in)) {
                Manifest manifest = new Manifest(in);
                attr = manifest.getMainAttributes();
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while processing MANIFEST.MF file");
        }
        return getWebAppVersion(attr);
    }

    /**
     * Validates DB connectivity
     *
     * @param timeOut
     * @return
     */
    private boolean checkDBConnectivity(int timeOut) throws RuntimeException {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(timeOut);
        final Map<String, Object> resultMap = jdbcTemplate.queryForMap(SELECT_QUERY);
        return MapUtils.isNotEmpty(resultMap);
    }

    /**
     * Returns WebApp version and Build number
     *
     * @param attr
     * @return {
     *          "version": "1.46.12",
     *          "build": "5249" //from ${BUILD_NUMBER_MASTER}, number appended to the war file and available on Nexus
     * }
     */
    private AppVersion getWebAppVersion(Attributes attr) {
        final AppVersion appInfo = new AppVersion();
        appInfo.setVersion(attr.getValue(CommonConstants.WEBAPP_VERSION));
        return appInfo;
    }
}
