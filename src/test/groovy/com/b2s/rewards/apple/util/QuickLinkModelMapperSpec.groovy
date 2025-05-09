package com.b2s.rewards.apple.util

import com.b2s.apple.entity.QuickLinkEntity
import com.b2s.apple.mapper.QuickLinkModelMapper
import org.tools4j.groovytables.GroovyTables
import spock.lang.Specification
import spock.lang.Subject

class QuickLinkModelMapperSpec extends Specification{

    @Subject
    private QuickLinkModelMapper mapper = new QuickLinkModelMapper()

    def 'test getQuickLinks()'() {

        given:
        final def Map<String, QuickLinkEntity> quickLinkEntities = getQuickLinkMap()

        when:
        def result = mapper.getQuickLinks(quickLinkEntities)

        then://when display is false, that will not be added into the list
        result.size() == 2

    }
    def getQuickLinkMap() {
        def Map<String, QuickLinkEntity> quickLinkEntities = new HashMap<>()
        GroovyTables.withTable {
            locale |varId | programId | linkCode    | linkText     | linkUrl                            |priority|showUnauthenticated| display
            "en_US"| "-1" | "default" | "airpods"   | "AirPods"    |"/store/browse/music/music-airpods/"| 10     | true              | true
            "en_US"| "-1" | "default" |"applepencil"|"Apple Pencil"|"/store/browse/ipad/apple-pencil/"  | 20     | true              | true
            "en_US"| "-1" | "default" | "homepod"   | "HomePod"    |"/store/configure/music/homepod/"   | 30     | true              | false
        }.forEachRow {
            def entity = new QuickLinkEntity(quickLinkId:new QuickLinkEntity.QuickLinkId
                    (locale:locale,varId:varId,programId:programId,
                            linkCode:linkCode),linkText:linkText,
                    linkUrl:linkUrl,priority:priority,
                    showUnauthenticated:showUnauthenticated,display:display)
            quickLinkEntities.put(linkCode, entity)
        }
        return quickLinkEntities
    }
}
