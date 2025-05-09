package com.b2s.apple.services;

import com.b2s.apple.mapper.NotificationMapper;
import com.b2s.apple.util.NotificationServiceUtil;
import com.b2s.rewards.apple.dao.OtpDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.HOTPAlgorithm;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.spark.api.apple.IAppleSparkEmailService;
import com.b2s.spark.api.apple.to.IAppleEmailRequest;
import com.b2s.spark.api.apple.to.impl.Branding;
import com.b2s.spark.api.apple.to.impl.OTPEmailData;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.PRIVACY_POLICY_URL;
import static com.b2s.rewards.common.util.CommonConstants.TERMS_OF_USE_URL;

/*** Created by srukmagathan on 8/23/2016.
 */
@Service
@Transactional
public class OtpService {

    @Autowired
    private OtpDao otpDao;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DomainVarMappingService domainVarMappingService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private NotificationServiceUtil notificationServiceUtil;

    private static final Logger logger= LoggerFactory.getLogger(OtpService.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private IAppleSparkEmailService sparkEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    protected MessageSource messageSource;


    public Long generateOTP(final String emailId, final String loginFunction,final String locale) throws B2RException {
        Long otp=0L;

        if(!domainVarMappingService.isValidDomain(emailId,  loginFunction, locale)){
            return otp;
        }

        // Create new OTP for given emailID
        try {
            otp= HOTPAlgorithm.generateOTP(emailId);
        } catch (final  NoSuchAlgorithmException e) {
            logger.error("OTP: Error generating OTP");
        } catch (final InvalidKeyException e) {
            logger.error("OTP: Error generating OTP");
        }

        // Persist generated OTP in DB
        Otp otpObject=otpDao.getByEmailId(emailId);
        if(otpObject==null){
            otpObject=new Otp();
        }
        otpObject.setEmailId(emailId);
        otpObject.setOtp(passwordEncoder.encode(otp.toString()));
        otpObject.setCreatedDate(new Date());
        otpObject.setUsageDate(null);
        otpObject.setIsUsed("N");

        otpDao.save(otpObject);

        // Return OTP
        return otp;
    }

    public boolean isValidateOtp(final String emailId, final Long otp, final String loginFunction){

        final Otp otpObject;

        //get OTP object by emailId and OTP and isUsed=N
        otpObject=otpDao.getByEmailId(emailId);

        //If its valid then mark OTP as used
        if(Objects.nonNull(otpObject) && passwordEncoder.matches(otp.toString(),otpObject.getOtp())
                && otpObject.getIsUsed().equalsIgnoreCase("N")){

            otpObject.setIsUsed("Y");
            otpObject.setUsageDate(new Date());
            otpDao.save(otpObject);

            // Expiry applicable only OTP and not for AWP
            if(CommonConstants.OTP.equalsIgnoreCase(loginFunction)){

                int days=Days.daysBetween( new DateTime(otpObject.getCreatedDate()),new DateTime()).getDays();
                int hour= Hours.hoursBetween(new DateTime(otpObject.getCreatedDate()),new DateTime()).getHours() % 24 ;

                int diffInHour=days*24+hour;
                // Check OTP expiry
                if(diffInHour> Integer.parseInt(applicationProperties.getProperty("otp.expiry"))){
                    return false;
                }
            }

            return true;
        }

        return false;

    }

    public void sendMail(final OtpModel otpModel, final Long otp, final String loginFunction, Locale locale){

        final DomainVarMapping mapping=domainVarMappingService.getDomainMapping(otpModel.getEmail(), loginFunction);

        final Program program = programService.getProgram(mapping.getVarId(), mapping.getProgramId(), locale);

        // Construct template for object
        final OTPEmailData emailData=new OTPEmailData();
        final Branding branding=new Branding();

        branding.setLogoURL(program.getImageUrl());
        branding.setProgramName(program.getName());
        branding.setLocale(locale.toString());
        branding.setTermsAndConditionsURL(notificationMapper.buildTermsURL(CommonConstants
            .TERMS_AND_CONDITIONS_BASE_PATH,locale));
        branding.setTermsOfUseURL(messageSource.getMessage(TERMS_OF_USE_URL,null,"urlNotFound",locale));
        branding.setPrivacyPolicyURL(messageSource.getMessage(PRIVACY_POLICY_URL,null,"urlNotFound",locale));
        emailData.setBranding(branding);
        emailData.setOneTimePassword(otp.toString());

        final Boolean isBccNeeded = Boolean.FALSE;
        Optional<Notification> notificationOpt=notificationServiceUtil.getEmailNotification(program,
                CommonConstants.NotificationName.OTP);
        IAppleEmailRequest appleEmailRequest =
            notificationMapper.populateOrderEmailRequest(new String[]{
                    StringUtils.isNotBlank(otpModel.getOverrideEmail()) ? otpModel.getOverrideEmail() :
                        otpModel.getEmail()}, notificationOpt, emailData, isBccNeeded, null);
        sparkEmailService.sendEmail(appleEmailRequest);

    }


}
