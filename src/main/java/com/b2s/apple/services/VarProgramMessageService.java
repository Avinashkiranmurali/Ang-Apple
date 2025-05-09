package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.VarProgramMessageDao;
import com.b2s.rewards.apple.model.VarProgramMessage;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rpillai on 2/21/2017
 */
@Service
public class VarProgramMessageService {

    @Autowired
    private VarProgramMessageDao varProgramMessageDao;

    public Properties getMessages(final Optional<String> varId, final Optional<String> programId, final String locale) {
        final Properties properties = new Properties();
        final String languageCode = LocaleUtils.toLocale(locale).getLanguage();
        final List<String> varIds = new ArrayList<>(Arrays.asList("-1"));
        if(varId.isPresent()) {
            varIds.add(varId.get());
        }
        final List<String> programIds = new ArrayList<>(Arrays.asList("-1"));
        if(programId.isPresent()) {
            programIds.add(programId.get());
        }
        final List<String> locales = Arrays.asList(languageCode, locale);
        final List<VarProgramMessage> messages = varProgramMessageDao.getMessages(varIds, programIds, locales);
        // add default messages for language code
        extractAndPopulateProperties(properties, messages, CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM, languageCode);
        // add default messages for locale
        extractAndPopulateProperties(properties, messages, CommonConstants.DEFAULT_VAR_PROGRAM, CommonConstants.DEFAULT_VAR_PROGRAM, locale);
        // add var specific messages for locale
        if(varId.isPresent()) {
            extractAndPopulateProperties(properties, messages, varId.get(), CommonConstants.DEFAULT_VAR_PROGRAM, locale);
        }
        // add var-program specific messages for locale
        if(varId.isPresent() && programId.isPresent()) {
            extractAndPopulateProperties(properties, messages, varId.get(), programId.get(), locale);
        }
        return properties;
    }

    public Properties getMessagesBasedOnCodeType(
        final Optional<String> varId,
        final Optional<String> programId,
        final String locale, final String codeType) {

        final Properties properties = new Properties();
        final String languageCode = LocaleUtils.toLocale(locale).getLanguage();
        final List<String> varIds = new ArrayList<>(Arrays.asList("-1"));
        if (varId.isPresent()) {
            varIds.add(varId.get());
        }
        final List<String> programIds = new ArrayList<>(Arrays.asList("-1"));
        if (programId.isPresent()) {
            programIds.add(programId.get());
        }
        final List<String> locales = Arrays.asList(languageCode, locale);
        final List<VarProgramMessage> messages = varProgramMessageDao.getMessages(varIds, programIds, locales, codeType);
        // add default messages for language code
        extractAndPopulateProperties(properties, messages, CommonConstants.DEFAULT_VAR_PROGRAM,
            CommonConstants.DEFAULT_VAR_PROGRAM, languageCode);
        // add default messages for locale
        extractAndPopulateProperties(properties, messages, CommonConstants.DEFAULT_VAR_PROGRAM,
            CommonConstants.DEFAULT_VAR_PROGRAM, locale);
        // add var specific messages for locale
        if (varId.isPresent()) {
            if(codeType.equalsIgnoreCase(CommonConstants.ANALYTICS)){
                extractAndPopulateProperties(properties, messages, varId.get(), CommonConstants.DEFAULT_VAR_PROGRAM,
                        languageCode);
            }
            extractAndPopulateProperties(properties, messages, varId.get(), CommonConstants.DEFAULT_VAR_PROGRAM,
                locale);
        }
        // add var-program specific messages for locale
        if (varId.isPresent() && programId.isPresent()) {
            extractAndPopulateProperties(properties, messages, varId.get(), programId.get(), locale);
        }
        return properties;
    }

    private void extractAndPopulateProperties(final Properties properties, final List<VarProgramMessage> messages, final String varId, final String programId, final String locale) {
        final List<VarProgramMessage> subList = messages.stream()
                .filter(message -> varId.equals(message.getVarId())
                        && programId.equals(message.getProgramId())
                        && locale.equals(message.getLocale()))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(subList)) {
            for(VarProgramMessage message : subList) {
                properties.put(message.getCode(), message.getMessage());
            }
        }
    }
}