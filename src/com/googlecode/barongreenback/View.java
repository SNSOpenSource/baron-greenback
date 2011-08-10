package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.ImmutableKeyword;
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

    public View withFields(Keyword<?>... keywords) {
        return withFields(Sequences.<Keyword>sequence(keywords));
    }

    public View withFields(Sequence<Keyword> sequence) {
        this.fields = sequence;
        return this;
    }

    public Keyword name() {
        return name;
    }

    public Sequence<Keyword> getFields() {
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
