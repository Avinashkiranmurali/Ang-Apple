package com.b2s.apple.services;

import com.b2s.apple.entity.ServicePlanInfoEntity;
import com.b2s.apple.mapper.ServicePlanInfoMapper;
import com.b2s.rewards.apple.dao.ServicePlanInfoDao;
import com.b2s.rewards.apple.model.ServicePlanData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Created by srajendran on 11/1/2022.
 */

@Service
public class ServicePlanInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePlanInfoService.class);

    @Autowired
    private ServicePlanInfoDao servicePlanInfoDao;

    @Autowired
    private ServicePlanInfoMapper servicePlanInfoMapper;


    /**
     * Get the Service Plan Info details based on given order id and line num
     *
     * @return
     */
    public ServicePlanData getServicePlanData(final long orderId, final int lineNum) {
        LOGGER.info("Retrieval of Service Plan data has been started... ");

        final ServicePlanInfoEntity servicePlanInfoEntity = servicePlanInfoDao.findByOrderIdAndLineNum(orderId, lineNum);

        if(Objects.nonNull(servicePlanInfoEntity)) {
            final ServicePlanData  servicePlanData = servicePlanInfoMapper.getServicePlanData(servicePlanInfoEntity);
            LOGGER.info("Retrieval of Service Plan data has been ended... ");
            return servicePlanData;
        } else {
            LOGGER.error("Service Plan data is empty for the orderId : {}, lineNum : {} ", orderId, lineNum);
            return null;
        }
    }
}
