package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.VarProgramTemplateService;
import com.b2s.rewards.apple.model.VarProgramTemplate;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author rkumar 2020-02-10
 */
@RestController
@RequestMapping(value = "/configData")
public class VarProgramTemplateController {

    @Autowired
    private VarProgramTemplateService varProgramTemplateService;

    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramTemplateController.class);

    @GetMapping(value = "", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getConfigDataByVarIdProgramId(final HttpServletRequest request) {

        final User user = (User)request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);


        if (Objects.nonNull(user)) {
            try {
                final VarProgramTemplate varProgramTemplate =
                    varProgramTemplateService.getConfigDataByUser(user);
                if (Objects.isNull(varProgramTemplate)) {
                    return new ResponseEntity<>("Invalid Response", HttpStatus.UNAUTHORIZED);
                } else {
                    return ResponseEntity.ok(varProgramTemplate.getConfigData());
                }
            } catch (final Exception ex) {
                LOGGER.error("Unexpected error occurred while fetching configData ", ex);
                return new ResponseEntity<>("Unexpected error occurred while fetching configData",
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        LOGGER.error("VarProgramTemplate: Invalid User varId and ProgramId");
        return new ResponseEntity<>("User varId and ProgramId not found", HttpStatus.NOT_FOUND);
    }

}
