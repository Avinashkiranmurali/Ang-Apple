package com.b2s.shop.common.order.var;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.services.MessageService;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.ErrorInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.util.USER_MSG;
import com.google.gson.Gson;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by hranganathan on 5/11/2017.
 */
public abstract class GenericVAROrderManager extends AbstractVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericVAROrderManager.class);

    @Autowired
    protected Map<String, VarIntegrationService> varIntegrationServices;

    @Autowired
    protected VarProgramConfigDao varProgramConfigDao;

    @Autowired
    protected MessageService messageService;

    @Autowired
    protected VarIntegrationServiceRemoteImpl varIntegrationServiceRemoteImpl;

    @Autowired
    protected VarIntegrationServiceLocalImpl varIntegrationServiceLocalImpl;


    protected VarIntegrationService getVISImplementation(final User user, final boolean isLocal) throws B2RException {
        VarProgramConfigEntity varProgramConfig = varProgramConfigDao.getVarProgramConfigByVarProgramName(user.getVarId(),
            user.getProgramId(), CommonConstants.VIS_MOCK_ENABLED);
        Boolean isMock = Boolean.FALSE;
        if(varProgramConfig != null) {
            isMock = Boolean.valueOf(varProgramConfig.getValue());
        }
        VISLookupStrategy visLookupStrategy = VISLookupStrategy.lookup(user.getVarId(), isLocal, isMock);
        return varIntegrationServices.get(visLookupStrategy.getStrategyKey());

    }

    @Override
    public boolean placeOrder(Order order, User user, Program program) {
        try {
            //used by  chase, delta, pnc, scotia vars
            final RedemptionResponse redemptionResponse = getRedemptionResponse(order, user, program);
            //assign/update order with the redemption response
            if (Objects.isNull(redemptionResponse)) {
                return false;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            //adjust user points
            getBalanceUserPoints(order, user);
            message = getMessage(redemptionResponse);

        } catch (final Exception ex) {
            LOGGER
                .error("Error updating user Var details in VarIntegrationService...Exception trace:  ", ex.getMessage(),
                    ex);
            insertMessageVISException(order, user);
        }

        return message.isSuccess();

    }

    protected RedemptionResponse getRedemptionResponse(final Order order, final User user, final Program program)
        throws Exception {
        final RedemptionResponse redemptionResponse;
        if (Objects.nonNull(program) && program.getIsLocal()) {
            redemptionResponse = varIntegrationServiceLocalImpl.performRedemption(order, user, program);
        } else {
            redemptionResponse = varIntegrationServiceRemoteImpl.performRedemption(order, user, program);
        }
        return redemptionResponse;
    }

    protected Message getMessage(final RedemptionResponse redemptionResponse) {

        final Message msg = new Message();
        msg.setSuccess(true);
        msg.setContentText("GenericVAROrderManager successfully placed order: Order #"+redemptionResponse.getOrderId());
        msg.setVAROrderId(VAROrderId);
        msg.setCode(OrderCodeStatus.SUCCESS.getValue());
        return msg;
    }

    protected void insertMessageVISException(final Order order, final User user) {
        message = new Message();

        //noinspection deprecation
        final String tst =
            messageService.getMessage(user.getVarId(), user.getProgramId(), -1, USER_MSG.ORDER_IN_PROCESS);
        if (Objects.nonNull(order)) {
            if (StringUtils.isBlank(order.getVarOrderId())) {
                order.setVarOrderId(tst);
            }
            message.setContentText("Error in VIS OrderRedemption process for Order: " + order.getOrderId());
        } else {
            message.setContentText("Error in VIS OrderRedemption process ");
        }
        message.setSuccess(false);
        message.setCode(OrderCodeStatus.FAIL.getValue());
        if (Objects.nonNull(order)) {
            message.setVAROrderId(order.getVarOrderId());
            messageService
                    .insertMessageException(user, this.getClass().getName(), "ERROR: Sending Order: " + order.getOrderId(),
                            null);
        }

    }

    @Override
    protected User getUser(final String sessionId, final Program program, final boolean selectProgram,
        final ServletRequest request) {
        return null;
    }

    @Override
    public boolean cancelOrder(Order order) {
        return false;
    }

    @Override
    public boolean cancelOrder(Order order, User user, Program program) {
        try {
            if (program != null && program.getIsLocal()) {
                varIntegrationServiceLocalImpl.performPartialCancelRedemption(order, user, program);
            } else {
                varIntegrationServiceRemoteImpl.performPartialCancelRedemption(order, user, program);
            }
            return true;
        } catch (final Exception ex) {
            LOGGER
                .error("Error updating user Var details in GenericVAROrderManager...Exception trace:  ", ex.getMessage(),
                    ex);
            return false;
        }
    }

    protected boolean performCancelRedemption(final Order order, Program program) {

        try {
            // VIS call
            int totalOrderPoints = order.getOrderTotalPointsPaid();
            if (program != null && program.getIsLocal()) {
                varIntegrationServiceLocalImpl.performCancelRedemption(order, totalOrderPoints);
            } else {
                varIntegrationServiceRemoteImpl.performCancelRedemption(order, totalOrderPoints);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while performing parial cancel redemption for order id {}, with error message {} and exception {}", order.getOrderId(), e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public int getUserPoints(User user, Program program) throws B2RException {
        final VarIntegrationService varIntegrationService = getVISImplementation(user, program.getIsLocal());
        try {
            if (program != null && program.getIsLocal()) {
                return varIntegrationService.getLocalUserPoints(user);
            } else {
                // Some how additionalInfo is empty that is why adding this again.
                // TODO : remove later - investigate where the additonInfo is getting empty
                Map<String, String> additonalInfo = new HashMap();
                if(user.getAdditionalInfo() != null) {
                    additonalInfo = user.getAdditionalInfo();
                }
                additonalInfo.put(CommonConstants.SESSIONID, user.getSid());
                user.setAdditionalInfo(additonalInfo);
                return varIntegrationService.getUserPoints(user.getVarId(), user.getProgramId(), user.getUserId(), user.getAdditionalInfo());
            }
        } catch (final MethodNotSupportedException e) {
            LOGGER.error("Error updating user Var details in GenericVAROrderManager...Exception trace:  ", e);
            throw new B2RException(e.getMessage());
        }
    }

    /**
     * @param properties
     * @param varOrderId
     * @param orderId
     * @param lineNum
     * @param carrier
     * @param shippingDesc
     * @param trackingNum
     * @param status
     * @param points
     * @return
     */
    @Override
    public boolean updateOrderStatus(final Map<String, Object> properties, final String varOrderId,
        final String orderId, final String lineNum, final String carrier, final String shippingDesc,
        final String trackingNum, final String status, final Double points) {

        return false;
    }

    protected void placeOrderExceptionHandler(Order order, User user, Exception ex) {
        LOGGER.error(
            "Error updating user Var details in VarIntegrationService for order id {}..Exception trace: {}",
            order.getOrderId(), ex);
        message = new Message();
        //noinspection deprecation
        final String tst =
            messageService.getMessage(user.getVarId(), user.getProgramId(), -1, USER_MSG.ORDER_IN_PROCESS);
        String errorMessage = ex.getMessage();
        ErrorInfo errorInfo = new Gson().fromJson(errorMessage, ErrorInfo.class);

        if (errorInfo != null && StringUtils.isNotBlank(errorInfo.getMessage())) {
            String[] errorArr = errorInfo.getMessage().split(":");
            if (ArrayUtils.isNotEmpty(errorArr)) {
                errorMessage = errorArr[errorArr.length - 1];
            }
        }
        message.setContentText(errorMessage);
        message.setSuccess(false);
        if (Objects.nonNull(order) && StringUtils.isBlank(order.getVarOrderId())) {
            order.setVarOrderId(tst);
        }
        message.setVAROrderId(order.getVarOrderId());
        message.setCode(OrderCodeStatus.FAIL.getValue());
        messageService
            .insertMessageException(user, this.getClass().getName(), "ERROR: Sending Order: " + order.getOrderId(),
                null);
    }

    protected void setMessageSuccess(final Order order, final Message msg) {
        msg.setSuccess(true);
        msg.setContentText("VAROrderManager successfully placed order: Order# " + order.getVarOrderId());
        msg.setVAROrderId(VAROrderId);
        msg.setCode(OrderCodeStatus.SUCCESS.getValue());
    }

    /**
     * Part of story APL-2184: Send "programId" in the VIMS AccountInfo and AccountBalance calls,
     * always adding programId with the additionalInfo
     */
    protected Map<String, String> retrieveUserAdditionalInfoWithProgramId(final User user) {
        Map<String, String> additionalInfo = user.getAdditionalInfo();
        if (Objects.isNull(additionalInfo)) {
            additionalInfo = new HashMap<>();
            user.setAdditionalInfo(additionalInfo);
        }
        additionalInfo.put(CommonConstants.PROGRAM_ID, user.getProgramId());
        return additionalInfo;
    }
}
