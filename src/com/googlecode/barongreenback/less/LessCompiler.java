package com.googlecode.barongreenback.less;

import java.io.IOException;

public interface LessCompiler {
    String compile(String less, LessCssHandler.Loader loader) throws IOException;
}
