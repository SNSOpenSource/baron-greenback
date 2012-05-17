package com.googlecode.barongreenback.persistence;

import java.io.File;

public interface Persistence {
    void deleteAll() throws Exception;

    void backup(File destination) throws Exception;
}
