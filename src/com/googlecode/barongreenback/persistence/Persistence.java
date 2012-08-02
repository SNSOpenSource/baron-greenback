package com.googlecode.barongreenback.persistence;

import java.io.File;

public interface Persistence {
    void delete() throws Exception;

    void backup(File destination) throws Exception;

    void restore(File file) throws Exception;
}
