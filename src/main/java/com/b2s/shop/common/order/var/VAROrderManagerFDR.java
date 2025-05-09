package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
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

@Component("varOrderManagerFDR")
public class VAROrderManagerFDR extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerFDR.class);

    private static final String VAR_ID = "FDR";

    private enum SAMLAttributes {
        NAVBACKURL("navBackURL"),
        VARID("varId"),
        PROGRAM_ID("programId"),
        POINTS_BALANCE("pointsBalance"),
        BROWSE_ONLY("browseOnly"),
        CSID("csid"),
        LOCALE("locale"),
        KEEPALIVEURL("keepAliveUrl"),
        COUNTRY("country"),
        ANONYMOUS("anonymous"),
        SID("sid"),
        PROXY_USER_ID("proxyUserId"),
        FULFILLMENT_AGENT("fulfillmentAgent"),
        KEYSTONE_BASE_URL("keystoneBaseUrl"),
        AGENT_BROWSE("agentBrowse"),
        CYCLE("cycle");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final User user ;
        Program program = null;
        CommonConstants.LoginType loginType = null;
        // 5 Box Login
        if (request.getParameter(CommonConstants.RELAY_STATE) == null) {
            user = selectLocalUser(request, new User());
            loginType = CommonConstants.LoginType.FIVEBOX;
            //Enable the checkbox at 5-box login for ANONYMOUS user to be added to User Object
            if(Objects.nonNull(request.getParameter(CommonConstants.IS_ANONYMOUS))){
                user.setAnonymous(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ANONYMOUS)));
            }

        } else {
            // SAML Flow
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes ->
                LOGGER.info("FDR/FDR_PSCU SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value)));
            LOGGER.info("FDR/FDR_PSCU SAML UserPrincipal ----{}", request.getUserPrincipal().getName());

            loginType = CommonConstants.LoginType.SAML;
            final boolean anonymous = anonymousCheck(request);

            //Anonymous check
            if (anonymous) {
                user = setAnonymousUser(request, new User());
            } else {
                user = getUser(request);
                initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);
                populateUserInfo(request, user, program);
            }

            if(Objects.nonNull(request.getAttribute(CommonConstants.FULFILLMENT_AGENT))){
                user.getAdditionalInfo().put(CommonConstants.FULFILLMENT_AGENT, (String)request.getAttribute(CommonConstants.FULFILLMENT_AGENT));
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

    private boolean anonymousCheck(HttpServletRequest request) {
        if (Objects.nonNull(request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE))) {
            return Boolean.valueOf((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE));
        }
        return false;
    }

    private void populateUserInfo(HttpServletRequest request, User user, Program program) {
        final AccountInfo accountInfo;
        if (program.getIsLocal()) {
            accountInfo = varIntegrationServiceLocalImpl.getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        } else {
            accountInfo = varIntegrationServiceRemoteImpl.getUserProfile(user.getVarId(), user.getUserId(), user.getAdditionalInfo());
        }

        if(Objects.nonNull(accountInfo)){
            if(Objects.nonNull(accountInfo.getUserInformation())){
                accountInfo.getUserInformation().copyDataToUser(user);
            }
            if(Objects.nonNull(accountInfo.getAccountBalance())){
                user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
                if (user.getAdditionalInfo() == null) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                user.getAdditionalInfo().put(CommonConstants.VIS_CURRENCY, accountInfo.getAccountBalance().getCurrency());
                user.getAdditionalInfo().put(CommonConstants.ACCOUNT_SESSIONID, user.getAdditionalInfo().get(CommonConstants.ACCOUNT_SESSIONID));
            }
        }else{
            if(program.getIsLocal() && Objects.nonNull(request.getAttribute(CommonConstants.POINTS_BALANCE))){
                user.setPoints(valueOf(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value)));
            }
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
                return true;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            getBalanceUserPoints(order,user);
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
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(),user.getProgramId(), user.getUserId(), user.getAdditionalInfo());
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
            CommonConstants.getRequestAttribute(request, SAMLAttributes.KEYSTONE_BASE_URL.value));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    @Override
    protected User getUser(HttpServletRequest request) {
        User user = new User();
        final String programId = (String)request.getAttribute(CommonConstants.PROGRAM_ID);
        final String userId = request.getUserPrincipal().getName();
        final String sId =  (String)request.getAttribute(CommonConstants.SID);
        final String accountSessionId =  (String)request.getAttribute(CommonConstants.ACCOUNT_SESSIONID);
        final boolean browseOnly = (Boolean.valueOf((String)request.getAttribute(SAMLAttributes.BROWSE_ONLY.value)));
        final boolean agentBrowse = (Boolean.parseBoolean((String)request.getAttribute(SAMLAttributes.AGENT_BROWSE.value)));
        user.setProgramId(programId);
        user.setVarId(getVARId());
        user.setUserId(userId);
        user.setSid(sId);
        user.setBrowseOnly(browseOnly);
        user.setAgentBrowse(agentBrowse);

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.ACCOUNT_SESSIONID,accountSessionId);

        final String cycle = CommonConstants.getRequestAttribute(request, SAMLAttributes.CYCLE.value);
        if (StringUtils.isNotBlank(cycle)) {
            user.getAdditionalInfo().put(SAMLAttributes.CYCLE.value, cycle);
        }

        user.setProxyUserId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROXY_USER_ID.value));
        user.setCsid(CommonConstants.getRequestAttribute(request, SAMLAttributes.CSID.value));
        return user;
    }
}
