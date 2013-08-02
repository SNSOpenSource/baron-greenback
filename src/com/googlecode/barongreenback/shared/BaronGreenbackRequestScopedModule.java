package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.parser.ParserFunctions;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.yadic.Container;

public interface BaronGreenbackRequestScopedModule extends Module {
    BaronGreenbackRequestScope addBaronGreenbackPerRequestObjects(BaronGreenbackRequestScope container);
}
