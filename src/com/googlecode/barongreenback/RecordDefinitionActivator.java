package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.FormParameters;

import java.util.concurrent.Callable;

public class RecordDefinitionActivator implements Callable<RecordDefinition>{
    private final FormParameters form;

    public RecordDefinitionActivator(FormParameters form) {
        this.form = form;
    }

    public RecordDefinition call() throws Exception {
        return new RecordDefinitionExtractor(form).extract();
    }
}
