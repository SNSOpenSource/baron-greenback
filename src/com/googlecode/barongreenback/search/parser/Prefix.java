package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Strings;

import static java.lang.String.format;

public enum Prefix {
    None,
    Plus,
    Minus;

    public static Prefix parse(String prefix) {
        if(Strings.isEmpty(prefix)){
            return None;
        }
        if(prefix.charAt(0) == '+'){
            return Plus;
        }
        if(prefix.charAt(0) == '-'){
            return Minus;
        }
        throw new UnsupportedOperationException(format("Unknown prefix '%s'", prefix));
    }
}
