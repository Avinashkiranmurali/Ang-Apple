package com.b2s.apple.mapper;

import com.b2s.apple.entity.ServicePlanInfoEntity;
import com.b2s.rewards.apple.model.ServicePlanData;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by srajendran on 11/1/2022.
 */

@Component
public class ServicePlanInfoMapper {

    public ServicePlanData getServicePlanData(final ServicePlanInfoEntity servicePlanInfoEntity) {
        return ServicePlanData.builder()
            .withPlanId(servicePlanInfoEntity.getPlanId())
            .withPlanUrl(servicePlanInfoEntity.getPlanUrl())
            .withHardwareSerialNumber(servicePlanInfoEntity.getHardwareSerialNumber())
            .withHardwareDescription(servicePlanInfoEntity.getHardwareDescription())
            .withPlanEndDate(servicePlanInfoEntity.getPlanEndDate())
            .withLastUpdateDate(servicePlanInfoEntity.getLastUpdateDate())
            .build();
    }
}
