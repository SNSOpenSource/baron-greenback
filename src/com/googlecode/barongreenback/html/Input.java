package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.records.xml.Xml;
import org.w3c.dom.Node;

public class Input {
    private final Node input;

    public Input(Node input) {
        this.input = input;
    }

    public Input value(String value) {
        Xml.selectNodes(input, "@value").head().setNodeValue(value);
        return this;
    }
}
