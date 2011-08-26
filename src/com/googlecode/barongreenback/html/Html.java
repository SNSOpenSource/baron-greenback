package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLInputElement;

public class Html {
    private final Document document;

    public Html(String document) {
//        System.out.println(document);
        this.document = Xml.document(document);
    }

    public static Html html(Response response) throws Exception {
        return new Html(new String(response.bytes(), "UTF-8"));
    }

    public String title() {
        return Xml.selectContents(document, "/html/head/title");
    }

    public Form form(String xpath) {
        return new Form(Xml.selectElement(document, xpath));
    }

    public Input input(String xpath) {
        return new Input(Xml.selectElement(document, xpath));
    }

    public Select select(String xpath) {
        return new Select(Xml.selectElement(document, xpath));
    }

    public Checkbox checkbox(String xpath) {
        return new Checkbox(Xml.selectElement(document, xpath));
    }

    public String selectContent(String xpath) {
        return Xml.selectContents(document, xpath);
    }

    public Link link(String xpath) {
        return new Link(Xml.selectElement(document, xpath));
    }
}
