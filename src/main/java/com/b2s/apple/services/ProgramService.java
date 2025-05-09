package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.*;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.entity.VarProgramCreditAddsFilterEntity;
import com.b2s.apple.mapper.PaymentOptionMapper;
import com.b2s.apple.mapper.ProgramMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rpillai on 7/5/2016.
 */
@Service
public class ProgramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramService.class);

    @Autowired
    private VarProgramDao varProgramDao;

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    @Autowired
    private VarProgramPaymentOptionDao varProgramPaymentOptionDao;

    @Autowired
    private VarProgramNotificationService varProgramNotificationService;

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private PaymentOptionMapper paymentOptionMapper;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private VarProgramCreditAddsFilterDao varProgramCreditAddsFilterDao;

    @Autowired
    private AMPProductConfigService ampProductConfigService;

    @Autowired
    private CarouselService carouselService;

    /**
     * Get program based on given var id and program id
     *
     * @param varId
     * @param programId
     * @return
     */
    public Program getProgram(String varId, String programId, Locale locale) {
        Program program = null;
        if(StringUtils.isNotBlank(varId) && StringUtils.isNotBlank(programId)) {
            VarProgram varProgram = varProgramDao.getActiveVarProgram(varId, programId);
            if(varProgram != null) {
                final Set<ProgramConfig> configs = getProgramConfigs(varId, programId);
                final List<VarProgramPaymentOption> varProgramPaymentOptions =
                    varProgramPaymentOptionDao.getVarProgramPaymentOption(varId, programId);

                final List<VarProgramNotification> varProgramNotifications =
                    varProgramNotificationService.getActiveEmailNotifications(varId, programId, locale.toString());

                program = programMapper.from(varProgram, new ArrayList<>(configs), locale);
                if (program != null) {
                    program.setPayments(paymentOptionMapper.from(varProgramPaymentOptions));
                    program.setNotifications(programMapper.from(varProgramNotifications));
                }
            } else {
                LOGGER.error("Unable to find the program with var id {} and program id {}. Either the program does not exists or it is not active", varId, programId);
            }

        }

        final List<VarProgramCreditAddsFilterEntity> varProgramCreditAddsFilters = getBinfilters(varId,programId);

        if (Objects.nonNull(program) && CollectionUtils.isNotEmpty(varProgramCreditAddsFilters)) {
            program.setCcFilters(varProgramCreditAddsFilters.stream()
                .filter(Objects::nonNull)
                .map(op -> new Program.CCBin(op.getVarProgramFilterId().getFilter()))
                .collect(Collectors.toList()));
        }

        //S-20396 - Populate AMP services if available
        program.setAmpSubscriptionConfig(ampProductConfigService.getAmpConfigurationByProgram(program));

        //Set Carousel configurations
        carouselService.setProgramCarouselConfig(program);

        return program;
    }

    public Set<ProgramConfig> getProgramConfigs(String varId, String programId) {    final Set<ProgramConfig> configs = new HashSet<>();
        final ProgramConfig analyticsConfig = programMapper.getProgramConfig(varProgramConfigDao
                .getVarProgramConfigByVarProgramName(CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM,
                        CommonConstants.ANALYTICS));
        final ProgramConfig sfFProWebFontConfig = programMapper.getProgramConfig(varProgramConfigDao
                .getVarProgramConfigByVarProgramName(CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM,
                        CommonConstants.SFPROWEBFONT));

        final ProgramConfig enableAppleCareServicePlan = programMapper.getProgramConfig(varProgramConfigDao
                .getVarProgramConfigByVarProgramName(CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM,
                        CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN));

        final ProgramConfig enableRelatedProducts = programMapper.getProgramConfig(varProgramConfigDao
            .getVarProgramConfigByVarProgramName(CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM,
                CommonConstants.IS_RELATED_PRODUCTS_ENABLED));

        final List<ProgramConfig> varConfigs = programMapper.getProgramConfigs(varProgramConfigDao.getVarProgramConfig(varId, CommonConstants.DEFAULT_PROGRAM_KEY));
        final List<ProgramConfig> programConfigs = programMapper.getProgramConfigs(varProgramConfigDao.getVarProgramConfig(varId, programId));

        if (Objects.nonNull(programConfigs)) {
            configs.addAll(programConfigs);
        }
        if (Objects.nonNull(varConfigs)) {
            configs.addAll(varConfigs);
        }
        if (Objects.nonNull(analyticsConfig)) {
            configs.add(analyticsConfig);
        }

        if (Objects.nonNull(sfFProWebFontConfig)) {
            configs.add(sfFProWebFontConfig);
        }

        if (Objects.nonNull(enableAppleCareServicePlan)) {
            configs.add(enableAppleCareServicePlan);
        }

        if (Objects.nonNull(enableRelatedProducts)) {
            configs.add(enableRelatedProducts);
        }

        return configs;
    }

    public HashMap<String, Serializable> addOrUpdateExternalUrls(final Program program, final CommonConstants.LoginType loginType,
                                                             final String hostName,
                                                             final String locale,
                                                             final HashMap<String, Serializable> tempExternalUrls) {
        HashMap<String, Serializable> externalUrls = new HashMap<>();
        if(tempExternalUrls!=null){
            externalUrls = tempExternalUrls;
        }
        if(program != null) {
            for(ExternalUrlConstants.SessionUrls url : ExternalUrlConstants.SessionUrls.values()) {
                //Keep Alive URL should not be set for Non SAML flow
                if (validateKeepAliveUrlWithSAMLFlow(loginType, url)) {

                    String urlStr = null;
                    String key = url.getValue();
                    urlStr = getUrlByLocale(program, locale, urlStr, key);
                    urlStr = getUrlByLoginType(program, loginType, urlStr, key);
                    if (StringUtils.isBlank(urlStr)) {
                        urlStr = (String) program.getConfig().get(key);
                        urlStr = getUrlForLogout(program, urlStr, key);
                        urlStr = getUrlFromApplicationProperties(program, loginType, urlStr, key);
                    }

                    urlStr = appendHostname(hostName, urlStr);
                    if (StringUtils.isNotBlank(urlStr)) {
                        externalUrls.put(url.getValue(), urlStr);
                    }
                }
            }
        }
        return externalUrls;
    }

    /**
     * Keep Alive URL should not be set for Non SAML flow
     *
     * @param loginType
     * @param url
     * @return
     */
    private boolean validateKeepAliveUrlWithSAMLFlow(CommonConstants.LoginType loginType,
        ExternalUrlConstants.SessionUrls url) {
        if (CommonConstants.LoginType.SAML == loginType) {
            return true;
        } else {
            return ExternalUrlConstants.SessionUrls.KEEP_ALIVE_URL != url;
        }
    }

    private String appendHostname(String hostName, String urlStr) {
        try {
            if(StringUtils.isNotBlank(urlStr) && !new URI(urlStr).isAbsolute()) {
                urlStr = hostName + urlStr;
            }
        } catch(URISyntaxException use) {
            LOGGER.error("Error while checking whether the url {} is absolute", urlStr, use);
        }
        return urlStr;
    }

    private String getUrlFromApplicationProperties(Program program, CommonConstants.LoginType loginType, String urlStr, String key) {
        if(StringUtils.isBlank(urlStr)) {
            if(loginType != null) {
                urlStr = applicationProperties.getProperty(program.getVarId().toLowerCase() + "." + program.getProgramId().toLowerCase() + "." + loginType.getValue() + "." + key);
            }
            if(StringUtils.isBlank(urlStr)) {
                urlStr = applicationProperties.getProperty(program.getVarId().toLowerCase()+ "."+program.getProgramId().toLowerCase()+ "."+key);
            }
        }
        return urlStr;
    }

    private String getUrlForLogout(Program program, String urlStr, String key) {
        if (StringUtils.isBlank(urlStr) && (key.equalsIgnoreCase(ExternalUrlConstants.SIGN_OUT_URL) ||
            key.equalsIgnoreCase(ExternalUrlConstants.TIME_OUT_URL))
            && Objects.nonNull(program.getConfig().get(ExternalUrlConstants.LOG_OUT_URL))) {
                urlStr = (String)program.getConfig().get(ExternalUrlConstants.LOG_OUT_URL);
        }
        return urlStr;
    }

    private String getUrlByLoginType(Program program, CommonConstants.LoginType loginType, String urlStr, String key) {
        if(StringUtils.isBlank(urlStr) && loginType != null) {
            urlStr = getURL(program, key, loginType.getValue());
        }
        return urlStr;
    }


    private String getUrlByLocale(Program program, String locale, String urlStr, String key) {
        if(StringUtils.isNotBlank(locale)) {
            urlStr = getURL(program, key, locale);
        }
        return urlStr;
    }

    private String getURL(Program program, String key, String name) {
        String urlStr = (String) program.getConfig().get(name + "." + key);
        if (StringUtils.isBlank(urlStr) && (key.equalsIgnoreCase(ExternalUrlConstants.SIGN_OUT_URL) ||
                key.equalsIgnoreCase(ExternalUrlConstants.TIME_OUT_URL))
                && Objects.nonNull(program.getConfig().get(name + "." + ExternalUrlConstants.LOG_OUT_URL))) {
            urlStr = (String) program.getConfig().get(name + "." + ExternalUrlConstants.LOG_OUT_URL);
        }
        return urlStr;
    }



    /**
     * Get var program config value for a given var id, program id and name
     *
     * @param varId
     * @param programId
     * @param name
     * @return
     */
    public Optional<String> getProgramConfigValue(final String varId, final String programId, final String name) {
        final VarProgramConfigEntity varProgramConfig = varProgramConfigDao.getVarProgramConfigByVarProgramName(varId, programId, name);
        return (varProgramConfig != null) ? Optional.of(varProgramConfig.getValue()) : Optional.empty();
    }

    public List<VarProgramCreditAddsFilterEntity> getBinfilters(final String varId, final String programId) {

        List<VarProgramCreditAddsFilterEntity> varProgramCreditAddsFilters =
            varProgramCreditAddsFilterDao.findByVarProgram(varId, programId);

        if (CollectionUtils.isEmpty(varProgramCreditAddsFilters)) {
            // Check if filter is available at var level.
            varProgramCreditAddsFilters = varProgramCreditAddsFilterDao.findByVarProgram(varId, "-1");
        }

        return varProgramCreditAddsFilters;
    }
}
