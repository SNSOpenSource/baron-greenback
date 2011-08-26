package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.records.xml.Xml;
import org.w3c.dom.Node;

public class Input implements NameValue{
    public static final String NAME = "@name";
    public static final String VALUE = "@value";
    protected final Node input;

    public Input(Node input) {
        this.input = input;
    }

    public String value() {
        return Xml.selectContents(input, VALUE);
    }

    public Input value(String value) {
        Xml.selectNodes(input, VALUE).head().setNodeValue(value);
        return this;
    }

    public String name() {
        return Xml.selectContents(input, NAME);
    }
}
