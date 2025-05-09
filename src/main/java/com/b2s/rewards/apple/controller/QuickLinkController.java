package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.QuickLinkService;
import com.b2s.rewards.apple.exceptionhandler.ArgumentsNotValidException;
import com.b2s.rewards.apple.exceptionhandler.InvalidResponseException;
import com.b2s.rewards.apple.model.QuickLink;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

import static com.b2s.rewards.common.util.CommonConstants.USER_SESSION_OBJECT;

@RestController
@RequestMapping(value = "/quicklinks")
public class QuickLinkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickLinkController.class);

    @Autowired
    private QuickLinkService quickLinkService;

    @Autowired
    private HttpServletRequest servletRequest;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<QuickLink>> getByLocaleVarIdProgramId() {

        final User user = (User) servletRequest.getSession().getAttribute(USER_SESSION_OBJECT);

        if (Objects.nonNull(user) && StringUtils.isNotBlank(user.getVarId()) &&
                StringUtils.isNotBlank(user.getProgramId()) && StringUtils.isNotBlank(user.getLocale().toString())) {
            try {
                final List<QuickLink> quickLink = quickLinkService.getByVarIdProgramIdLocale(user);
                return new ResponseEntity<>(quickLink, HttpStatus.OK);
            } catch (final InvalidResponseException ex) {
                LOGGER.error("Unexpected error occurred while fetching QuickLink data", ex);
                throw new InvalidResponseException();
            }
        }
        LOGGER.error("VarProgramTemplate: Invalid locale, varId and ProgramId");
        throw new ArgumentsNotValidException();
    }
}
