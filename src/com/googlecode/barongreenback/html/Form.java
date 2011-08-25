package com.googlecode.barongreenback.html;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import org.w3c.dom.Node;

public class Form {
    private final Node form;

    public Form(Node form) {
        this.form = form;
    }

    public Request submit(String xpath) {
        String action = Xml.selectContents(form, "@action");
        String method = Xml.selectContents(form, "@method");
        Pair<String, String> submitButton = nameValuePairs(xpath).head();
        Sequence<Pair<String, String>> textInputParams = nameValuePairs("//input[@type='text']");

        return textInputParams.add(submitButton).fold(new RequestBuilder(method, action), addFormParams()).build();
    }

    private Sequence<Pair<String, String>> nameValuePairs(String xpath) {
        return Xml.selectNodes(form, xpath).map(toNameAndValue());
    }

    private Callable2<RequestBuilder, Pair<String, String>, RequestBuilder> addFormParams() {
        return new Callable2<RequestBuilder, Pair<String, String>, RequestBuilder>() {
            public RequestBuilder call(RequestBuilder requestBuilder, Pair<String, String> pair) throws Exception {
                return requestBuilder.withForm(pair.first(), pair.second());
            }
        };
    }

    private Callable1<Node, Pair<String, String>> toNameAndValue() {
        return new Callable1<Node, Pair<String, String>>() {
            public Pair<String, String> call(Node node) throws Exception {
                return Pair.pair(Xml.selectContents(node, "@name"), Xml.selectContents(node, "@value"));
            }
        };
    }
}
