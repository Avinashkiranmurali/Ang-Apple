package com.b2s.common.services.productservice;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.ClientRequest;

import java.util.Optional;

/**
 * <p>
 * This class is used to holds common functions related to product services and category services.
 *
 @author sjonnalagadda
  * Date: 08/26/13
  * Time: 09:45 AM
 *
 */
public abstract class AbstractProductService {

    protected final ProductServiceFactoryWrapper productServiceFactoryWrapper;

    protected AbstractProductService(final ProductServiceFactoryWrapper productServiceFactoryWrapper) throws ServiceException{
        if(Optional.ofNullable(productServiceFactoryWrapper).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.PRODUCT_FACTORY_WRAPPER_ABSENT);
        }
        this.productServiceFactoryWrapper = productServiceFactoryWrapper;
    }



    protected void validateSearchInputs(final ClientRequest clientRequest) throws ServiceException{
        if(Optional.ofNullable(clientRequest).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.CLIENT_REQUEST_IS_ABSENT) ;
        }
    }
}
