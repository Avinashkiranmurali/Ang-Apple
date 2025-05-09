package com.b2s.rewards.apple.controller

import com.b2s.rewards.apple.integration.model.aep.CreateMerchantRequest
import com.b2s.rewards.apple.dao.MerchantListDao
import com.b2s.apple.entity.MerchantEntity
import com.google.gson.Gson
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ServiceControllerSpec extends Specification {

    def merchantDao = Mock(MerchantListDao)

    @Subject
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ServiceController(merchantDao: merchantDao)).build()

    @Unroll
    def 'test createNewMerchant'(){
        given:
        def merchantModel = new MerchantEntity()
        def createMerchantRequest = new CreateMerchantRequest(merchantId: merchantId, merchantName: name,merchantSimpleName: name,supplierId: supplierId)

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post("/merchants/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new Gson().toJson(createMerchantRequest)))
                .andReturn()
        then:
        merchantDao.getMerchant(201,9797) >> merchantModel
        result.response.status == status
        where:
        merchantId|name|supplierId|status
        97|'sandbox'|200|200
        9797|'sandbox'|201|400
    }
}
