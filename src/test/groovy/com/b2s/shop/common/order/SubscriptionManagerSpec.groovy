package com.b2s.shop.common.order


import com.b2s.apple.services.VarProgramMessageService
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.model.AMPConfig
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.Subscription
import com.b2s.shop.common.User
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class SubscriptionManagerSpec extends Specification {

    final varProgramMessageService = Mock(VarProgramMessageService)
    private ApplicationContext context;
    @Subject
    private SubscriptionManager subscriptionManager = new SubscriptionManager(varProgramMessageService: varProgramMessageService)

    def setup() {
        context = Mock()
    }

    @Unroll
    def 'verify addSubscriptionOrderLine with static link'() {
        setup:
        User user = new User(userid: 'sethu', varId: 'RBC')
        Subscription subscription1 = new Subscription(itemId, duration, false)
        Program program = new Program()
        program.setAmpSubscriptionConfig(getAmpSubscriptionConfigs(true))

        when:
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        OrderLine result = subscriptionManager.addSubscriptionOrderLine(user, subscription1, program)

        then:
        result != null
        result.getOrderAttributes() != null
        result.getOrderAttributes().get(0).value == subscriptionURL

        where:
        itemId          || duration || subscriptionURL
        "amp-music"     || 30       || "https://music.apple.com/subscribe?app=music&p=web&at=1000l36zP&ct=RBCMusic&itscg=30200&itsct=Bakkt_Music&ls=1"
        "amp-news-plus" || null     || "https://news.apple.com/subscription?itsct=Bakkt_News&itscg=30200&at=1000l36zP&ct=YYYYY&campaign_id=aff_1000l36zP&app=news"
        "amp-tv-plus"   || 7        || "https://tv.apple.com/channel/tvs.sbd.4000?itsct=Bakkt_TV&itscg=30200&at=1000l36zP&ct=RBCTV"
    }

    @Unroll
    def 'verify addSubscriptionOrderLine without static link'() {
        setup:
        User user = new User(userid: 'sethu', varId: 'RBC')
        Subscription subscription1 = new Subscription("amp-music", 30, false)
        Program program = new Program()
        program.setAmpSubscriptionConfig(getAmpSubscriptionConfigs(false))

        when:
        varProgramMessageService.getMessages(_, _, _) >> vpmProp()
        OrderLine result = subscriptionManager.addSubscriptionOrderLine(user, subscription1, program)

        then:
        result != null
        result.getOrderAttributes() == null
    }

    def getAmpSubscriptionConfigs(boolean useStaticLink) {
        Set<AMPConfig> ampConfigs = new HashSet<>()

        AMPConfig musicConfigWithDuration = AMPConfig.builder()
                .withCategory("music")
                .withItemId("amp-music")
                .withUseStaticLink(useStaticLink)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(musicConfigWithDuration)

        AMPConfig musicConfigDefaultDuration = AMPConfig.builder()
                .withCategory("music")
                .withItemId("amp-music")
                .withUseStaticLink(useStaticLink)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(null)
                .build()
        ampConfigs.add(musicConfigDefaultDuration)

        AMPConfig tvConfig = AMPConfig.builder()
                .withCategory("tv")
                .withItemId("amp-tv-plus")
                .withUseStaticLink(useStaticLink)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(tvConfig)

        AMPConfig newsConfig = AMPConfig.builder()
                .withCategory("ipad-mini")
                .withItemId("amp-news-plus")
                .withUseStaticLink(useStaticLink)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(7)
                .build()
        ampConfigs.add(newsConfig)

        return ampConfigs
    }

    Properties vpmProp() {
        Properties properties = new Properties()
        properties.put("amp-music-sp-subscription-url", "https://music.apple.com/subscribe?app=music&p=web&at=1000l36zP&ct=YYYYY&itscg=30200&itsct=Bakkt_Music&ls=1")
        properties.put("amp-music-30-sp-subscription-url", "https://music.apple.com/subscribe?app=music&p=web&at=1000l36zP&ct=RBCMusic&itscg=30200&itsct=Bakkt_Music&ls=1")
        properties.put("amp-news-plus-sp-subscription-url", "https://news.apple.com/subscription?itsct=Bakkt_News&itscg=30200&at=1000l36zP&ct=YYYYY&campaign_id=aff_1000l36zP&app=news")
        properties.put("amp-tv-plus-7-sp-subscription-url", "https://tv.apple.com/channel/tvs.sbd.4000?itsct=Bakkt_TV&itscg=30200&at=1000l36zP&ct=RBCTV")
        return properties
    }
}