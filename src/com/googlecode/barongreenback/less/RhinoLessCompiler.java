package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStreamReader;

public class RhinoLessCompiler implements LessCompiler {
    private Scriptable scope;
    private Function parseLess;

    private void init() throws IOException {
        if (scope == null) {
            Context context = Context.enter();
            context.setOptimizationLevel(9);
            scope = context.initStandardObjects();
            context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("less-rhino-1.1.3.js")), "less", 1, null);
            context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("less.wrapper.js")), "wrapper", 1, null);
            parseLess = (Function) scope.get("parseLess", scope);
        }
    }

    public String compile(String less, Callable1<String, String> loader) throws IOException {
        init();
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        return parseLess.call(context, scope, scope, new Object[]{less, Context.javaToJS(loader, scope)}).toString();
    }
}
