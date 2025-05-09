package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.BannerConfigService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.dao.BannerConfigurationDao;
import com.b2s.rewards.apple.integration.model.BannerConfigResponse;
import com.b2s.rewards.apple.model.Banner;
import com.b2s.rewards.apple.model.BannerConfiguration;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Banner CRUD services
 *
 * @author rperumal
 */
@RestController
@RequestMapping(value="/banner", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
@ResponseBody
public class BannerController {

    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);
    public static final String APPLE_BANNER = "apple_banner_";

    @Autowired
    BannerConfigurationDao bannerConfigurationDao;

    @Autowired
    private AppSessionInfo appSessionInfo;

    @Autowired
    private BannerConfigService bannerConfigService;

    @ResponseBody
    @RequestMapping(value = {"/template"}, method = RequestMethod.GET)
    public  ResponseEntity<List<Banner>> getBanner() {

        try {
            StringBuilder nameBld = new StringBuilder(APPLE_BANNER);
            final User user = appSessionInfo.currentUser();
            nameBld.append(HtmlUtils.htmlEscape(user.getLocale().toString()));
            String name = new String(nameBld.toString());

            Gson gson = new Gson();
            BannerConfiguration bannerConfig = loadBanner(name);
            if (bannerConfig == null)
            {
                logger.info("No Banner found for current user var/program/locale : {} {} {}", user.getVarId(), user.getProgramId(), user.getLocale());
                return new ResponseEntity("No Banner found for the given var/program/locale/name : " + user.getVarId() + "/" + user.getProgramId() + "/" + user.getLocale().toString() , HttpStatus.NO_CONTENT);
            }

            List<Banner> templateBanners = gson.fromJson(bannerConfig.getValue(), List.class);

            logger.info("Banner {} successfully loaded", bannerConfig.getName());
            return new ResponseEntity(templateBanners, HttpStatus.OK);
        }
        catch (final Exception e) {
            logger.error("Unexpected error occurred while fetching Banner", e);
            return new ResponseEntity("Unexpected error occurred while fetching Banner ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, List<BannerConfigResponse>>> getBanners() {
        Map<String, List<BannerConfigResponse>> response = new HashMap<>();
        try {
            final User user = appSessionInfo.currentUser();
            response = bannerConfigService.getBanners(user);

            if (MapUtils.isEmpty(response)) {
                logger.info("No Banner found for current user var/program/locale : {} {} {}", user.getVarId(),
                    user.getProgramId(), user.getLocale());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }
            logger.info("Banner loaded successfully. {}", response);
            return ResponseEntity.ok(response);
        } catch (final Exception e) {
            logger.error("Unexpected error occurred while fetching Banner Configurations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     *   Load banner
     */
    private BannerConfiguration loadBanner( String name) throws ServiceException{
        try {
            BannerConfiguration banner = null;
            List<BannerConfiguration> bannerConfigurations = bannerConfigurationDao.getByName(name);
            if (bannerConfigurations != null) {

                // Check for the given var/default program
                if (banner == null) {
                    banner = bannerConfigurations.stream()
                            .filter(p -> p.getProgramId().equals(CommonConstants.DEFAULT_VAR_PROGRAM) )
                            .findFirst()
                            .orElse(null);
                }
                // Check for the default var/default program
                if (banner == null) {
                    banner = bannerConfigurations.stream()
                            .filter(p -> p.getVarId().equals(CommonConstants.DEFAULT_VAR_PROGRAM))
                            .findFirst()
                            .orElse(null);
                }
            }
            return banner ;
        }
        catch (Exception ex) {
            logger.error("Failed to load banner: {} " , ex.getMessage());
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }
}