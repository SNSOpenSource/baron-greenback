package com.googlecode.barongreenback.search;

import com.googlecode.lazyrecords.parser.ParserDateConverter;
import com.googlecode.totallylazy.time.DateConverter;
import com.googlecode.totallylazy.time.DateFormatConverter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.googlecode.totallylazy.time.DateFormatConverter.defaultConverter;

public class ParserUkDateConverter extends ParserDateConverter {

    public ParserUkDateConverter(DateConverter dateConverter) {
        super(dateConverter);
    }

    public ParserUkDateConverter() {
        this(new DateFormatConverter(defaultConverter().formats()
                .append(ukShortDateTimeFormat())
                .append(ukShortDateFormat())
                .append(dateTimeFormat())
                .append(dateFormat())));
    }

    public static SimpleDateFormat ukShortDateTimeFormat() {
        return simpleDateFormatInstanceFor("dd/MM/yy HH:mm:ss");
    }

    public static SimpleDateFormat ukShortDateFormat() {
        return simpleDateFormatInstanceFor("dd/MM/yy");
    }

    public static SimpleDateFormat dateTimeFormat() {
        return simpleDateFormatInstanceFor("yy/MM/dd HH:mm:ss");
    }

    public static SimpleDateFormat dateFormat() {
        return simpleDateFormatInstanceFor("yy/MM/dd");
    }

    public static SimpleDateFormat simpleDateFormatInstanceFor(final String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }
}