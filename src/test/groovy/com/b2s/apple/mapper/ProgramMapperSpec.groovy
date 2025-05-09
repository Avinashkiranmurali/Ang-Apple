package com.b2s.apple.mapper


import com.b2s.rewards.apple.model.ProgramConfig
import com.b2s.rewards.apple.model.RedemptionOption
import com.b2s.rewards.apple.model.VarProgram
import com.b2s.rewards.apple.util.ContextUtil
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.web.B2RReloadableResourceBundleMessageSource
import spock.lang.Specification
import spock.lang.Subject

import java.sql.Timestamp

class ProgramMapperSpec extends Specification {
    def applicationProperties = Mock(Properties)
    def contextUtil = Mock(ContextUtil.class)
    def imageServerUrl = "https://localhost/imageserver"

    @Subject
    def programMapper = new ProgramMapper(imageServerUrl: imageServerUrl, applicationProperties: applicationProperties,
            contextUtil: contextUtil)

    def 'mapper maps/transforms correctly - 1 varProgram'() {
        given: 'the varProgram'
        final def paymentOption = "splitpay"
        final def varProgram =
                new VarProgram(varId: 'WF',
                        programId: 'b2s_qa_only',
                        name: 'Go Far Rewards',
                        active: 'Y',
                        imageUrl: '/apple-gr/vars/wf/logo.png',
                        convRate: 0.0077,
                        pointName: 'wf.points',
                        pointFormat: 'NULL',
                        demo: 'N',
                        remote: 'N',
                        faqs: 'N',
                        enableAcknowledgeTermsConds: 'Y')

        def redemptionOptions = new ArrayList<RedemptionOption>()
        def redemptionOption1 = new RedemptionOption(id:230,
                varId:"WF",
                programId:"b2s_qa_only",
                paymentOption:"splitpay",
                limitType:"dollar",
                paymentMinLimit:0,
                paymentMaxLimit:300,
                orderBy:2,
                isActive:true,
                lastUpdatedBy:"Raji",
                lastUpdatedDate:new Timestamp(System.currentTimeMillis()))
        def redemptionOption2 = new RedemptionOption(id:167,
                varId:"WF",
                programId:"b2s_qa_only",
                paymentOption:"splitpay",
                limitType:"percentage",
                paymentMinLimit:0,
                paymentMaxLimit:80,
                orderBy:2,
                isActive:true,
                lastUpdatedBy:"BRIDGE2SOLUTION\\\\rjesuraj",
                lastUpdatedDate:new Timestamp(System.currentTimeMillis()))
        redemptionOptions.add(redemptionOption1)
        redemptionOptions.add(redemptionOption2)
        varProgram.setRedemptionOptions(redemptionOptions)

        final def configs = new ArrayList<ProgramConfig>()
        final def config = ProgramConfig.builder().withName("logOutUrl")
                .withValue("https://wfbk-uat-mn.epsilon.com/home/LogOutRedir")
                .build()
        configs.add(config)
        final def locale = Locale.US

        when:
        B2RReloadableResourceBundleMessageSource messageSource = Mock()
        contextUtil.getMessageSource(_) >> messageSource
        applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY) >> "https://localhost/imageserver"
        applicationProperties.getProperty("PS3_DEFAULT_CATALOG") >> "apple"
        messageSource.getMessage(_, _, _, _) >> "/apple-gr/vars/wf/logo.png"

        then:
        final def program = programMapper.from(varProgram, configs, locale)

        expect:
        program.config.get("logOutUrl").equals(config.value)
        program.getRedemptionOptions().containsKey(paymentOption) ? (program.getRedemptionOptions().get(paymentOption).size() == 2) : false
        program.enableAcknowledgeTermsConds
    }

    def 'test merge default program config with loaded config'() {
        when:
        final def conf1 = ProgramConfig.builder().withName("name1").withValue("value1").build()
        def mergedConfig = programMapper.getMergedProgramConfigs([conf1])

        then:
        mergedConfig.size() == 3
        mergedConfig.get("key1") == "value1"
        mergedConfig.get("key2") == "value2"
        mergedConfig.get("name1") == "value1"
    }

    def 'test merge default program config with loaded config with overriden config values'() {
        when:
        final def conf1 = ProgramConfig.builder().withName("key1").withValue("value3").build()
        def mergedConfig = programMapper.getMergedProgramConfigs([conf1])

        then:
        mergedConfig.size() == 2
        mergedConfig.get("key1") == "value3"
        mergedConfig.get("key2") == "value2"
    }

    def 'test merge default program config with loaded config with empty loaded config'() {
        when:
        def mergedConfig = programMapper.getMergedProgramConfigs([])

        then:
        mergedConfig.size() == 2
        mergedConfig.get("key1") == "value1"
        mergedConfig.get("key2") == "value2"
    }

}
