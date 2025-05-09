package com.b2s.common.services.financeoptions;

import com.b2s.common.services.financeoptions.service.FinanceOptionsService;

/**
 * Factory class to fetch the appropriate Finance Options Service implementation
 */
public interface FinanceOptionsServiceFactory {

    /**
     * Get the associated Finance Options Service Implementation
     *
     * @param financeServiceIdentifier
     * @return
     */
    FinanceOptionsService getFinanceOptionsService(final String financeServiceIdentifier);
}
