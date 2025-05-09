package com.b2s.apple.mapper

import com.b2s.apple.services.CategoryConfigurationService
import com.b2s.apple.services.EngravingService
import com.b2s.common.services.util.ImageObfuscatory
import com.b2s.common.services.util.MerchantRepositoryHolder
import com.b2s.rewards.apple.model.Price
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.RedemptionPaymentLimit
import com.b2s.rewards.apple.model.VarProgramRedemptionOption
import com.b2s.rewards.apple.util.ShipmentQuoteUtil
import spock.lang.Specification
import spock.lang.Subject

import java.sql.Timestamp

class ProductMapperSpec extends Specification {
    def optionMapper = Mock(OptionMapper)
    def imageObfuscatory = Mock(ImageObfuscatory)
    def categoryConfigurationService = Mock(CategoryConfigurationService)
    def engravingService = Mock(EngravingService)
    def shipmentQuoteUtil = Mock(ShipmentQuoteUtil)
    Map<String, String> supplierProductMapping = new HashMap()
    def merchantRepositoryHolder = Mock(MerchantRepositoryHolder)
    Map<String, String> legacyMerchantCodeMappings = new HashMap()

    @Subject
    def productMapper = new ProductMapper(optionMapper: optionMapper, imageObfuscatory: imageObfuscatory,
            categoryConfigurationService: categoryConfigurationService, engravingService: engravingService,
            shipmentQuoteUtil: shipmentQuoteUtil, supplierProductMapping: supplierProductMapping,
            merchantRepositoryHolder: merchantRepositoryHolder, legacyMerchantCodeMappings: legacyMerchantCodeMappings)

    def 'test smartPrice'() {
        when:
        Program program = getProgram(varId, programId)

        then:
        def smartPrice = productMapper.getSmartPrice(redemptionPaymentLimit, program)
        smartPrice.points == minPoint
        smartPrice.amount == maxCash
        smartPrice.getCurrencyCode() == currency
        smartPrice.isCashMaxLimitReached == isCashMaxLimitReached

        where:
        varId | programId | redemptionPaymentLimit || currency || minPoint || maxCash            || isCashMaxLimitReached
        'UA'  | 'MP'      | getUAPayment()         || "USD"    || 24800    || 179.84024999999997 || false
        'WF'  | 'A1'      | getWFPayment()         || "USD"    || 5379     || 299.98765          || false
        'WF'  | 'A1'      | getWFMaxPayment()      || "USD"    || 229092   || 300                || true
        'RBC' | 'AIB'     | getRBCPayment()        || "CAD"    || 10630    || 276.354            || false
    }

    def getProgram(String varId, String programId) {
        def pointsOnlyStr = "pointsonly"
        def splitPayStr = "splitpay"

        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)

        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()

        if (varId == 'UA') {
            List<VarProgramRedemptionOption> varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption pointsOnly = getVarProgramRedemptionOption("percentage", 0, 50, pointsOnlyStr, 1, varId, programId)
            varProgramRedemptionOptionList.add(pointsOnly)
            redemptionOptions.put(pointsOnlyStr, varProgramRedemptionOptionList)

            varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption splitPay = getVarProgramRedemptionOption("percentage", 50, 0, splitPayStr, 2, varId, programId)
            varProgramRedemptionOptionList.add(splitPay)
            redemptionOptions.put(splitPayStr, varProgramRedemptionOptionList)
        } else if (varId == 'WF') {
            List<VarProgramRedemptionOption> varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption pointsOnly = getVarProgramRedemptionOption("percentage", 0, 20, pointsOnlyStr, 1, varId, programId)
            varProgramRedemptionOptionList.add(pointsOnly)
            redemptionOptions.put(pointsOnlyStr, varProgramRedemptionOptionList)

            varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption splitPayPercentage = getVarProgramRedemptionOption("percentage", 80, 0, splitPayStr, 2, varId, programId)
            varProgramRedemptionOptionList.add(splitPayPercentage)
            VarProgramRedemptionOption splitPayDollar = getVarProgramRedemptionOption("dollar", 300, 0, splitPayStr, 2, varId, programId)
            varProgramRedemptionOptionList.add(splitPayDollar)
            redemptionOptions.put(splitPayStr, varProgramRedemptionOptionList)
        } else if (varId == 'RBC') {
            List<VarProgramRedemptionOption> varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption pointsOnly = getVarProgramRedemptionOption("percentage", 0, 20, pointsOnlyStr, 1, varId, programId)
            varProgramRedemptionOptionList.add(pointsOnly)
            redemptionOptions.put(pointsOnlyStr, varProgramRedemptionOptionList)

            varProgramRedemptionOptionList = new ArrayList<>()
            VarProgramRedemptionOption splitPay = getVarProgramRedemptionOption("percentage", 80, 0, splitPayStr, 2, varId, programId)
            varProgramRedemptionOptionList.add(splitPay)
            redemptionOptions.put(splitPayStr, varProgramRedemptionOptionList)
        }
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getVarProgramRedemptionOption(String limitType, Integer paymentMaxLimit, Integer paymentMinLimit, String paymentOption,
                                      Integer orderBy, String programId, String varId) {
        return VarProgramRedemptionOption.builder()
                .withLimitType(limitType)
                .withPaymentMaxLimit(paymentMaxLimit)
                .withPaymentMinLimit(paymentMinLimit)
                .withPaymentOption(paymentOption)
                .withActive(true)
                .withLastUpdatedBy("updatedBy")
                .withLastUpdatedDate(new Timestamp(1476706090633))
                .withOrderBy(orderBy)
                .withProgramId(programId)
                .withVarId(varId)
                .build()
    }

    def getUAPayment() {
        Price cartMaxLimit = null
        Price cashMaxLimit = new Price(179.84, "USD", 24800)
        Price cashMinLimit = new Price(0, "USD", 0)
        Price pointsMaxLimit = new Price(267.84, "USD", 49600)
        Price pointsMinLimit = new Price(133.92, "USD", 24800)
        Price useMaxPoints = new Price(0, "USD", 49600)
        Price useMinPoints = new Price(179.84024999999997, "USD", 24800)

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build()
    }

    def getWFPayment() {
        Price cartMaxLimit = null
        Price cashMaxLimit = new Price(299.99, "USD", 21513)
        Price cashMinLimit = new Price(0, "USD", 0)
        Price pointsMaxLimit = new Price(268.92, "USD", 26892)
        Price pointsMinLimit = new Price(53.79, "USD", 5379)
        Price useMaxPoints = new Price(0, "USD", 26892)
        Price useMinPoints = new Price(299.98765, "USD", 5379)

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build()
    }

    def getWFMaxPayment() {
        Price cartMaxLimit = null
        Price cashMaxLimit = new Price(300, "USD", 30000)
        Price cashMinLimit = new Price(0, "USD", 0)
        Price pointsMaxLimit = new Price(2590.92, "USD", 259092)
        Price pointsMinLimit = new Price(2290.92, "USD", 229092)
        Price useMaxPoints = new Price(0, "USD", 259092)
        Price useMinPoints = new Price(300, "USD", 229092)

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build()
    }

    def getRBCPayment() {
        Price cartMaxLimit = null
        Price cashMaxLimit = new Price(276.36, "CAD", 42516)
        Price cashMinLimit = new Price(0, "CAD", 0)
        Price pointsMaxLimit = new Price(345.44900000000007, "CAD", 53146)
        Price pointsMinLimit = new Price(69.09500000000001, "CAD", 10630)
        Price useMaxPoints = new Price(0, "CAD", 53146)
        Price useMinPoints = new Price(276.354, "CAD", 10630)

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build()
    }
}
