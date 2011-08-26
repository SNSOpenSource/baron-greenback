package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.records.xml.Xml;
import org.w3c.dom.Node;

public class Checkbox extends Input{
    public Checkbox(Node input) {
        super(input);
    }

    public boolean checked() {
        return Xml.selectContents(input, "@checked").equals("checked");
    }
}
