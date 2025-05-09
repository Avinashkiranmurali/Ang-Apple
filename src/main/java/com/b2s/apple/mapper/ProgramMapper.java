package com.b2s.apple.mapper;

import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.ContextUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.rewards.common.util.YesOrNo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by rpillai on 6/24/2016.
 */
@Component
public class ProgramMapper {

    private static final Logger logger = LoggerFactory.getLogger(ProgramMapper.class);

    private static final String IMAGE_SERVER_URL = "imageServerUrl";

    @Value("${image.server.url}")
    private String imageServerUrl;

    @Value("${user.program.formatPointName}")
    private String programDefaultFormatPointName;

    private final String PROGRAM_DEFAULT_CONFIG_FILENAME = "defaultUserProgramConfig.json";


    @Autowired
    private Properties applicationProperties;

    @Autowired
    private ContextUtil contextUtil;

    public Program from(VarProgram varProgram, List<ProgramConfig> configs, Locale locale) {
        Program program = null;
        if(varProgram != null) {
            MessageSource messageSource = contextUtil.getMessageSource(varProgram.getVarId());
            program = new Program();
            program.setVarId(varProgram.getVarId());
            program.setProgramId(varProgram.getProgramId());
            program.setConvRate(varProgram.getConvRate());
            program.setBundledPricingOption(BundledPricingOption.fromDatabaseValue(varProgram.getFaqs()));
            program.setName(varProgram.getName());
            program.setPointName(varProgram.getPointName());
            program.setFormatPointName(!StringUtils.isEmpty(program.getPointName()) ? program.getPointName() : programDefaultFormatPointName);
            program.setPointFormat(varProgram.getPointFormat());
            program.setIsActive(YesOrNo.YES.getValue().equalsIgnoreCase(varProgram.getActive()));
            program.setIsDemo(YesOrNo.YES.getValue().equalsIgnoreCase(varProgram.getDemo()));
            program.setIsLocal(YesOrNo.NO.getValue().equalsIgnoreCase(varProgram.getRemote()));
            program.setEnableAcknowledgeTermsConds(YesOrNo.YES.getValue().equalsIgnoreCase(varProgram.getEnableAcknowledgeTermsConds()));
            logger.info("Locale value : {}", locale);
            if(locale != null) {
                program.setTargetCurrency(CurrencyUnit.of(Currency.getInstance(locale).getCurrencyCode().toUpperCase()));
            }
            updateProgramImageUrl(varProgram, locale, program, messageSource);

            program.setConfig(getMergedProgramConfigs(configs));

            //Add Custom Config
            program.getConfig().put(IMAGE_SERVER_URL, imageServerUrl);

            //Add session timeout and warning configuration
            setConfigValueFromPropertiesIfNotAvailable(program,CommonConstants.SESSION_TIMEOUT,CommonConstants.SESSION_TIMEOUT_MINUTES);
            setConfigValueFromPropertiesIfNotAvailable(program,CommonConstants.SESSION_TIMEOUT_WARNING,CommonConstants.SESSION_TIMEOUT_WARNING_MINUTES);

            updateProgramCatalogId(locale, program);
            if (CollectionUtils.isNotEmpty(varProgram.getRedemptionOptions())) {
                final Map<String, List<VarProgramRedemptionOption>> varProgramRedemptionOptionMap =
                    new HashMap<>();

                varProgram.getRedemptionOptions().stream().
                    filter(RedemptionOption::getActive)
                    .forEach(redemptionOption -> {
                        this.buildRedemptionOption(redemptionOption,
                            varProgramRedemptionOptionMap);
                    });
                program.setRedemptionOptions(varProgramRedemptionOptionMap);

            }
        }
        return program;
    }

    public Map<String, Object> getMergedProgramConfigs(final List<ProgramConfig> configs) {
        Map<String, Object> loadedConfig = CollectionUtils.isEmpty(configs) ? Map.of() :
            configs.stream().collect(Collectors.toMap(ProgramConfig::getName, ProgramConfig::getValue));

        final Map<String, Object> defaultConfig = loadProgramDefaultConfig();
        return Stream.of(defaultConfig, loadedConfig)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                v -> v.getValue() instanceof String && BooleanUtils.toBooleanObject((String)v.getValue()) != null ? Boolean.valueOf((String)v.getValue()) : v.getValue(),
                (v1, v2) -> null != v2 ? v2 : v1));
    }

    public Map<String, Object> loadProgramDefaultConfig() {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:" + PROGRAM_DEFAULT_CONFIG_FILENAME);
            jsonFile.setExecutable(false);
            jsonFile.setReadable(true);
            jsonFile.setWritable(false);

            return new ObjectMapper().readValue(jsonFile, Map.class);
        } catch (IOException e) {
            logger.error("Error while reading the default config json file : {} {}", PROGRAM_DEFAULT_CONFIG_FILENAME, e);
        }
        return Map.of();
    }

    private void updateProgramCatalogId(final Locale locale, final Program program) {
        String catalogPrefix = (String) program.getConfig().get(CommonConstants.CONFIG_CATALOG_ID);
        if (StringUtils.isBlank(catalogPrefix)) {
            catalogPrefix = applicationProperties.getProperty("PS3_DEFAULT_CATALOG");
        }
        final String catalogId = AppleUtil.getCategoryIdByLocale(catalogPrefix, locale);
        program.setCatalogId(catalogId);
        program.getConfig().put(CommonConstants.CONFIG_CATALOG_ID, catalogId);
    }

    private void updateProgramImageUrl(final VarProgram varProgram, final Locale locale, final Program program,
        final MessageSource messageSource) {
        try {
            if (StringUtils.isNotBlank(varProgram.getImageUrl())) {
                if(new URI(varProgram.getImageUrl()).isAbsolute()) {
                    program.setImageUrl(varProgram.getImageUrl());
                } else {
                    program.setImageUrl(applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY)+messageSource.getMessage(varProgram.getImageUrl(), null, varProgram.getImageUrl(), locale));
                }
            }
        } catch(Exception e) {
            logger.error("Error while mapping program image url for var id: {}, program id: {}. Exception: {}",program.getVarId(), program.getProgramId(), e);
        }
    }

    public List<ProgramConfig> getProgramConfigs(final List<VarProgramConfigEntity> varProgramConfigs) {
        return varProgramConfigs
                .stream()
            .map(varProgramConfig -> getProgramConfig(varProgramConfig))
                .collect(Collectors.toList());
    }

    public ProgramConfig getProgramConfig(final VarProgramConfigEntity varProgramConfig) {
        ProgramConfig programConfig = null;
        if (Objects.nonNull(varProgramConfig)) {
            programConfig = ProgramConfig.builder()
                .withName(varProgramConfig.getName())
                .withValue(varProgramConfig.getValue())
                .build();
        }
        return programConfig;
    }

    public List<Notification> from(List<VarProgramNotification> varProgramNotifications) {
        List<Notification> notifications = null;

        if(CollectionUtils.isNotEmpty(varProgramNotifications)) {
            notifications = new ArrayList<>();
            try {
                for(VarProgramNotification varProgramNotification : varProgramNotifications) {
                    if(varProgramNotification != null) {
                        Notification notification = new Notification();
                        BeanUtils.copyProperties(notification, varProgramNotification);
                        notifications.add(notification);
                    }
                }
            } catch(Exception e) {
                logger.error("Error while mapping VarProgramNotification object to Notification object for var id:{}, program id: {}. Exception: {}", varProgramNotifications.get(0).getVarId(), varProgramNotifications.get(0).getVarId(), e);
            }

        }
        return notifications;
    }

    private void setConfigValueFromPropertiesIfNotAvailable(Program program, String keyInConfig, String keyInPropertiesFile){

        final String value=CommonConstants.getApplicationProperty(keyInPropertiesFile,program.getVarId(),program.getProgramId(), applicationProperties);
        if (Objects.isNull(program.getConfig().get(keyInConfig)) && StringUtils.isNotBlank(value)){
            program.getConfig().put(keyInConfig, value);
        }

    }

    private Map<String, List<VarProgramRedemptionOption>> buildRedemptionOption(final RedemptionOption redemptionOption,
        final Map<String, List<VarProgramRedemptionOption>> varProgramRedemptionOptionMap) {

            if(Arrays.stream(PaymentOptions.values()).filter(e -> e.getPaymentOption().equals(redemptionOption.getPaymentOption())).findFirst().isPresent()){
            List<VarProgramRedemptionOption> varProgramRedemptionOptionList = varProgramRedemptionOptionMap.get(redemptionOption.getPaymentOption());

            if(varProgramRedemptionOptionList == null) {
                varProgramRedemptionOptionList = new ArrayList<>();
                varProgramRedemptionOptionList.add(buildVarProgramRedemptionOption(redemptionOption));
                varProgramRedemptionOptionMap.put(redemptionOption.getPaymentOption(), varProgramRedemptionOptionList);
            } else {
                varProgramRedemptionOptionList.add(buildVarProgramRedemptionOption(redemptionOption));
            }
        }
        return varProgramRedemptionOptionMap;
    }

    private VarProgramRedemptionOption buildVarProgramRedemptionOption(final RedemptionOption redemptionOption) {
        return VarProgramRedemptionOption.builder().withId(redemptionOption.getId())
                .withVarId(redemptionOption.getVarId())
                .withProgramId(redemptionOption.getProgramId())
                .withPaymentOption(redemptionOption.getPaymentOption())
                .withLimitType(redemptionOption.getLimitType())
                .withPaymentMinLimit(redemptionOption.getPaymentMinLimit())
                .withPaymentMaxLimit(redemptionOption.getPaymentMaxLimit())
                .withOrderBy(redemptionOption.getOrderBy())
                .withActive(redemptionOption.getActive())
                .withPaymentProvider(redemptionOption.getPaymentProvider())
                .withLastUpdatedBy(redemptionOption.getLastUpdatedBy())
                .withLastUpdatedDate(redemptionOption.getLastUpdatedDate()).build();
    }

}
