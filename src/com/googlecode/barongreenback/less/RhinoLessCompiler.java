package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.io.IOException;
import java.io.InputStreamReader;

public class RhinoLessCompiler implements LessCompiler {
    private Scriptable scope;
    private Function parseLess;

    synchronized private void init() throws IOException {
        if (scope == null) {
            Global global = new Global();
            Context context = Context.enter();
            context.setOptimizationLevel(9);
            global.init(context);
            scope = global;
            context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("less-rhino-1.3.1.js")), "less", 1, null);
            context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream("less.wrapper.js")), "wrapper", 1, null);
            parseLess = (Function) scope.get("parseLess", scope);
        }
    }

    public String compile(String less, LessCssHandler.Loader loader) throws IOException {
        init();
        Global global = new Global();
        Context context = Context.enter();
        context.setOptimizationLevel(9);
        global.init(context);
        context.setOptimizationLevel(-1);
        return parseLess.call(context, scope, global, new Object[]{less, Context.javaToJS(loader, scope)}).toString();
    }
}
