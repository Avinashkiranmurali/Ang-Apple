package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.financeoptions.FinanceOptionsServiceFactory;
import com.b2s.common.services.financeoptions.service.FinanceOptionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value="/", produces = "application/json;charset=UTF-8")
@ResponseBody
public class FinanceOptionsController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceOptionsController.class);

    private FinanceOptionsService financeOptionsService;

    @Autowired
    private FinanceOptionsServiceFactory financeOptionsServiceFactory;

    @Autowired
    private AppSessionInfo appSessionInfo;
    /**
     * Get Finance Options for the associated VAR
     *
     * @param user {@link User}
     * @param amount Double
     * @param cardId String
     * @param request HttpServletRequest
     * @return ResponseEntity<FinanceOptionsResponse>
     */
    @ResponseBody
    @RequestMapping(value = {"/financeOption"}, method = RequestMethod.GET)
    public ResponseEntity<FinanceOptionsResponse> getFinanceOptions (@RequestParam(required = false, value = "amount") Double amount,
                                                              @RequestParam(required = false, value = "cardId") String cardId,
                                                              HttpServletRequest request) {

        final FinanceOptionsResponse financeOptionsResponse;
        final User user = appSessionInfo.currentUser();
        try {
            final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);

            financeOptionsService = financeOptionsServiceFactory.getFinanceOptionsService(AppleUtil.getFinanceOptionsServiceIdentifier(program));
            financeOptionsResponse = financeOptionsService.getFinanceOptions(program, amount, cardId);

        } catch (ServiceException se) {
            logger.error("Error from FinanceOptionsController.getFinanceOptions()", se.getMessage(), se);
            return new ResponseEntity(se.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(financeOptionsResponse, HttpStatus.OK);
    }

    /**
     * Get the list of Cards for the associated VAR
     *
     * @param user {@link User}
     * @param request HttpServletRequest
     * @return ResponseEntity<CardDetails>
     */
    @ResponseBody
    @RequestMapping(value = {"/getCards"}, method = RequestMethod.GET)
    public ResponseEntity<CardsResponse> getCards (HttpServletRequest request) {

        final CardsResponse cardsResponse;
        final User user = appSessionInfo.currentUser();
        try {
            final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);

            financeOptionsService = financeOptionsServiceFactory.getFinanceOptionsService(AppleUtil.getFinanceOptionsServiceIdentifier(program));
            cardsResponse = financeOptionsService.getCards();

        } catch (Exception se) {
            logger.error("Error from FinanceOptionsController.getCards()", se.getMessage(), se);
            return new ResponseEntity(se.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(cardsResponse, HttpStatus.OK);
    }

}