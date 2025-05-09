package com.b2s.rewards.apple.util

import com.b2s.apple.util.NotificationServiceUtil
import com.b2s.rewards.apple.model.Notification
import com.b2s.rewards.apple.model.Program
import spock.lang.Specification
import spock.lang.Unroll

import static com.b2s.rewards.common.util.CommonConstants.NotificationName.*;

class NotificationServiceUtilSpec extends Specification {

    @Unroll
    def 'test getEmailNotification'() {
        given:
        Program program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)

        List<Notification> notifications = getMatchingEntities(varId, programId, locale)
        program.setNotifications(notifications)

        when:
        NotificationServiceUtil notificationServiceUtil = new NotificationServiceUtil()
        def output = notificationServiceUtil.getEmailNotification(program, notificationType)

        then:
        output.get().templateId == result

        where:
        varId | programId     | locale  | notificationType || result
        //Matching Var Program Locale
        'UA'  | 'MP'          | 'en_US' | CONFIRMATION     || 'confirmation-template'
        'UA'  | 'MP'          | 'en_US' | AMP_MUSIC        || 'amp-music-confirmation-template'
        'UA'  | 'MP'          | 'en_US' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-template'
        'UA'  | 'MP'          | 'en_US' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-template'
        'UA'  | 'MP'          | 'en_US' | SERVICE_PLAN     || 'service-plan-poc-template'
        //Matching Var Program
        'UA'  | 'MP'          | 'en_CA' | CONFIRMATION     || 'confirmation-var-prog-template'
        'UA'  | 'MP'          | 'en_CA' | AMP_MUSIC        || 'amp-music-confirmation-var-prog-template'
        'UA'  | 'MP'          | 'en_CA' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-var-prog-template'
        'UA'  | 'MP'          | 'en_CA' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-var-prog-template'
        'UA'  | 'MP'          | 'en_CA' | SERVICE_PLAN     || 'service-plan-poc-var-prog-template'
        //Matching Var Locale
        'UA'  | 'b2s_qa_only' | 'en_US' | CONFIRMATION     || 'confirmation-locale-var-template'
        'UA'  | 'b2s_qa_only' | 'en_US' | AMP_MUSIC        || 'amp-music-confirmation-locale-var-template'
        'UA'  | 'b2s_qa_only' | 'en_US' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-locale-var-template'
        'UA'  | 'b2s_qa_only' | 'en_US' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-locale-var-template'
        'UA'  | 'b2s_qa_only' | 'en_US' | SERVICE_PLAN     || 'service-plan-poc-locale-var-template'
        //Matching Var
        'UA'  | 'b2s_qa_only' | 'en_CA' | CONFIRMATION     || 'confirmation-var-UA-template'
        'UA'  | 'b2s_qa_only' | 'en_CA' | AMP_MUSIC        || 'amp-music-confirmation-var-UA-template'
        'UA'  | 'b2s_qa_only' | 'en_CA' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-var-UA-template'
        'UA'  | 'b2s_qa_only' | 'en_CA' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-var-UA-template'
        'UA'  | 'b2s_qa_only' | 'en_CA' | SERVICE_PLAN     || 'service-plan-poc-var-UA-template'
        //Matching Locale
        'PNC' | 'b2s_qa_only' | 'en_US' | CONFIRMATION     || 'confirmation-locale-US-template'
        'PNC' | 'b2s_qa_only' | 'en_US' | AMP_MUSIC        || 'amp-music-confirmation-locale-US-template'
        'PNC' | 'b2s_qa_only' | 'en_US' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-locale-US-template'
        'PNC' | 'b2s_qa_only' | 'en_US' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-locale-US-template'
        'PNC' | 'b2s_qa_only' | 'en_US' | SERVICE_PLAN     || 'service-plan-poc-locale-US-template'
        //None Matching
        'PNC' | 'b2s_qa_only' | 'en_CA' | CONFIRMATION     || 'confirmation-default-template'
        'PNC' | 'b2s_qa_only' | 'en_CA' | AMP_MUSIC        || 'amp-music-confirmation-default-template'
        'PNC' | 'b2s_qa_only' | 'en_CA' | AMP_NEWS_PLUS    || 'amp-news-plus-confirmation-default-template'
        'PNC' | 'b2s_qa_only' | 'en_CA' | AMP_TV_PLUS      || 'amp-tv-plus-confirmation-default-template'
        'PNC' | 'b2s_qa_only' | 'en_CA' | SERVICE_PLAN     || 'service-plan-poc-default-template'
    }

    def getMatchingEntities(String varId, String programId, String locale) {
        List<Notification> allConfigs = new ArrayList<>()
        allConfigs.addAll(getAllNotifications())

        List<Notification> matchingConfigs = new ArrayList<>()
        for (Notification config : allConfigs) {
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
            } else if ("default".equalsIgnoreCase(config.getProgramId())) {
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

    def getAllNotifications(){
        return [getVARSpecificNotificationForServicePlan(),
                getProgramSpecificNotificationForConfirmation(),
                getLocaleGenericNotificationForNewsPlus(),
                getVARGenericNotificationForMusic(),
                getDefaultLocaleNotificationForTVPlus(),
                getDefaultLocaleNotificationForMusic(),
                getDefaultLocaleNotificationForConfirmation(),
                getVARGenericNotificationForNewsPlus(),
                getVARGenericNotificationForConfirmation(),
                getProgramSpecificNotificationForNewsPlus(),
                getLocaleGenericNotificationForMusic(),
                getVARSpecificNotificationForMusic(),
                getLocaleGenericNotificationForServicePlan(),
                getDefaultLocaleNotificationForNewsPlus(),
                getProgramSpecificNotificationForMusic(),
                getProgramSpecificNotificationForTVPlus(),
                getVARProgramNotificationForMusic(),
                getVARSpecificNotificationForConfirmation(),
                getVARProgramNotificationForNewsPlus(),
                getVARSpecificNotificationForTVPlus(),
                getVARProgramNotificationForTVPlus(),
                getLocaleGenericNotificationForConfirmation(),
                getVARGenericNotificationForServicePlan(),
                getVARGenericNotificationForTVPlus(),
                getDefaultLocaleNotificationForServicePlan(),
                getProgramSpecificNotificationForServicePlan(),
                getVARProgramNotificationForServicePlan(),
                getVARProgramNotificationForConfirmation(),
                getLocaleGenericNotificationForTVPlus(),
                getVARSpecificNotificationForNewsPlus()]
    }

    def getDefaultLocaleNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-default-template')
        return notification
    }

    def getLocaleGenericNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-locale-US-template')
        return notification
    }

    def getVARGenericNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-var-UA-template')
        return notification
    }

    def getVARSpecificNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-locale-var-template')
        return notification
    }

    def getVARProgramNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('-1')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-var-prog-template')
        return notification
    }

    def getProgramSpecificNotificationForServicePlan() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('en_US')
        notification.setName('service_plan')
        notification.setTemplateId('service-plan-poc-template')
        return notification
    }

    def getDefaultLocaleNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-default-template')
        return notification
    }

    def getLocaleGenericNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-locale-US-template')
        return notification
    }

    def getVARGenericNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-var-UA-template')
        return notification
    }

    def getVARSpecificNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-locale-var-template')
        return notification
    }

    def getVARProgramNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('-1')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-var-prog-template')
        return notification
    }

    def getProgramSpecificNotificationForMusic() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('en_US')
        notification.setName('amp-music')
        notification.setTemplateId('amp-music-confirmation-template')
        return notification
    }

    def getDefaultLocaleNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-default-template')
        return notification
    }

    def getLocaleGenericNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-locale-US-template')
        return notification
    }

    def getVARGenericNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-var-UA-template')
        return notification
    }

    def getVARSpecificNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-locale-var-template')
        return notification
    }

    def getVARProgramNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('-1')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-var-prog-template')
        return notification
    }

    def getProgramSpecificNotificationForNewsPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('en_US')
        notification.setName('amp-news-plus')
        notification.setTemplateId('amp-news-plus-confirmation-template')
        return notification
    }

    def getDefaultLocaleNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-default-template')
        return notification
    }

    def getLocaleGenericNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-locale-US-template')
        return notification
    }

    def getVARGenericNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-var-UA-template')
        return notification
    }

    def getVARSpecificNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-locale-var-template')
        return notification
    }

    def getVARProgramNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('-1')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-var-prog-template')
        return notification
    }

    def getProgramSpecificNotificationForTVPlus() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('en_US')
        notification.setName('amp-tv-plus')
        notification.setTemplateId('amp-tv-plus-confirmation-template')
        return notification
    }

    def getDefaultLocaleNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-default-template')
        return notification
    }

    def getLocaleGenericNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('-1')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-locale-US-template')
        return notification
    }

    def getVARGenericNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('-1')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-var-UA-template')
        return notification
    }

    def getVARSpecificNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('default')
        notification.setLocale('en_US')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-locale-var-template')
        return notification
    }

    def getVARProgramNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('-1')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-var-prog-template')
        return notification
    }

    def getProgramSpecificNotificationForConfirmation() {
        Notification notification = new Notification()
        notification.setVarId('UA')
        notification.setProgramId('MP')
        notification.setLocale('en_US')
        notification.setName('CONFIRMATION')
        notification.setTemplateId('confirmation-template')
        return notification
    }
}