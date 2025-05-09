package com.b2s.rewards.apple.controller

import com.b2s.apple.entity.QuickLinkEntity
import com.b2s.apple.services.QuickLinkService
import com.b2s.rewards.apple.exceptionhandler.ArgumentsNotValidException
import com.b2s.rewards.apple.exceptionhandler.CustomRestExceptionHandler
import com.b2s.rewards.apple.exceptionhandler.InvalidResponseException
import com.b2s.rewards.apple.model.QuickLink
import com.b2s.shop.common.User
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.tools4j.groovytables.GroovyTables
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import java.util.function.Predicate

class QuickLinkControllerSpec extends Specification {

    def quickLinkService = Mock(QuickLinkService)

    def request = Mock(HttpServletRequest)

    @Subject
    final QuickLinkController quickLinkController = new QuickLinkController(quickLinkService: quickLinkService, servletRequest: request)

    private MockMvc mvc

    private User user

    private MockHttpSession session

    private List<QuickLinkEntity> quickLinkList

    def setup() {
        mvc = MockMvcBuilders.standaloneSetup(quickLinkController).setControllerAdvice(new CustomRestExceptionHandler()).build()
        session = Mock(MockHttpSession)
        user = getUser()
        quickLinkList = getListOfQuickLinks()
        request.session >> session
    }

    def cleanup() {
        mvc = null
        session = null
        user = null
        quickLinkList = null
    }

    def "get 400 without user"() {

        given:
        request.getSession().getAttribute("USER") >> null
        quickLinkService.getByVarIdProgramIdLocale(_) >> {
            throw new ArgumentsNotValidException()
        }

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/quicklinks").session(session)).andReturn()

        then:
        result.response.status == 400
    }

    def "get 500 with service exception"() {

        given:
        request.getSession().getAttribute("USER") >> user
        quickLinkService.getByVarIdProgramIdLocale(_) >> {
            throw new InvalidResponseException()
        }

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/quicklinks").session(session)).andReturn()

        then:
        result.response.status == 500
    }

    def "get empty list when data not found"() {

        given:
        request.getSession().getAttribute("USER") >> user
        quickLinkService.getByVarIdProgramIdLocale(_) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/quicklinks").session(session)).andReturn()

        then:
        result.response.status == 200
        result.response.contentAsString.isEmpty()

    }

    def "get 200 OK with user"() {

        given:
        request.getSession().getAttribute("USER") >> user
        quickLinkService.getByVarIdProgramIdLocale(_) >> getListOfQuickLinks()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/quicklinks").session(session)).andReturn()

        then:
        result.response.status == 200
        result.response.contentAsString.contains("linkCode")

    }

    def "test getByLocaleVarIdProgramId"() {

        given:
        request.getSession().getAttribute("USER") >> getUser2()
        quickLinkService.getByVarIdProgramIdLocale(_) >> listOfQuickLinks

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/quicklinks").session(session)).andReturn()

        then:
        result.response.status == 200
        result.response.contentAsString.split("linkCode").size()-1 == listCount
        where:
        listOfQuickLinks                | listCount
        getQuickLinks_display_all()     |   3
        getQuickLinks_partial_display() |   2
    }

    User getUser() {
        User user = new User()
        user.varId = "RBC"
        user.programId = "PBA"
        user.locale = Locale.CANADA_FRENCH
        user.loginType = "fivebox"
        user
    }


    User getUser2() {
        User user = new User()
        user.varId = "-1"
        user.programId = "default"
        user.locale = Locale.ENGLISH
        user.loginType = "fivebox"
        user
    }
    List<QuickLink> getListOfQuickLinks() {
        List<QuickLink> theList = new ArrayList<>()
        theList.add(QuickLink.builder()
                .withLocale("fr_CA")
                .withLinkCode("airpods")
                .withVarId("-1")
                .withProgramId("default")
                .withLinkText("Le AirPods")
                .withLinkUrl("/store/browse/music/music-airpods/")
                .withOrder(10)
                .withDisplay(true)
                .withShowUnauthenticated(true).build())
        return theList
    }

    def getQuickLinks_display_all() {
        List<QuickLink> theList = new ArrayList<>()

        GroovyTables.withTable {
            locale |varId|programId| linkCode    | linkText     | linkUrl                            |order|showUnauthenticated | display
            "en_US"| "-1"|"default"| "airpods"   | "AirPods"    |"/store/browse/music/music-airpods/"| 10  | true               | true
            "en_US"| "-1"|"default"|"applepencil"|"Apple Pencil"|"/store/browse/ipad/apple-pencil/"  | 20  | true               | true
            "en_US"| "-1"|"default"| "homepod"   | "HomePod"    |"/store/configure/music/homepod/"   | 30  | true               | true
        }.forEachRow {
            theList.add(QuickLink.builder()
                    .withLocale(locale)
                    .withLinkCode(linkCode)
                    .withVarId(varId)
                    .withProgramId(programId)
                    .withLinkText(linkText)
                    .withLinkUrl(linkUrl)
                    .withOrder(order)
                    .withDisplay(display)
                    .withShowUnauthenticated(showUnauthenticated).build())
        }
        return theList.stream().filter({ quickLink -> quickLink.isDisplay()} as Predicate<QuickLink>).collect()
    }

    def getQuickLinks_partial_display() {
        List<QuickLink> theList = new ArrayList<>()

        GroovyTables.withTable {
            locale |varId|programId|  linkCode   |   linkText   |   linkUrl                          |order|  showUnauthenticated |   display
            "en_US"|"-1" |"default"|  "airpods"  |   "AirPods"  |"/store/browse/music/music-airpods/"| 30  |  true                |   true
            "en_US"|"-1" |"default"|"applepencil"|"Apple Pencil"|"/store/browse/ipad/apple-pencil/"  | 10  |  true                |   true
            "en_US"|"-1" |"default"|  "homepod"  |   "HomePod"  |"/store/configure/music/homepod/"   | 20  |  true                |   false
        }.forEachRow {
            theList.add(QuickLink.builder()
                    .withLocale(locale)
                    .withLinkCode(linkCode)
                    .withVarId(varId)
                    .withProgramId(programId)
                    .withLinkText(linkText)
                    .withLinkUrl(linkUrl)
                    .withOrder(order)
                    .withDisplay(display)
                    .withShowUnauthenticated(showUnauthenticated).build())
        }
        return theList.stream().filter({ quickLink -> quickLink.isDisplay()} as Predicate<QuickLink>).collect()
    }
}
