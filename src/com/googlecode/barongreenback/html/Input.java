package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.records.xml.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.googlecode.totallylazy.records.xml.Xml.selectContents;
import static com.googlecode.totallylazy.records.xml.Xml.selectElement;
import static com.googlecode.totallylazy.records.xml.Xml.selectNode;

public class Input implements NameValue{
    public static final String NAME = "@name";
    public static final String VALUE = "@value";
    protected final Element input;

    public Input(Element input) {
        this.input = input;
    }

    public String value() {
        return selectContents(input, VALUE);
    }

    public Input value(String value) {
        selectNode(input, VALUE).setTextContent(value);
        return this;
    }

    public String name() {
        return selectContents(input, NAME);
    }
}
