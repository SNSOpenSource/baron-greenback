package com.sky.sns.barongreenback.shared;

import com.googlecode.utterlyidle.Request;

import java.util.concurrent.Callable;

import static com.googlecode.utterlyidle.Requests.query;

public class AdvancedModeActivator implements Callable<AdvancedMode>{
    private final Request request;

    public AdvancedModeActivator(Request request) {
        this.request = request;
    }

    public AdvancedMode call() throws Exception {
        if(query(request).contains("advanced")){
            return AdvancedMode.Enable;
        }
        return AdvancedMode.Disable;
    }
}
