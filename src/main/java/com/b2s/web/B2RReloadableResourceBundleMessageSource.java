package com.b2s.web;

import com.b2s.apple.services.VarProgramMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by ewaktola on 8/7/2014.
 */
public class B2RReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private static final String PROGRAM_SEPARATOR = "|";

    /**
     * Cache to hold already loaded properties per filename
     */
    private static final Map<String, PropertiesHolder> cachedDBProperties = new HashMap<String, PropertiesHolder>();

    private String varId;
    private String programId;

    //Previously there was no DB Cache, this cause multiple DB calls triggered for an muti variation product detail call.
    // to overcome we have introduced cache for 6 seconds(old value is '-1' to disable DB cache) by default.
    //we can override this cache duration in applicationContext.xml by Var level.
    private long dbCacheMillis = 6000;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    public Properties getAllMessages(final Locale locale, final String codeType){
        this.clearCache();
        Properties properties = null;
        if(StringUtils.isNotBlank(codeType)){
            PropertiesHolder propertiesHolder = getPropertiesBasedOnCodeType(locale.toString(), codeType);
            if (Objects.nonNull(propertiesHolder)) {
                properties = propertiesHolder.getProperties();
            }
        } else {
            properties = getMergedProperties(locale).getProperties();
        }
        return properties;
    }

    @Override
    protected PropertiesHolder getMergedProperties(Locale locale) {
        PropertiesHolder propertiesHolder = super.getMergedProperties(locale);
        if (Objects.isNull(propertiesHolder.getProperties())) {
            propertiesHolder = new PropertiesHolder(new Properties(), -1l);
        } else {
            if (StringUtils.isNotBlank(programId)) {
                Properties properties = propertiesHolder.getProperties();
                Properties programProperties = new Properties();
                for (String name : properties.stringPropertyNames()) {
                    //The key starts with current programid and then . replace with the ones already there in the properties
                    if (name.startsWith(programId + PROGRAM_SEPARATOR)) {
                        programProperties.setProperty(name.substring(name.indexOf(PROGRAM_SEPARATOR) + 1), properties.getProperty(name));
                        //Remove the existing entry as it will be replaced with the program specific one.
                        properties.remove(name);
                    }
                }
                //Merge/overwrite existing properties with program properties
                properties.putAll(programProperties);
            }
        }
        getDBProperties(locale, propertiesHolder);
        return propertiesHolder;
    }

    private void getDBProperties(Locale locale, PropertiesHolder propertiesHolder) {
        if(StringUtils.isNotBlank(varId) && StringUtils.isNotBlank(programId)) {
            PropertiesHolder dbPropertiesHolder = getDBProperties(locale.toString());
            if(dbPropertiesHolder != null && dbPropertiesHolder.getProperties() != null) {
                propertiesHolder.getProperties().putAll(dbPropertiesHolder.getProperties());
            }
        }
    }

    private PropertiesHolder getPropertiesBasedOnCodeType(final String locale, final String codeType) {

        final Properties dbProperties =
            varProgramMessageService.getMessagesBasedOnCodeType(
                Optional.ofNullable(varId),
                Optional.ofNullable(programId),
                locale,
                    codeType);

        PropertiesHolder propertiesHolder = null;
        if (dbProperties != null && !dbProperties.isEmpty()) {
            long currentTimeInMillis = System.currentTimeMillis();
            propertiesHolder = new PropertiesHolder(dbProperties, currentTimeInMillis);
            propertiesHolder.setRefreshTimestamp(currentTimeInMillis);
        }
        return propertiesHolder;
    }

    private PropertiesHolder getDBProperties(String locale) {
        PropertiesHolder propHolder = this.cachedDBProperties.get(getVarId() + "_" + getProgramId()+ "_" + locale);
        if (propHolder != null && (propHolder.getRefreshTimestamp() < 0 || propHolder.getRefreshTimestamp() > System.currentTimeMillis() - this.dbCacheMillis)) {
            // up to date
            return propHolder;
        }
        return refreshDBProperties(locale);
    }

    private PropertiesHolder refreshDBProperties(String locale) {
        final Properties dbProperties = varProgramMessageService.getMessages(Optional.ofNullable(varId), Optional.ofNullable(programId), locale);
        PropertiesHolder propertiesHolder = null;
        if (dbProperties != null && !dbProperties.isEmpty()) {
            long currentTimeInMillis = System.currentTimeMillis();
            propertiesHolder = new PropertiesHolder(dbProperties, currentTimeInMillis);
            propertiesHolder.setRefreshTimestamp(currentTimeInMillis);
            cachedDBProperties.put(getVarId()+"_"+getProgramId()+ "_" + locale, propertiesHolder);
        }
        return propertiesHolder;
    }

    @Override
    protected MessageFormat resolveCode(final String code,final Locale locale) {
        PropertiesHolder dbPropertiesHolder = getDBProperties(locale.toString());
        MessageFormat messageFormat = null;
        if(dbPropertiesHolder != null) {
            messageFormat = dbPropertiesHolder.getMessageFormat(code, locale);
            if(messageFormat != null) {
                return messageFormat;
            }
        }
        return super.resolveCode(code, locale);
    }

    @Override
    protected String resolveCodeWithoutArguments(final String code, final Locale locale) {
        PropertiesHolder dbPropertiesHolder = getDBProperties(locale.toString());
        String property = null;
        if(dbPropertiesHolder != null) {
            property = dbPropertiesHolder.getProperty(code);
            if(StringUtils.isNotBlank(property)) {
                return property;
            }
        }
        return super.resolveCodeWithoutArguments(code, locale);
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public void setDbCacheSeconds(int dbCacheSeconds) {
        this.dbCacheMillis = (dbCacheSeconds * 1000);
    }

}