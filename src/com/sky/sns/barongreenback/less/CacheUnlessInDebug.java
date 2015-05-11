package com.sky.sns.barongreenback.less;

import static com.googlecode.totallylazy.Debug.inDebug;

public class CacheUnlessInDebug implements LessCssConfig{
    public boolean useCache() {
        return !inDebug();
    }
}
