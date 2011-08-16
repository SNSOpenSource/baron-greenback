package com.googlecode.barongreenback;


import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.util.concurrent.Callable;

public class ModelRendererActivator implements Callable<ModelRenderer>{
    private final HierarchicalPath path;

    public ModelRendererActivator(Request request) {
        path = request.url().path();
    }

    public ModelRenderer call() throws Exception {
        return new ModelRenderer(path.file());
    }
}
