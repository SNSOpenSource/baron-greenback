package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callers;
import org.lesscss.LessException;
import org.lesscss.LessResolver;
import org.lesscss.LessSource;

import java.io.IOException;

public class OfficialLessCompiler implements LessCompiler {

    private final org.lesscss.LessCompiler compiler = new org.lesscss.LessCompiler();
    @Override
    public String compile(String less, LessCssHandler.Loader loader) throws IOException {
        try {
            compiler.init();
            return compiler.compile(new LessSource(loader.uri().path(), new LoaderLessResolver(loader)));
        } catch (LessException e) {
            throw new RuntimeException(e);
        }
    }


    public static class LoaderLessResolver implements LessResolver {

        private Callable1<String, String> resolver;

        public LoaderLessResolver(Callable1<String, String> resolver) {
            this.resolver = resolver;
        }

        @Override
        public boolean exists(String s) {
            return true;
        }

        @Override
        public String resolve(String s) throws IOException {
                return Callers.call(resolver, s);
        }

        @Override
        public long getLastModified(String s) {
            return 0;
        }

        @Override
        public LessResolver resolveImport(String s) {
            return this;
        }
    }
}
