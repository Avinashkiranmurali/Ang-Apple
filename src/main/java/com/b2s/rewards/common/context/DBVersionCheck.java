package com.b2s.rewards.common.context;

import com.b2s.rewards.common.exception.B2RException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Scanner;

/**
 * This class holds functions for DB version check
 * Created by rpillai on 7/15/2015.
 */
@Component
public class DBVersionCheck extends JdbcDaoSupport {

    @Autowired
    private DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(DBVersionCheck.class);

    @Value("${db.version}")
    private String dbVersion;

    @Value("${ignoreDbMinorVersion}")
    private String ignoreDbMinorVersion;

    @PostConstruct
    public void init() {
        super.setDataSource(dataSource);
    }

    /**
     * This method gets db.version property value from application.properties and max success version from flyway_schema_history db table and
     * if the versions does not match throws and exception failing application startup
     * @return
     * @throws B2RException
     */
    public boolean checkDbVersion() throws B2RException {
        boolean ignoreDbMinorVersionFlag = new Boolean(ignoreDbMinorVersion);
        boolean versionMatch = false;
        String query = "select top(1) version from flyway_schema_history where success=1 and version is not null order by installed_rank desc";
        Map<String, Object> resultMap = null;
        try {
            resultMap = getJdbcTemplate().queryForMap(query);
        } catch(Exception e) {
            LOGGER.error("Exception occured while querying flyway_schema_history DB table for max success version. Exception: ", e);
            throw new B2RException("Exception occured while querying flyway_schema_history DB table for max success version. Exception message: "+e.getMessage());

        }
        if(resultMap != null && resultMap.get("version") != null){
            String version = resultMap.get("version").toString();
            if(compareVersion(version, this.dbVersion, false) == 0) {
                versionMatch = true;
                LOGGER.info("DB Version matches. db.version property value from application.properties is {} and max success version from flyway_schema_history is {}",dbVersion, version);
            } else {
                if(compareVersion(version, this.dbVersion, ignoreDbMinorVersionFlag) == 0) {
                    LOGGER.info("DB Version does not match. However major version matches and the runtime parameter ignoreDbMinorVersionFlag is set to true. " +
                            "So ignoring the minor version mismatch. db.version property value from application.properties is {} and max success version from flyway_schema_history is {}",dbVersion, version);
                    versionMatch = true;
                } else {
                    LOGGER.info("DB Version does not match. db.version property value from application.properties is {} and max version from flyway_schema_history is {}",dbVersion, version);
                    throw new B2RException("DB Version does not match. db.version property value from application.properties is "+dbVersion+" and max version from flyway_schema_history is "+version);
                }
            }
        } else {
            LOGGER.error("Unable to find DB version from flyway_schema_history DB table. db.version property value from application.properties is {} and response from database is {}", dbVersion, resultMap);
            throw new B2RException("Unable to find DB version from flyway_schema_history DB table. db.version property value from application.properties is "+dbVersion+" and response from database is "+resultMap);
        }
        return versionMatch;
    }

    public int compareVersion(String version1, String version2, boolean ignoreMinorVersion) {
        try(Scanner s1 = new Scanner(version1); Scanner s2 = new Scanner(version2)){

            s1.useDelimiter("\\.");
            s2.useDelimiter("\\.");

            int versionPartCounter = 0;
            while (s1.hasNextInt() && s2.hasNextInt()) {
                versionPartCounter++;
                if (ignoreMinorVersion && versionPartCounter == 3) {
                    return 0;
                }
                int v1 = s1.nextInt();
                int v2 = s2.nextInt();
                if (v1 < v2) {
                    return -1;
                } else if (v1 > v2) {
                    return 1;
                }
            }
        }
        catch(Exception e) {
            LOGGER.error("Exception occurred while comparing the versions.");
            throw e;
        }
        return 0;
    }
}
