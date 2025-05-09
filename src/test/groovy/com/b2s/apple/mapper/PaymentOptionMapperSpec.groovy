package com.b2s.apple.mapper

import com.b2s.rewards.apple.model.PaymentOption
import com.b2s.rewards.apple.model.VarProgramPaymentOption
import spock.lang.Specification
import spock.lang.Subject

/**
 * Unit test specifications for the {@link PaymentOptionMapper} class.
 */
class PaymentOptionMapperSpec extends Specification {

    @Subject
    def paymentOptionMapper = new PaymentOptionMapper()

    def 'mapper maps/transforms correctly - 1 paymentOption'() {
        given: 'the varProgramPaymentOption'
        final def varProgramPaymentOption1 =
            new VarProgramPaymentOption(paymentOption: 'PAYROLL_DEDUCTION',
                                        paymentProvider: 'GRASSROOTS',
                                        paymentTemplate: 'pd_default',
                                        isActive: true,
                                        orderBy: 1,
                                        paymentMinLimit: 200,
                                        paymentMaxLimit: 4000)

        final def varProgramPaymentOptions = [varProgramPaymentOption1]

        when:
        final def paymentOptions = paymentOptionMapper.from(varProgramPaymentOptions)

        then:
        !paymentOptions.empty
        paymentOptions.size() == 1

        final def paymentOption1 = paymentOptions[0]
        paymentOption1.paymentOption == varProgramPaymentOption1.paymentOption
        paymentOption1.paymentProvider == PaymentOption.PaymentProvider.GRASSROOTS
        paymentOption1.paymentTemplate == varProgramPaymentOption1.paymentTemplate
        paymentOption1.isActive == varProgramPaymentOption1.isActive
        paymentOption1.orderBy == varProgramPaymentOption1.orderBy
        paymentOption1.paymentMinLimit == varProgramPaymentOption1.paymentMinLimit
        paymentOption1.paymentMaxLimit == varProgramPaymentOption1.paymentMaxLimit
    }

    def 'mapper maps/transforms correctly - multiple paymentOptions'() {
        given: 'the varProgramPaymentOption'
        final def varProgramPaymentOption1 =
            new VarProgramPaymentOption(paymentOption: 'CASH',
                                        paymentTemplate: 'pd_default',
                                        isActive: true,
                                        orderBy: 1,
                                        paymentMinLimit: 200,
                                        paymentMaxLimit: 4000)
        final def varProgramPaymentOption2 =
            new VarProgramPaymentOption(paymentOption: 'PAYROLL_DEDUCTION',
                                        paymentProvider: 'PPC',
                                        paymentTemplate: 'pd_default',
                                        isActive: true,
                                        orderBy: 2)

        final def varProgramPaymentOptions = [varProgramPaymentOption1, varProgramPaymentOption2]

        when:
        final def paymentOptions = paymentOptionMapper.from(varProgramPaymentOptions)

        then:
        !paymentOptions.empty
        paymentOptions.size() == 2

        final def paymentOption1 = paymentOptions[0]
        paymentOption1.paymentOption == varProgramPaymentOption1.paymentOption
        !paymentOption1.paymentProvider
        paymentOption1.paymentTemplate == varProgramPaymentOption1.paymentTemplate
        paymentOption1.isActive == varProgramPaymentOption1.isActive
        paymentOption1.orderBy == varProgramPaymentOption1.orderBy
        paymentOption1.paymentMinLimit == varProgramPaymentOption1.paymentMinLimit
        paymentOption1.paymentMaxLimit == varProgramPaymentOption1.paymentMaxLimit

        final def paymentOption2 = paymentOptions[1]
        paymentOption2.paymentOption == varProgramPaymentOption2.paymentOption
        paymentOption2.paymentProvider == PaymentOption.PaymentProvider.PPC
        paymentOption2.paymentTemplate == varProgramPaymentOption2.paymentTemplate
        paymentOption2.isActive == varProgramPaymentOption2.isActive
        paymentOption2.orderBy == varProgramPaymentOption2.orderBy
        paymentOption2.paymentMinLimit == varProgramPaymentOption2.paymentMinLimit
        paymentOption2.paymentMaxLimit == varProgramPaymentOption2.paymentMaxLimit
    }

    def 'mapper maps/transforms correctly - empty paymentOptions'() {
        given: 'the empty varProgramPaymentOptions'
        final def varProgramPaymentOptions = []

        when:
        final def paymentOptions = paymentOptionMapper.from(varProgramPaymentOptions)

        then:
        paymentOptions.empty
    }

    def 'mapper maps/transforms correctly - null payment provider'() {
        given: 'the varProgramPaymentOption'
        final def varProgramPaymentOption1 =
            new VarProgramPaymentOption(paymentOption: 'POINTS',
                                        paymentTemplate: 'pd_default',
                                        paymentProvider: null,
                                        isActive: true,
                                        orderBy: 1,
                                        paymentMinLimit: 200,
                                        paymentMaxLimit: 4000)

        final def varProgramPaymentOptions = [varProgramPaymentOption1]

        when:
        final def paymentOptions = paymentOptionMapper.from(varProgramPaymentOptions)

        then:
        !paymentOptions.empty
        paymentOptions.size() == 1

        final def paymentOption1 = paymentOptions[0]
        paymentOption1.paymentOption == varProgramPaymentOption1.paymentOption
        !paymentOption1.paymentProvider
        paymentOption1.paymentTemplate == varProgramPaymentOption1.paymentTemplate
        paymentOption1.isActive == varProgramPaymentOption1.isActive
        paymentOption1.orderBy == varProgramPaymentOption1.orderBy
        paymentOption1.paymentMinLimit == varProgramPaymentOption1.paymentMinLimit
        paymentOption1.paymentMaxLimit == varProgramPaymentOption1.paymentMaxLimit
    }

}
