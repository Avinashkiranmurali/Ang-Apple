package com.b2s.apple.services;

import com.b2s.apple.entity.CarouselEntity;
import com.b2s.apple.entity.CarouselTemplateEntity;
import com.b2s.apple.model.CarouselConfig;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.dao.CarouselDao;
import com.b2s.rewards.apple.dao.CarouselTemplateDao;
import com.b2s.rewards.apple.integration.model.CarouselResponse;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.VarProgramComparator;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.b2s.rewards.common.util.CommonConstants.BAG;
import static com.b2s.rewards.common.util.CommonConstants.COMMA;
import static com.b2s.rewards.common.util.CommonConstants.SHOW_ONLY_ON_EMPTY_CART;
import static com.b2s.rewards.common.util.CommonConstants.CarouselType;

@Service
public class CarouselService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselService.class);

    @Autowired
    private CarouselDao carouselDao;

    @Autowired
    private CarouselTemplateDao carouselTemplateDao;

    @Autowired
    private CarouselHolder carouselHolder;

    @Lazy
    @Autowired
    private CarouselService carouselService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    /**
     * Get Carousel Response based on Carousel Configuration in Program
     *
     * @param user
     * @param program
     * @param page
     * @return
     * @throws ServiceException
     */
    public Set<CarouselResponse> getCarouselResponse(final User user, final Program program, final String page)
        throws ServiceException {
        try {
            final Set<CarouselResponse> carouselResponses = new TreeSet<>();

            //Get Carousels based on Page from Program Carousel Configurations
            if (program.getCarouselConfig().containsKey(page) &&
                MapUtils.isNotEmpty(program.getCarouselConfig().get(page))) {

                for (final Map.Entry<CarouselType, CarouselConfig> carouselConfig :
                    program.getCarouselConfig().get(page).entrySet()) {
                    LOGGER.info("Retrieval of Carousel Response for page {} has started...", page);
                    buildCarouselResponse(user, program, carouselConfig.getValue(), page)
                        .ifPresent(carouselResponses::add);
                    LOGGER.info("Retrieval of Carousel Response for page {} has ended...", page);
                }
            }
            LOGGER.debug("Carousel Response for page {} --> {}", page, carouselResponses);
            return carouselResponses;
        } catch (final RuntimeException ex) {
            LOGGER.error("Failed to get Carousel Response for the user: {}", user.getUserId(), ex);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    /**
     * Builds Carousel Response based on Carousel configuration
     * Sets Products based on Carousel Type
     * Sets Carousel config based on Template Name
     *
     * @param user
     * @param program
     * @param carouselConfig
     * @return
     */
    private Optional<CarouselResponse> buildCarouselResponse(final User user, final Program program,
        final CarouselConfig carouselConfig, final String page) {
        try {
            List<Product> products = new ArrayList<>();
            if (Objects.nonNull(carouselConfig)) {
                final Map<String, Object> carouselTemplates = getCarouselTemplate(carouselConfig.getTemplateName());

                if (StringUtils.isNotBlank(page) &&
                    (!BAG.equalsIgnoreCase(page) || isShowOnlyOnEmptyCart(carouselTemplates))) {

                    products = getCarouselProducts(user, program, carouselConfig.getType(),
                        carouselConfig.getMaxProductCount());
                }

                return Optional.ofNullable(CarouselResponse.builder()
                    .withName(carouselConfig.getType().getValue())
                    .withConfig(carouselTemplates)
                    .withProducts(products)
                    .build());
            }
        } catch (final RuntimeException ex) {
            LOGGER.error("Failed to build Carousel Response for the user: {} with Carousel Config: {}.",
                user.getUserId(), carouselConfig, ex);
        }
        return Optional.empty();
    }

    /**
     * Show Carousel only on Empty Cart page
     * returns true, if showOnlyOnEmptyCart not configured
     * returns true, if showOnlyOnEmptyCart is configured and Cart is Empty, otherwise false
     *
     * @param carouselTemplates - Configured Carousel Templates
     * @return true/false
     */
    private boolean isShowOnlyOnEmptyCart(final Map<String, Object> carouselTemplates) {
        if (MapUtils.isNotEmpty(carouselTemplates) && Objects.nonNull(carouselTemplates.get(SHOW_ONLY_ON_EMPTY_CART)) &&
            (Boolean) carouselTemplates.get(SHOW_ONLY_ON_EMPTY_CART)) {

            final Cart cart = appSessionInfo.getSessionCart();
            if (Objects.nonNull(cart)) {
                return CollectionUtils.isEmpty(cart.getCartItems());
            }
        }
        return true;
    }

    /**
     * Get Products based on Carousel Type
     * if carouselType = recentlyViewed, get products based on Recently viewed products PSIDs
     *
     * @param user
     * @param program
     * @param carouselType
     * @param configMaxProductCount
     * @return
     */
    private List<Product> getCarouselProducts(final User user, final Program program, final CarouselType carouselType,
        final Integer configMaxProductCount) {
        try {
            return carouselHolder.getCarouselService(carouselType)
                .getCarouselProducts(user, program, configMaxProductCount);
        } catch (final RuntimeException e) {
            LOGGER.error("Exception {} when loading Carousel Manager for carouselType={} ",
                (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), carouselType, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get Carousel Template based on Template Name
     *
     * @param templateName
     * @return
     */
    private Map<String, Object> getCarouselTemplate(final String templateName) {
        final List<CarouselTemplateEntity> carouselTemplateEntities =
            carouselService.getCarouselTemplates(templateName);
        if (CollectionUtils.isNotEmpty(carouselTemplateEntities)) {
            return carouselTemplateEntities
                .parallelStream()
                .collect(Collectors.toMap(CarouselTemplateEntity::getName,
                    entity -> Objects.nonNull(BooleanUtils.toBooleanObject(entity.getValue())) ?
                        Boolean.valueOf(entity.getValue()) : entity.getValue()));
        }
        return new HashMap<>();
    }

    /**
     * Set Carousel configurations such as carouselPages and carouselConfig in Program object
     *
     * @param program
     */
    public void setProgramCarouselConfig(final Program program) {
        final Map<String, Map<CarouselType, CarouselConfig>> carouselConfigs =
            getActiveCarouselConfigs(program.getVarId(), program.getProgramId());
        if (MapUtils.isNotEmpty(carouselConfigs)) {
            // Setting this carousel configuration details to keep track of needed Carousel configurations
            // such as Template Name, Type based on the configurations
            LOGGER.info("Program Carousel Configurations: {}", carouselConfigs);
            program.setCarouselConfig(carouselConfigs);
            program.setCarouselPages(new ArrayList<>(carouselConfigs.keySet()));
        } else {
            program.setCarouselConfig(new HashMap<>());
            program.setCarouselPages(new ArrayList<>());
        }
    }

    /**
     * Get Carousel Entities based on Page and Type
     *
     * @param varId
     * @param programId
     * @return
     */
    private Map<String, Map<String, List<CarouselEntity>>> getCarouselEntitiesBasedOnPageAndType(final String varId,
        final String programId) {
        final List<CarouselEntity> carouselEntities = carouselDao.getActiveCarouselEntities(
            Arrays.asList(varId, CommonConstants.DEFAULT_VAR_PROGRAM),
            Arrays.asList(programId, CommonConstants.DEFAULT_VAR_PROGRAM));

        final Map<String, Map<String, List<CarouselEntity>>> carouselsBasedOnPageAndType = new HashMap<>();
        for (final CarouselEntity carouselEntity : carouselEntities) {
            if (!isProgramInExcludePrograms(programId, carouselEntity.getProgramExclusion())) {
                for (final String page : carouselEntity.getDisplayPages().split(COMMA)) {
                    buildCarouselsBasedOnPageAndType(carouselsBasedOnPageAndType, carouselEntity, page);
                }
            }
        }
        return carouselsBasedOnPageAndType;
    }

    /**
     * Updates Carousel entities list based on Page and Type
     *
     * @param carouselsBasedOnPageAndType
     * @param carouselEntity
     * @param page
     */
    private void buildCarouselsBasedOnPageAndType(
        final Map<String, Map<String, List<CarouselEntity>>> carouselsBasedOnPageAndType,
        final CarouselEntity carouselEntity, final String page) {
        if (carouselsBasedOnPageAndType.containsKey(page)) {
            final Map<String, List<CarouselEntity>> carouselsBasedOnType = carouselsBasedOnPageAndType.get(page);
            if (carouselsBasedOnType.containsKey(carouselEntity.getType())) {
                final List<CarouselEntity> carouselEntityList = carouselsBasedOnType.get(carouselEntity.getType());
                carouselEntityList.add(carouselEntity);
            } else {
                final List<CarouselEntity> carouselEntityList = new ArrayList<>();
                carouselEntityList.add(carouselEntity);
                carouselsBasedOnType.put(carouselEntity.getType(), carouselEntityList);
            }
        } else {
            final Map<String, List<CarouselEntity>> carouselsBasedOnType = new HashMap<>();
            final List<CarouselEntity> carouselEntityList = new ArrayList<>();
            carouselEntityList.add(carouselEntity);
            carouselsBasedOnType.put(carouselEntity.getType(), carouselEntityList);
            carouselsBasedOnPageAndType.put(page, carouselsBasedOnType);
        }
    }


    /**
     * Get Active carousel configurations based on Var Program precedence
     *
     * @param varId
     * @param programId
     * @return
     */
    private Map<String, Map<CarouselType, CarouselConfig>> getActiveCarouselConfigs(final String varId,
        final String programId) {
        final Map<String, Map<CarouselType, CarouselConfig>> carouselConfigBasedOnPageAndType = new HashMap<>();

        for (final Map.Entry<String, Map<String, List<CarouselEntity>>> carouselsBasedOnPageAndType :
            getCarouselEntitiesBasedOnPageAndType(varId, programId).entrySet()) {
            final String pageName = carouselsBasedOnPageAndType.getKey();

            for (final List<CarouselEntity> carouselEntities : carouselsBasedOnPageAndType.getValue().values()) {
                final Optional<CarouselEntity> carouselEntity =
                    carouselEntities.stream()
                        .min(VarProgramComparator::compare);

                carouselEntity.ifPresent(
                    carousel -> {
                        final CarouselConfig carouselConfig = getCarouselConfig(pageName, carousel);
                        if (carouselConfigBasedOnPageAndType.containsKey(pageName)) {
                            carouselConfigBasedOnPageAndType.get(pageName)
                                .put(carouselConfig.getType(), carouselConfig);
                        } else {
                            final Map<CarouselType, CarouselConfig> carouselBasedOnType = new HashMap<>();
                            carouselBasedOnType.put(carouselConfig.getType(), carouselConfig);
                            carouselConfigBasedOnPageAndType.put(pageName, carouselBasedOnType);
                        }
                    }
                );
            }
        }
        return carouselConfigBasedOnPageAndType;
    }

    /**
     * Validates whether the program id is in program exclusion list
     * in order to Exclude the configuration
     *
     * @param programId
     * @param excludePrograms
     * @return
     */
    private boolean isProgramInExcludePrograms(final String programId, final String excludePrograms) {
        if (StringUtils.isNotBlank(excludePrograms)) {
            return List.of(excludePrograms.toLowerCase().split(COMMA)).contains(programId.toLowerCase());
        }
        return false;
    }

    /**
     * Set Carousel Configurations based on Carousel Entity persisted in DB
     *
     * @param page
     * @param carouselEntity
     * @return
     */
    private CarouselConfig getCarouselConfig(final String page, final CarouselEntity carouselEntity) {
        return CarouselConfig.builder()
            .withTemplateName(carouselEntity.getTemplateName())
            .withType(CarouselType.get(carouselEntity.getType()))
            .withPage(page)
            .withMaxProductCount(carouselEntity.getMaxProductCount())
            .withDisplayPages(List.of(carouselEntity.getDisplayPages().toLowerCase().split(COMMA)))
            .withProgramExclusion(StringUtils.isNotBlank(carouselEntity.getProgramExclusion()) ?
                List.of(carouselEntity.getProgramExclusion().toLowerCase().split(COMMA)) : new ArrayList<>())
            .build();
    }

    /**
     * Fetch Carousel Templates either from DB or from Cache
     *
     * @param templateName
     * @return
     */
    @Cacheable(value = CommonConstants.CACHE_CAROUSEL_TEMPLATES, key = "#templateName")
    public List<CarouselTemplateEntity> getCarouselTemplates(final String templateName) {
        return carouselTemplateDao.getCarouselTemplates(templateName);
    }
}
