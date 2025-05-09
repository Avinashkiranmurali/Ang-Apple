package com.b2s.common.services.productservice;

import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.ClientRequest;
import com.b2s.common.services.requests.productservice.CoreCategoryTaxonomyRequest;
import com.b2s.common.services.transformers.Transformer;
import com.b2s.common.services.transformers.TransformersHolder;
import com.b2s.service.product.client.api.CategoryServiceClient;
import com.b2s.service.product.client.application.categories.CategoryTaxonomyRequest;
import com.b2s.service.product.client.domain.CategoryNode;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * <p>
 * This class is used to by core platform to load the category information from product service layer.
 *
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 9:17 AM
 *
 */
public class CategoryServiceV3 extends AbstractProductService{


    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceV3.class);
    private final TransformersHolder<CoreCategoryTaxonomyRequest,CategoryTaxonomyRequest,CategoryNode,CategoryInfo> categoryTransformersHolder;

    public  CategoryServiceV3(final ProductServiceFactoryWrapper productServiceFactoryWrapper,
                              final TransformersHolder<CoreCategoryTaxonomyRequest,CategoryTaxonomyRequest,CategoryNode,CategoryInfo> categoryTransformersHolder)
                              throws ServiceException{
        super(productServiceFactoryWrapper);
        validateCategoryTransformersHolder(categoryTransformersHolder);
        this.categoryTransformersHolder = categoryTransformersHolder;
    }

    private <A,B,C,D> void validateCategoryTransformersHolder(final TransformersHolder<A,B,C,D> categoryTransformersHolderInput) throws ServiceException {
        if(Optional.ofNullable(categoryTransformersHolderInput).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.CATEGORY_TRANSFORMERS_HOLDER_ABSENT);
        }

        if(Optional.ofNullable(categoryTransformersHolderInput.getRequestTransformer()).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.CATEGORY_REQUEST_TRANSFORMER_ABSENT);
        }

        if(Optional.ofNullable(categoryTransformersHolderInput.getResponseTransformer()).isEmpty()){
            throw new ServiceException(ServiceExceptionEnums.CATEGORY_RESPONSE_TRANSFORMER_ABSENT);
        }
    }

    public Collection<CategoryNode> queryCategoryTaxonomyByLocale(final ClientRequest request) throws ServiceException{
        Collection<CategoryNode> categoryNodes = null;
        try{
            validateSearchInputs(request);
            //Transform it to server request
            final CategoryTaxonomyRequest serverRequest = this.categoryTransformersHolder.getRequestTransformer().transform(
                    (CoreCategoryTaxonomyRequest) request, null, null);

            if(Optional.ofNullable(serverRequest).isPresent()){
                final CategoryServiceClient categoryServiceClient = this.productServiceFactoryWrapper.getCategoryServiceClient();
                //Send request to server
                categoryNodes  =  categoryServiceClient.taxonomy(serverRequest);
            }
        } catch(ServiceException se){
            logger.error("Exception while retrieving categories",se);
            throw se;
        } catch(EntityNotFoundException enfe) {
            logger.warn("No products in catalog {}", ((CoreCategoryTaxonomyRequest)request).getDefaultCatalogId());
        } catch (Exception e){
            logger.error("Exception while retrieving categories",e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION,e);
        }
        return categoryNodes;
    }

    public Transformer<CategoryNode,CategoryInfo> getResponseTransformer(){
       return categoryTransformersHolder.getResponseTransformer();
    }

}
