package com.b2s.common.services.financeoptions.amex.impl;

import com.b2s.rewards.apple.dao.VarProgramFinanceOptionDao;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.VarProgramFinanceOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.mapper.FinanceOptionsMapper;
import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOptionsPaymentProvider;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.common.services.financeoptions.service.FinanceOptionsService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(CommonConstants.FINANCE_SERVICE_IDENTIFIER_AMEX)
public class AmexFinanceOptionsServiceImpl implements FinanceOptionsService {



    @Autowired
    private VarProgramFinanceOptionDao varProgramFinanceOptionDao;

    @Autowired
    private FinanceOptionsMapper financeOptionsMapper;

    @Override
    public String getPaymentProvider() {
        return FinanceOptionsPaymentProvider.AMEX.getValue();
    }

    /**
     * Get Finance Options for Amex
     *
     * @param program {@link Program}
     * @param amount Double
     * @param cardId String
     * @return {@link FinanceOptionsResponse}
     */
    @Override
    public FinanceOptionsResponse getFinanceOptions(final Program program, final Double amount, final String cardId) {
        List<VarProgramFinanceOption> varProgramFinanceOptions;
        FinanceOptionsResponse financeOptionsResponse;

        varProgramFinanceOptions = varProgramFinanceOptionDao.getVarProgramFinanceOption(program.getVarId(), program.getProgramId());
        if (CollectionUtils.isEmpty(varProgramFinanceOptions)) {
            varProgramFinanceOptions = varProgramFinanceOptionDao.getVarProgramFinanceOption(program.getVarId(), CommonConstants.DEFAULT_PROGRAM_KEY);
        }
        financeOptionsResponse = financeOptionsMapper.buildFinanceOptionsResponse(varProgramFinanceOptions, program, amount);

        return financeOptionsResponse;
    }

    @Override
    public CardsResponse getCards() {
        //TODO: Implement the method if needed for Amex
        return null;
    }
}
