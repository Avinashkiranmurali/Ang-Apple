package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.DomainVarMappingDao;
import com.b2s.rewards.apple.model.DomainVarMapping;
import com.b2s.rewards.apple.model.Failure;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.exception.ErrorConstants;
import com.b2s.rewards.common.util.CommonConstants;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vmurugesan on 9/23/2016.
 */
@Service
public class DomainVarMappingService {

    @Autowired
    private DomainVarMappingDao domainVarMappingDao;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private Properties applicationProperties;

    private static final Logger LOG= org.slf4j.LoggerFactory.getLogger(DomainVarMappingService.class);


    public boolean isValidDomain(final String emailId, String loginFunction, String locale) throws B2RException {

        if(CommonConstants.AWP.equalsIgnoreCase(loginFunction)){
            return isValidAWPDomain(emailId, locale);
        }

        final boolean isMappingExist;
        final String domain = emailId.substring(emailId.indexOf('@') + 1);

        isMappingExist = domainVarMappingDao.isDomainMappingExist(domain, loginFunction);

        return isMappingExist;
    }

    public DomainVarMapping getDomainMapping(final String emailId, final String loginFunction) {

        final String domain = emailId.substring(emailId.indexOf('@') + 1);

        if(CommonConstants.AWP.equalsIgnoreCase(loginFunction)){
            return getDefaultAWPMapping(domain);
        }

        return domainVarMappingDao.findByDomain(domain, loginFunction);

    }

    public DomainVarMapping findDomainByPattern(final String url, final String domain) {
        List<DomainVarMapping> domainList = domainVarMappingDao.getDomainByLoginType(url);
        Optional<DomainVarMapping> domainVarMapping = domainList.stream().filter(domainValue -> {
            Pattern p = Pattern.compile(domainValue.getDomain());
            Matcher m = p.matcher(domain);
            return m.matches();
        }).findFirst();

        if (domainVarMapping.isPresent()) {
            return domainVarMapping.get();
        }

        return null;

    }

    public List<DomainVarMapping> findDomainsByPattern(final String loginType, final String domain) {
        final List<DomainVarMapping> domainList = domainVarMappingDao.getDomainByLoginType(loginType);
        final List<DomainVarMapping> domainVarMapping = domainList.stream().filter(domainValue -> {
            Pattern p = Pattern.compile(domainValue.getDomain());
            Matcher m = p.matcher(domain);
            return m.matches();
        }).collect(Collectors.toList());

        return domainVarMapping;

    }

    public boolean isValidAWPDomain(final String emailId, String locale) throws B2RException {

        String emailDomain=emailId.substring(emailId.indexOf('@') + 1);

        StringBuilder url=new StringBuilder();
        url.append(applicationProperties.getProperty(CommonConstants.AWP_SERVICE_URL))
            .append(CommonConstants.AWP_VALIDATE_DOMAIN_PATH)
            .append(emailId)
                .append('/')
                .append(locale)
            .append('/');

        try {
            String response=httpClientUtil.getHttpResponse(url.toString(), String.class, HttpMethod.GET,null);
            if(StringUtils.isNotBlank(response) && response.equalsIgnoreCase("true")){
                LOG.debug("Successfully validated AWP domain");
                return true;
            }
        } catch (B2RException e) {
            LOG.error("Unable to validate AWP email domain {}" , emailDomain);
            if(StringUtils.isNotBlank(e.getMessage())) {
                Failure failure = new Gson().fromJson(e.getMessage(), Failure.class);
                if(failure != null && ErrorConstants.PAYROLL_ENABLED_DOMAIN_ACCESS_INVALID.equals(failure.getMessageId())) {
                    throw new B2RException(null, failure.getMessageId());
                }
            }
        }

        return false;
    }

    // DRP is constant program for AWP and it is same for all domain.
    public DomainVarMapping getDefaultAWPMapping(final String emailDomain){

        final DomainVarMapping mapping=new DomainVarMapping();

        mapping.setVarId(CommonConstants.VAR_APPLE_WORKPLACE);
        mapping.setProgramId(CommonConstants.AWP_PROGRAM_DRP);
        mapping.setLoginType(CommonConstants.AWP);
        mapping.setCreatedDate(new Date().toString());
        mapping.setIsActive("Y");
        mapping.setDomain(emailDomain);

        return mapping;

    }

}
