package com.b2s.common.services

import com.b2s.apple.entity.AMPProductConfigEntity
import com.b2s.apple.mapper.AMPMapper
import com.b2s.apple.services.AMPProductConfigService
import com.b2s.rewards.apple.dao.AMPProductConfigurationDao
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

/**
 * Created by ssundaramoorthy on 7/12/2021.
 * Unit test specifications for ampProductConfigService.
 */
class AMPProductServiceSpec extends Specification {

    final ampProductConfigurationDao = Mock(AMPProductConfigurationDao)
    final ampMapper = new AMPMapper()
    @Subject
    final ampProductConfigService = new AMPProductConfigService(ampProductConfigurationDao: ampProductConfigurationDao, ampMapper: ampMapper)

    final mockMvc = MockMvcBuilders.standaloneSetup(ampProductConfigService).build()

    def 'get AMP Config'() {
        given:
        Program program = new Program()
        program.setVarId(var_id)
        program.setProgramId(program_Id)
        program.setConfig(getVPC(ampFlag, disableProduct))

        ampProductConfigurationDao.getActiveAMPConfig("-1", "-1") >> buildMockConfigEntityDefault()
        ampProductConfigurationDao.getActiveAMPConfig(var_id, "-1") >> varConfig
        ampProductConfigurationDao.getActiveAMPConfig(var_id, program_Id) >> varProgramConfig

        when:
        final Set<AMPConfig> result = ampProductConfigService.getAmpConfigurationByProgram(program)
        final itemId = getItemId(result, cateogry)
        def resultSize = 0
        if (result != null) {
            resultSize = result.size()
        }

        then:
        resultSize == count
        if(item_Id!=null){
        itemId.contains(item_Id)}

        where:
        var_id | program_Id    | ampFlag | varConfig                              | count | cateogry | item_Id         | varProgramConfig                         | disableProduct
       "FDR"  | "b2s_qa_only" | false   | buildEmptyEntity()                     | 3     | "music"  | "amp-music"     | buildEmptyEntity()                       | false
        "UA"   | "MP"          | true    | buildEmptyEntity()                     | 0     | "music"  | null            | buildEmptyEntity()                       | false
        "RBC"  | "acquisition" | false   | buildEmptyEntity()                     | 4     | "music"  | "amp-tv-plus"   | buildVarProgEntity("RBC", "acquisition") | false
        "PNC"  | "b2s_qa_only" | false   | buildMockConfigEntityVarDefault("FDR") | 4     | "mac"    | "amp-news-plus" | buildEmptyEntity()                 | false
        "FDR"  | "b2s_qa_only" | false   | buildEmptyEntity()                     | 2     | "ipad"   | null            | buildEmptyEntity()                       | true
   }

    private static List<AMPProductConfigEntity> buildMockConfigEntityVarDefault(String varId) {
        AMPProductConfigEntity ampProductConfigEntity = new AMPProductConfigEntity()
        ampProductConfigEntity.setProgramId("-1")
        ampProductConfigEntity.setVarId(varId)
        ampProductConfigEntity.setIsActive(true)
        ampProductConfigEntity.setCategory("mac")
        ampProductConfigEntity.setItemId("amp-news-plus")
        List<AMPProductConfigEntity> configEntityList = new ArrayList<AMPProductConfigEntity>()
        configEntityList.add(ampProductConfigEntity)
        return configEntityList
    }

    private static List<AMPProductConfigEntity> buildMockConfigEntityDefault() {
        AMPProductConfigEntity ampProductConfigEntity = new AMPProductConfigEntity()
        ampProductConfigEntity.setProgramId("-1")
        ampProductConfigEntity.setVarId("-1")
        ampProductConfigEntity.setIsActive(true)
        ampProductConfigEntity.setCategory("music")
        ampProductConfigEntity.setItemId("amp-music")

        AMPProductConfigEntity ampProductConfigEntity2 = new AMPProductConfigEntity()
        ampProductConfigEntity2.setProgramId("-1")
        ampProductConfigEntity2.setVarId("-1")
        ampProductConfigEntity2.setIsActive(true)
        ampProductConfigEntity2.setCategory("apple-tv-apple-tv-4k")
        ampProductConfigEntity2.setItemId("amp-tv-plus")

        AMPProductConfigEntity ampProductConfigEntity3 = new AMPProductConfigEntity()
        ampProductConfigEntity3.setProgramId("-1")
        ampProductConfigEntity3.setVarId("-1")
        ampProductConfigEntity3.setIsActive(true)
        ampProductConfigEntity3.setCategory("ipad")
        ampProductConfigEntity3.setItemId("amp-news-plus")

        List<AMPProductConfigEntity> configEntityList = new ArrayList<AMPProductConfigEntity>()
        configEntityList.add(ampProductConfigEntity)
        configEntityList.add(ampProductConfigEntity2)
        configEntityList.add(ampProductConfigEntity3)
        return configEntityList
    }

    private static List<AMPProductConfigEntity> buildVarProgEntity(String varId, String programId) {
        AMPProductConfigEntity ampProductConfigEntity = new AMPProductConfigEntity()
        ampProductConfigEntity.setProgramId(varId)
        ampProductConfigEntity.setVarId(programId)
        ampProductConfigEntity.setIsActive(true)
        ampProductConfigEntity.setCategory("music")
        ampProductConfigEntity.setItemId("amp-tv-plus")

        List<AMPProductConfigEntity> configEntityList = new ArrayList<AMPProductConfigEntity>()
        configEntityList.add(ampProductConfigEntity)

        return configEntityList
    }

    private static List<String> getItemId(Set<AMPConfig> result, String cat) {
        List<String> itemIdList = new ArrayList<>()
        for (AMPConfig ampConfig : result) {
            if (ampConfig.getCategory() == cat) {
                itemIdList.add(ampConfig.getItemId())
            }
        }
        return itemIdList
    }

    private static List<AMPProductConfigEntity> buildEmptyEntity() {
        return new ArrayList<AMPProductConfigEntity>()
    }

    private static Map<String, Object> getVPC(boolean ampFlag, boolean productDisable) {
        Map<String, Object> config = new HashMap<>()
        config.put(CommonConstants.DISABLE_AMP, ampFlag)
        config.put("disable-amp-news-plus", productDisable)
        return config
    }
}