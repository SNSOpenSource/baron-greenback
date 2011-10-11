package com.googlecode.barongreenback.views;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;

public class View {
    private final Keyword name;
    private Sequence<Keyword> fields;

    public View(Keyword name) {
        this.name = name;
    }

    public static View view(Keyword name) {
        return new View(name);
    }

    public static Callable1<? super View,Sequence<Keyword>> asFields() {
        return new Callable1<View, Sequence<Keyword>>() {
            public Sequence<Keyword> call(View view) throws Exception {
                return view.fields();
            }
        };
    }

    public View fields(Keyword<?>... keywords) {
        return fields(Sequences.<Keyword>sequence(keywords));
    }

    public View fields(Sequence<Keyword> sequence) {
        this.fields = sequence;
        return this;
    }

    public Keyword name() {
        return name;
    }

    public Sequence<Keyword> fields() {
        return fields;
    }

    @Override
    public int hashCode() {
        return values().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof View && values().equals(((View) obj).values());
    }

    private Sequence values() {
        return fields.add(name());
    }
}
