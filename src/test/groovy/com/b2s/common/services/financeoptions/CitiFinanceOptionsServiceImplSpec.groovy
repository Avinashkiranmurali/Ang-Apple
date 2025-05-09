package com.b2s.common.services.financeoptions

import com.b2s.apple.mapper.FinanceOptionsMapper
import com.b2s.common.services.financeoptions.citi.impl.CitiFinanceOptionsServiceImpl
import com.b2s.rewards.apple.model.Program
import org.joda.money.CurrencyUnit
import spock.lang.Specification
import spock.lang.Subject

class CitiFinanceOptionsServiceImplSpec extends Specification {

    FinanceOptionsMapper financeOptionsMapper = new FinanceOptionsMapper()

    @Subject
    CitiFinanceOptionsServiceImpl serviceImpl=new CitiFinanceOptionsServiceImpl(financeOptionsMapper:financeOptionsMapper)

    def 'getFinanceOptions'() {
        given:
        def amount = 200.0
        def cardId = "4141704f4c746c6253416d35483468563433754e64723177717871764c624f312b5647677741724f4739593d"
        Program program = getProgram()
        when:
        def result  = serviceImpl.getFinanceOptions(program,amount,cardId)
        then:
        result.financeOptions.get(0).varId == program.varId
        result.financeOptions.get(0).programId == program.programId
        result.financeOptions.get(0).installment == 12
    }


    def getProgram() {
        Program program = new Program()
        program.setVarId("UA")
        program.setProgramId("MP")
        program.targetCurrency = CurrencyUnit.USD
        return program
    }

}
