package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.services.AppleKountService;
import com.client.kount.KountService;
import com.client.kount.model.KountSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by rpillai on 7/9/2018.
 */
@Controller
public class KountController {

    private static final Logger logger = LoggerFactory.getLogger(KountService.class);

    @Autowired
    private KountService kountService;

    @Autowired
    private AppleKountService appleKountService;

    @RequestMapping(value = "/kount/data-collector",method = RequestMethod.GET)
    public ResponseEntity getDataCollector(final HttpServletRequest request) {

        final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        if(appleKountService.isKountEnabled(program)) {
            KountSession kountSession = (KountSession) request.getSession().getAttribute(CommonConstants.KOUNT_SESSION_OBJECT);
            if(kountSession == null) {
                kountSession = new KountSession();
            }
            request.getSession().setAttribute(CommonConstants.KOUNT_SESSION_OBJECT, kountSession);
            try {
                return ResponseEntity.ok(kountService.getDataCollectorHtml(kountSession));
            } catch (Exception e) {
                logger.error("Error while creating kount data collector", e);
            }
        }
        return ResponseEntity.noContent().build();
    }


}
