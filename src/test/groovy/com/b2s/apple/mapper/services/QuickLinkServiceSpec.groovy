package com.b2s.apple.mapper.services

import com.b2s.apple.entity.QuickLinkEntity
import com.b2s.apple.mapper.QuickLinkModelMapper
import com.b2s.apple.services.QuickLinkService
import com.b2s.rewards.apple.dao.QuickLinkDao
import com.b2s.shop.common.User
import org.tools4j.groovytables.GroovyTables
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Predicate

class QuickLinkServiceSpec extends Specification {

    def quickLinkDao = Mock(QuickLinkDao)
    def quickLinkModelMapper = new QuickLinkModelMapper()

    @Subject
    def quickLinkService = new QuickLinkService(quickLinkDao: quickLinkDao,quickLinkModelMapper:quickLinkModelMapper)

    def 'test getByVarIdProgramIdLocale - anonymous user'(){
        given:
        def user = getUser()
        user.anonymous = true
        quickLinkDao.getByVarIdProgramIdLocaleLinkCode(_,_,_,_) >> getQuickLinkEntities(user)
        when:
        def result  = quickLinkService.getByVarIdProgramIdLocale(user)
        then: /*result list count is 1 as it filter out the object which has display as false and showunauthenticated
         is false when user logged in as anonymous*/
        result.size() == 1
    }

    def 'test getByVarIdProgramIdLocale - not an anonymous user'(){
        given:
        def user = getUser()
        quickLinkDao.getByVarIdProgramIdLocaleLinkCode(_,_,_,_) >> getQuickLinkEntities(user)
        when:
        def result  = quickLinkService.getByVarIdProgramIdLocale(user)
        then: /*result list count is 2 as it filter out the object which has display as false. showunauthenticated
         is not considered as the user is not logged in as anonymous*/
        result.size() == 2
    }

    User getUser() {
        User user = new User()
        user.varId = "-1"
        user.programId = "default"
        user.locale = Locale.ENGLISH
        user.loginType = "fivebox"
        return user
    }

    def getQuickLinkEntities(User user) {
        def List<QuickLinkEntity> quickLinkEntities = new ArrayList<>()
        GroovyTables.withTable {
            locale |varId|programId| linkCode    | linkText     | linkUrl                            |priority|showUnauthenticated| display
            "en_US"|"-1" |"default"| "airpods"   | "AirPods"    |"/store/browse/music/music-airpods/"| 10     | true              | true
            "en_US"|"-1" |"default"|"applepencil"|"Apple Pencil"|"/store/browse/ipad/apple-pencil/"  | 20     | false             | true
            "en_US"|"-1" |"default"| "homepod"   | "HomePod"    |"/store/configure/music/homepod/"   | 30     | true              | false
        }.forEachRow {
            def entity = new QuickLinkEntity(quickLinkId:new QuickLinkEntity.QuickLinkId
                    (locale:locale,varId:varId,programId:programId,
                            linkCode:linkCode),linkText:linkText,
                    linkUrl:linkUrl,priority:priority,
                    showUnauthenticated:showUnauthenticated,display:display)
            quickLinkEntities.add(entity)
        }
        if(user.isAnonymous()) {
            return quickLinkEntities.stream().filter({ quickLink -> quickLink.isShowUnauthenticated()} as
                    Predicate<QuickLinkEntity>)
                    .collect()
        }
        return quickLinkEntities
    }
}
