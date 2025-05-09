package com.b2s.apple.model.finance;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;

import java.util.Arrays;

public enum FinanceOptionsPaymentProvider {
    CITI(CommonConstants.FINANCE_SERVICE_IDENTIFIER_CITI),
    AMEX(CommonConstants.FINANCE_SERVICE_IDENTIFIER_AMEX);

    private final String value;

    FinanceOptionsPaymentProvider(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String lookup(final String paymentProvider) throws ServiceException {

        return Arrays.stream(values())
                .filter(visLookupStrategy -> visLookupStrategy.name().equals(paymentProvider))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ServiceExceptionEnums.FINANCE_OPTIONS_SERVICE_NOT_FOUND_EXCEPTION, new String[]{paymentProvider}))
                .getValue();
    }
}
