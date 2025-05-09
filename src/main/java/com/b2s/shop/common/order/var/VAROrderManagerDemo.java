package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component("varOrderManagerDemo")
public class VAROrderManagerDemo extends GenericVAROrderManager {

    private static final String VAR_ID = "Demo";
    private static final String LOCALE_STRING = "en_US";

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    protected String getLocale() {
        return LOCALE_STRING;
    }


    @Override
    public boolean isSendOrderConfirmationEmailToUser() {
        return true;
    }

    @Override
    public User selectUser(HttpServletRequest httpServletRequest) throws B2RException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final User user = selectLocalUser(request, new User());

        initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);

        //Select program information from database
        final Program program = getProgram(user);
        if(program.getConfig() != null && "epp".equalsIgnoreCase(program.getProgramId())) {
            program.getConfig().put(CommonConstants.PAY_PERIODS, applicationProperties.getProperty(CommonConstants.NUM_PAY_PERIODS));
        }
        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);

        prepareUserAddress(user);
        user.setIsEligibleForPayrollDeduction(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ELIGIBLE_FOR_PAYROLL_DEDUCTION)));
        user.setIsEligibleForDiscount(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ELIGIBLE_FOR_DISCOUNT)));
        final String discountCode = request.getParameter("discountcode");
        if (isNotBlank(discountCode)) {
            applyDiscountCode(request, user, discountCode);
        }

        //Set Bag Menu URLs from DB
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), null);
        return user;
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, Program program) {
        return true;
    }

}
