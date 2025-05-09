package com.b2s.common.services.transformers.productservice;


import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.transformers.Helper;
import com.b2s.common.services.transformers.Transformer;
import com.b2s.rewards.apple.model.Program;
import com.b2s.service.product.client.domain.CategoryNode;

import java.util.Optional;


/**
 * <p>
 * Used to transform category taxonomy responses and set to CategoryInfo.
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 5:29 PM
 *
 */
public class CategoryNodeToCategoryInfo implements Transformer<CategoryNode,CategoryInfo> {

    @Override
    public CategoryInfo transform(final CategoryNode from, final Helper helper, final Program program){
        if(Optional.ofNullable(from).isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.CATEGORY_INFO_ABSENT.getErrorMessage());
        }
        if(Optional.ofNullable(helper).isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.TRANSFORMER_HELPER_CAN_NOT_BE_NULL.getErrorMessage());
        }
        if(helper instanceof LocaleHelper){
            final LocaleHelper localeHelper = (LocaleHelper)helper;
            return new CategoryInfo(from.getSlug(), from.getName(),localeHelper.getUserLanguage().orElse(null), localeHelper.getHierarchyFromRootNode(),localeHelper.getRootCategorySlug(),from.getDepth());
        }else{
            throw new IllegalArgumentException(ServiceExceptionEnums.TRANSFORMER_HELPER_OBJECT_IS_DIFFERENT.getErrorMessage());
        }
    }

}
