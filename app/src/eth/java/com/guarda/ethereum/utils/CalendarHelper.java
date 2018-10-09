package com.guarda.ethereum.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarHelper {

    public static String parseDateToddMMyyyy(long time) {
        String outputPattern = "dd.MM.yyyy";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());

        Date date = new Date();

        date.setTime(time);

        return outputFormat.format(date);
    }

    public static long jodaTimeToMilliseconds(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

}
