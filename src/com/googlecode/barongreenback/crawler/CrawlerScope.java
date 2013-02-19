package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failures.FailureHandler;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.handlers.Auditor;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.TypeMap;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerScope implements Container {
    private SimpleContainer container;

    private CrawlerScope(Container requestScope, CheckpointUpdater checkpointUpdater) {
        container = new SimpleContainer(requestScope);
        container.add(CountLatch.class);
        container.add(StagedJobExecutor.class);
        container.add(Auditor.class, PrintAuditor.class);
        container.add(FailureHandler.class);
        container.addInstance(AtomicInteger.class, new AtomicInteger(0));
        container.addInstance(CheckpointUpdater.class, checkpointUpdater);
        Containers.selfRegister(container);
    }

    public static CrawlerScope crawlerScope(Container requestScope, CheckpointUpdater checkpointUpdater) {
        return new CrawlerScope(requestScope, checkpointUpdater);
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
    public <T> Option<Resolver<T>> removeOption(Type type) {
        return container.removeOption(type);
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
