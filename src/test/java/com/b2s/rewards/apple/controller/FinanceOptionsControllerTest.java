package com.b2s.rewards.apple.controller;

import com.b2s.apple.model.finance.CardsResponse;
import com.b2s.apple.model.finance.FinanceOptionsPaymentProvider;
import com.b2s.apple.model.finance.FinanceOptionsResponse;
import com.b2s.apple.services.AppSessionInfo;
import com.b2s.common.services.financeoptions.FinanceOptionsServiceFactory;
import com.b2s.common.services.financeoptions.service.FinanceOptionsService;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.VarProgramRedemptionOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.money.CurrencyUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceOptionsControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceOptionsControllerTest.class);

    private MockMvc mockMvc;

    @Mock
    private View mockView;

    private MockHttpSession session =new MockHttpSession();

    @Mock
    private AppSessionInfo appSessionInfo;

    @Mock
    private FinanceOptionsService financeOptionsService;

    @Mock
    private FinanceOptionsServiceFactory financeOptionsServiceFactory;

    @InjectMocks
    private FinanceOptionsController financeOptionsController;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(financeOptionsController).setSingleView(mockView).build();
    }


    /**
     * Unit testing Get Cards
     *
     * @throws Exception
     */
    @Test
    public void testGetCards() throws Exception {

        User user = new User();
        user.setUserId("TestUser");
        user.setVarId("AU");
        user.setProgramId("b2s_qa_only");

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram(user, FinanceOptionsPaymentProvider.CITI.name(), CurrencyUnit.of("AUD")));


        CardsResponse cardsResponseMock = getCardsResponse();

        when(appSessionInfo.currentUser()).thenReturn(user);
        when(financeOptionsServiceFactory.getFinanceOptionsService(anyString())).thenReturn(financeOptionsService);
        when(financeOptionsService.getCards()).thenReturn(cardsResponseMock);

        MvcResult result = mockMvc.perform(get("/getCards").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        CardsResponse cardsResponseAssert = new ObjectMapper().readValue(content, CardsResponse.class);

        assertThat(cardsResponseAssert, notNullValue());
        assertThat(cardsResponseAssert.getCards(), notNullValue());
        assertThat(cardsResponseAssert.getCards(), hasSize(2));
        assertThat(cardsResponseAssert.getCards().stream().findFirst().get().getDisplayCardNumber(), equalToIgnoringCase("XXXXXXXXXXXX6361"));
    }

    /**
     * Unit testing Get Cards for Exception behaviour
     *
     * @throws Exception
     */
    @Test
    public void testGetCardsInvalidPaymentProvider() throws Exception {

        User user = new User();
        user.setUserId("TestUser");
        user.setVarId("AU");
        user.setProgramId("b2s_qa_only");

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        //Passing invalid payment provider for Citi
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram(user, "TEST", CurrencyUnit.of("AUD")));

        CardsResponse cardsResponseMock = getCardsResponse();

        when(financeOptionsServiceFactory.getFinanceOptionsService(anyString())).thenReturn(financeOptionsService);
        when(financeOptionsService.getCards()).thenReturn(cardsResponseMock);

        mockMvc.perform(get("/getCards").session(session))
                .andExpect(status().is5xxServerError())
                .andReturn();

    }

    /**
     * Unit testing Get Finance Options
     *
     * @throws Exception
     */
    @Test
    public void testGetFinanceOptions() throws Exception {

        User user = new User();
        user.setUserId("AmexUser");
        user.setVarId("AmexAU");
        user.setProgramId("b2s_qa_only");

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram(user, FinanceOptionsPaymentProvider.AMEX.name(), CurrencyUnit.of("AUD")));

        final String fileName = "finance_getPlans_response.json";
        FinanceOptionsResponse financeOptionsResponseMock = getFinanceOptionsResponse(fileName);

        when(financeOptionsServiceFactory.getFinanceOptionsService(anyString())).thenReturn(financeOptionsService);
        when(financeOptionsService.getFinanceOptions(any(), any(), anyString())).thenReturn(financeOptionsResponseMock);

        MvcResult result = mockMvc.perform(get("/financeOption?amount=1500&cardId=234213").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        FinanceOptionsResponse financeOptionsResponseAssert = new ObjectMapper().readValue(content, FinanceOptionsResponse.class);

        assertThat(financeOptionsResponseAssert, notNullValue());
        assertThat(financeOptionsResponseAssert.getFinanceOptions(), notNullValue());
        assertThat(financeOptionsResponseAssert.getFinanceOptions(), hasSize(4));
        assertThat(financeOptionsResponseAssert.getFinanceOptions().stream().findFirst().get().getInstallmentAmount(), comparesEqualTo(102.78));
        assertThat(financeOptionsResponseAssert.getFinanceOptions().stream().findFirst().get().getEstablishmentFeeRate().intValue(), comparesEqualTo(4));

    }

    /**
     * Unit testing Get Finance Options without Card id in the request
     *
     * @throws Exception
     */
    @Test
    public void testGetFinanceOptionsWithoutCardId() throws Exception {

        User user = new User();
        user.setUserId("AmexUser");
        user.setVarId("AmexAU");
        user.setProgramId("b2s_qa_only");

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram(user, FinanceOptionsPaymentProvider.AMEX.name(), CurrencyUnit.of("AUD")));

        final String fileName = "finance_getPlans_SG_response.json";
        FinanceOptionsResponse financeOptionsResponseMock = getFinanceOptionsResponse(fileName);

        when(financeOptionsServiceFactory.getFinanceOptionsService(anyString())).thenReturn(financeOptionsService);
        when(financeOptionsService.getFinanceOptions(any(), any(), any())).thenReturn(financeOptionsResponseMock);

        MvcResult result = mockMvc.perform(get("/financeOption?amount=1500").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        FinanceOptionsResponse financeOptionsResponseAssert = new ObjectMapper().readValue(content, FinanceOptionsResponse.class);

        assertThat(financeOptionsResponseAssert, notNullValue());
        assertThat(financeOptionsResponseAssert.getFinanceOptions(), notNullValue());
        assertThat(financeOptionsResponseAssert.getFinanceOptions(), hasSize(3));
        //Asserting the last entry in the list
        long count = financeOptionsResponseAssert.getFinanceOptions().stream().count();
        assertThat(financeOptionsResponseAssert.getFinanceOptions().stream().skip(count - 1 ).findFirst().get().getMessageCode(), equalToIgnoringCase("finance.message.3.text"));

    }

    /**
     * Unit testing Get Finance Options with Invalid Payment Provider
     *
     * @throws Exception
     */
    @Test
    public void testGetFinanceOptionsInvalidPaymentProvider() throws Exception {

        User user = new User();
        user.setUserId("AmexUser");
        user.setVarId("AmexAU");
        user.setProgramId("b2s_qa_only");

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram(user, "TEST", CurrencyUnit.of("AUD")));

        final String fileName = "finance_getPlans_response.json";
        FinanceOptionsResponse financeOptionsResponseMock = getFinanceOptionsResponse(fileName);

        when(financeOptionsServiceFactory.getFinanceOptionsService(anyString())).thenReturn(financeOptionsService);
        when(financeOptionsService.getFinanceOptions(any(), any(), anyString())).thenReturn(financeOptionsResponseMock);

        mockMvc.perform(get("/financeOption?amount=1500&cardId=234213").session(session))
                .andExpect(status().is5xxServerError())
                .andReturn();



    }

    /**
     * Mocking CardsResponse object from json
     *
     * @return
     */
    public CardsResponse getCardsResponse() {

        CardsResponse cardsResponse = new CardsResponse();
        try {
            //Mocked getCards Response
            File jsonFile = ResourceUtils.getFile("classpath:json/finance_getCards_Response.json");
            jsonFile.setExecutable(false);
            jsonFile.setReadable(true);
            jsonFile.setWritable(false);
            //JSON to Object Mapper using Jackson
            cardsResponse = new ObjectMapper().readValue(jsonFile, CardsResponse.class);
        } catch (Exception e) {
            LOGGER.error("Exception " + e);
        }

        return cardsResponse;
    }

    /**
     * Mocking FinanceOptionsResponse object from json
     *
     * @return
     */
    public FinanceOptionsResponse getFinanceOptionsResponse(final String fileName) {

        FinanceOptionsResponse financeOptionsResponse = new FinanceOptionsResponse();
        try {
            //Mocked getCards Response
            File jsonFile = ResourceUtils.getFile("classpath:json/" + fileName);
            jsonFile.setExecutable(false);
            jsonFile.setReadable(true);
            jsonFile.setWritable(false);
            //JSON to Object Mapper using Jackson
            financeOptionsResponse = new ObjectMapper().readValue(jsonFile, FinanceOptionsResponse.class);
        } catch (Exception e) {
            LOGGER.error("Exception " + e);
        }

        return financeOptionsResponse;
    }


    /**
     * Set the Program object
     *
     * @param user
     * @param paymentProvider
     * @param currencyUnit
     * @return
     */
    private static Program getProgram (final User user, final String paymentProvider, final CurrencyUnit currencyUnit){

        final Program program=new Program();
        program.setVarId(user.getVarId());
        program.setProgramId(user.getProgramId());
        program.setTargetCurrency(currencyUnit);

        VarProgramRedemptionOption varProgramRedemptionOption = VarProgramRedemptionOption.builder()
                .withId(1)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.FINANCE.getPaymentOption())
                .withOrderBy(1)
                .withPaymentProvider(paymentProvider)
                .withActive(true)
                .build();

        List<VarProgramRedemptionOption> varProgramRedemptionOptions = new ArrayList<>();
        varProgramRedemptionOptions.add(varProgramRedemptionOption);
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>();
        redemptionOptions.put(PaymentOptions.FINANCE.getPaymentOption(), varProgramRedemptionOptions);
        program.setRedemptionOptions(redemptionOptions);
        return program;
    }


}
