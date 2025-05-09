package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CarouselService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.rewards.apple.integration.model.CarouselResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/carousel")
public class CarouselController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselController.class);

    @Autowired
    private AppSessionInfo appSessionInfo;

    @Autowired
    private CarouselService carouselService;

    @GetMapping("/{page}")
    public ResponseEntity<Set<CarouselResponse>> getCarouselResponse(@PathVariable("page") final String pageVariable,
        final HttpServletRequest request, final HttpServletResponse response) {
        final String page = HtmlUtils.htmlEscape(pageVariable);
        LOGGER.info("Get Carousel Response for Page: {}", page);
        final User user = appSessionInfo.currentUser();
        Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);

        Set<CarouselResponse> carouselResponses = new TreeSet<>();
        try {
            carouselResponses = carouselService.getCarouselResponse(user, program, page);
            return ResponseEntity.ok(carouselResponses);
        } catch (ServiceException e) {
            LOGGER.error("Failed to load {} page Carousel products for the user: {}", page, user.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(carouselResponses);
        }
    }
}
