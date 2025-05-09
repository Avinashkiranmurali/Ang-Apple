package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.VarProgramNotificationDao;
import com.b2s.rewards.apple.model.VarProgramNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_LOCALE;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

/**
 * Created by hranganathan on 11/7/2016.
 */
@Service
public class VarProgramNotificationService {

    @Autowired
    private VarProgramNotificationDao varProgramNotificationDao;

    public List<VarProgramNotification> getActiveEmailNotifications(final String varId, final String programId,
        final String locale) {
        return varProgramNotificationDao.getActiveEmailNotifications(
            List.of(DEFAULT_VAR_PROGRAM, varId),
            List.of(DEFAULT_PROGRAM_KEY, programId),
            List.of(DEFAULT_LOCALE, locale)
        );
    }
}
