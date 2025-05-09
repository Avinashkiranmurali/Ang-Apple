package com.b2s.rewards.security.controller;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.mapper.DomainVarEnitityResponseMapper;
import com.b2s.apple.services.DomainVarMappingService;
import com.b2s.apple.services.OtpService;
import com.b2s.apple.services.VarProgramDomainUserRestrictionService;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.rewards.apple.model.DomainVarMapping;
import com.b2s.rewards.apple.model.OtpModel;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import java.util.Objects;
import java.util.Properties;
/*** Created by srukmagathan on 8/31/2016.
 */
@RestController
@RequestMapping("/otp")
public class OTPController {

    private static final Logger LOG= LoggerFactory.getLogger(OTPController.class);
    public static final String ERROR_PROCESSING_REQUEST = "Error processing your request";


    @Autowired
    private OtpService otpService;

    @Autowired
    private DomainVarMappingService domainVarMappingService;

    @Autowired
    private VarProgramDomainUserRestrictionService varProgramDomainUserRestrictionService;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    @Autowired
    private DomainVarEnitityResponseMapper domainVarEnitityResponseMapper;

    @RequestMapping(value = "/generateOTP",method = RequestMethod.POST)
    public ResponseEntity<String> generateOTP(@RequestBody final OtpModel otpModel){
        final Long otp;
        String locale = otpModel.getLocale();
        try {

            if (Objects.isNull(otpModel) || StringUtils.isBlank(otpModel.getEmail())) {
                LOG.error("generateOTP: Invalid email");
                return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.BAD_REQUEST);
            }

            // resolve override email domain, if exists
            if(new Boolean(applicationProperties.getProperty(CommonConstants.OTP_OVERRIDE_EMAIL_DOMAIN_ENABLE_KEY))) {
                resolveOverrideEmailDomain(otpModel);
            }


            if (!domainVarMappingService.isValidDomain(otpModel.getEmail(), CommonConstants.OTP,locale)) {
                LOG.error("generateOTP: email domain is either not listed / inactive in approved domain list ");
                return new ResponseEntity("Given emailId is not in approved domain list", HttpStatus.UNAUTHORIZED);
            }
            //Check for user Permission
            final boolean isUserPermitted =  varProgramDomainUserRestrictionService.isOtpUserPermitted(otpModel
                .getEmail(),CommonConstants.OTP);
            if(!isUserPermitted){
                LOG.error("generateOTP: emailId : {} - is not Authorized ", otpModel.getEmail());
                return new ResponseEntity("emailId is not Authorized", HttpStatus.UNAUTHORIZED);
            }

            // Generate OTP
            otp = otpService.generateOTP(otpModel.getEmail(),CommonConstants.OTP,locale);

            if (otp == 0) {
                LOG.error("generateOTP: Unable to generate OTP. issue may be due to wrong input / system error");
                return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.BAD_REQUEST);
            }

            //Send email
            otpService.sendMail(otpModel, otp, CommonConstants.OTP, LocaleUtils.toLocale(otpModel.getLocale()));

        }catch (final Exception e){
            LOG.error("generateOTP: Exception block, unable to generate OTP", e);
            return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity("OTP sent via email", HttpStatus.OK);
    }

    private void resolveOverrideEmailDomain(final OtpModel otpModel) {

        String originalEmail = otpModel.getEmail();
        String otpOverrideEmailDomainsStr = applicationProperties.getProperty(CommonConstants.OTP_OVERRIDE_EMAIL_DOMAIN_KEY);
        if (StringUtils.isNotBlank(otpOverrideEmailDomainsStr)) {
            String[] otpOverrideEmailDomains = otpOverrideEmailDomainsStr.split(",");
            if (otpOverrideEmailDomains != null && otpOverrideEmailDomains.length > 0) {
                for (String otpOverrideEmailDomain : otpOverrideEmailDomains) {
                    overrideOtpEmail(otpModel, originalEmail, otpOverrideEmailDomain);
                }
            }
        }
    }

    private void overrideOtpEmail(OtpModel otpModel, String originalEmail, String otpOverrideEmailDomain) {
        String email;
        String overrideEmail;
        if (originalEmail.startsWith(otpOverrideEmailDomain + "-")) {
            email = originalEmail.substring(originalEmail.indexOf(otpOverrideEmailDomain) + (otpOverrideEmailDomain + "-").length());
            overrideEmail = email.substring(0, email.indexOf("@")) + "@" + otpOverrideEmailDomain;
            if (StringUtils.isNotBlank(email)) {
                otpModel.setEmail(email);
            }
            if (StringUtils.isNotBlank(overrideEmail)) {
                otpModel.setOverrideEmail(overrideEmail);
            }
            LOG.info("Overriding otp email with override email: {}, actual email: {}, original email value: {}", overrideEmail, email, originalEmail);
        }
    }


    @RequestMapping(value = "/validateOTP",method = RequestMethod.POST)
    public ResponseEntity validateOTP(@RequestBody final OtpModel otpModel)
        throws ServletException {

        try {
            if (Objects.isNull(otpModel)) {
                LOG.error("validateOTP: Error processing validate OTP request. Invalid input");
                return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.BAD_REQUEST);
            }

            final Long otp = Long.valueOf(otpModel.getPassword());

            if (StringUtils.isBlank(otpModel.getEmail()) || otp == 0L) {
                LOG.error("validateOTP: Error processing validate OTP request. Invalid input" );
                return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.BAD_REQUEST);
            }

            // resolve override email domain, if exists
            if(new Boolean(applicationProperties.getProperty(CommonConstants.OTP_OVERRIDE_EMAIL_DOMAIN_ENABLE_KEY))) {
                resolveOverrideEmailDomain(otpModel);
            }

            final boolean isValid = otpService.isValidateOtp(otpModel.getEmail(), otp,CommonConstants.OTP);

            if (isValid) {
                final DomainVarMapping userInfo = domainVarMappingService.getDomainMapping(otpModel.getEmail(),
                                                                                            CommonConstants.OTP);
                if (Objects.isNull(userInfo)) {
                    LOG.error("validateOTP: Error processing your request. Domain mapping entry does not exist / " +
                                                                    "inactive for emailId {}", otpModel.getEmail());
                    return new ResponseEntity("Error processing request", HttpStatus.BAD_REQUEST);
                }
                // Set userId as emailId
                userInfo.setUserId(otpModel.getEmail());
                userInfo.setEmail(StringUtils.isNotBlank(otpModel.getOverrideEmail()) ? otpModel.getOverrideEmail() : otpModel.getEmail());

                return new ResponseEntity(userInfo, HttpStatus.OK);
            } else {
                LOG.error("validateOTP: Error processing your request. Invalid / used OTP # {}", otp);
                return new ResponseEntity(ERROR_PROCESSING_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        }catch(final Exception ex){
            if(Objects.nonNull(otpModel)) {
                LOG.error("validateOTP: Exception block, Unable to validate OTP for emailId # {}",
                        otpModel.getEmail(), ex);
            }
            return new ResponseEntity("Error processing request", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean getDisplayStatusForTermsOfUse(final String varId,final String programId){
        final VarProgramConfigEntity varProgramConfig = varProgramConfigDao.getVarProgramConfigByVarProgramName(varId, programId,
            CommonConstants.AWP_DISPLAY_TERMS_OF_USE);
        if(Objects.nonNull(varProgramConfig)){
            return BooleanUtils.toBoolean(varProgramConfig.getValue());
        }
        return Boolean.FALSE;
    }

}
