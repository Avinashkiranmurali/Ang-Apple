package com.b2s.common.services.productservice;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.service.product.client.api.*;
import com.b2s.service.product.client.config.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * <p>
 * This class is used for holding client factory for communicating with product services and category services.
 *
 @author sjonnalagadda
  * Date: 8/26/13
  * Time: 9:00 AM
 *
 */
public class ProductServiceFactoryWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceFactoryWrapper.class);
    private final ProductServiceClientFactory productServiceClientFactory;

    /**

     * Initializes <code>ProductServiceClientFactory</code> for communicating with product services and category services.
     * @param url product and category services http endpoint URL
     * @param timeOut max wait time on socket while receiving response.
     * @param poolSize Thread executor pool size for enabling parallel execution of product search and detail service calls.
     * @throws ServiceException when URL is not valid (or) timeout is not greater than zero (or) pool size is not greater than zero
     *        (or) unable to initialize <code>ProductServiceClientFactory</code>
     */

    public ProductServiceFactoryWrapper(String url, final Integer timeOut, final Integer poolSize) throws ServiceException{
        this(url, timeOut, poolSize, false);
    }

        /**
         * Initializes <code>ProductServiceClientFactory</code> for communicating with product services and category services.
         * @param url product and category services http endpoint URL
         * @param timeOut max wait time on socket while receiving response.
         * @param poolSize Thread executor pool size for enabling parallel execution of product search and detail service calls.
         * @throws ServiceException when URL is not valid (or) timeout is not greater than zero (or) pool size is not greater than zero
         *        (or) unable to initialize <code>ProductServiceClientFactory</code>
         */

    public ProductServiceFactoryWrapper(String url, final Integer timeOut, final Integer poolSize, final boolean mock) throws ServiceException{

        validateUrl(url);
        validateTimeOut(timeOut);
        validatePoolSize(poolSize);
        try{
            final ClientConfig.Builder builder =  ClientConfig.builder();
            builder.withConnectTimeout(timeOut);
            builder.withTimeout(timeOut);
            builder.withUrl(url);
            builder.withPoolSize(poolSize);
            this.productServiceClientFactory =
                    new ProductServiceClientFactory(builder.build());
        }catch(Exception e){
            logger.error(ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorMessage(),e);
            throw  new ServiceException(ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION, e);
        }
    }

    /**
     * Validates URL
     * @param url the URL to be validated
     * @throws ServiceException when URL is null (or) zero character string.
     */

    private void validateUrl(final String url) throws ServiceException {
        if(Optional.ofNullable(url).isEmpty() || url.isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_SERVICE_URL_VALUE_ABSENT);
        }
    }

    /**
     * Validates timeout
     * @param timeOut The product service timeout is checked for positive or not
     * @throws ServiceException when timeout is null (or) not greater than zero.
     */

    private void validateTimeOut(final Integer timeOut) throws ServiceException{
        if(Optional.ofNullable(timeOut).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_SERVICE_TIMEOUT_VALUE_ABSENT);
        }
        if(timeOut <= 0){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_SERVICE_TIMEOUT_VALUE_IS_NOT_POSITIVE);
        }
    }

    /**
     * Validates pool size.
     * @param poolSize The pool size is checked for null
     * @throws ServiceException when pool size is null (or) not greater than zero.
     */

    private void validatePoolSize(final Integer poolSize) throws ServiceException{
        if(Optional.ofNullable(poolSize).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_SERVICE_CONN_POOL_VALUE_ABSENT);
        }
        if(poolSize <= 0){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_SERVICE_CONN_POOL_VALUE_IS_NOT_POSITIVE);
        }
    }

    /**
     * Returns <code>ProductServiceClient</code> from <code>ProductServiceClientFactory</code>
     * @return  ProductServiceClient
     */

    public ProductServiceClient getProductServiceClient(){
        return this.productServiceClientFactory.productServiceClient();
    }

    /**
     * Returns <code>CategoryServiceClient</code> from <code>ProductServiceClientFactory</code>
     * @return  CategoryServiceClient
     */

    public CategoryServiceClient getCategoryServiceClient(){
        return this.productServiceClientFactory.categoryServiceClient();
    }

    /**
     * Returns <code>StoreServiceClient</code> from <code>ProductServiceClientFactory</code>
     * @return  StoreServiceClient
     */

    public StoreServiceClient getStoreServiceClient(){
        return this.productServiceClientFactory.storeServiceClient();
    }
    /**
     * Returns <code>StoreServiceClient</code> from <code>ProductServiceClientFactory</code>
     * @return  StoreServiceClient
     */

    public ManagementServiceClient getManagementServiceClient(){
        return this.productServiceClientFactory.managementServiceClient();
    }
}
