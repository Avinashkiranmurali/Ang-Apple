package com.b2s.common.services

import com.b2s.apple.entity.ServicePlanInfoEntity
import com.b2s.apple.mapper.ServicePlanInfoMapper
import com.b2s.apple.services.ServicePlanInfoService
import com.b2s.rewards.apple.dao.ServicePlanInfoDao
import com.b2s.rewards.apple.model.ServicePlanData
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

/**
 * Created by srajendran on 12/1/2022.
 * Unit test specifications for Service Plan Info's.
 */
class ServicePlanInfoServiceSpec extends Specification {

    final servicePlanInfoDao = Mock(ServicePlanInfoDao)

    final servicePlanInfoMapper = new ServicePlanInfoMapper()

    @Subject
    final servicePlanInfoService = new ServicePlanInfoService(servicePlanInfoDao: servicePlanInfoDao,
            servicePlanInfoMapper: servicePlanInfoMapper)

    final mockMvc = MockMvcBuilders.standaloneSetup(servicePlanInfoService).build()

    def 'get getServicePlanData() - with valid data'() {
        given:
        servicePlanInfoDao.findByOrderIdAndLineNum(_, _) >> getServicePlanInfoEntity()
        servicePlanInfoMapper.getServicePlanData(_) >> getServicePlanData()

        when:
        ServicePlanData servicePlanData = servicePlanInfoService.getServicePlanData(27056345, 3)

        then:
        servicePlanData != null
        servicePlanData.getPlanId() == "ABC123"
        servicePlanData.getPlanUrl() == "https://google.com"
        servicePlanData.getHardwareSerialNumber() == "7863699"
        servicePlanData.getHardwareDescription() == "desc"
    }

    def 'get getServicePlanData() - with null data'() {
        given:
        servicePlanInfoDao.findByOrderIdAndLineNum(_, _) >> null
        servicePlanInfoMapper.getServicePlanData(_) >> getServicePlanData()

        when:
        ServicePlanData servicePlanData = servicePlanInfoService.getServicePlanData(27056345, 3)

        then:
        servicePlanData == null
    }

    def getServicePlanData() {
        return ServicePlanData
                .builder()
                .withPlanId("ABC123")
                .withPlanUrl("https://google.com")
                .withHardwareSerialNumber("7863699")
                .withHardwareDescription("desc")
                .withPlanEndDate(new Date())
                .withLastUpdateDate(new Date())
                .build()
    }

    def getServicePlanInfoEntity() {
        ServicePlanInfoEntity entity = new ServicePlanInfoEntity()
        entity.setId(1)
        entity.setOrderId(27056345)
        entity.setLineNum(1)
        entity.setLastUpdateDate(new Date())
        entity.setPlanId("ABC123")
        entity.setPlanEndDate(new Date())
        entity.setPlanUrl("https://google.com")
        entity.setHardwareSerialNumber("7863699")
        entity.setHardwareDescription("desc")
        return entity
    }

}