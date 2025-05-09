package com.b2s.apple.services;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    public enum AnalyticsEventName {
        ORDER_CANCELLATION
    }

    public enum AnalyticsType {
        HEAP("heap");

        private String value;

        AnalyticsType(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private HeapAnalyticsService heapAnalyticsService;

    public void trackEvent(final Order order,
                           final Program program,
                           final User user,
                           final AnalyticsEventName eventName,
                           final AnalyticsType analyticsType) {

        if (isAnalyticsEnabled(program, analyticsType)) {
            switch (analyticsType) {
                case HEAP:
                    heapAnalyticsService.trackEvent(order, program, user, eventName);
                    break;
                default:
                    logger.warn("Analytics {} not defined", analyticsType.getValue());
                    break;
            }
        } else {
            logger.debug("{} analytics is disabled for var : {} - program : {}",
                    analyticsType.getValue(),
                    program.getVarId(),
                    program.getProgramId());
        }
    }

    private boolean isAnalyticsEnabled(final Program program, final AnalyticsType analyticsType) {
        final List<String> analytics = AppleUtil.getProgramConfigValueAsList(program, CommonConstants.ANALYTICS);
        return analytics.contains(analyticsType.getValue());
    }

}
