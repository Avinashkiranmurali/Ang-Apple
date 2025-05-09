package com.b2s.common.services.discountservice;

import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.ssl.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;

import static com.b2s.rewards.apple.util.AppleUtil.gsonToJsonString;

/*** Created by rpillai on 7/11/2016.
 */
@Component
public class DiscountServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DiscountServiceClient.class);
    private static final String DISCOUNT_SERVICE_URL = "discount.service.url";
    private static final String COUPON_MANIPULATION = "/couponManipulation";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    @Qualifier("httpClientUtil")
    private HttpClientUtil httpClient;

    @Autowired
    protected MessageSource messageSource;

    //Utility method to create request object
    private CouponInputs createCouponRequest(final String couponCode, final User user) {

        final CouponInputs couponInputs = new CouponInputs();
        couponInputs.setCouponCode(couponCode);
        couponInputs.setUserId(user.getUserId());
        if (StringUtils.isNotBlank(user.getIPAddress())) {
            couponInputs.setIpAddress(user.getIPAddress());
        } else {
            couponInputs.setIpAddress("127.0.0.1");
        }
        couponInputs.setVarName(user.getVarId());
        couponInputs.setProgramName(user.getProgramId());
        return couponInputs;
    }

    public CouponDetails getValidDiscountCode(final String couponCode, final User user) {
        CouponDetails couponDetails = null;
        if (StringUtils.isNotBlank(couponCode) && user != null) {
            final StringBuilder discountServiceUrl = new StringBuilder(applicationProperties.getProperty(
                    DISCOUNT_SERVICE_URL));
            discountServiceUrl.append(COUPON_MANIPULATION).append("/getValidCouponInfo");

            final CouponInputs couponInputs = createCouponRequest(couponCode, user);

            final String couponInputsJsonString = gsonToJsonString(couponInputs);
            logger.info("Discount service: getValidCouponInfo request json: {}", couponInputsJsonString);
            try {
                couponDetails = httpClient.getHttpResponseWithHeaders(discountServiceUrl.toString(), CouponDetails.class, HttpMethod.POST, couponInputs, createHeaders());
                couponDetails.setCouponStatus(CouponDetails.CouponStatus.AVAILABLE.value);

            } catch (final B2RException be) {
                logger.error(be.getMessage(), be);
                couponDetails = getCouponDetailsWithError(couponCode, user, be);
            }

        }
        return couponDetails;
    }

    private CouponDetails getCouponDetailsWithError(String couponCode, User user, B2RException be) {
        CouponDetails couponDetails;
        couponDetails = new CouponDetails();
        couponDetails.setIsValid(false);
        String errorCode = couponDetails.parseErrorMessage(be.getMessage());
        String[] params = new String[]{couponCode};
        String errorHeader = messageSource.getMessage(errorCode + ".header", params, user.getLocale());
        String errorMessage = messageSource.getMessage(errorCode + ".message", params, user.getLocale());
        couponDetails.setCouponError(new CouponError(couponCode, errorCode, errorHeader, errorMessage));
        return couponDetails;
    }

    public CouponDetails redeemDiscountCode(final String couponCode, final User user) {
        CouponDetails couponDetails = null;

        if (StringUtils.isNotBlank(couponCode) && user != null) {
            final StringBuilder discountServiceUrl = new StringBuilder(applicationProperties.getProperty(
                    DISCOUNT_SERVICE_URL));
            discountServiceUrl.append(COUPON_MANIPULATION).append("/redeemCoupon");
            // Create request object to redeem couponcode
            final CouponInputs couponInputs = createCouponRequest(couponCode, user);
            final String couponInputJsonString = gsonToJsonString(couponInputs);
            logger.info("Discount service: redeemCoupon request json: {}", couponInputJsonString);
            try {
                //Call Client to redeem coupon code via service
                couponDetails = httpClient.getHttpResponseWithHeaders(discountServiceUrl.toString(),
                        CouponDetails.class, HttpMethod.POST, couponInputs, createHeaders());
                // Response is null as of now. So setting discountCode as not valid
                if (Objects.isNull(couponDetails)) {
                    couponDetails = new CouponDetails();
                    couponDetails.setIsValid(true);
                    couponDetails.setCouponStatus(CouponDetails.CouponStatus.AVAILABLE.value);
                }
            } catch (final B2RException be) {
                logger.error(be.getMessage(), be);
                couponDetails = getCouponDetailsWithError(couponCode, user, be);
            }
        }
        return couponDetails;
    }

    public void rollbackDiscountCodeRedeemption(final String couponCode, final User user) {

        if (StringUtils.isNotBlank(couponCode) && user != null) {
            final StringBuilder discountServiceUrl = new StringBuilder(applicationProperties.getProperty(
                    DISCOUNT_SERVICE_URL));
            discountServiceUrl.append(COUPON_MANIPULATION).append("/activateCoupon");

            final CouponInputs couponInputs = createCouponRequest(couponCode, user);

            final String couponInputJsonString = gsonToJsonString(couponInputs);
            logger.info("Discount service: rollbackDiscountCodeRedemption request json: {}", couponInputJsonString);
            try {
                httpClient.getHttpResponseWithHeaders(discountServiceUrl.toString(), CouponDetails.class,
                        HttpMethod.POST, couponInputs, createHeaders());

            } catch (final B2RException be) {
                logger.error("Unable to rollback discount code  # ", couponCode, be.getMessage(), be);
            }
        }
    }

    public String validateDiscountCode(final String couponCode, final User user) throws B2RException {
        if (StringUtils.isNotBlank(couponCode) && user != null) {
            final StringBuilder discountServiceUrl = new StringBuilder(applicationProperties.getProperty(
                    DISCOUNT_SERVICE_URL));
            discountServiceUrl.append("/couponValidation").append("/validateCoupon");

            final CouponInputs couponInputs = createCouponRequest(couponCode, user);

            final String couponInputJsonString = gsonToJsonString(couponInputs);
            logger.info("Discount service: ValidateCoupon request json: {}", couponInputJsonString);
            httpClient.getHttpResponseWithHeaders(discountServiceUrl.toString(), CouponDetails.class, HttpMethod.POST,
                    couponInputs, createHeaders());
        }
        return couponCode;
    }


    HttpHeaders createHeaders() {
        final String auth = applicationProperties.getProperty("DS_USER") + ':' + applicationProperties.getProperty("DS_PASSWORD");
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        final String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", authHeader);
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.set("Accept", "application/json");
        return httpHeaders;
    }

}
