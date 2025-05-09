package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.CategoryConfigurationDao;
import com.b2s.rewards.apple.dao.ProductAttributeConfigurationDao;
import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.ProductAttributeConfiguration;
import com.b2s.rewards.apple.model.Program;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.requests.productservice.CoreProductDetailRequest;
import com.b2s.common.services.requests.productservice.MultiProductDetailRequest;
import com.b2s.common.services.responses.productservice.CoreProductDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.b2s.rewards.common.util.CommonConstants.APEX_HEADER_PARTNERCODE;

/**
 * Created by ssrinivasan on 3/30/2015.
 */
@Service
public class DetailService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    @Qualifier("productServiceV3Service")
    private ProductServiceV3 productServiceV3;

    @Autowired
    private ProductAttributeConfigurationDao productAttributeConfigurationDao;

    @Autowired
    private CategoryConfigurationDao categoryConfigurationDao;

    @Autowired
    private Properties applicationProperties;
    /**
     * This returns core Product information, used for ShoppingCart
     * Planning to remove its dependency
     *
     * @param psid       Product PSID
     * @param userLocale Locale of the current user session
     * @param program    VarProram
     *
     * @return  Returns core Product
     */
    @Deprecated
    public com.b2s.rewards.model.Product getCoreProductDetail(final String psid, final Locale userLocale,
        final Program program, final boolean checkDRP, final boolean withVariations){
        final CoreProductDetailRequest coreProductDetailRequest = CoreProductDetailRequest.builder()
                .withPsid(psid)
                .withNeedVariationsInfo(withVariations)
                .withNeedRealTimeInfo(false)
                .withTargetCurrencies(productServiceV3.getTargetCurrencies(program))
                .build();
        final MultiProductDetailRequest multiProductDetailRequest = new MultiProductDetailRequest(Optional.ofNullable(userLocale),
                program, applicationProperties.getProperty(APEX_HEADER_PARTNERCODE));
        multiProductDetailRequest.withProductDetailRequest(coreProductDetailRequest);
        try{
            final CoreProductDetailResponse coreProductDetail = productServiceV3.productDetail(multiProductDetailRequest, "merchandise", program, checkDRP);

            if(Optional.ofNullable(coreProductDetail).isPresent() && !coreProductDetail.getFailures().isEmpty()){
                if (isServiceException(coreProductDetail.getFailures())) {
                    throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION);
                }
            } else {
                return coreProductDetail.getProductDetailByPsid(psid).isPresent() ? coreProductDetail.getProductDetailByPsid(psid).get(): null ;
            }
        }catch(ServiceException se){
            LOG.error("Error calling product search service", se);
        }
        return null;

    }
    private static boolean isServiceException(Map<String, Throwable> failures) throws ServiceException{
        for (String key : failures.keySet()) {
            // UnknownHostException is being recived as ExecutionException
            if (failures.get(key) instanceof ExecutionException) {
                return true;
            }
        }
        return false;
    }

    public List<ProductAttributeConfiguration> getAllProductAttributeConfiguration() throws DataAccessException {
        return productAttributeConfigurationDao.getAll(2000);
    }
    public List<CategoryConfiguration> getAllCategoryConfigurations() throws DataAccessException {
        return categoryConfigurationDao.getAll(2000);
    }
}
