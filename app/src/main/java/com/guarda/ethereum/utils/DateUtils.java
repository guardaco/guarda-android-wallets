package com.guarda.ethereum.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by SV on 18.08.2017.
 */

public final class DateUtils {

    private DateUtils() {
    }

    public static Date stringToDate(String dateStr){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date;
        try {
            date = format.parse(dateStr);
        } catch (ParseException | NullPointerException ignore) {
            date = new Date(Calendar.getInstance().getTimeInMillis());
        }
        return date;
    }

}
