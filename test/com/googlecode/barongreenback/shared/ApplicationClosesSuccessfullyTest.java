package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.lucene.LuceneModule;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.yadic.Container;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class ApplicationClosesSuccessfullyTest {

    @Test
    public void applicationClosesWhenUsingRamDirectory() throws IOException {
        System.setProperty("baron-greenback.lucene.index.type", "RAM");

        Application testApplication = new RestApplication(new ApplicationScopedModule() {
            public Module addPerApplicationObjects(Container container) throws Exception {
                container.addInstance(Properties.class, System.getProperties());
                return this;
            }
        }, new LuceneModule());

        IndexWriter indexWriter = getObjectsFromContainerToForceInstantiation(testApplication);

        addAnUnCommitedDocument(indexWriter);

        testApplication.close();
    }

    private void addAnUnCommitedDocument(IndexWriter indexWriter) throws IOException {
        indexWriter.addDocument(new Document());
    }

    private IndexWriter getObjectsFromContainerToForceInstantiation(Application testApplication) {
        testApplication.applicationScope().get(Directory.class);
        return testApplication.applicationScope().get(IndexWriter.class);
    }
}
