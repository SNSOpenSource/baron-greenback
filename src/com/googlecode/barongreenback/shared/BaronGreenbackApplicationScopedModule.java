package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.parser.ParserParameters;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.yadic.Container;

public interface BaronGreenbackApplicationScopedModule extends Module {
    BaronGreenbackApplicationScope addBaronGreenbackPerApplicationObjects(BaronGreenbackApplicationScope applicationScope);
}
