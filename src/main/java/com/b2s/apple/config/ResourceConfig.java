package com.b2s.apple.config;

import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_ENCODING_UTF8;

/**
 * @author rkumar 2020-02-05
 */
@Configuration
public class ResourceConfig {

    private static final String CLASSPATH_MESSAGES = "classpath:messages";
    private static final String[] BASE_NAME_B2S_MESSAGE =
        {"classpath:b2s_override_messages", "classpath:b2s_messages", CLASSPATH_MESSAGES};
    private static final String WEB_INF_I_18_N_B_2_R_REWARDSTEP_RESOURCES = "/WEB-INF/i18n/b2r_rewardstep_resources";
    private static final String[] BASE_NAME_MESSAGE_REWARD_STEP =
        {CLASSPATH_MESSAGES, WEB_INF_I_18_N_B_2_R_REWARDSTEP_RESOURCES};
    private static final String[] BASE_NAME_PUBLIC_MESSAGES = {"classpath:public_messages"};
    private static final String[] BASE_NAME_MESSAGES = {CLASSPATH_MESSAGES};
    private static final String[] BASE_NAME_IDEO_MESSAGES = {"classpath:ideo_messages", CLASSPATH_MESSAGES};
    private static final String[] BASE_NAME_EPP_MESSAGE_REWARD_STEP =
        {"classpath:epp_messages", CLASSPATH_MESSAGES, WEB_INF_I_18_N_B_2_R_REWARDSTEP_RESOURCES};
    private static final String[] BASE_NAME_USM_MESSAGE_REWARD_STEP =
        {"classpath:usm_messages", CLASSPATH_MESSAGES, WEB_INF_I_18_N_B_2_R_REWARDSTEP_RESOURCES};
    private static final String[] BASE_NAME_CITI_MESSAGE_REWARD_STEP =
        {"classpath:citi_messages", CLASSPATH_MESSAGES, WEB_INF_I_18_N_B_2_R_REWARDSTEP_RESOURCES};

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSource() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource publicMessageSource() {
        return setDefaultMessageSource(BASE_NAME_PUBLIC_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceB2S() {
        return setDefaultMessageSource(BASE_NAME_B2S_MESSAGE);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceBSWIFT() {
        return setDefaultMessageSource(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceIDEO() {
        return setDefaultMessageSource(BASE_NAME_IDEO_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSource1() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceVitalityUS() {
        return setDefaultMessageSource(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceRBC() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceEPP() {
        return setDefaultMessageSource(BASE_NAME_EPP_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceAmex() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceUSM() {
        return setDefaultMessageSource(BASE_NAME_USM_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceVitalityUK() {
        return setDefaultMessageSource(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceSCOTIA() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceWeightWatchers() {
        return setDefaultMessageSource(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceUA() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceVirginAU() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceDelta() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceChase() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceAmexAU() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceFDR() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean(name = "messageSourceFDR_PSCU")
    public B2RReloadableResourceBundleMessageSource messageSourceFDRPSCU() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceVitalityCA() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourcePNC() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceWF() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceGrassRootsUK() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceAU() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceSG() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceMY() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceMX() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceHK() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceTW() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourcePH() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceAE() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceTH() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_CITI_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceAWP() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceDemo() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGES);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceCitiFintech() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceLAUSD() {
        return setDefaultMessageSourceWithFallBack(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    @Bean
    public B2RReloadableResourceBundleMessageSource messageSourceFSV() {
        return setDefaultMessageSource(BASE_NAME_MESSAGE_REWARD_STEP);
    }

    private B2RReloadableResourceBundleMessageSource setDefaultMessageSource(final String[] baseNames) {
        final B2RReloadableResourceBundleMessageSource messageSource = new B2RReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(60);
        messageSource.setDefaultEncoding(DEFAULT_ENCODING_UTF8);
        messageSource.setBasenames(baseNames);
        return messageSource;
    }

    private B2RReloadableResourceBundleMessageSource setDefaultMessageSourceWithFallBack(final String[] baseNames) {
        final B2RReloadableResourceBundleMessageSource messageSource = setDefaultMessageSource(baseNames);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

}
