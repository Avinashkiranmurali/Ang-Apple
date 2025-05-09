package com.b2s.common.services.util;

import com.b2s.apple.entity.MerchantEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skither on 12/10/2018.
 */
@Component
public class MerchantRepositoryHolder {

    private static final Map<String, List<MerchantEntity>> MERCHANT_MAP = new HashMap<>();

    public Map<String, List<MerchantEntity>> getMerchantRepository() {
        return MERCHANT_MAP;
    }

    public void addMerchantRepository(Map<String, List<MerchantEntity>> merchant) {
        MERCHANT_MAP.putAll(merchant);
    }


}
