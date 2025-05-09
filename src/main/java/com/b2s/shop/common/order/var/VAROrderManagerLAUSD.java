package com.b2s.shop.common.order.var;

import com.b2s.apple.services.LAUSDIntegrationService;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.lausd.ParentInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.security.oauth.OAuthConfig;
import com.b2s.security.oauth.OAuthCredentials;
import com.b2s.security.oauth.Token;
import com.b2s.security.oauth.service.OAuthTokenService;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component("varOrderManagerLAUSD")
public class VAROrderManagerLAUSD extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerLAUSD.class);

    private static final String VAR_ID = "LAUSD";
    private static final String PROGRAM_ID = "iPad";
    private static final String LOCALE_STRING = "en_US";

    @Autowired
    private OAuthTokenService authTokenService;

    @Autowired
    private OAuthConfig authConfig;

    @Autowired
    private LAUSDIntegrationService integrationService;

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    protected String getLocale() {
        return LOCALE_STRING;
    }

    private enum SAMLAttributes {
        TENANTID("http://schemas.microsoft.com/identity/claims/tenantid"),
        OBJECTIDENTIFIER("http://schemas.microsoft.com/identity/claims/objectidentifier"),
        DISPLAYNAME("http://schemas.microsoft.com/identity/claims/displayname"),
        IDENTITYPROVIDER("http://schemas.microsoft.com/identity/claims/identityprovider"),
        AUTHNMETHODSREFERENCES("http://schemas.microsoft.com/claims/authnmethodsreferences"),
        GIVENNAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"),
        SURNAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"),
        EMAILADDRESS("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"),
        NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
        public String getValue()
        {
            return value;
        }
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final UserLAUSD user;
        String discountCode = "";
        CommonConstants.LoginType loginType = null;
        Program program = null;

        if (Objects.isNull(request.getParameter(CommonConstants.RELAY_STATE))) {
            // 5 Box Login
            user = (UserLAUSD) selectLocalUser(request, new UserLAUSD());
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            // SAML Flow
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes ->
                LOGGER.info("LAUSD SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value)));
            LOGGER.info("LAUSD SAML UserPrincipal ----{}", request.getUserPrincipal().getName());
            user = getUser(request);
            loginType = CommonConstants.LoginType.SAML;
        }

        user.setLoginType(loginType.getValue());
        initializeLocaleDependents(request, user, getLocale(),null);
        program = getProgram(user);

        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        if(!program.getIsLocal()){
            //set parent information
            getParentInformation(user);
        }


        //LAUSD users will get DiscountCode from VPC
        if (MapUtils.isNotEmpty(program.getConfig())) {
            discountCode = (String)program.getConfig().get(CommonConstants.DISCOUNTCODE_KEY);
            if (StringUtils.isNotBlank(discountCode)) {
                applyDiscountCode(request, user, discountCode);
            }
        }

        // Discount code validation
        if(CollectionUtils.isEmpty(user.getDiscounts()) || user.getDiscounts().size()==0){
            request.setAttribute(CommonConstants.LOCALE, user.getLocale().toString());
            throw new IllegalArgumentException(CommonConstants.INVALID_DISCOUNT_CODE);
        }
        setSessionTimeOut(request, user);

        //Bag Menu URLs from DB is having less precedence than SAML response
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);

        prepareUserAddress(user);
        return user;
    }

    @Override
    protected UserLAUSD getUser(HttpServletRequest request) {
        UserLAUSD user = new UserLAUSD();
        user.setVarId(getVARId());
        user.setProgramId(PROGRAM_ID);
        user.setUserId(request.getUserPrincipal().getName());
        return user;
    }

    @Override
    public boolean placeOrder(Order order, User user, Program program) {
        try {

            order.setVarOrderId(order.getOrderId().toString());
            final Message msg = new Message();
                msg.setSuccess(true);
                msg.setContentText("VAROrderManagerLAUSD successfully placed order: Order #"+order.getOrderId());
                msg.setVAROrderId(VAROrderId);
                msg.setCode(OrderCodeStatus.SUCCESS.getValue());

            message = msg;

        } catch (final Exception ex) {
            LOGGER.error("Error updating user Var details in VarIntegrationService...Exception trace:  ", ex.getMessage(), ex);
            insertMessageVISException(order, user);
        }
        return message.isSuccess();

    }

    @Override
    public boolean cancelOrder(Order order, User user, Program program) {
        return true;
    }

    @Override
    public int getUserPoints(User user, Program program) throws B2RException {
        if (Objects.nonNull(program) && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isOrderReadyForProcessing() {
        return false;
    }


    private void getParentInformation(final UserLAUSD user) throws B2RException {
        int counter = 0;
        OAuthCredentials authCredential=authConfig.getOAuthCredentials(user.getVarId());

        //get token
        Token token=authTokenService.getTokenFormClientCredential(authCredential);

        if(Objects.isNull(token) || StringUtils.isBlank(token.getAccessToken())){
            LOGGER.warn("LAUSD - Unable to retrieve token ");
        }else {
            //invoke parent WS
            List<ParentInfo> parentInfoList = integrationService.getParentInformation(user, authCredential, token);

            if(CollectionUtils.isNotEmpty(parentInfoList)){
                user.setParents(parentInfoList);

                for (ParentInfo parent : parentInfoList) {
                    counter++;
                    user.getAdditionalInfo().put(CommonConstants.PARENT_EMAIL + counter, parent.getParentEmail());
                    user.getAdditionalInfo().put(CommonConstants.PARENT_FIRST_NAME + counter, parent.getParentFirstName());
                    user.getAdditionalInfo().put(CommonConstants.PARENT_LAST_NAME + counter, parent.getParentLastName());
                }
            }
        }
    }
}