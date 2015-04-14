package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.totallylazy.Files;
import com.googlecode.yadic.Container;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.Test;

import static com.googlecode.yadic.Containers.container;
import static com.googlecode.yadic.Containers.selfRegister;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

public class LuceneModuleTest {
    @Test
    public void canAddACustomDirectory() throws Exception {
        Container container = selfRegister(container()).add(BaronGreenbackApplicationScope.class);
        Container persistenceContainer = container.get(BaronGreenbackApplicationScope.class).value();
        Directory customDirectory = new MMapDirectory(Files.temporaryDirectory());

        persistenceContainer.addInstance(Directory.class, customDirectory);

        new LuceneModule().addPerApplicationObjects(container);

        assertThat(persistenceContainer.get(Directory.class), sameInstance(customDirectory));
    }
}
