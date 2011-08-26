package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.records.xml.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.googlecode.totallylazy.records.xml.Xml.selectContents;

public class Select implements NameValue {
    public static final String SELECTED = "selected";
    private final Element select;

    public Select(Node select) {
        this.select = (Element) select;
    }

    public String value() {
        String selected = selectContents(select, "option[@selected='selected']/@value");
        return selected.equals(Strings.EMPTY) ? selectContents(select, "option[1]/@value") : selected;
    }

    public Select value(String value){
        for (Element element : Xml.selectNodes(select, "option").safeCast(Element.class)) {
            element.removeAttribute(SELECTED);
        }
        Element element = Xml.selectNodes(select, "option[@value='" + value + "']").safeCast(Element.class).head();
        element.setAttribute(SELECTED, SELECTED);
        return this;
    }

    public String name() {
        return selectContents(select, "@name");
    }
}
