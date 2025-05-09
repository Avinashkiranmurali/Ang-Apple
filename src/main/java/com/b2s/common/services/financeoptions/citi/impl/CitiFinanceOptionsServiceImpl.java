package com.b2s.common.services.financeoptions.citi.impl;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.mapper.FinanceOptionsMapper;
import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOptionsPaymentProvider;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.apple.model.finance.citi.CardDetails;
import com.b2s.apple.model.finance.citi.EasyPaymentResponse;
import com.b2s.common.services.financeoptions.service.FinanceOptionsService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@Component(CommonConstants.FINANCE_SERVICE_IDENTIFIER_CITI)
public class CitiFinanceOptionsServiceImpl implements FinanceOptionsService {

    private static final Logger logger = LoggerFactory.getLogger(CitiFinanceOptionsServiceImpl.class);

    @Autowired
    private FinanceOptionsMapper financeOptionsMapper;

    @Override
    public String getPaymentProvider() {
        return FinanceOptionsPaymentProvider.CITI.getValue();
    }

    /**
     * Get Finance Options for the associated VAR
     *
     * @param program {@link Program}
     * @param amount Double
     * @param cardId String
     * @return {@link FinanceOptionsResponse}
     */
    @Override
    public FinanceOptionsResponse getFinanceOptions(final Program program, final Double amount, final String cardId) {

        final FinanceOptionsResponse financeOptionsResponse;

        //TODO: Call API by passing Card ID for Citi. For Other VARS, need to determine the design
        EasyPaymentResponse easyPaymentResponse = null;
        try {
            //Mocked getPlans Response
            File jsonFile = ResourceUtils.getFile("classpath:json/getPlans_Response.json");
            jsonFile.setExecutable(false);
            jsonFile.setReadable(true);
            jsonFile.setWritable(false);
            //JSON to Object Mapper using Jackson
            easyPaymentResponse = new ObjectMapper().readValue(jsonFile, EasyPaymentResponse.class);
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException " , e);
        } catch (JsonParseException e) {
            logger.error("JsonParseException " , e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException " , e);
        } catch (IOException e) {
            logger.error("IOException " , e);
        }
        financeOptionsResponse = financeOptionsMapper.buildFinanceOptionsResponse(easyPaymentResponse, program, amount);

        return financeOptionsResponse;
    }

    /**
     * Get the list of Cards for the associated VAR
     *
     * @return {@link CardDetails}
     */
    @Override
    public CardsResponse getCards() {

        CardDetails cardDetails = null;
        CardsResponse cardsResponse = new CardsResponse();
        try {
            //TODO: Call API to get Card Details for Citi. For Other VARS, need to determine the design
            //Mocked getCards Response
            File jsonFile = ResourceUtils.getFile("classpath:json/getCards_Response.json");
            jsonFile.setExecutable(false);
            jsonFile.setReadable(true);
            jsonFile.setWritable(false);
            //JSON to Object Mapper using Jackson
            cardDetails = new ObjectMapper().readValue(jsonFile, CardDetails.class);
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException ", e);
        } catch (JsonParseException e) {
            logger.error("JsonParseException ", e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException ", e);
        } catch (IOException e) {
            logger.error("IOException ", e);
        }

        //Getting Active Local Cards - Testing
        if (Objects.nonNull(cardDetails) && Objects.nonNull(cardDetails.getCardDetails())) {

            cardsResponse = financeOptionsMapper.buildCardsResponse(cardDetails);

        }

        return cardsResponse;
    }

}
