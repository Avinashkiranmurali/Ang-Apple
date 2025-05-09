package com.b2s.common.services.financeoptions.service;

import com.b2s.rewards.apple.model.Program;
import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.apple.model.finance.citi.CardDetails;

public interface FinanceOptionsService {

    /**
     * Get the payment provider from Enum
     * @return String
     */
    String getPaymentProvider();

    /**
     * Get Finance Options for the associated VAR
     *
     * @param program {@link Program}
     * @param installmentAmount Double
     * @param cardId String
     * @return {@link FinanceOptionsResponse}
     */
    FinanceOptionsResponse getFinanceOptions(final Program program, final Double installmentAmount, final String cardId);

    /**
     *
     * @return {@link CardDetails}
     */
    CardsResponse getCards();

    //TODO: Other APIs whichever is applicable

}
