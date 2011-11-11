package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.FormParameters;

import java.util.concurrent.Callable;

public class ModelActivator implements Callable<Model> {
    private final FormParameters formParameters;

    public ModelActivator(FormParameters formParameters) {
        this.formParameters = formParameters;
    }

    public Model call() throws Exception {
        return ParametersToModel.modelOf(formParameters);
    }
}
