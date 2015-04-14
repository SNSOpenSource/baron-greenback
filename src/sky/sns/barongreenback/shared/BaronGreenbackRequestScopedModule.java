package com.googlecode.barongreenback.shared;

import com.googlecode.utterlyidle.modules.Module;

public interface BaronGreenbackRequestScopedModule extends Module {
    BaronGreenbackRequestScope addBaronGreenbackPerRequestObjects(BaronGreenbackRequestScope container);
}
