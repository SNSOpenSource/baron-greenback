package com.googlecode.barongreenback.crawler;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.Auditor;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.TypeMap;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerScope implements Container {
    private SimpleContainer container;

    private CrawlerScope(Container requestScope, PrintStream log, CrawlerHttpClient crawlerHttpHandler, CheckpointUpdater checkpointUpdater) {
        container = new SimpleContainer(requestScope);
        container.add(StagedJobExecutor.class);
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.addInstance(HttpHandler.class, crawlerHttpHandler);
        container.add(HttpClient.class, AuditHandler.class);
        container.add(FailureHandler.class);
        container.addInstance(AtomicInteger.class, new AtomicInteger(0));
        container.addInstance(CheckpointUpdater.class, checkpointUpdater);
        Containers.selfRegister(container);
    }

    public static CrawlerScope crawlerScope(Container requestContainer, PrintStream log, CrawlerHttpClient crawlerHttpHandler, CheckpointUpdater checkpointUpdater) {
        return new CrawlerScope(requestContainer, log, crawlerHttpHandler, checkpointUpdater);
    }

    @Override
    public <T> T get(Class<T> aClass) {
        return container.get(aClass);
    }

    @Override
    public <T> Callable<T> getActivator(Class<T> aClass) {
        return container.getActivator(aClass);
    }

    @Override
    public <T> Container add(Class<T> concrete) {
        return container.add(concrete);
    }

    @Override
    public <I, C extends I> Container add(Class<I> anInterface, Class<C> concrete) {
        return container.add(anInterface, concrete);
    }

    @Override
    public <I, C extends I> Container addInstance(Class<I> anInterface, C instance) {
        return container.addInstance(anInterface, instance);
    }

    @Override
    public <T, A extends Callable<T>> Container addActivator(Class<T> aClass, Class<A> activator) {
        return container.addActivator(aClass, activator);
    }

    @Override
    public <T> Container addActivator(Class<T> aClass, Callable<? extends T> activator) {
        return container.addActivator(aClass, activator);
    }

    @Override
    public <I, C extends I> Container decorate(Class<I> anInterface, Class<C> concrete) {
        return container.decorate(anInterface, concrete);
    }

    @Override
    public <I, C extends I> Container replace(Class<I> anInterface, Class<C> newConcrete) {
        return container.replace(anInterface, newConcrete);
    }

    @Override
    public TypeMap addType(Type type, Resolver<?> resolver) {
        return container.addType(type, resolver);
    }

    @Override
    public TypeMap addType(Type type, Class<? extends Resolver> resolverClass) {
        return container.addType(type, resolverClass);
    }

    @Override
    public TypeMap addType(Type type, Type concrete) {
        return container.addType(type, concrete);
    }

    @Override
    public <T> Resolver<T> getResolver(Type type) {
        return container.getResolver(type);
    }

    @Override
    public <T> Resolver<T> remove(Type type) {
        return container.remove(type);
    }

    @Override
    public boolean contains(Type type) {
        return container.contains(type);
    }

    @Override
    public TypeMap decorateType(Type anInterface, Type concrete) {
        return container.decorateType(anInterface, concrete);
    }

    @Override
    public Object resolve(Type type) throws Exception {
        return container.resolve(type);
    }

    @Override
    public <T> T create(Type type) throws Exception {
        return container.create(type);
    }
}