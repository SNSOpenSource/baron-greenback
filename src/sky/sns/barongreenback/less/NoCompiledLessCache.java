package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.None.none;

public enum NoCompiledLessCache implements CompiledLessCache {
    instance;

    @Override
    public Option<CompiledLess> get(String key) {
        return none(CompiledLess.class);
    }

    @Override
    public boolean put(String key, CompiledLess result) {
        return false;
    }
}
