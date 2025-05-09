package com.b2r.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * To diagnose problems in datetime formatting
 */
public class TimeZoneTest {

    public static final int MILLIS_PER_HOUR = 1000 * 60 * 60;

    private void prln(Object x) {
        System.out.println(x);
    }

    @Test
    public void testTimeZone() throws ParseException {
        final TimeZone tzNY = TimeZone.getTimeZone("America/New_York");
        final TimeZone tzEST = TimeZone.getTimeZone("EST");

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
        df.setTimeZone(tzNY);

        Date summerDate = df.parse("Aug 12, 2013");
        Date winterDate = df.parse("Dec 25, 2013");

        //In summer, New York time is different from EST (because of day light saving)
        assertEquals(-4, tzNY.getOffset (summerDate.getTime()) / MILLIS_PER_HOUR);
        assertEquals(-5, tzNY.getOffset (winterDate.getTime()) / MILLIS_PER_HOUR);
        assertEquals(-5, tzEST.getOffset(summerDate.getTime()) / MILLIS_PER_HOUR);
        assertEquals(-5, tzEST.getOffset(winterDate.getTime()) / MILLIS_PER_HOUR);

        final DateTimeFormatter NY_DATE_FORMAT =
                DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy HH:mm:ss")
                        .withLocale(Locale.ENGLISH)
                        .withZone(DateTimeZone.forTimeZone(tzNY));

        assertEquals("Monday, August 12, 2013 00:00:00",NY_DATE_FORMAT.print(summerDate.getTime()));
        assertEquals("Wednesday, December 25, 2013 00:00:00",NY_DATE_FORMAT.print(winterDate.getTime()));
    }

    @Test
    public void testDstChangeOver() {
        DateTimeFormatter dfParse = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss")
                .withZone(DateTimeZone.forID("America/New_York"))
                .withLocale(Locale.ENGLISH);

        DateTimeFormatter dfPrint = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy HH:mm:ss ")
                .withZone(DateTimeZone.forID("America/New_York"))
                .withLocale(Locale.ENGLISH);

        //just before change over
        DateTime dt = dfParse.parseDateTime("11/03/2013 01:59:00");
        assertEquals("Sunday, November 03, 2013 01:59:00 ", dt.toString(dfPrint));
        //just after change over
        assertEquals("Sunday, November 03, 2013 01:00:00 ", dt.plusMinutes(1).toString(dfPrint));
    }
}
