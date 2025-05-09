package com.b2s.rewards.apple.controller

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.apple.services.CategoryConfigurationService
import com.b2s.apple.services.DomainVarMappingService
import com.b2s.apple.services.ImageServerVersionService
import com.b2s.apple.services.MaintenanceMessageService
import com.b2s.apple.services.OrderHistoryService
import com.b2s.apple.services.ProgramService
import com.b2s.apple.services.VarProgramMessageService
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.CategoryConfiguration
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.CitiNavigationTarget
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.security.controller.ValidateLoginController
import com.b2s.rewards.security.util.ExternalUrlConstants
import com.b2s.service.product.client.application.search.ProductSearchRequest
import com.b2s.service.product.common.domain.SpellCheckInfo
import com.b2s.service.product.common.domain.Suggestion
import com.b2s.service.product.common.domain.response.ProductSearchDocumentGroup
import com.b2s.service.product.common.domain.response.ProductSearchResponse
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.VAROrderManagerChase
import com.b2s.shop.common.order.var.VarOrderManagerHolder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.servlet.ServletContext
import java.security.Principal

import static com.b2s.rewards.security.controller.ValidateLoginController.BASE_DEEPLINK_CURATED_URL
import static com.b2s.rewards.security.controller.ValidateLoginController.BASE_DEEPLINK_URL
import static com.b2s.rewards.security.controller.ValidateLoginController.CONFIGURE
import static com.b2s.rewards.security.controller.ValidateLoginController.DEEPLINK_URL_CART
import static com.b2s.rewards.security.controller.ValidateLoginController.FAIL_VIEW
import static com.b2s.rewards.security.controller.ValidateLoginController.REDIRECT
import static com.b2s.rewards.security.controller.ValidateLoginController.SUCCESS_VIEW;

class ValidateLoginControllerSpec extends Specification {

    def productServiceV3 = Mock(ProductServiceV3)
    def categoryConfigurationService = Mock(CategoryConfigurationService)
    def orderHistoryService = Mock(OrderHistoryService)
    def domainVarMappingService = Mock(DomainVarMappingService)
    def varOrderManagerHolder = Mock(VarOrderManagerHolder)
    def programService = Mock(ProgramService)
    def applicationProperties = Mock(Properties)
    def servletContext = Mock(ServletContext)
    def varProgramMessageService = Mock(VarProgramMessageService)
    def imageServerVersionService = Mock(ImageServerVersionService)
    def maintenanceMessageService = Mock(MaintenanceMessageService)

    final request = new MockHttpServletRequest()
    final session = new MockHttpSession()

    @Subject
    final validateLoginController = new ValidateLoginController(productServiceV3: productServiceV3,
            categoryConfigurationService: categoryConfigurationService,
            domainVarMappingService: domainVarMappingService,
            servletContext: servletContext,
            imageServerVersionService: imageServerVersionService,
            varOrderManagerHolder: varOrderManagerHolder,
            applicationProperties: applicationProperties,
            varProgramMessageService: varProgramMessageService,
            maintenanceMessageService: maintenanceMessageService,
            orderHistoryService: orderHistoryService)

    def varOrderManagerChase = new VAROrderManagerChase(
            applicationProperties: applicationProperties,
            programService: programService)

    String LOCALE = "locale"
    String USERID = "userId"
    String VARID = "varId"
    String PROGRAMID = "programId"
    String EMAIL = "email"
    String TELEPHONE = "telephone"
    String POINTBALANCE = "pointBalance"
    String ADDRESS1_ADDRESSLINE1 = "address1.addressLine1"
    String ADDRESS1_ADDRESSLINE2 = "address1.addressLine2"
    String ADDRESS1_COUNTRY = "address1.country"
    String ADDRESS1_POSTAL = "address1.postal"
    String ADDRESS1_STATE = "address1.state"
    String ADDRESS1_CITY = "address1.city"
    String NAVBACKURL = "navBackURL"
    String KEEPALIVEURL = "keepAliveUrl"
    String BROWSEONLY = "browseOnly"
    String SESSIONSTATE = "sessionState"
    String FIRSTNAME = "firstName"
    String LASTNAME = "lastName"
    String ORDER_HISTORY_URL = "orderHistoryUrl"
    String SAML_RELAY_STATE = "RelayState"
    static String ORDER_HISTORY_PAGE = "/order-history/"
    static String WEBSHOP_DEEPLINK = "/webshop/"
    String AND = "%26;"
    String SAML_RELAY_STATE_URL = "https://webapp-vip-saml.apldev.bridge2solutions.net/apple-gr/ssoLoginAction.do?referer=https://dev-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do" + AND

    @Unroll
    def 'test processLogin()'() {
        setup:
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put(CommonConstants.ANALYTICS, analyticsConfig)

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)

        mockSamlRequest(varId)
        request.setParameter(SAML_RELAY_STATE, "https://chase.localhost/apple-gr/ssoLoginAction.do?page=orderHistory")
        request.setAttribute(ORDER_HISTORY_URL, "https://ultimaterewardspointsuat.chase.com/rest/explore-experience/apple?url=/apple-sso/storefront/order-history&AI=6348834")

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL) >> ""
        applicationProperties.getProperty("matomo.endpoint") >> "https://bridge2-dev.innocraft.cloud/"
        applicationProperties.getProperty("rbc.webtrends.endpoint") >> "https://localhost/imageserver/apple-gr/analytics/webtrends.js"
        applicationProperties.getProperty("ensighten.endpoint") >> "//nexus.ensighten.com/citi/grdev/Bootstrap.js"
        applicationProperties.getProperty("ua.tealium.endpoint") >> "//tags.tiqcdn.com/utag/unitedairlines/ual-partners/qa/utag.js"
        applicationProperties.getProperty("heap.endpoint") >> "https://cdn.heapanalytics.com/js"
        applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY) >> "https://localhost/imageserver"
        applicationProperties.getProperty(CommonConstants.CHASE_ANALYTICS_ROOT_URL_KEY) >> "http://ssoanalyticsurl"

        when:
        final String response = validateLoginController.execute(request, null)
        final Program sessionProgram = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT)
        final User sessionUser = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT)

        then:
        response != null
        response == SUCCESS_VIEW + ORDER_HISTORY_PAGE
        sessionProgram != null
        sessionUser != null
        sessionUser.getAdditionalInfo().containsKey(CommonConstants.ORDER_HISTORY_URL)
        sessionProgram.getConfig().get("matomoEndPoint") == matomoEndPoint
        sessionProgram.getConfig().get("webtrendsEndPoint") == webtrendsEndPoint
        sessionProgram.getConfig().get("tealiumEndPoint") == tealiumEndPoint
        sessionProgram.getConfig().get("ensightenEndPoint") == ensightenEndPoint
        sessionProgram.getConfig().get("heapEndPoint") == heapEndPoint

        where:
        varId | analyticsConfig     || matomoEndPoint                         || webtrendsEndPoint                                               || tealiumEndPoint                                                 || ensightenEndPoint                               || heapEndPoint
        'RBC' | "webtrends"         || null                                   || 'https://localhost/imageserver/apple-gr/analytics/webtrends.js' || null                                                            || null                                            || null
        'PNC' | "matomo"            || 'https://bridge2-dev.innocraft.cloud/' || null                                                            || null                                                            || null                                            || null
        'UA'  | "tealium"           || null                                   || null                                                            || '//tags.tiqcdn.com/utag/unitedairlines/ual-partners/qa/utag.js' || null                                            || null
        'SG'  | "ensighten"         || null                                   || null                                                            || null                                                            || '//nexus.ensighten.com/citi/grdev/Bootstrap.js' || null
        'RBC' | "webtrends,matomo"  || 'https://bridge2-dev.innocraft.cloud/' || 'https://localhost/imageserver/apple-gr/analytics/webtrends.js' || null                                                            || null                                            || null
        'UA'  | "tealium,webtrends" || null                                   || null                                                            || '//tags.tiqcdn.com/utag/unitedairlines/ual-partners/qa/utag.js' || null                                            || null
        'UA'  | "tealium,matomo"    || 'https://bridge2-dev.innocraft.cloud/' || null                                                            || '//tags.tiqcdn.com/utag/unitedairlines/ual-partners/qa/utag.js' || null                                            || null
        'FDR' | ""                  || null                                   || null                                                            || null                                                            || null                                            || null
        'UA'  | "heap"              || null                                   || null                                                            || null                                                            || null                                            || 'https://cdn.heapanalytics.com/js'
        'UA'  | "matomo, heap"      || 'https://bridge2-dev.innocraft.cloud/' || null                                                            || null                                                            || null                                            || 'https://cdn.heapanalytics.com/js'
    }

    def 'test generateLoyaltyPartnerUserId()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put(CommonConstants.ANALYTICS, 'heap')

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)

        request.setParameter("userid", "anonymous")
        request.setParameter("varid", varId)
        request.setParameter("programid", "b2s_qa_only")
        request.setSession(session)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase
        programService.getProgram(_, _, _) >> program

        when:
        validateLoginController.execute(request, null)
        final User sessionUser = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT)

        then:
        sessionUser != null
        sessionUser.hashedUserId != null
        sessionUser.hashedUserId.length() == 32
        sessionUser.getHashedUserId().equalsIgnoreCase("5298282145bcd742dc747185fd6bf97e")
    }

    @Unroll
    def 'test deeplinkUrl for WebShop supplied from SAML()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put(CommonConstants.ACTIVE_WEB_SHOPS, webshopsAvailableForVarPrgram)

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)

        mockSamlRequest(varId)
        request.setParameter(SAML_RELAY_STATE, SAML_RELAY_STATE_URL + "shop=" + webshopName)
        request.setAttribute(CommonConstants.ANALYTICS_URL,analyticsUrl)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        def facets = new ArrayList<>();
        ProductSearchRequest.Builder productRequest = Mock(ProductSearchRequest.Builder.class)
        productServiceV3.getProductSearchRequestBuilder(_, _, _, _, _, _, _, _, _, _, _, _, _) >> productRequest
        productServiceV3.searchProducts(_) >> new ProductSearchResponse(getProductSearchDocumentGroupMap(totalProductCount),
                facets, new SpellCheckInfo(true, new ArrayList<Suggestion>()));

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response == responseUrl

        where:
        webshopName   | webshopsAvailableForVarPrgram | totalProductCount || analyticsUrl              || responseUrl
        'demoWebShop' | 'demoWebShop,shop,demo'       | 0                 || null                      || SUCCESS_VIEW
        'demo'        | 'demo'                        | 0                 || null                      || SUCCESS_VIEW
        'demoWebShop' | 'demoWebShop,shop,demo'       | 8                 || null                      || SUCCESS_VIEW + WEBSHOP_DEEPLINK + "demoWebShop"
        'demo'        | 'demo'                        | 8                 || null                      || SUCCESS_VIEW + WEBSHOP_DEEPLINK + "demo"
        'shop'        | 'demoWebShop,demo'            | 0                 || null                      || SUCCESS_VIEW
        ''            | 'demoWebShop,demo'            | 0                 || null                      || SUCCESS_VIEW
        'default'     | '-'                           | 0                 || null                      || SUCCESS_VIEW
        'demoWebShop' | null                          | 8                 || null                      || SUCCESS_VIEW
        null          | 'demoWebShop,demo'            | 0                 || "jp_cmp=cc/mom_nov_2021/" || SUCCESS_VIEW + '?jp_cmp=cc/mom_nov_2021/'
    }

    @Unroll
    def 'test orderHistoryUrl for Anonymous Login based on VPC()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put(CommonConstants.VIEW_ANONYMOUS_ORDER_DETAIL, vpcValue)
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)

        mockSamlRequest(varId)
        request.setParameter("uid", uid)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL) >> ""
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program
        orderHistoryService.updateUserIdIfOrderExist(_, _, _, _, _) >> userOrderExist

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response == responseUrl

        where:
        vpcValue | userOrderExist | uid                                                    || responseUrl
        true     | true           | 'MTIzNDU2fE1hdGhpdmFuYW4uU2Fua2FyYWRvc3NAYmFra3QuY29t' || SUCCESS_VIEW + ORDER_HISTORY_PAGE + '123456'
        true     | true           | ''                                                     || SUCCESS_VIEW
        true     | true           | 'IHw='                                                 || FAIL_VIEW
        true     | true           | 'IHwg'                                                 || SUCCESS_VIEW + ORDER_HISTORY_PAGE + ' '
        true     | true           | 'fA=='                                                 || FAIL_VIEW
        true     | true           | 'M'                                                    || FAIL_VIEW
        true     | true           | 'MTIzND'                                               || FAIL_VIEW
        true     | true           | 'MTIzNDU2'                                             || FAIL_VIEW
        true     | true           | 'GhpdmFuYW4uU2Fua2FyYWRvc'                             || FAIL_VIEW
        true     | false          | 'MTIzNDU2fE1hdGhpdmFuYW4uU2Fua2FyYWRvc3NAYmFra3QuY29t' || FAIL_VIEW
        false    | false          | ''                                                     || SUCCESS_VIEW
    }

    @Unroll
    def 'test customLandingPageUrl based on VPC()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put('landing_page_url', vpcValue)
        program.getConfig().put(CommonConstants.ENABLE_FIVE9_CHAT, true)
        program.getConfig().put(CommonConstants.SET_NEW_XSRF_TOKEN, true)
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)

        mockSamlRequest(varId)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL) >> ""
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response == responseUrl

        where:
        vpcValue           || responseUrl
        'landing_page_url' || SUCCESS_VIEW + 'landing_page_url'
        ''                 || SUCCESS_VIEW
        ' '                || SUCCESS_VIEW
        null               || SUCCESS_VIEW
    }

    @Unroll
    def 'test redirect based on request attributes()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        request.setAttribute(reqAttrKey, reqAttrValue)

        mockSamlRequest(varId)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL) >> ""
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response == responseUrl

        where:
        reqAttrKey                        | reqAttrValue                                    || responseUrl
        CommonConstants.CITI_USER_CONSENT | 'test'                                          || REDIRECT + CitiNavigationTarget.CONSENT.getPath()
        CommonConstants.CITI_RELAY_STATE  | 'relayState'                                    || REDIRECT + 'relayState'
        CommonConstants.ANALYTICS_URL     | 'jp_cmp=cc/mom_nov_2021/ema/freedom_applestore' || SUCCESS_VIEW + '?jp_cmp=cc/mom_nov_2021/ema/freedom_applestore'
        CommonConstants.ANALYTICS_URL     | 'jp_cmp=cc/mom_nov_2021/ema/'                   || SUCCESS_VIEW + '?jp_cmp=cc/mom_nov_2021/ema/'
        'test'                            | 'test'                                          || SUCCESS_VIEW
    }

    @Unroll
    def 'test deeplinkUrl to Product based on RelayState()'() {
        setup:
        String varId = "RBC"
        def user = getUser(varId)
        def program = getProgram(varId)

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)

        mockSamlRequest(varId)
        request.setParameter(SAML_RELAY_STATE, SAML_RELAY_STATE_URL+ key + "=" + value)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase

        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        def facets = new ArrayList<>();
        ProductSearchRequest.Builder productRequest = Mock(ProductSearchRequest.Builder.class)
        productServiceV3.getProductSearchRequestBuilder(_, _, _, _, _, _, _, _, _, _, _, _, _) >> productRequest
        productServiceV3.searchProducts(_) >> new ProductSearchResponse(getProductSearchDocumentGroupMap(8), facets, new SpellCheckInfo(true, new ArrayList<Suggestion>()));

        productServiceV3.getDetailPageProduct(_, _, _, _, _) >> getProductInDetail(slugName)
        categoryConfigurationService.getCategoryConfiguration(_) >> getCategoryConfiguration(categoryConfigDBURL)

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response == responseUrl

        where:
        key                      | value         | slugName              | categoryConfigDBURL             || responseUrl
        CommonConstants.ITEM_ID  | '30001itemId' | 'macbook-pro'         | ''                              || SUCCESS_VIEW + BASE_DEEPLINK_URL + 'mac/macbook-pro/30001itemId'
        CommonConstants.ITEM_ID  | 'itemId'      | 'macbook-pro'         | ''                              || SUCCESS_VIEW + BASE_DEEPLINK_URL + 'mac/macbook-pro/30001itemId'
        CommonConstants.ITEM_ID  | 'itemId'      | 'macbook-pro'         | '/DB/mac/macbook-pro/'          || SUCCESS_VIEW + '/DB/mac/macbook-pro/30001itemId'
        CommonConstants.ITEM_ID  | 'itemId'      | 'acc-mac-accessories' | ''                              || SUCCESS_VIEW + BASE_DEEPLINK_CURATED_URL + 'accessories/mac/acc-mac-accessories/30001itemId'
        CommonConstants.ITEM_ID  | 'itemId'      | 'macbook-pro'         | CONFIGURE + '/mac/macbook-pro/' || SUCCESS_VIEW + CONFIGURE + '/mac/macbook-pro/'
        CommonConstants.ITEM_ID  | ''            | 'macbook-pro'         | ''                              || SUCCESS_VIEW
        CommonConstants.CATEGORY | 'category'    | 'macbook-pro'         | ''                              || SUCCESS_VIEW
        CommonConstants.CATEGORY | 'category'    | 'macbook-pro'         | '/DB/mac/macbook-pro/'          || SUCCESS_VIEW + '/DB/mac/macbook-pro/'
        CommonConstants.CATEGORY | 'category'    | 'acc-mac-accessories' | ''                              || SUCCESS_VIEW
        CommonConstants.CATEGORY | 'category'    | 'macbook-pro'         | CONFIGURE + '/mac/macbook-pro/' || SUCCESS_VIEW + CONFIGURE + '/mac/macbook-pro/'
        CommonConstants.CATEGORY | ''            | 'macbook-pro'         | ''                              || SUCCESS_VIEW
        CommonConstants.PRODUCT  | 'product'     | 'macbook-pro'         | ''                              || SUCCESS_VIEW
        CommonConstants.PRODUCT  | 'product'     | 'macbook-pro'         | '/DB/mac/macbook-pro/'          || SUCCESS_VIEW + '/DB/mac/macbook-pro/'
        CommonConstants.PRODUCT  | 'product'     | 'acc-mac-accessories' | ''                              || SUCCESS_VIEW
        CommonConstants.PRODUCT  | 'product'     | 'macbook-pro'         | CONFIGURE + '/mac/macbook-pro/' || SUCCESS_VIEW + CONFIGURE + '/mac/macbook-pro/'
        CommonConstants.PRODUCT  | ''            | 'macbook-pro'         | ''                              || SUCCESS_VIEW
        CommonConstants.SUPPLIER | 'applecart'   | 'macbook-pro'         | ''                              || SUCCESS_VIEW + DEEPLINK_URL_CART
    }

    def 'test maintenanceMessage()'() {
        setup:
        String varId = "UA"
        def user = getUser(varId)
        def program = getProgram(varId)
        program.getConfig().put(CommonConstants.ANALYTICS, "matomo,webtrends")

        mockSamlRequest(varId)

        varOrderManagerHolder.getVarOrderManager(_) >> varOrderManagerChase
        varOrderManagerChase.getDemoUserFromDB(_, _, _, _, _, _) >> user
        varProgramMessageService.getMessages(_, _, _) >> new Properties()
        programService.getProgram(_, _, _) >> program

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)

        maintenanceMessageService.getMaintenanceMessage(_, _) >> "Display Maintenance Message"

        when:
        final String response = validateLoginController.execute(request, null)

        then:
        response != null
        response == ValidateLoginController.MAINTENANCE_PAGE_VIEW
    }

    def mockSamlRequest(String varId) {
        request.setParameter("userid", "User ID")
        request.setParameter("varid", varId)
        request.setParameter("programid", "b2s_qa_only")
        request.setParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP, "SAMLResponse")

        request.setAttribute(LOCALE, "en_US")
        request.setAttribute(USERID, "User ID")
        request.setAttribute(VARID, varId)
        request.setAttribute(PROGRAMID, "b2s_qa_only")
        request.setAttribute(NAVBACKURL, "http://chase.com/navBack")
        request.setAttribute(KEEPALIVEURL, "http://chase.com/keepAlive")
        request.setAttribute(BROWSEONLY, "false")
        request.setAttribute(SESSIONSTATE, "ewoJInByb2R1Y3RUeXBlcyI6ICIwMDA4MCIsCgkicHJvZHVjdE51bWJlcnMiOiAiOTBkSVhmSTRJeDQwV0JKdXRRd0NTcmhLeHdhZDRXTzZ1bTBiY3lZRUpEVXVORkJGSmRURFV4RHZHZTcyMWlVYytoIiwKCSJjdXN0b21lcklkIjogIjUyNjM1MjM5IiwKCSJhY2NvdW50SW5kZXgiOiAiMTAwMTAyMSIsCgkiY2lnQ2FjaGVJZCI6ICIzMjMzNCIsCgkic2VnbWVudGF0aW9uQ29kZSI6ICJTQVBQSElSRVJTUlYiCn0")
        request.setAttribute(FIRSTNAME, "First Name")
        request.setAttribute(LASTNAME, "Last Name")

        request.setAttribute(ADDRESS1_ADDRESSLINE1, "5 th cross street ,Atlanta")
        request.setAttribute(ADDRESS1_ADDRESSLINE2, "7 th cross street ,Atlanta")
        request.setAttribute(ADDRESS1_POSTAL, "12345")
        request.setAttribute(ADDRESS1_STATE, "NY")
        request.setAttribute(ADDRESS1_COUNTRY, "US")
        request.setAttribute(ADDRESS1_CITY, "Wilmington")
        request.setAttribute(EMAIL, "ab_ZA@bd.com")
        request.setAttribute(TELEPHONE, "542524323")
        request.setAttribute(POINTBALANCE, "1000")

        request.setUserPrincipal(new Principal() {
            @Override
            String getName() {
                return "userPrincipal"
            }
        })

        request.setSession(session)
    }

    def getCategoryConfiguration(String dbDeepLinkUrl) {
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration(1, "categoryName")
        categoryConfiguration.setPsid("psid")
        categoryConfiguration.setEngraveBgImageLocation("imageUrl")
        categoryConfiguration.setEngravable(false)
        categoryConfiguration.setDeepLinkUrl(dbDeepLinkUrl)
        return categoryConfiguration
    }

    def getProductInDetail(String slugName){
        Product product = new Product()
        List<Category> categories = new ArrayList<Category>()
        List<Category> parents = new ArrayList<>()
        Category category = new Category()
        category.setSlug(slugName)
        Category parentCategory = new Category();
        parentCategory.setSlug("mac")
        parents.add(parentCategory)
        category.setParents(parents)
        categories.add(category)
        product.setCategory(categories)
        return product
    }

    DemoUserEntity getDemoUserEntity() {
        DemoUserEntity demoUserEntity = new DemoUserEntity()
        DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId()
        demoUserId.setUserId("UserID")
        demoUserId.setVarId("Chase")
        demoUserId.setProgramId("b2s_qa_only")
        demoUserEntity.setDemoUserId(new DemoUserEntity.DemoUserId())
        demoUserEntity.setActiveind("Y")
        demoUserEntity.setDemoUserId()
        demoUserEntity.setPoints(100)
        demoUserEntity.setFirstname("Firstname")
        demoUserEntity.setLastname("Lastname")
        demoUserEntity.setAddr1("Addr1")
        demoUserEntity.setAddr2("Addr2")
        demoUserEntity.setCity("City")
        demoUserEntity.setState("State")
        demoUserEntity.setZip("Zip")
        demoUserEntity.setCountry("Country")
        demoUserEntity.setPhone("Phone")
        demoUserEntity.setEmail("Email")
        return demoUserEntity
    }

    User getUser(String varId){
        def user = new User()
        user.setVarId(varId)
        return user
    }

    Program getProgram(String varId) {
        Program program = new Program()
        program.setVarId(varId)
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        return program
    }

    Map<String, ProductSearchDocumentGroup> getProductSearchDocumentGroupMap(int totalProductCount) {
        Map<String, ProductSearchDocumentGroup> productSearchDocumentGroupMap = new HashMap<>();
        productSearchDocumentGroupMap.put("DEFAULT_GROUP", ProductSearchDocumentGroup.builder().withTotalFound(totalProductCount).build());
        return productSearchDocumentGroupMap;
    }
}
