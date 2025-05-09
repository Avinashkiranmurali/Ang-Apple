package com.b2s.shop.common.order.var;


import com.b2s.db.model.Order;
import com.b2s.rewards.apple.model.OrderAttributeValue;
import com.b2s.rewards.apple.model.Program;
import com.b2s.apple.services.CartOrderConverterService;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Amex manager. User is anonymous and its all cash purchase so there is no remote VIMS call.
 * Date 2019/10/31
 */
@Component("varOrderManagerAmex")
public class VAROrderManagerAmex extends GenericVAROrderManager {
    private static final String VAR_ID = "Amex";

    /**
     * @return VAR_ID
     */
    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    /**
     * Creates and returns a user using AbstractVAROrderManager's initializeProgramProperties method
     *
     * @param httpServletRequest HttpServletRequest
     * @return user User
     */
    @Override
    public User selectUser(final HttpServletRequest httpServletRequest) throws B2RException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final User user;
        String discountCode;
        if(anonymousLogin(request)) {
            user = getUser(request);
            user.setAnonymous(true);
            discountCode = request.getParameter(CommonConstants.ANONYMOUS_DISCOUNT_CODE_REQ_PARAM);
        } else {
            user = selectLocalUser(request, new User());
            discountCode = request.getParameter("discountcode");
            user.setIsEligibleForDiscount(Boolean.valueOf(request.getParameter(CommonConstants.IS_ELIGIBLE_FOR_DISCOUNT)));
        }

        prepareUserAddress(user);
        setSessionTimeOut(request, user);
        if (isNotBlank(discountCode)) {
            applyDiscountCode(request, user, discountCode);
        }

        final Program program = getProgram(user);
        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), null);
        user.setBalance(getUserPoints(user, program));
        return user;
    }

    /**
     * Amex user is anonymous.
     * @param httpServletRequest HttpServletRequest
     * @return User
     */
    @Override
    protected User getUser(final HttpServletRequest httpServletRequest){
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        String localeStr = request.getParameter(CommonConstants.LOCALE);
        final Locale locale = (StringUtils.isNotBlank(localeStr)) ? LocaleUtils.toLocale(localeStr) : Locale.US;
        User user = updateUser(request, new User(), locale.getCountry(), false);
        return user;
    }

    /**
     * Attempts to place order through varIntegrationServiceLocalImp and creates message based on fail or success
     *
     * @param order the order
     * @param user  the current user
     * @return boolean based on if order is successful
     */
    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            final OrderAttributeValue orderAttributeValue = CartOrderConverterService
                .buildOrderAttribute(CommonConstants.PROGRAM_LOGO_URL, program.getImageUrl(), order.getOrderId());
            if (Objects.nonNull(orderAttributeValue)) {
                orderAttributeValueDao.insert(orderAttributeValue);
            }
            varIntegrationServiceLocalImpl.performRedemption(order, user, program);
            message = MessageUtils.setSuccessMessage(VAROrderId, order);
            order.setVarOrderId(order.getOrderId().toString());
        } catch (final Throwable ex) {
            message = MessageUtils.setFailureMessage(VAROrderId, order);
        }
        return message.isSuccess();
    }

    /**
     * Attempts to cancel order and refund points through varIntegrationServiceLocalImp and creates message based on
     * fail or success
     *
     * @param order Order
     * @param user User
     * @return boolean based on if cancel is successful
     */
    @Override
    public boolean cancelOrder(final Order order, final User user, final Program program) {

        return true;
    }

    /**
     * No implementation
     *
     * @param properties Map
     * @param varOrderId String
     * @param orderId String
     * @param lineNum Strin g
     * @param carrier String
     * @param shippingDesc String
     * @param trackingNum String
     * @param status String
     * @param points Double
     * @return true
     */
    @Override
    public boolean updateOrderStatus(final Map<String, Object> properties, final String varOrderId,
                                     final String orderId, final String lineNum,
                                     final String carrier, final String shippingDesc, final String trackingNum, final String status,
                                     final Double points) {
        return true;
    }

    /**
     * Returns User's current points
     *
     * @param user current user
     * @return points, current points as int
     */
    @Override
    public int getUserPoints(final User user, final Program program) {
        return 999999; // cash var
    }
}
