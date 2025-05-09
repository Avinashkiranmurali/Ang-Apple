package com.b2s.common.services.financeoptions

import com.b2s.apple.mapper.FinanceOptionsMapper
import com.b2s.common.services.financeoptions.amex.impl.AmexFinanceOptionsServiceImpl
import com.b2s.rewards.apple.dao.VarProgramFinanceOptionDao
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.VarProgramFinanceOption
import org.joda.money.CurrencyUnit
import org.tools4j.groovytables.GroovyTables
import spock.lang.Specification
import spock.lang.Subject

class AmexFinanceOptionsServiceImplSpec extends Specification {
    VarProgramFinanceOptionDao varProgramFinanceOptionDao = Mock()
    FinanceOptionsMapper financeOptionsMapper = new FinanceOptionsMapper()

    @Subject
    AmexFinanceOptionsServiceImpl serviceImpl=new AmexFinanceOptionsServiceImpl(varProgramFinanceOptionDao:
            varProgramFinanceOptionDao, financeOptionsMapper:financeOptionsMapper)

    def 'getFinanceOptions'() {
        given:
        def amount = 200.0
        def cardId = "4141704f4c746c6253416d35483468563433754e64723177717871764c624f312b5647677741724f4739593d"
        Program program = getProgram()

        when:
        varProgramFinanceOptionDao.getVarProgramFinanceOption(_,_) >> getVarProgramFinanceOptions()

        then:
        def result  = serviceImpl.getFinanceOptions(program,amount,cardId)

        expect:
        result.financeOptions.size() == 3
        result.financeOptions.get(0).varId == program.varId
        result.financeOptions.get(0).programId == program.programId
        result.financeOptions.get(0).installment == 12
        result.financeOptions.get(1).installment == 6
        result.financeOptions.get(1).messageCode == "finance.month.6.text"
        result.financeOptions.get(2).installment == 3
        result.financeOptions.get(2).orderBy == 3
    }

    def getProgram() {
        Program program = new Program()
        program.setVarId("UA")
        program.setProgramId("MP")
        program.targetCurrency = CurrencyUnit.USD

        return program
    }

    def getVarProgramFinanceOptions() {
        List<VarProgramFinanceOption> varProgramFinanceOptions = GroovyTables.createListOf(VarProgramFinanceOption).fromTable {
            active  |   orderBy |   installment |   installmentPeriod   |   messageCode
            true    |   1       |   12          |   "month"             |   "finance.month.12.text"
            true    |   2       |   6           |   "month"             |   "finance.month.6.text"
            true    |   3       |   3           |   "month"             |   "finance.month.3.text"
        }
        return varProgramFinanceOptions
    }
    }