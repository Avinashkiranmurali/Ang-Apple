package com.b2s.rewards.apple.controller

import com.b2s.apple.entity.BannerConfigEntity
import com.b2s.apple.entity.BannerEntity
import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.BannerConfigService
import com.b2s.apple.services.CategoryConfigurationService
import com.b2s.rewards.apple.dao.BannerConfigDao
import com.b2s.rewards.apple.dao.BannerConfigurationDao
import com.b2s.rewards.apple.integration.model.BannerConfigResponse
import com.b2s.rewards.apple.model.BannerConfiguration
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.google.common.base.Charsets
import com.google.common.io.Resources
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

/**
 *  Created by srukmagathan on 2/19/2016.
 */
class BannerControllerSpec extends Specification {

    private BannerConfigurationDao bannerConfigurationDao;

    private BannerConfigDao bannerConfigDao

    private BannerConfigService bannerConfigService;

    private BannerController controller;

    private CategoryConfigurationService categoryConfigurationService;

    private AppSessionInfo appSessionInfo;

    private MockMvc mvc;

    private MockHttpSession session;

    private User user;


    def setup() {

        bannerConfigurationDao = Mock();
        categoryConfigurationService = Mock();
        bannerConfigDao = Mock();
        appSessionInfo = Mock();
        bannerConfigService = new BannerConfigService(bannerConfigDao: bannerConfigDao, appSessionInfo: appSessionInfo);
        controller = new BannerController(bannerConfigurationDao: bannerConfigurationDao,
                appSessionInfo: appSessionInfo,
                bannerConfigService: bannerConfigService);
        session = Mock();
        user = new User();
        mvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def cleanup() {

        controller = null;
        bannerConfigurationDao = null;
        session = null;
        user = null;
        mvc = null;
    }


    @Unroll
    def "getBanner - default banner configuration"() {

        given:
        appSessionInfo.currentUser() >> getUser('-1', '-1', Locale.US)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/banner/template")
                .session(session))
                .andReturn()

        then:
        bannerConfigurationDao.getByName(_) >> daoResult
        result.response.status == status
        result.response.contentAsString.contains(keyToCheckInResponseMessage) == isKeyAvailableInResponse


        where:

        keyToCheckInResponseMessage                             | daoResult                                     || status || isKeyAvailableInResponse
        "stacks"                                                | getDefaultBannerConfig("-1", "-1")            || 200    || true
        "tiles"                                                 | getDefaultBannerConfig("-1", "-1")            || 200    || false
        "No Banner found for the given var/program/locale/name" | getDefaultBannerConfig("GrassRootsUK", "DRP") || 204    || true
        "No Banner found for the given var/program/locale/name" | null                                          || 204    || true
        "Unexpected error occurred while fetching Banner"       | getDefaultBannerConfigWithNullJson()          || 500    || true
    }

    @Unroll
    def "getBannerConfig - test config disable category"() {
        given:
        appSessionInfo.currentUser() >> getUser('RBC', 'b2s_qa_only', Locale.CANADA)
        def request = Mock(HttpServletRequest)

        when:
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram('RBC', 'b2s_qa_only')

        appSessionInfo.getCategories() >> getCategoryMap()
        bannerConfigDao.findByVarProgramLocale(_, _, _) >> getEntitiesBasedOnCategoryDisable(categoryValue, disable, configType)
        def result = mvc.perform(MockMvcRequestBuilders.get("/banner/config")
                .session(session))
                .andReturn()
        isEmpty = (result.response.contentAsString.size() == 0)

        then:
        result.response.contentAsString.contains(response)
        result.response.status == statusCode

        where:
        categoryValue | configType |  disable  | isEmpty || response                     || statusCode
        'music'       | 'product'  | 'false'   | false   || 'default Desktop image'      || 200
        'music'       | 'product'  | 'true'    | false   || ''                           || 200
        'mac'         | 'family'   | 'true'    | false   || ''                           || 200
        'mac'         | 'family'   | 'false'   | false   || 'default Desktop image'      || 200
    }

    @Unroll
    def "getBannerConfig - test different configurations with same var program"() {
        given:
        appSessionInfo.currentUser() >> getUser('RBC', 'b2s_qa_only', Locale.CANADA)
        def request = Mock(HttpServletRequest)

        when:
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram('RBC', 'b2s_qa_only')

        appSessionInfo.getCategories() >> getCategoryMap()
        bannerConfigDao.findByVarProgramLocale(_, _, _) >> bannerConfigEntities
        def result = mvc.perform(MockMvcRequestBuilders.get("/banner/config")
                .session(session))
                .andReturn()

        then:
        result.response.contentAsString.contains(response)

        where:
        bannerConfigEntities || response
        defaultConf()        || 'default desktop image'
        localeVarConf()      || 'RBC-en_CA desktop image'
        localeConf()         || 'en_CA desktop image'
        varConf()            || 'RBC desktop image'
        varProgramConf()     || 'RBC-AIB musicDesktop image'
        specificConf()       || 'RBC-AIB-en_CA musicDesktop image'
        localeVarConfigs()   || 'RBC-en_CA desktop image'
        localeConfigs()      || 'en_CA desktop image'
        varConfigs()         || 'RBC desktop image'
        varProgramConfigs()  || 'RBC-AIB musicDesktop image'
        specificConfigs()    || 'RBC-AIB-en_CA musicDesktop image'
    }

    @Unroll
    def "getBannerConfig - test config with different locale var program combination"() {
        given:
        appSessionInfo.currentUser() >> getUser(varId, programId, new Locale(locale))
        def request = Mock(HttpServletRequest)

        when:
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram(varId, programId)

        appSessionInfo.getCategories() >> getCategoryMap()
        bannerConfigDao.findByVarProgramLocale(_, _, _) >> getMatchingEntities(varId, programId, locale)
        def result = mvc.perform(MockMvcRequestBuilders.get("/banner/config")
                .session(session))
                .andReturn()

        then:
        result.response.contentAsString.contains(response)

        where:
        varId       | programId     | locale  || response
        'UA'        | 'MP'          | 'en_US' || 'UA desktop image'
        'UA'        | 'qa_only'     | 'en_US' || 'default desktop image'
        'UA'        | 'qa_only'     | 'en_CA' || 'default desktop image'
        'RBC'       | 'b2s_qa_only' | 'fr_CA' || 'RBC desktop image'
        'RBC'       | 'AIB'         | 'fr_CA' || 'RBC-AIB musicDesktop image'
        'Scotia'    | 'Amex'        | 'en_CA' || 'en_CA desktop image'
        'RBC'       | 'b2s_qa_only' | 'en_CA' || 'RBC desktop image'
        'RBC'       | 'AIB'         | 'en_CA' || 'RBC-AIB-en_CA musicDesktop image'
        'RBC'       | 'apple_qa'    | 'en_US' || 'RBC desktop image'
        'RBC'       | 'qa_only'     | 'en_US' || 'RBC desktop image'
        'RBC'       | 'apple_qa'    | 'en_CA' || 'RBC desktop image'
        'FDR'       | 'b2s_qa_only' | 'en_US' || 'default desktop image'
        'WF'        | 'qa_only'     | 'en_CA' || 'default desktop image'
        'Chase'     | 'qa_only'     | 'en_US' || 'default desktop image'
        'VitalityUS'| 'qa_only'     | 'en_US' || 'default desktop image'
        'WF'        | 'qa_only'     | 'en_US' || 'default desktop image'
        'Default'   | 'Default'     | 'fr_CA' || 'default desktop image'

    }

    @Unroll
    def 'test getBannerConfig - with different Entities combination'() {
        given:
        bannerConfigDao.findByVarProgramLocale(_, _, _) >> bannerEntities
        appSessionInfo.currentUser() >> getUser("UA", "MP", new Locale("en_US"))
        def request = Mock(HttpServletRequest)

        when:
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram("UA", "MP")
        appSessionInfo.getCategories() >> getCategoryMap()

        def result = mvc.perform(MockMvcRequestBuilders.get("/banner/config")
                .session(session))
                .andReturn()

        then:
        result.response.contentAsString.contains(configType)
        result.response.contentAsString.contains(category)
        result.response.contentAsString.contains(name)

        where:
        bannerEntities          || configType     || category    || name
        defaultConf()           || 'family'       || 'mac'       || 'desktopImage'
        UAVarProgramConf()      || 'landing'      || 'music'     || 'desktopImage'
        VarGenericConf()        || 'family'       || 'imac'      || 'mobileImage'
        getMatchingConfigs()    || 'family'       || 'mac'       || 'desktopImage'
        allConfigs()            || 'landing'      || 'ipad'      || 'minimumFontSize'
        allConfigs()            || 'family'       || 'ipad'      || 'minimumFontSize'
    }

    @Unroll
    def 'test getBannerConfig - with different name value combination'() {
        given:
        bannerConfigDao.findByVarProgramLocale(_, _, _) >> bannerEntities
        appSessionInfo.currentUser() >> getUser("UA", "MP", new Locale("en_US"))
        def request = Mock(HttpServletRequest)

        when:
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram("UA", "MP")
        appSessionInfo.getCategories() >> getCategoryMap()

        final response = controller.getBanners();

        then:
        final Map<String, List<BannerConfigResponse>> bannerResponse = response.getBody()
        final Map<String, Object> configs = bannerResponse.get("family").get(0).getConfig();
        expectedValue == configs.get(name)

        where:
        bannerEntities                      || name                 || expectedValue
        getNameValueCombinationEntities()   || "disable"            || false
        getNameValueCombinationEntities()   || "name"               || "value"
        getNameValueCombinationEntities()   || "showButton"         || true
        getNameValueCombinationEntities()   || "desktopImageURL"    || "www.imageurl.com"
    }

    def getDefaultBannerConfigWithNullJson() {

        def bannerConfiguration = new BannerConfiguration()
        bannerConfiguration.id = 1;
        bannerConfiguration.activeInd = "Y"
        bannerConfiguration.locale = "en_US"
        bannerConfiguration.name = "apple_banner_en_US"
        bannerConfiguration.programId = "-1"
        bannerConfiguration.varId = "-1"
        bannerConfiguration.value = "{}"
    }

    def getDefaultBannerConfig(varId, prgmId) {

        def bannerConfiguration = new BannerConfiguration()
        bannerConfiguration.id = 1;
        bannerConfiguration.activeInd = "Y"
        bannerConfiguration.locale = "en_US"
        bannerConfiguration.name = "apple_banner_en_US"
        bannerConfiguration.programId = prgmId
        bannerConfiguration.value = Resources.toString(Resources.getResource("Banner.json"), Charsets.UTF_8);
        bannerConfiguration.varId = varId

        return Arrays.asList(bannerConfiguration)
    }

    def getUser(def varId, def programId, def locale) {
        return new User(varId: varId, programId: programId, locale: locale)
    }

    def getProgram(def varId, def programId) {
        return new Program(varId: varId, programId: programId)
    }

    def getEntitiesBasedOnCategoryDisable(def categoryValue, def disable, def configType) {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1', 'disable', disable, true, configType, categoryValue),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1', 'DesktopImage', 'default Desktop image', true, configType, categoryValue),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1', 'MobileImage', 'default Mobile image', true, configType, categoryValue),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1', 'displayOrder', 'music,mac', true, configType, "-1")
        ]
    }

    def getDefaultCategoryEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                'name', 'value', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'disable', false, true, 'family', 'mac')]
    }

    def getNameValueCombinationEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                'name', 'value', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'disable', false, true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'showButton', true, true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'desktopImageURL', 'www.imageurl.com', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'displayOrder', 'mac', true, 'family', '-1')]
    }

    def defaultConf() {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                'desktopImage', 'default desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'displayOrder', 'mac', true, 'family', '-1')
        ]
    }

    def defaultWhatsNewConf() {
        return [getBannerConfigEntity('1', getBannerWhatsNewEntity(), '-1', '-1', '-1',
                'desktopImage', 'default desktop image', true, 'whatsnew', 'mac'),
                getBannerConfigEntity('1', getBannerWhatsNewEntity(), '-1', '-1', '-1',
                        'displayOrder', 'mac', true, 'whatsnew', '-1')
        ]
    }

    def varConfigs() {
        def varConfigs = new ArrayList()
        varConfigs.addAll(varConf())
        varConfigs.addAll(localeConf())
        varConfigs.addAll(defaultConf())
        return varConfigs
    }

    def UAVarProgramConf() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US', 'desktopImage',
                'UA desktop image', true, 'family', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US',
                        'displayOrder', 'music', true, 'family', '-1')
        ]
    }

    def VarGenericConf() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'UA', '-1', 'en_US', 'mobileImage',
                'UA desktop image', true, 'family', 'imac'),
                getBannerConfigEntity('1', getBannerEntity(), 'UA', '-1', 'en_US',
                        'displayOrder', 'imac', true, 'family', '-1')
        ]
    }

    def getMatchingConfigs () {
        return [getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US', 'desktopImage',
                'UA desktop image', true, 'landing', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US', 'desktopImage',
                        'UA desktop image', true, 'landing', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'desktopImage', 'default desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'displayOrder', 'music,mac', true, 'family', '-1')
        ]
    }

    def varConf() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', '-1', '-1', 'RBCdesktopImage',
                        'RBC desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), 'RBC', '-1', '-1',
                        'displayOrder', 'mac', true, 'family', '-1')
        ]
    }

    def localeConfigs() {
        def varConfigs = new ArrayList()
        varConfigs.addAll(defaultConf())
        varConfigs.addAll(localeConf())
        return varConfigs
    }

    def localeConf() { //not expected configuration
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', 'en_CA', 'enCAdesktopImage',
                'en_CA desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', 'en_CA',
                        'displayOrder', 'mac', true, 'family', '-1')
        ]
    }

    def getEnglishCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', 'en_CA', 'enCAMusicdesktopImage',
                'en_CA musicDesktop image', true, 'family', 'music')]
    }

    def getFrenchCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', 'fr_CA', 'frCAMusicdesktopImage',
                'fr_CA musicDesktop image', true, 'family', 'music')]
    }

    def allConfigs() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US', 'minimumFontSize',
                'UA desktop image', true, 'family', 'ipad'),
                getBannerConfigEntity('1', getBannerEntity(), 'UA', 'MP', 'en_US', 'desktopImage',
                        'UA desktop image', true, 'landing', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'desktopImage', 'default desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'displayOrder', 'music,ipad', true, 'family', '-1'),
                getBannerConfigEntity('1', getBannerEntity(), '-1', '-1', '-1',
                        'displayOrder', 'music', true, 'landing', '-1')
        ]
    }

    def varProgramConfigs() {
        def varConfigs = new ArrayList()
        varConfigs.addAll(varConf())
        varConfigs.addAll(localeConf())
        varConfigs.addAll(localeVarConf())
        varConfigs.addAll(defaultConf())
        varConfigs.addAll(varProgramConf())
        return varConfigs
    }

    def varProgramConf() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', 'AIB', '-1', 'RBC-AIBdesktopImage',
                'RBC-AIB musicDesktop image', true, 'family', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), 'RBC', 'AIB', '-1',
                        'displayOrder', 'music', true, 'family', '-1')
        ]
    }

    def localeVarConfigs() {
        def varConfigs = new ArrayList()
        varConfigs.addAll(varConf())
        varConfigs.addAll(localeConf())
        varConfigs.addAll(localeVarConf())
        varConfigs.addAll(defaultConf())
        return varConfigs
    }

    def localeVarConf() {
        return getVarEnglishCanadianBannerEntities()
    }

    def specificConfigs() {
        def specificConfigs = new ArrayList()
        specificConfigs.addAll(specificConf())
        specificConfigs.addAll(localeVarConf())
        specificConfigs.addAll(defaultConf())
        specificConfigs.addAll(varConf())
        specificConfigs.addAll(localeConf())
        specificConfigs.addAll(varProgramConf())
        return specificConfigs
    }

    def specificConf() {
        return getProgramEnglishCanadianBannerEntities()
    }

    def getVarEnglishCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', '-1', 'en_CA', 'RBC-en_CAdesktopImage',
                'RBC-en_CA desktop image', true, 'family', 'mac'),
                getBannerConfigEntity('1', getBannerEntity(), 'RBC', '-1', '-1',
                        'displayOrder', 'mac', true, 'family', '-1')
        ]
    }

    def getVarFrenchCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', '-1', 'fr_CA', 'RBCMusicdesktopImage',
                'RBC musicDesktop image', true, 'family', 'music')
        ]
    }

    def getProgramEnglishCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', 'AIB', 'en_CA', 'RBC-AIB-en_CAMdesktopImage',
                'RBC-AIB-en_CA musicDesktop image', true, 'family', 'music'),
                getBannerConfigEntity('1', getBannerEntity(), 'RBC', 'AIB', 'en_CA',
                        'displayOrder', 'music', true, 'family', '-1')
        ]
    }

    def getProgramFrenchCanadianBannerEntities() {
        return [getBannerConfigEntity('1', getBannerEntity(), 'RBC', 'AIB', 'fr_CA', 'RBC_MusicdesktopImage',
                'RBC musicDesktop image', true, 'family', 'music')
        ]
    }

    def getMatchingEntities(String varId, String programId, String locale) {
        List<BannerConfigEntity> allConfigs = new ArrayList<>()
        allConfigs.addAll(defaultConf())
        allConfigs.addAll(defaultWhatsNewConf())
        allConfigs.addAll(varConf())
        allConfigs.addAll(varProgramConf())
        allConfigs.addAll(localeConf())
        allConfigs.addAll(localeVarConf())
        allConfigs.addAll(specificConf())
        allConfigs.addAll(UAVarProgramConf())

        List<BannerConfigEntity> matchingConfigs = new ArrayList<>()
        for (BannerConfigEntity config : allConfigs) {
            if (config.getProgramId().equalsIgnoreCase(programId)) {
                if (config.getLocale().equalsIgnoreCase(locale)) {
                    if (config.getVarId().equalsIgnoreCase(varId)) {
                        matchingConfigs.add(config)
                    } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                        matchingConfigs.add(config)
                    }
                } else if ("-1".equalsIgnoreCase(config.getLocale())) {
                    if (config.getVarId().equalsIgnoreCase(varId)) {
                        matchingConfigs.add(config)
                    } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                        matchingConfigs.add(config)
                    }
                }
            } else if ("-1".equalsIgnoreCase(config.getProgramId())) {
                if (config.getLocale().equalsIgnoreCase(locale)) {
                    if (config.getVarId().equalsIgnoreCase(varId)) {
                        matchingConfigs.add(config)
                    } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                        matchingConfigs.add(config)
                    }
                } else if ("-1".equalsIgnoreCase(config.getLocale())) {
                    if (config.getVarId().equalsIgnoreCase(varId)) {
                        matchingConfigs.add(config)
                    } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                        matchingConfigs.add(config)
                    }
                }
            }
        }
        return matchingConfigs
    }

    def getBannerConfigEntity(def configId, def bannerEntity, def varId, def programId, def locale, def name, def
            value, def isActive, def configType, def category) {
        return new BannerConfigEntity(configId: configId, bannerEntity: bannerEntity, varId: varId,
                programId: programId, locale: locale, name: name, value: value, isActive: isActive, configType:
                configType, category: category )
    }

    def getCategoryMap() {
        Map<String, String> categoryMap = new HashMap<>()
        categoryMap.put('music', 'Music')
        categoryMap.put('ipad', 'iPad')
        categoryMap.put('watch', 'Watch')
        categoryMap.put('mac', 'Mac')
        categoryMap.put('imac', 'iMac')
        return categoryMap
    }

    def getCategory(def depth, def slug, def name, def i18nName) {
        return new Category(depth: depth, slug: slug, name: name, i18nName: i18nName)
    }

    def getBannerEntity() {
        BannerEntity bannerEntity = new BannerEntity()
        bannerEntity.setBannerId(1)
        bannerEntity.setBannerName("family1")
        bannerEntity.setBannerType("Family")
        bannerEntity.setActive(true)
        return bannerEntity
    }

    def getBannerWhatsNewEntity() {
        BannerEntity bannerEntity = new BannerEntity()
        bannerEntity.setBannerId(1)
        bannerEntity.setBannerName("what's new banner")
        bannerEntity.setBannerType("whatsnew")
        bannerEntity.setActive(true)
        return bannerEntity
    }
}

