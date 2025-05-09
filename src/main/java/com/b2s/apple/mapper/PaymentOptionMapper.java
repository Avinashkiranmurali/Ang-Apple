package com.b2s.apple.mapper;

import com.b2s.rewards.apple.model.PaymentOption;
import com.b2s.rewards.apple.model.VarProgramPaymentOption;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by rpillai on 6/24/2016.
 */
@Component
public class PaymentOptionMapper {

    public List<PaymentOption> from(final List<VarProgramPaymentOption> varProgramPaymentOptions) {
        if (CollectionUtils.isEmpty(varProgramPaymentOptions)) {
            return new ArrayList<>();
        }

        return varProgramPaymentOptions.stream()
            .filter(VarProgramPaymentOption::getIsActive)
            .map(this::buildPaymentOption).collect(Collectors.toList());
    }

    private PaymentOption buildPaymentOption(final VarProgramPaymentOption varProgramPaymentOption) {
        final PaymentOption paymentOption = new PaymentOption();
        paymentOption.setPaymentOption(varProgramPaymentOption.getPaymentOption());
        paymentOption.setPaymentProvider(
            PaymentOption.PaymentProvider.fromName(varProgramPaymentOption.getPaymentProvider()).orElse(null));
        paymentOption.setPaymentTemplate(varProgramPaymentOption.getPaymentTemplate());
        paymentOption.setIsActive(varProgramPaymentOption.getIsActive());
        paymentOption.setOrderBy(varProgramPaymentOption.getOrderBy());
        paymentOption.setPaymentMinLimit(
            Optional.ofNullable(varProgramPaymentOption.getPaymentMinLimit()).map(Double::valueOf).orElse(null));
        paymentOption.setPaymentMaxLimit(
            Optional.ofNullable(varProgramPaymentOption.getPaymentMaxLimit()).map(Double::valueOf).orElse(null));
        paymentOption.setSupplementaryPaymentMinLimit(varProgramPaymentOption.getSupplementaryPaymentMinLimit());
        paymentOption.setSupplementaryPaymentMaxLimit(varProgramPaymentOption.getSupplementaryPaymentMaxLimit());
        paymentOption.setSupplementaryPaymentLimitType(varProgramPaymentOption.getSupplementaryPaymentLimitType());
        paymentOption.setSupplementaryPaymentType(varProgramPaymentOption.getSupplementaryPaymentType());
        return paymentOption;
    }

}
