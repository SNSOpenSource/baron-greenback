package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.totallylazy.Files;
import org.junit.After;
import org.junit.Test;

import java.io.File;

public class PartitionedIndexActivatorTest {
    private static final String indexLocation = "/tmp/" +PartitionedIndexActivatorTest.class.getSimpleName() + "-index";

    @After
    public void cleanup() {
        Files.delete(new File(indexLocation));
    }

    @Test
    public void canCreateInMemoryIndex() throws Exception {
        new PartitionedIndexActivator(new PersistenceUri("lucene:mem")).call();
    }

    @Test
    public void canCreateNioIndex() throws Exception {
        new PartitionedIndexActivator(new PersistenceUri("lucene:nio://" + indexLocation)).call();
    }

    @Test
    public void canCreateFileIndex() throws Exception {
        new PartitionedIndexActivator(new PersistenceUri("lucene:file://" + indexLocation)).call();
    }
}
