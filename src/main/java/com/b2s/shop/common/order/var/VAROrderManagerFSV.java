package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Integer.valueOf;

@Component("varOrderManagerFSV")
public class VAROrderManagerFSV extends GenericVAROrderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerFSV.class);

    private static final String VAR_ID = CommonConstants.VAR_FSV;

    private enum SAMLAttributes {
        VARID("varId"),
        PROGRAM_ID("programId"),
        POINTS_BALANCE("pointsBalance"),
        BROWSE_ONLY("browseOnly"),
        ANONYMOUS("anonymous"),
        LOCALE("locale"),
        COUNTRY("country"),
        NAVBACKURL("navBackURL"),
        CSID("csid"),
        SID("sid"),
        KEYSTONE_BASE_URL("keystoneBaseUrl");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final User user;
        Program program = null;
        CommonConstants.LoginType loginType = null;
        // 5 Box Login
        if (request.getParameter(CommonConstants.RELAY_STATE) == null) {
            user = selectLocalUser(request, new User());
            loginType = CommonConstants.LoginType.FIVEBOX;
            //Enable the checkbox at 5-box login for ANONYMOUS user to be added to User Object
            if (Objects.nonNull(request.getParameter(CommonConstants.IS_ANONYMOUS))) {
                user.setAnonymous(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ANONYMOUS)));
            }
            //Enable the Browse Only checkbox in 5-box login for BROWSE ONLY experience
            if (Objects.nonNull(request.getParameter(CommonConstants.IS_BROWSE_ONLY))) {
                user.setBrowseOnly(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_BROWSE_ONLY)));
            }

        } else {
            // SAML Flow
            Arrays.stream(VAROrderManagerFSV.SAMLAttributes.values()).forEach(samlAttributes ->
                    LOGGER.info("FSV SAML attributes ------{} ### {}", samlAttributes.value,
                            request.getAttribute(samlAttributes.value)));
            LOGGER.info("FSV SAML UserPrincipal ----{}", request.getUserPrincipal().getName());

            loginType = CommonConstants.LoginType.SAML;
            final boolean anonymous = anonymousCheck(request);

            //Anonymous check
            if (anonymous) {
                user = setAnonymousUser(request, new User());
            } else {
                user = getUser(request);
                initializeLocaleDependents(request, user, CommonConstants.LOCALE_EN_US, CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);
                populateUserInfo(request, user, program);
            }
        }
        user.setLoginType(loginType.getValue());

        if (Objects.isNull(program)) {
            initializeLocaleDependents(request, user, CommonConstants.LOCALE_EN_US, CommonConstants.COUNTRY_CODE_US);
            program = getProgram(user);
        }

        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);
        prepareUserAddress(user);
        setExternalUrls(request, program);

        // Override the Bag Menu URLs from Program Config
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);
        return user;
    }

    private boolean anonymousCheck(final HttpServletRequest request) {
        if (Objects.nonNull(request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE))) {
            return Boolean.parseBoolean((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE));
        }
        return false;
    }

    private void populateUserInfo(final HttpServletRequest request, final User user, final Program program) {
        final AccountInfo accountInfo;
        if (program.getIsLocal()) {
            accountInfo = varIntegrationServiceLocalImpl.getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        } else {
            accountInfo = varIntegrationServiceRemoteImpl.getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        }

        if (Objects.nonNull(accountInfo)) {
            if (Objects.nonNull(accountInfo.getUserInformation())) {
                accountInfo.getUserInformation().copyDataToUser(user);

                setSecondaryImageUrl(program, accountInfo.getUserInformation().getAdditionalInfo());
            }
            if (Objects.nonNull(accountInfo.getAccountBalance())) {
                user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
                if (user.getAdditionalInfo() == null) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                user.getAdditionalInfo().put(CommonConstants.VIS_CURRENCY, accountInfo.getAccountBalance().getCurrency());
                user.getAdditionalInfo().put(CommonConstants.ACCOUNT_SESSIONID, user.getAdditionalInfo().get(CommonConstants.ACCOUNT_SESSIONID));
            }
        } else {
            if (program.getIsLocal() && Objects.nonNull(request.getAttribute(CommonConstants.POINTS_BALANCE))) {
                user.setPoints(valueOf(CommonConstants.getRequestAttribute(request, VAROrderManagerFSV.SAMLAttributes.POINTS_BALANCE.value)));
            }
        }
    }

    //Set VPC secondaryImageUrl if headerImageUrl exist in Fiserv Affinity API response
    private void setSecondaryImageUrl(final Program program, final Map<String, String> additionalInfo) {
        if (MapUtils.isNotEmpty(additionalInfo) && Objects.nonNull(additionalInfo.get(CommonConstants.HEADER_IMAGE_URL))) {
            program.getConfig().put(CommonConstants.SECONDARY_IMAGE_URL, additionalInfo.get(CommonConstants.HEADER_IMAGE_URL));
        }
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            final RedemptionResponse redemptionResponse;
            if (Objects.nonNull(program) && program.getIsLocal()) {
                redemptionResponse = varIntegrationServiceLocalImpl.performRedemption(order, user, program);
            } else {
                redemptionResponse = varIntegrationServiceRemoteImpl.performRedemption(order, user, program);
            }
            if (Objects.isNull(redemptionResponse)) {
                LOGGER.error("Fiserv Redemption response is Empty");
                return false;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            getBalanceUserPoints(order, user);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public int getUserPoints(final User user, final Program program) throws B2RException {
        if (Objects.nonNull(program) && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), user.getUserId(), user.getAdditionalInfo());
        }
    }

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    private void setExternalUrls(final HttpServletRequest request, final Program program) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);

        if (Objects.nonNull(program.getConfig().get(ExternalUrlConstants.KEEP_ALIVE_URL))) {
            externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL,
                    (String) program.getConfig().get(ExternalUrlConstants.KEEP_ALIVE_URL));
        }

        setKeystoneUrls(externalUrls,
                CommonConstants.getRequestAttribute(request, VAROrderManagerFSV.SAMLAttributes.KEYSTONE_BASE_URL.value));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    @Override
    protected User getUser(final HttpServletRequest request) {
        final User user = new User();
        final String programId = (String) request.getAttribute(CommonConstants.PROGRAM_ID);
        final String userId = request.getUserPrincipal().getName();
        final String sId = (String) request.getAttribute(CommonConstants.SID);
        final boolean browseOnly = Boolean.parseBoolean((String) request.getAttribute(VAROrderManagerFSV.SAMLAttributes.BROWSE_ONLY.value));
        user.setProgramId(programId);
        user.setVarId(getVARId());
        user.setUserId(userId);
        user.setSid(sId);
        user.setBrowseOnly(browseOnly);

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.SESSIONID, sId);

        user.setCsid(CommonConstants.getRequestAttribute(request, VAROrderManagerFSV.SAMLAttributes.CSID.value));
        return user;
    }
}
