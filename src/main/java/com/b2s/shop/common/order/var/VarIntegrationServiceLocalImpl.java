package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.DemoUserDao;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.apple.entity.DemoUserEntity;
import com.b2s.apple.services.DemoUserService;
import com.b2s.common.util.EncryptionUtil;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *Created by rpillai on 3/3/2016.
 */
@Service
public class VarIntegrationServiceLocalImpl implements VarIntegrationService {
    private static final Logger LOG = LoggerFactory.getLogger(VarIntegrationServiceLocalImpl.class);

    @Autowired
    private DemoUserService demoUserService;

    @Autowired
    private DemoUserDao demoUserDao;

    @Override
    public String getBaseUrl(String contextPath) {
        return null;
    }

    @Override
    public RedemptionResponse performRedemption(final Order order, final User user, final Program program) throws Exception {
        RedemptionResponse redemptionResponse = null;
        int totalOrderPoints=0;
        try {
            OrderLine line=null;
            for (int y=0; y<order.getOrderLines().size();y++) {
                line=(OrderLine)order.getOrderLines().get(y);
                totalOrderPoints+=(line.getOrderLinePoints()*line.getQuantity());
            }

            if(!user.isAnonymous()){
                user.setPoints(user.getPoints() - totalOrderPoints);
                if (!CommonConstants.SAML.equalsIgnoreCase(user.getLoginType())){
                    updateUserPoints(order, user.getPoints());
                }
            }

            redemptionResponse = new RedemptionResponse();
            redemptionResponse.setOrderId(order.getOrderId().toString());
            redemptionResponse.setVarOrderId(order.getOrderId().toString());
            final List<RedemptionOrderLine> redemptionOrderLines = new ArrayList<>();
            order.getOrderLines().stream().forEach(op -> {
                                                           final RedemptionOrderLine redemptionOrderLine =new RedemptionOrderLine();
                                                           redemptionOrderLine.setVarOrderLineId(((OrderLine)op).getSupplierOrderId());
                                                           redemptionOrderLines.add(redemptionOrderLine);
                                                         });
            redemptionResponse.setOrderLines(redemptionOrderLines);
        }catch (final Exception ex) {
            redemptionResponse = null;
            LOG.error("Error while performing redemption for local program: {}", user.getProgramId(), ex);
        }
        return redemptionResponse;
    }

    private void updateUserPoints(final Order order, final Integer points) {
        //Update UserPoints
        final DemoUserEntity demoUserEntity =
            demoUserService.selectUser(order.getVarId(), order.getProgramId(), order.getUserId());
        demoUserEntity.setPoints(points);
        demoUserService.updateUser(demoUserEntity);
    }

    @Override
    public CancelRedemptionResponse performPartialCancelRedemption(final Order order, final User user,
        final Program program) {
        CancelRedemptionResponse cancelRedemptionResponse = null;
        int totalOrderPoints=0;
        try {
            OrderLine line=null;
            for (int y=0; y<order.getOrderLines().size();y++) {
                line=(OrderLine)order.getOrderLines().get(y);
                totalOrderPoints+=(line.getOrderLinePoints()*line.getQuantity());
            }
            user.setPoints(user.getPoints()+totalOrderPoints);

            updateUserPoints(order, user.getPoints());

            cancelRedemptionResponse = new CancelRedemptionResponse();
        }catch (final Exception ex) {
            cancelRedemptionResponse = null;
            LOG.error("Error while performing cancel redemption for local program: {}", user.getProgramId(), ex);
        }
        return cancelRedemptionResponse;
    }

    @Override
    public CancelRedemptionResponse performCancelRedemption(final Order order, final int totalPointsRefund) throws IOException, Exception {
        CancelRedemptionResponse cancelRedemptionResponse = null;
        int totalOrderPoints=0;
        try {
            OrderLine line=null;
            for (int y=0; y<order.getOrderLines().size();y++) {
                line=(OrderLine)order.getOrderLines().get(y);
                totalOrderPoints+=(line.getOrderLinePoints()*line.getQuantity());
            }
            final DemoUserEntity demoUserEntity = demoUserService.selectUser(order.getVarId(), order.getProgramId(), order.getUserId());
            demoUserEntity.setPoints(demoUserEntity.getPoints()+totalOrderPoints);
            demoUserService.updateUser(demoUserEntity);
            cancelRedemptionResponse = new CancelRedemptionResponse();
        }catch (final Exception ex) {
            cancelRedemptionResponse = null;
            LOG.error("Error while performing cancel redemption for local program: {}", order.getProgramId(), ex);
        }
        return cancelRedemptionResponse;
    }

    @Override
    public int getLocalUserPoints(final User user) {
        User dbUser = new User();
        dbUser.setProgramid(user.getProgramid());
        dbUser.setUserid(user.getUserId());
        dbUser.setVarid(user.getVarId());
        dbUser.setPassword(user.getPassword());
        final DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId();
        demoUserId.setProgramId(user.getProgramId());
        demoUserId.setVarId(user.getVarId());
        demoUserId.setUserId(user.getUserId());
        final DemoUserEntity demoUserEntity = demoUserDao.findByDemoUserIdAndPassword(demoUserId, EncryptionUtil.encrypt(user.getPassword()));

        dbUser = dbUser.selectUserOnly(demoUserEntity);
        if (Objects.nonNull(dbUser)){
            return dbUser.getBalance();
        }
        return user.getBalance();
    }

    @Override
    public int getUserPoints(final String varId, final String programId, final String accountId, final Map<String, String> addInfo) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method Not Supported for Local Programs!");
    }

    @Override
    public AccountInfo getUserProfile(final String varId, final String accountId, final Map<String, String> additionalInfo) {
        return null;
    }

    @Override
    public SessionResponse getUserSession(final User user) {
        return null;
    }

}
