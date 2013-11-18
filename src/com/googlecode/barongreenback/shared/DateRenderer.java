package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Renderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateRenderer implements Renderer<Date> {
    public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private final String dateFormat;

    public DateRenderer(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String render(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }

    public static DateRenderer toLexicalDateTime() {
        return new DateRenderer(DEFAULT_DATE_FORMAT);
    }

    public static DateRenderer toLexicalDateTime(String dateFormat) {
        return new DateRenderer(dateFormat);
    }
}
