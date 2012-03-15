package com.googlecode.barongreenback.persistence.lucene;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringPrintStream extends PrintStream{
    public StringPrintStream() {
        super(new ByteArrayOutputStream());
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
