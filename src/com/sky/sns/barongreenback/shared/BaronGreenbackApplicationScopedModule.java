package com.sky.sns.barongreenback.shared;

import com.googlecode.utterlyidle.modules.Module;

public interface BaronGreenbackApplicationScopedModule extends Module {
    BaronGreenbackApplicationScope addBaronGreenbackPerApplicationObjects(BaronGreenbackApplicationScope applicationScope);
}
