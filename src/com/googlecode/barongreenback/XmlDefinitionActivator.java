package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.FormParameters;

import java.util.concurrent.Callable;

public class XmlDefinitionActivator implements Callable<XmlDefinition>{
    private final FormParameters form;

    public XmlDefinitionActivator(FormParameters form) {
        this.form = form;
    }

    public XmlDefinition call() throws Exception {
        return XmlDefinitionExtractor.extractFrom(form);
    }
}
