package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Link implements NameValue{
    private final Element link;

    public Link(Element link) {
        this.link = link;
    }

    public Request click() {
        return RequestBuilder.get(value()).build();
    }

    public String value() {
        return Xml.selectContents(link, "@href");
    }

    public String name() {
        return link.getTextContent();
    }
}
