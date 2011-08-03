package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;

import java.net.URL;

public class XmlSource {
    private final URL url;
    private final Keyword<Object> element;
    private final Sequence<Keyword> fields;

    public XmlSource(URL url, Keyword<Object> element, Keyword<?>... fields) {
        this(url, element, Sequences.<Keyword>sequence(fields));
    }

    public XmlSource(URL url, Keyword<Object> element, Sequence<Keyword> fields) {
        this.url = url;
        this.element = element;
        this.fields = fields;
    }

    public URL getUrl() {
        return url;
    }

    public Keyword<Object> getElement() {
        return element;
    }

    public Sequence<Keyword> getFields() {
        return fields;
    }
}
