package com.b2s.rewards.apple.util;

import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by vkrishnan on 4/11/2019.
 */
@Component
public class ShipmentQuoteUtil {
    /*
   *S-02472:Persist Ship Quote as Date
   *
   *@param days  : Shipping timeframe from var_program_message
   *@param date  : Current Date
   *
   * @return shipmentQuoteDate
   */
    public String getShipmentQuoteDate(final String days,final LocalDate date){
        String shipmentDate = null;
        int workingDays = 0;
        if(StringUtils.isNotBlank(days)){
            workingDays =calculateDays(days);
        }
        if(workingDays!=0){
            LocalDate quoteDate = addWorkingDays(date,workingDays);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstants.SHIPMENT_DATE_FORMAT);
            shipmentDate = quoteDate.format(formatter);
        }
        return  shipmentDate;
    }

    private int calculateDays(final String value){
        return value.contains(CommonConstants.SHIPMENT_IN_WEEKS)?
            7* NumberUtils.toInt(StringUtils.substringBefore(value," ")):
            NumberUtils.toInt(StringUtils.substringBefore(value," "));
    }

    private LocalDate addWorkingDays(LocalDate date,int businessDays) {
        for (int i = 0; i < Math.abs(businessDays); i++) {
            date = nextWorkingDay(date, 1);
        }
        return date;
    }

    private LocalDate nextWorkingDay(LocalDate date, int step) {
        do {
            date = date.plusDays(step);
        } while (isWeekend(date));
        return date;
    }

    private boolean isWeekend(LocalDate date){
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
