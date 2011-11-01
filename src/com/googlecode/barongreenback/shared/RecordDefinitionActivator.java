package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.FormParameters;

import java.util.concurrent.Callable;

public class RecordDefinitionActivator implements Callable<RecordDefinition>{
    private final FormParameters form;

    public RecordDefinitionActivator(FormParameters form) {
        this.form = form;
    }

    public RecordDefinition call() throws Exception {
        Model model = ParametersToModel.modelOf(form);
        Model record = model.get("record", Model.class);
        return RecordDefinition.convert(record);
    }
}
