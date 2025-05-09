package com.b2s.apple.services;

import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Created by vkrishnan on 4/25/2019.
 */
@Service
public class DBVersionService {

    @Autowired
    private DataSource dataSource;

    private static final String FLY_VERSION_QUERY = "SELECT top 1 (version) AS version FROM flyway_schema_history " +
        " (NOLOCK) WHERE success = 1 and version IS NOT NULL " +
        "ORDER BY installed_on DESC";

    public AppDetails getFlywayVersion(int timeOut) throws DataAccessException {
        AppDetails appDetails = new AppDetails();
        appDetails.setApplicationName(CommonConstants.FLYWAY_MIGRATE);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(timeOut);
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(FLY_VERSION_QUERY);
        if (resultMap != null && resultMap.get(CommonConstants.VERSION) != null) {
            appDetails.setApplicationInfo(
                new AppVersion(CommonConstants.APP_UP, resultMap.get(CommonConstants.VERSION).toString(),
                    CommonConstants.APP_STATUS_NA));
        } else {
            appDetails.setApplicationInfo(new AppVersion(CommonConstants.APP_DOWN, null,
                CommonConstants.APP_STATUS_NA));
        }
        return appDetails;
    }
}
