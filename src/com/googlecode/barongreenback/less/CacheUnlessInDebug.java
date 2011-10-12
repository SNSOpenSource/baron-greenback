package com.googlecode.barongreenback.less;

import static com.googlecode.barongreenback.less.Debug.inDebug;

public class CacheUnlessInDebug implements LessCssConfig{
    public boolean useCache() {
        return !inDebug();
    }
}
