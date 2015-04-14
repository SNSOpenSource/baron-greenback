package com.googlecode.barongreenback.views;

import com.googlecode.totallylazy.Value;

public class CurrentView implements Value<String> {
    private String value;

    public CurrentView(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
