package com.b2s.rewards.apple.controller

import com.b2s.apple.entity.VarProgramConfigEntity
import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.SearchRedirectService
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.dao.VarProgramConfigDao
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.service.product.client.application.search.ProductSearchRequest
import com.b2s.shop.common.User
import com.google.common.collect.Sets
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification
import spock.lang.Subject
/**
 * Created by ppalpandi on 5/13/2019.
 */
class ProductsControllerSpec extends Specification{

    def productServiceV3 = Mock(ProductServiceV3)
    def varProgramConfigDao = Mock(VarProgramConfigDao)
    def user = getUser()
    final request = new MockHttpServletRequest()
    final session = new MockHttpSession()
    def searchRedirectService = Mock(SearchRedirectService)
    final appSessionInfo = Mock(AppSessionInfo)
    def applicationProperties = Mock(Properties)

    @Subject
    final productsController = new ProductsController(productServiceV3: productServiceV3,
            searchRedirectService: searchRedirectService,varProgramConfigDao:varProgramConfigDao,
            appSessionInfo: appSessionInfo, applicationProperties: applicationProperties)

    def 'test filterProducts()'(){

        given:
        def productRequest = getProductRequest()
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)
        appSessionInfo.currentUser() >> user

        when:
        final response = productsController.filterProducts(productRequest, request )

        then:
        response.statusCodeValue == 200

    }

    def 'test getProducts() - invalid request'(){

        when:
        final response = productsController.getProducts(null, null, null, 12, 0, null, null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING",false, getFacetsFilters(), true)

        then:
        response.statusCodeValue == 400
        response.body.toString().contains("Invalid request")

    }


    def 'test getProducts()'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_,_) >> new ProductResponse()

        when:
        final response = productsController.getProducts(Sets.newHashSet("macbook-pro"), null, null, 12, 0, null, null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING",false, getFacetsFilters(), true)

        then:
        response.statusCodeValue == 200

    }


    def 'test getProducts search call ALTERNATE'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_) >> new ProductResponse()
        searchRedirectService.getSearchRedirect(_, _, _) >> getAlternateSearchResponse()

        when:
        final response = productsController.getProducts(Sets.newHashSet(), null, null, 12, 0, "adaptor", null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING",false, new HashMap<String, List<Option>>(), true)

        then:
        response.statusCodeValue == 200
        ProductResponse productResponse = response.getBody()
        productResponse.getSearchRedirect().getAlternateSearchText() == "adapter"

    }


    def 'test getProducts search call REDIRECT'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_,_) >> new ProductResponse()
        searchRedirectService.getSearchRedirect(_, _, _) >> getRedirectSearchResponse()

        when:
        final response = productsController.getProducts(Sets.newHashSet(), null, null, 12, 0, "airpod pro", null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING",false, new HashMap<String, List<Option>>(), true)

        then:
        response.statusCodeValue == 200
        ProductResponse productResponse = response.getBody()
        productResponse.getSearchRedirect().getRedirectURL() == "#/store/browse/music/music-airpods/"

    }


    def 'test getProducts search call REDIRECT_ON_NO_RESULT EmptyPSResponse'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_) >> new ProductResponse()
        searchRedirectService.getSearchRedirect(_, _, _) >> getRedirectOnNoResultSearchResponse()

        when:
        final response = productsController.getProducts(Sets.newHashSet(), null, null, 12, 0, "airfly", null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING", false, new HashMap<String, List<Option>>(), true)

        then:
        response.statusCodeValue == 200
        ProductResponse productResponse = response.getBody()
        productResponse.getSearchRedirect().getRedirectURL() == "#/store/curated/accessories/all-accessories/all-accessories-wireless-headphones"

    }


    def 'test getProducts search call REDIRECT_ON_NO_RESULT WithPSResponse'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_) >> getProducts()
        searchRedirectService.getSearchRedirect(_, _, _) >> getRedirectOnNoResultSearchResponse()

        when:
        final response = productsController.getProducts(Sets.newHashSet(), null, null, 12, 0, "airfly", null, "DISPLAY_PRICE",
                false, getUser(), request, "ASCENDING", false, new HashMap<String, List<Option>>(), true)

        then:
        response.statusCodeValue == 200
        ProductResponse productResponse = response.getBody()
        productResponse.getSearchRedirect() == null

    }

    def 'test getProducts  with Configuration-SortOptionOrder'(){
        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)

        and:
        appSessionInfo.currentUser() >> user
        productServiceV3.getProductSearchRequestBuilder(_,_,_,_,_,_,_,_,_,_,_,_) >> new ProductSearchRequest.Builder()
        productServiceV3.getProducts(_,_,_,_,_) >> getProductsSort()
        searchRedirectService.getSearchRedirect(_, _, _) >> getRedirectSearchResponse()
        varProgramConfigDao.getVarProgramConfigByVarProgramName(_,_,_) >>getVarProgramConfig()
        applicationProperties.getProperty(CommonConstants.OPTION_NAME_KEY) >> "storage,color"


        when:
        final response = productsController.getProductsWithConfiguration(
                "iphone-iphone-11", null, null, null,null,null,null,request)

        then:
        response.statusCodeValue == 200
        ProductResponse productResponse = response.getBody()
        productResponse.getOptionsConfigurationData().keySet().toArray()[0]=="storage"

    }

    def 'test filterProducts() - withProducts'(){

        given:
        def productRequest = getProductRequest()
        productRequest.withProducts = getWithProductFlag
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.setSession(session)
        appSessionInfo.currentUser() >> user

        when:
        final response = productsController.filterProducts(productRequest, request )

        then:
        response.statusCodeValue == expectedStatusCode

        where:
        getWithProductFlag  |   expectedStatusCode
        true                |   200
        false               |   200

    }

    def getProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProducts(products);
        return productResponse;
    }

    def getAlternateSearchResponse(){
        return SearchRedirect.builder()
                .withVarId(getProgram().getVarId())
                .withProgramId(getProgram().getProgramId())
                .withCatalogId(getProgram().getCatalogId())
                .withSearchKeyword("adaptor")
                .withActionType(CommonConstants.ALTERNATE)
                .withValue("adapter")
                .withActive(true)
                .build()
    }

    def getRedirectSearchResponse(){
        return SearchRedirect.builder()
                .withVarId(getProgram().getVarId())
                .withProgramId(getProgram().getProgramId())
                .withCatalogId(getProgram().getCatalogId())
                .withSearchKeyword("airpod pro")
                .withActionType(CommonConstants.REDIRECT)
                .withValue("#/store/browse/music/music-airpods/")
                .withActive(true)
                .build()
    }

    def getRedirectOnNoResultSearchResponse(){
        return SearchRedirect.builder()
                .withVarId(getProgram().getVarId())
                .withProgramId(getProgram().getProgramId())
                .withCatalogId(getProgram().getCatalogId())
                .withSearchKeyword("airfly")
                .withActionType(CommonConstants.REDIRECT_ON_NO_RESULT)
                .withValue("#/store/curated/accessories/all-accessories/all-accessories-wireless-headphones")
                .withActive(true)
                .build()
    }

    def getUser() {
        User user = new User()
        user.varId = "SCOTIA"
        user.programId = "AmexROC"
        user.locale = Locale.CANADA
        return user
    }

    def getProgram() {
        Program program = new Program()
        program.varId = "SCOTIA"
        program.programId = "AmexROC"
        program.catalogId = "apple-ca-en"
        Map<String,String> configMap=new HashMap<String,String>()
        program.setConfig(configMap)
        return program
    }

    def getProductsSort() {
        List<Product> products = new ArrayList<>();
        Product product=new Product()
        Option op1=new Option()
        op1.setName("color")
        op1.setKey("Red")
        op1.setValue("Red")
        Option op2=new Option()
        op2.setName("storage")
        op2.setKey("512GB")
        op2.setValue("512GB")
        List<Option> optionList=new ArrayList<Option>()
        optionList.add(op1)
        optionList.add(op2)
        product.setName("iPhone 11");
        product.setOptions(optionList)
        products.add(product);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProducts(products);
        return productResponse;
    }

    def getFacetsFilters() {
        Map facetsFiltersMap = new HashMap()
        List<Option> options1 = new ArrayList<>()
        options1.add(new Option("", "Space Gray", Optional.empty()))
        options1.add(new Option("", "Silver", Optional.empty()))
        facetsFiltersMap.put("color", options1)
        List<Option> options2 = new ArrayList<>()
        options2.add(new Option("", "1,4 GHz quadricœur (Turbo Boost jusqu’à 3,9 GHz)", Optional.empty()))
        facetsFiltersMap.put("processor", options2)
        return facetsFiltersMap
    }

    def getProductRequest() {
        def productRequest = new ProductsRequest()
        productRequest.categorySlugs = Sets.newHashSet("macbook-pro")
        productRequest.pageSize = 12
        productRequest.resultOffSet = 0
        productRequest.sort = "DISPLAY_PRICE"
        productRequest.withVariations = false
        productRequest.order = "ASCENDING"
        productRequest.facetsFilters = getFacetsFilters()
        return productRequest
    }

    def getVarProgramConfig(){
        def varProgramConfig = new VarProgramConfigEntity()
        varProgramConfig.setValue("silver,gold,Red,rosegold,rosegld,space_gray,blue,pink,product_red")
        return  varProgramConfig
    }

}