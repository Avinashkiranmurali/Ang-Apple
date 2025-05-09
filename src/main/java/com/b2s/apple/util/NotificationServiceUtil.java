package com.b2s.apple.util;

import com.b2s.rewards.apple.dao.OrderLineDao;
import com.b2s.rewards.apple.model.Notification;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_LOCALE;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

/**
 * Created by preddy on 8/26/2014.
 */
@Component
public class NotificationServiceUtil {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceUtil.class);

    @Autowired
    private OrderLineDao orderLineDao;

    public void updateNotificationByKey(final Long orderId, final long notificationId, final boolean isAmp,
        final Integer lineNum)
        throws Exception {

        final int rows = orderLineDao.updateNotificationByOrderId(notificationId, orderId, isAmp, lineNum);
        if (rows > 0) {
            logger.info("Notification id is updated in Orderline table - Order id {} updated rows {}", orderId, rows);
        } else {
            logger.error("Failed - Notification id is not updated in Orderline table - Order id {}", orderId);
        }
    }

    public Optional<Notification> getEmailNotification(Program program,
        CommonConstants.NotificationName notificationName) {
        Optional<Notification> notificationOpt = Optional.empty();

        if (program != null && CollectionUtils.isNotEmpty(program.getNotifications())) {
            notificationOpt = program.getNotifications()
                .stream()
                .filter(emailConfig -> notificationName.value.equalsIgnoreCase(emailConfig.getName()))
                .min((first, next) -> {
                    if (!first.getProgramId().equalsIgnoreCase(next.getProgramId())) {
                        if (next.getProgramId().equalsIgnoreCase(DEFAULT_PROGRAM_KEY)) {
                            return DEFAULT_VAR_PROGRAM.compareTo(first.getProgramId());
                        } else if (first.getProgramId().equalsIgnoreCase(DEFAULT_PROGRAM_KEY)) {
                            return next.getProgramId().compareTo(DEFAULT_VAR_PROGRAM);
                        } else {
                            return next.getProgramId().compareTo(first.getProgramId());
                        }
                    } else if (!first.getVarId().equalsIgnoreCase(next.getVarId())) {
                        return next.getVarId().compareTo(first.getVarId());
                    } else {
                        return next.getLocale().compareTo(first.getLocale());
                    }
                });
        }
        return notificationOpt;
    }
}

