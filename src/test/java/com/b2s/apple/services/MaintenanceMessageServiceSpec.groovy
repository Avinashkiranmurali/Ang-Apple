package com.b2s.apple.services

import com.b2s.apple.entity.MaintenanceMessageEntity
import com.b2s.rewards.apple.dao.MaintenanceMessageDao
import spock.lang.Specification
import spock.lang.Subject

class MaintenanceMessageServiceSpec extends Specification{

    def maintenanceMessageDao = Mock(MaintenanceMessageDao)

    @Subject
    final MaintenanceMessageService maintenanceMessageService = new MaintenanceMessageService(maintenanceMessageDao:
            maintenanceMessageDao)


    def "get Maintenance Message"() {

        given:
        maintenanceMessageDao.getVarProgramMessageValue(_, _) >> getMaintenanceMessageEntities

        expect:
        message == maintenanceMessageService.getMaintenanceMessage(varId, programId)

        where:
        varId        | programId     | getMaintenanceMessageEntities || message
        "VitalityUS" | "Apollo"      | specificEntities()            || "VitalityUS - Apollo Maintenance Message"
        "VitalityUS" | "JohnHancock" | varSpecificEntities()         || "VitalityUS Maintenance Message"
        "FDR"        | "Demo"        | genericEntities()             || "Maintenance Message"
        "FDR"        | "Demo"        | emptyEntities()               || ""
    }

    def specificEntities(){
        List<MaintenanceMessageEntity> maintenanceMessageEntities = new ArrayList<>()
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("-1","default","Maintenance Message",true))
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("VitalityUS","Apollo","VitalityUS - Apollo Maintenance Message",true))
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("VitalityUS","default","VitalityUS Maintenance Message",true))
        return maintenanceMessageEntities
    }

    def varSpecificEntities(){
        List<MaintenanceMessageEntity> maintenanceMessageEntities = new ArrayList<>()
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("VitalityUS","default","VitalityUS Maintenance Message",true))
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("-1","default","Maintenance Message",true))
        return maintenanceMessageEntities
    }

    def genericEntities(){
        List<MaintenanceMessageEntity> maintenanceMessageEntities = new ArrayList<>()
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("-1","default","Maintenance Message",true))
        maintenanceMessageEntities.add(getNewMaintenanceMessageEntity("VitalityUS","Apollo","VitalityUS - Apollo Maintenance Message",true))
        return maintenanceMessageEntities
    }

    def emptyEntities(){
    }

    def getNewMaintenanceMessageEntity(String varId, String programId, String message, boolean activeInd){
        MaintenanceMessageEntity maintenanceMessageEntity = new MaintenanceMessageEntity()
        maintenanceMessageEntity.setVarId(varId)
        maintenanceMessageEntity.setProgramId(programId)
        maintenanceMessageEntity.setMessage(message)
        maintenanceMessageEntity.setActive(activeInd)
        return maintenanceMessageEntity
    }
}
