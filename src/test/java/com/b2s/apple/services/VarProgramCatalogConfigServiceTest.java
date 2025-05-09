package com.b2s.apple.services;

import com.b2s.apple.entity.VarProgramCatalogConfigEntity;
import com.b2s.rewards.apple.dao.VarProgramCatalogConfigDao;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VarProgramCatalogConfigServiceTest {
    @InjectMocks
    private VarProgramCatalogConfigService varProgramCatalogConfigService;

    @Mock
    private VarProgramCatalogConfigDao varProgramCatalogConfigDao;

    final List<String> catalogIds = new ArrayList<>();
    final List<String> varIds = new ArrayList<>();
    final List<String> programIds = new ArrayList<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        catalogIds.add(DEFAULT_CATALOG_ID);
        varIds.add(DEFAULT_VAR_PROGRAM);
        programIds.add(DEFAULT_PROGRAM_KEY);
    }

    @Test
    public void testEmptyTable() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = "someName";

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, name)).
                thenReturn(new ArrayList<>());
        String value =  varProgramCatalogConfigService.getValue(catalogId, varId, programId, name);
        assertEquals(StringUtils.EMPTY, value);
    }

    @Test
    public void testNameNotFound() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = "someName";

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, name)).
            thenReturn(getCapitalCitiesList());
        String value =  varProgramCatalogConfigService.getValue(catalogId, varId, programId, name);
        assertEquals(StringUtils.EMPTY, value);
    }

    @Test
    public void testNameFound() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = "Germany";

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(any(List.class), any(List.class), any(List.class), any(String.class))).
            thenReturn(getCapitalCitiesList());
        String value =  varProgramCatalogConfigService.getValue(catalogId, varId, programId, name);
        assertNotNull(value);
    }

    @Test
    public void testExcludeCategoryEmptyMap() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = EXCLUDE_CATEGORY;

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, name)).
            thenReturn(new ArrayList<>());
        List<String> value =  varProgramCatalogConfigService.getListOfValue(catalogId, varId, programId, name);
        assertTrue(value.isEmpty());
    }

    @Test
    public void testExcludeCategoryEmptyValue() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = EXCLUDE_CATEGORY;

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        List<VarProgramCatalogConfigEntity> catalogConfigEntities = new ArrayList<>();
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName(name);
        varProgramCatalogConfigEntity.setValue("");

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, name)).
            thenReturn(catalogConfigEntities);
        List<String> value =  varProgramCatalogConfigService.getListOfValue(catalogId, varId, programId, name);
        assertTrue(value.isEmpty());
    }

    @Test
    public void testExcludeCategoryOneValue() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = EXCLUDE_CATEGORY;

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        List<VarProgramCatalogConfigEntity> catalogConfigEntities = new ArrayList<>();
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName(name);
        varProgramCatalogConfigEntity.setValue("Photography");
        catalogConfigEntities.add(varProgramCatalogConfigEntity);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(any(List.class), any(List.class),
            any(List.class), any(String.class))).
            thenReturn(catalogConfigEntities);
        List<String> value =  varProgramCatalogConfigService.getListOfValue(catalogId, varId, programId, name);
        assertEquals(1, value.size());
    }

    @Test
    public void testExcludeCategoryMultipleValues() {
        String catalogId = "apple-us-en";
        String varId = "RBC";
        String programId = "PBA";
        String name = EXCLUDE_CATEGORY;

        catalogIds.add(catalogId);
        varIds.add(varId);
        programIds.add(programId);

        List<VarProgramCatalogConfigEntity> catalogConfigEntities = new ArrayList<>();
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName(name);
        varProgramCatalogConfigEntity.setValue("Photography,imac,slugName");
        catalogConfigEntities.add(varProgramCatalogConfigEntity);

        when(varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(any(List.class), any(List.class), any(List.class), any(String.class))).
            thenReturn(catalogConfigEntities);
        List<String> value =  varProgramCatalogConfigService.getListOfValue(catalogId, varId, programId, name);
        assertEquals(3, value.size());
    }

    private List<VarProgramCatalogConfigEntity> getCapitalCitiesList(){
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntityList = new ArrayList<>();
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName("England");
        varProgramCatalogConfigEntity.setValue("London");
        varProgramCatalogConfigEntityList.add(varProgramCatalogConfigEntity);
        varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName("Germany");
        varProgramCatalogConfigEntity.setValue("Berlin");
        varProgramCatalogConfigEntityList.add(varProgramCatalogConfigEntity);
        varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName("Norway");
        varProgramCatalogConfigEntity.setValue("Oslo");
        varProgramCatalogConfigEntityList.add(varProgramCatalogConfigEntity);
        varProgramCatalogConfigEntity = getDefaultVarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setName("USA");
        varProgramCatalogConfigEntity.setValue("Washington DC");
        varProgramCatalogConfigEntityList.add(varProgramCatalogConfigEntity);
        return varProgramCatalogConfigEntityList;
    }

    private VarProgramCatalogConfigEntity getDefaultVarProgramCatalogConfigEntity(){
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = new VarProgramCatalogConfigEntity();
        varProgramCatalogConfigEntity.setCatalogId("-1");
        varProgramCatalogConfigEntity.setVarId("-1");
        varProgramCatalogConfigEntity.setProgramId("default");
        varProgramCatalogConfigEntity.setActiveInd("1");
        return varProgramCatalogConfigEntity;
    }
}
