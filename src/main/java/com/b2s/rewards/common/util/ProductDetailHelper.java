package com.b2s.rewards.common.util;

import com.b2s.rewards.model.Product;
import org.apache.commons.lang.StringUtils;

import static com.b2s.rewards.common.util.CommonConstants.SUPPLIER_TYPE_GIFTCARD;
import static com.b2s.rewards.common.util.CommonConstants.SUPPLIER_TYPE_MERC;

/**
 * Helper for product details manipulation on the view layer.
 *
 * @author dmontoya
 * @version 1.0, 3/25/13 12:53 PM
 * @since b2r-rewardstep 6.0
 */
public final class ProductDetailHelper {

    public static String getDetailUrl(final Product product) {
        final int supplierId = product.getSupplier().getSupplierId();
        if (supplierId == SUPPLIER_TYPE_MERC) {
            return "/merchandise/itemDetail.do?itemId=" + product.getProductId();
        }
        if (supplierId == SUPPLIER_TYPE_GIFTCARD) {
            return "/giftcard/itemDetail.do?itemId=" + product.getProductId();
        }
        if(supplierId == CommonConstants.SUPPLIER_LOCAL_GENERIC_STORE || supplierId == CommonConstants.SUPPLIER_CUSTOM) {
            final String parentId = StringUtils.isNotBlank(product.getParentProductId())?product.getParentProductId():product.getProductId();
            return "/genericstore/genericStore.do?method=showProductDetail&GS_SELECT_PRODUCT_ID=" + parentId + "&GS_SELECT_ITEM_ID=" + product.getProductId() + "&storeid=" + product.getStoreId();
        }
        return null;
    }
}
