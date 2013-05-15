package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Renderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateRenderer implements Renderer<Date> {
    public String render(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }

    public static DateRenderer toLexicalDateTime() {
        return new DateRenderer();
    }
}
