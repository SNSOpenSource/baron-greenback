package com.googlecode.barongreenback;

import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class SearchModule implements ResourcesModule, ApplicationScopedModule, RequestScopedModule {
    public Module addResources(Resources resources) {
        resources.add(annotatedClass(SearchResource.class));
        return this;
    }

    public Module addPerApplicationObjects(Container container) {
        container.addInstance(Version.class, Version.LUCENE_33);
        container.addActivator(Directory.class, DirectoryActivator.class);
        container.addActivator(IndexWriter.class, IndexWriterActivator.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) {
        container.add(LuceneRecords.class);
        container.addActivator(Records.class, container.getActivator(LuceneRecords.class));
        container.add(Analyzer.class, StandardAnalyzer.class);
        container.addActivator(QueryParser.class, QueryParserActivator.class);
        return this;
    }
}
