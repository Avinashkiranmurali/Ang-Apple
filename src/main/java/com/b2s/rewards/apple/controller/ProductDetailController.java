package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.GiftPromoService;
import com.b2s.apple.services.RecentlyViewedProductsService;
import com.b2s.common.services.discountservice.CouponCodeValidator;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.PricingModelConfiguration;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * All Search, Browse, Detail of products are in this controller
 *
 * @author Ssrinivasan
 */
@RestController
@RequestMapping(value = "/", produces = "application/json;charset=UTF-8")
@ResponseBody
public class ProductDetailController {

    @Autowired
    private ProductServiceV3 productServiceV3;

    @Autowired
    private PricingModelConfigurationDao pricingModelConfigurationDao;

    @Autowired
    private CouponCodeValidator couponCodeValidator;

    @Autowired
    GiftPromoService giftPromoService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    @Autowired
    private RecentlyViewedProductsService recentlyViewedProductsService;

    private static final Logger logger = LoggerFactory.getLogger(ProductDetailController.class);

    /**
     * Gets the ProductDetail for the given 'psid', as part of the request. This service will return details of a
     * product with variations, if any. Otherwise, just the core product information will be sent. The product price
     * sent back in this Product is the one calculated through PricingService
     *
     * @return Product Detail with variations, if any, along with the calculated price computed from PricingService
     */
    @ResponseBody
    @RequestMapping(value = {"/detail/**", "/products/**"}, method = RequestMethod.GET)
    public ResponseEntity<Object> detail(@RequestParam(required = false) final Long pgId,
        @RequestParam(required = false) final boolean withVariations,
        @RequestParam(required = false) final boolean withEngraveConfig,
        @RequestParam(required = false) final boolean withRelatedProduct,
        HttpServletRequest request) {
        logger.debug("detail with path: {} - ENTRY", request.getPathInfo());
        final Product product;
        final User user = appSessionInfo.currentUser();
        try {
            final String path = request.getPathInfo();
            String psid = AppleUtil.getPatternValue("/detail/(.*)", path);
            if (StringUtils.isBlank(psid)) {
                psid = AppleUtil.getPatternValue("/products/(.*)", path);
            }
            final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            Cart cart = (Cart) request.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);

            //[S-20239] Persisting recently viewed products into DB if product call is not an engrave config
            if (!withEngraveConfig) {
                recentlyViewedProductsService.updateProducts(user, program, psid);
            }

            // To unapply invalid discount codes
            user.setDiscounts(couponCodeValidator.removeInvalidDiscount(user, user.getDiscounts()));

            if (!StringUtils.isBlank(psid)) {
                product = productServiceV3.getDetailPageProduct(psid, program, user, withEngraveConfig, withRelatedProduct);
                if (Objects.isNull(product)) {
                    logger.error("Product Detail Not found for the given psid {}", psid);
                    return new ResponseEntity<>("Product Detail Not found for the given psid " + psid, HttpStatus
                            .NO_CONTENT);
                }
            } else {
                logger.error("Missing/Invalid PSID {}", psid);
                return new ResponseEntity<>("Invalid/Missing PSID sent from request", HttpStatus.BAD_REQUEST);
            }
        } catch (final Exception se) {
            logger.error("Exception in fetching Apple Product Detail ", se);
            return new ResponseEntity<>("Product/Pricing Service Exception", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Printing response in detail method: {}", new Gson().toJson(product));
            logger.debug("detail with path: {} - EXIT", request.getPathInfo());
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = {"/priceModel"}, method = RequestMethod.GET)
    public ResponseEntity<List<PricingModelConfiguration>> priceModel(final HttpServletRequest request) {
        logger.info("Pricing Model Configuration with path: {}", request.getPathInfo());
        final User user = appSessionInfo.currentUser();

        List<PricingModelConfiguration> pricingModelConfigurations = pricingModelConfigurationDao
            .getByVarIdProgramIdPriceType(user.getVarId(), user.getProgramId(), "Subsidy");

        if (pricingModelConfigurations == null) {
            pricingModelConfigurations = new ArrayList<>();
            logger.info("No PricingModelConfigurations found for {} Market", user.getLocale().getCountry());
            return new ResponseEntity<>(pricingModelConfigurations, HttpStatus.NO_CONTENT);
        }

        logger.info("Successfully returning Pricing Model Configurations for {} market",
            user.getLocale().getCountry());
        return new ResponseEntity<>(pricingModelConfigurations, HttpStatus.OK);

    }

    @ResponseBody
    @RequestMapping(value = {"/priceModelsByVarProgram"}, method = RequestMethod.GET)
    public ResponseEntity<Object> priceModelsByVarProgram(final HttpServletRequest httpServletRequest) {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
        logger.info("Pricing Model Configuration with path: {}", request.getPathInfo());
        final String varId = request.getParameter("var");
        final String programId = request.getParameter("program");
        if (StringUtils.isNotBlank(varId) && StringUtils.isNotBlank(programId)) {
            List<PricingModelConfiguration> pricingModelConfigurations =
                pricingModelConfigurationDao.getByVarIdProgramIdPriceType(varId,
                    programId, "Subsidy");
            if (pricingModelConfigurations == null) {
                pricingModelConfigurations = new ArrayList<>();
                logger.info("No PricingModelConfigurations found for VarId: {}, ProgramId: {} ", varId, programId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(pricingModelConfigurations);
            }
            logger.info("Successfully returning Pricing Model Configurations for VarId: {}, ProgramId: {}", varId,programId);
            return ResponseEntity.ok(pricingModelConfigurations);
        } else {
            logger.error("Missing/Invalid VarId: {}, ProgramId: {}", varId, programId);
            return ResponseEntity.badRequest().body("Invalid/Missing VarId, ProgramId sent from request");
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/activationFee"}, method = RequestMethod.GET)
    public ResponseEntity<Object> activationFee() {
        logger.info("Getting ActivationFee from PricingModelConfiguration");
        final User user = appSessionInfo.currentUser();
        final Map<String, Map<String, String>> activationFee =
                productServiceV3.getAllActivationFees(user.getVarId(), user.getProgramId());

        final String localeCountry = user.getLocale().getCountry();
        if (activationFee.isEmpty()) {
            logger.info("No ActivationFee available in PricingModelConfiguration for {} market", localeCountry);
            return new ResponseEntity<>(activationFee, HttpStatus.NO_CONTENT);
        }
        logger.info("Successfully returning ActivationFee from PricingModelConfiguration for {} market", localeCountry);
        return new ResponseEntity<>(activationFee, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping(value = {"/giftItem"})
    public ResponseEntity<Object> getGiftProducts(final HttpServletRequest request,
        @RequestParam final String qualifyingPsid) {

        final User user = appSessionInfo.currentUser();
        final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        List<Product> productList = productServiceV3.getGiftItem(user, program, qualifyingPsid, true);

        if (CollectionUtils.isEmpty(productList)) {
            logger.error("Gift Items not found for the given Psid {}", qualifyingPsid);
            return new ResponseEntity<>("Gift Items not found for the given Psid " + qualifyingPsid,
                HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(productList, HttpStatus.OK);
    }
}