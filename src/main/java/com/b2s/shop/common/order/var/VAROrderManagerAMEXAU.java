package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component("varOrderManagerAmexAU")
public class VAROrderManagerAMEXAU extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerAMEXAU.class);

    private static final String VAR_ID = CommonConstants.VAR_AMEX_AU;
    private static final String DEFAULT_LOCALE = CommonConstants.LOCALE_EN_AU;


    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    @Override
    public User selectUser(final HttpServletRequest request)
        throws B2RException {

        UserAmexAU user ;
        Program program = null;

        CommonConstants.LoginType loginType = null;

        if (request.getParameter(CommonConstants.USER_ID) != null &&
            request.getParameter(CommonConstants.USER_ID).toLowerCase()
                .contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())) {
            user = (UserAmexAU) updateUser(request, new UserAmexAU(), CommonConstants.COUNTRY_CODE_AU, true);
            program = getProgram(user);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        } else if (request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) == null) {
            user = (UserAmexAU) selectLocalUser(request, new UserAmexAU());
            setAmexUserData(request, user);
            LOGGER.debug("AMEX user data {}", user);
            initializeLocaleDependents(request, user, DEFAULT_LOCALE,null);
            program = getProgram(user);
            program.getConfig().put(CommonConstants.PAY_PERIODS, getPayPeriod(program));
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            //TODO User creation for SAML login.
            user = getUserFromSAMLAttributes(request);
            loginType = CommonConstants.LoginType.SAML;
        }

        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        // set default balance
        user.setBalance(getUserPoints(user, program));
        // Default true for now.
        user.setIsEligibleForPayrollDeduction(true);

        // Trim address fields.
        prepareUserAddress(user);

        setSessionTimeOut(request, user);

        //Set Bag Menu URLs from DB
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);
        return user;
    }


    public User setAmexUserData(final HttpServletRequest request, final UserAmexAU user) {

        final Properties properties = (Properties) AppContext.getApplicationContext()
            .getBean(CommonConstants.APPLICATION_PROPERTIES);
        final String imageServerUrl = properties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY);
        UserAmexAU.CardInContext cic =
            user.new CardInContext(imageServerUrl + "/apple-gr/vars/amexau/cards/gold-card.png",
                "Gold Card", 12345, "3782822246310005", 2011);
        user.setCardInContext(cic);

        List<UserAmexAU.CreditCardInfo> cards = new ArrayList<>();
        UserAmexAU.CreditCardInfo card =
            user.new CreditCardInfo(imageServerUrl + "/apple-gr/vars/amexau/cards/platinum-travel-card.png",
                "Platinum Travel Card", "https://amex.com/login/wewred");
        cards.add(card);

        card = user.new CreditCardInfo(imageServerUrl + "/apple-gr/vars/amexau/cards/payback-card.png",
            "Payback Card",
            "https://amex.com/login/gsdfge");
        cards.add(card);

        card = user.new CreditCardInfo(
            imageServerUrl + "/apple-gr/vars/amexau/cards/platinum-reserve-card.png",
            "Platinum Reserve Card", "https://amex.com/login/prcweg");
        cards.add(card);

        user.setAdditionalCards(cards);
        return user;
    }
    private String getPayPeriod(final Program program) {

        if (StringUtils.isNotBlank((String) program.getConfig().get(CommonConstants.INSTALLMENT))) {
            final String[] payPeriod = program.getConfig().get(CommonConstants.INSTALLMENT).toString().split(",");
            return String.valueOf(Arrays.stream(payPeriod).mapToInt(Integer::parseInt).reduce(Integer.MIN_VALUE, Integer::max));
        } else {
            return null;
        }
    }

    private UserAmexAU getUserFromSAMLAttributes(final HttpServletRequest request) {
        UserAmexAU user = new UserAmexAU();
        //TODO User creation for SAML login.
        return user;
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        //TODO will implement in new story
        return true;
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, final Program program) {
        //TODO will implement in new story
        return false;
    }

    @Override
    public int getUserPoints(final User user, final Program program)
        throws B2RException {
        //TODO will implement in new story
        return 9999999;  //  Var is cash based so setting points to max overcome not enough balance scenario
    }

}
