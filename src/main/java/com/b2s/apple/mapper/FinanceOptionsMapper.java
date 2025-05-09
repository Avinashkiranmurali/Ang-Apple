package com.b2s.apple.mapper;

import com.b2s.apple.model.finance.Card;
import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOption;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.apple.model.finance.citi.CardDetail;
import com.b2s.apple.model.finance.citi.CardDetails;
import com.b2s.apple.model.finance.citi.EasyPaymentPlan;
import com.b2s.apple.model.finance.citi.EasyPaymentResponse;
import com.b2s.apple.services.VarProgramMessageService;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.VarProgramFinanceOption;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FinanceOptionsMapper {

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    /**
     * Build FinanceOptionsResponse from the database
     *
     * @param varProgramFinanceOptions
     * @param program
     * @param amount
     * @return FinanceOptionsResponse {@link FinanceOptionsResponse}
     */
    public FinanceOptionsResponse buildFinanceOptionsResponse (final List<VarProgramFinanceOption> varProgramFinanceOptions,
                                                               final Program program,
                                                               final Double amount) {

        List<FinanceOption> financeOptionsList = varProgramFinanceOptions.stream()
                .filter(VarProgramFinanceOption::getActive)
                .sorted(Comparator.comparingInt(VarProgramFinanceOption::getOrderBy))
                .map(varProgramFinanceOption -> buildFinanceOption(varProgramFinanceOption, program, amount))
                .collect(Collectors.toList());
        FinanceOptionsResponse financeOptionsResponse = new FinanceOptionsResponse();
        financeOptionsResponse.setFinanceOptions(financeOptionsList);

        return financeOptionsResponse;
    }

    /**
     * Build FinanceOption presentation response from the database
     *
     * @param varProgramFinanceOption
     * @param program
     * @param amount
     * @return FinanceOption {@link FinanceOption}
     */
    private FinanceOption buildFinanceOption(final VarProgramFinanceOption varProgramFinanceOption,
                                             final Program program,
                                             final Double amount) {
        final FinanceOption financeOption = new FinanceOption();

        financeOption.setVarId(program.getVarId());
        financeOption.setProgramId(program.getProgramId());
        financeOption.setInstallment(varProgramFinanceOption.getInstallment());
        financeOption.setInstallmentPeriod(varProgramFinanceOption.getInstallmentPeriod());
        financeOption.setInstallmentAmount(new BigDecimal(amount).divide(new BigDecimal(varProgramFinanceOption.getInstallment()), 2, RoundingMode.FLOOR).doubleValue());
        financeOption.setMessageCode(varProgramFinanceOption.getMessageCode());
        financeOption.setActive(varProgramFinanceOption.getActive());
        financeOption.setOrderBy(varProgramFinanceOption.getOrderBy());
        financeOption.setCurrency(program.getTargetCurrency().toCurrency());
        financeOption.setEstablishmentFeeType(varProgramFinanceOption.getEstablishmentFeeType());
        financeOption.setEstablishmentFeeRate(varProgramFinanceOption.getEstablishmentFeeRate());
        if (CommonConstants.PERCENTAGE.equalsIgnoreCase(varProgramFinanceOption.getEstablishmentFeeType())) {
            financeOption.setEstablishmentFeeAmt(new BigDecimal(amount).multiply(new BigDecimal(varProgramFinanceOption.getEstablishmentFeeRate())).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.FLOOR).floatValue());
        } else {
            financeOption.setEstablishmentFeeAmt(varProgramFinanceOption.getEstablishmentFeeRate());
        }

        return financeOption;
    }

    /**
     * Build Finance Options response from API data
     *
     * @param easyPaymentResponse
     * @param program
     * @param amount
     * @return FinanceOptionsResponse {@link FinanceOptionsResponse}
     */
    public FinanceOptionsResponse buildFinanceOptionsResponse (final EasyPaymentResponse easyPaymentResponse,
                                                               final Program program,
                                                               final Double amount) {
        List<FinanceOption> financeOptionsList = easyPaymentResponse.getEasyPaymentPlans().stream()
                .sorted(Comparator.comparingInt(EasyPaymentPlan::getTenor).reversed())
                .map(easyPaymentPlan -> buildFinanceOption(easyPaymentPlan, program, amount)).collect(Collectors.toList());
        FinanceOptionsResponse financeOptionsResponse = new FinanceOptionsResponse();
        financeOptionsResponse.setFinanceOptions(financeOptionsList);

        return financeOptionsResponse;
    }

    /**
     * Build FinanceOption presentation response from API data
     *
     * @param easyPaymentPlan
     * @param program
     * @param amount
     * @return FinanceOption {@link FinanceOption}
     */
    private FinanceOption buildFinanceOption(final EasyPaymentPlan easyPaymentPlan,
                                             final Program program,
                                             final Double amount) {
        final FinanceOption financeOption = new FinanceOption();

        financeOption.setVarId(program.getVarId());
        financeOption.setProgramId(program.getProgramId());
        financeOption.setInstallment(easyPaymentPlan.getTenor());
        //TODO: Hardcoded for testing. Need to check the API mapping for this or will it be defaulted to month
        financeOption.setInstallmentPeriod("month");
        financeOption.setInstallmentAmount(new BigDecimal(amount).divide(new BigDecimal(easyPaymentPlan.getTenor()), 2, RoundingMode.FLOOR).doubleValue());
        financeOption.setMessageCode(CommonConstants.FINANCE_MESSAGE_KEY_PREFIX + easyPaymentPlan.getTenor() + CommonConstants.FINANCE_MESSAGE_KEY_SUFFIX);
        financeOption.setActive(true);
        financeOption.setCurrency(program.getTargetCurrency().toCurrency());

        return financeOption;
    }

    /**
     * Build CardsResponse from API data
     *
     * @param cardDetails
     * @return CardsResponse {@link CardsResponse}
     */
    public CardsResponse buildCardsResponse (final CardDetails cardDetails) {
        List<Card> cardsList = cardDetails.getCardDetails().stream()
                .filter(cardDetail -> cardDetail.getLocalCardActivationIndicator().equalsIgnoreCase("ACTIVE"))
                .map(cardDetail -> buildCardDetail(cardDetail))
                .collect(Collectors.toList());

        final CardsResponse cardsResponse = new CardsResponse();
        cardsResponse.setCards(cardsList);

        return cardsResponse;
    }

    /**
     * Build Card presentation response from API data
     * @param cardDetail
     * @return Card {@link Card}
     */
    private Card buildCardDetail(final CardDetail cardDetail) {
        //TODO: Hardcoding few values for testing purpose. Later this would either come from API or dynamically framed
        final Card card = Card.builder()
                .withCardType("VISA")
                .withCardId(cardDetail.getCardId())
                .withDisplayCardNumber(cardDetail.getDisplayCardNumber())
                .withExpireMonth("**")
                .withExpireYear("**")
                .build();

        return card;
    }

}
