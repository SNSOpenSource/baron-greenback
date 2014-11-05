package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.crawler.CompositeCrawler;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.util.List;

import static com.googlecode.barongreenback.shared.RecordDefinition.ExpensiveModelToKeyword.expensiveModelToKeyword;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.Strings.isEmpty;
import static java.lang.Boolean.TRUE;

public class RecordDefinition {
    public static final Keyword<RecordDefinition> RECORD_DEFINITION = keyword(RecordDefinition.class.getName(), RecordDefinition.class);
    public static final Keyword<Boolean> SUBFEED = keyword("subfeed", Boolean.class);
    public static final Keyword<String> PRIORITY = keyword("priority", String.class);
    public static final Predicate<Keyword<?>> UNIQUE_FILTER = Predicates.and(where(metadata(Keywords.unique), is(notNullValue())), where(metadata(Keywords.unique), is(true)));
    private final Definition definition;

    public RecordDefinition(Definition definition) {
        this.definition = definition;
    }

    public RecordDefinition recordDefinition(Definition definition) {
        return new RecordDefinition(definition);
    }

    public Definition definition() {
        return definition;
    }

    public static Sequence<Keyword<?>> uniqueFields(RecordDefinition recordDefinition) {
        return uniqueFields(recordDefinition.definition());
    }

    public static Sequence<Keyword<?>> uniqueFields(Definition definition) {
        return allFields(definition).filter(UNIQUE_FILTER);
    }

    public static Sequence<Keyword<?>> allFields(RecordDefinition recordDefinition) {
        return allFields(recordDefinition.definition());
    }

    public static Sequence<Keyword<?>> allFields(Definition definition) {
        return definition.fields().flatMap(allFields());
    }

    public static Sequence<Keyword<?>> allFields(Sequence<Keyword<?>> keywords) {
        return keywords.flatMap(allFields());
    }

    public static Callable1<Keyword<?>, Sequence<Keyword<?>>> allFields() {
        return new Callable1<Keyword<?>, Sequence<Keyword<?>>>() {
            public Sequence<Keyword<?>> call(Keyword<?> keyword) throws Exception {
                RecordDefinition recordDefinition = keyword.metadata().get(RecordDefinition.RECORD_DEFINITION);
                if (recordDefinition != null) {
                    return Sequences.<Keyword<?>>one(keyword).join(allFields(recordDefinition));
                }
                return Sequences.<Keyword<?>>one(keyword);
            }
        };
    }

    public Model toModel() {
        return toModel(definition());
    }

    public static Model toModel(final Definition definition) {
        return recordDefinition(definition.name(),
                definition.fields().map(asKeywordDefinition()).toArray(Model.class));
    }

    private static Callable1<Keyword, Model> asKeywordDefinition() {
        return new Callable1<Keyword, Model>() {
            public Model call(Keyword keyword) throws Exception {
                return keywordDefinition(name(keyword), alias(keyword), group(keyword), type(keyword), unique(keyword),
                        visible(keyword), subfeed(keyword), recordDefinition(keyword), checkpoint(keyword), priority(keyword), showFacet(keyword), facetEntries(keyword));
            }
        };
    }

    public static String priority(Keyword keyword) {
        return keyword.metadata().get(PRIORITY);
    }

    public static Option<Model> recordDefinition(Keyword keyword) {
        return option(keyword.metadata().get(RecordDefinition.RECORD_DEFINITION)).map(new Callable1<RecordDefinition, Model>() {
            public Model call(RecordDefinition recordDefinition) throws Exception {
                return recordDefinition.toModel();
            }
        });
    }

    private static boolean visible(Keyword keyword) {
        return booleanValueOf(keyword, ViewsRepository.VISIBLE);
    }

    private static boolean subfeed(Keyword keyword) {
        return booleanValueOf(keyword, RecordDefinition.SUBFEED);
    }

    private static boolean checkpoint(Keyword keyword) {
        return booleanValueOf(keyword, CompositeCrawler.CHECKPOINT);
    }

    private static boolean booleanValueOf(Keyword keyword, Keyword<Boolean> metaKeyword) {
        return TRUE.equals(keyword.metadata().get(metaKeyword));
    }

    private static boolean unique(Keyword keyword) {
        return booleanValueOf(keyword, Keywords.unique);
    }

    private static String name(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return ((AliasedKeyword) keyword).source().name();
        }
        return keyword.name();
    }

    private static String alias(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return keyword.name();
        }
        return "";
    }

    private static String group(Keyword keyword) {
        return keyword.metadata().get(ViewsRepository.GROUP);
    }

    private static Boolean showFacet(Keyword keyword) {
        return booleanValueOf(keyword, ViewsRepository.SHOW_FACET);
    }

    private static Number facetEntries(Keyword keyword) {
        final Number facetEntries = keyword.metadata().get(ViewsRepository.FACET_ENTRIES);
        return facetEntries == null ? 0 : facetEntries;
    }

    private static String type(Keyword keyword) {
        return keyword.forClass().getName();
    }

    public static Model recordDefinition(String recordName, Model... fields) {
        return model().add("name", recordName).add("keywords", Sequences.sequence(fields).toList());
    }

    public static Model keywordDefinition(String name, String alias, String group, String type, boolean unique, boolean visible, boolean subfeed, Option<Model> recordDefinition, boolean checkpoint, String priority, boolean showFacet, Number facetEntries) {
        return model().
                add("name", name).
                add("alias", alias).
                add(ViewsRepository.GROUP.name(), group).
                add("type", type).
                add(Keywords.unique.name(), unique).
                add(ViewsRepository.VISIBLE.name(), visible).
                add(RecordDefinition.SUBFEED.name(), subfeed).
                add("record", subfeed ? recordDefinition.getOrNull() : null).
                add(CompositeCrawler.CHECKPOINT.name(), checkpoint).
                add(PRIORITY.name(), priority).
                add(ViewsRepository.SHOW_FACET.name(), showFacet).
                add(ViewsRepository.FACET_ENTRIES.name(), facetEntries);
    }

    public static RecordDefinition convert(Model model) {
        if (model == null) {
            return null;
        }
        return new RecordDefinition(convertToDefinition(model));
    }

    public static Definition convertToDefinition(Model model) {
        return convertToDefinition(model.get("name", String.class), model.getValues("keywords", Model.class));
    }

    public static Definition convertToDefinition(String name, List<Model> keywords) {
        return Definition.constructors.definition(name, toKeywords(keywords));
    }

    public static Callable1<? super Model, Sequence<Keyword<?>>> asKeywords() {
        return new Callable1<Model, Sequence<Keyword<?>>>() {
            public Sequence<Keyword<?>> call(Model model) throws Exception {
                return toKeywords(model);
            }
        };
    }

    public static Sequence<Keyword<?>> toKeywords(Model model) {
        return toKeywords(model.getValues("keywords", Model.class));
    }


    public static Sequence<Keyword<?>> toKeywords(List<Model> keywords) {
        return sequence(keywords).filter(where(value("name", String.class), is(not(empty())))).map(expensiveModelToKeyword()).realise();
    }

    public static class ExpensiveModelToKeyword implements Callable1<Model, Keyword<?>> {
        private ExpensiveModelToKeyword() {
        }

        public static ExpensiveModelToKeyword expensiveModelToKeyword() {
            return new ExpensiveModelToKeyword();
        }

        public Keyword<?> call(Model model) throws Exception {
            Keyword<?> keyword = keyword(model.get("name", String.class), Class.forName(model.get("type", String.class)));

            String alias = model.get("alias", String.class);
            if (!isEmpty(alias)) {
                keyword = ((ImmutableKeyword) keyword).as((Keyword) keyword(alias, keyword.forClass()));
            }
            return keyword.metadata(Record.constructors.record().
                    set(Keywords.unique, model.get(Keywords.unique.name(), Keywords.unique.forClass())).
                    set(ViewsRepository.VISIBLE, model.get(ViewsRepository.VISIBLE.name(), ViewsRepository.VISIBLE.forClass())).
                    set(ViewsRepository.GROUP, model.get(ViewsRepository.GROUP.name(), ViewsRepository.GROUP.forClass())).
                    set(ViewsRepository.SHOW_FACET, model.get(ViewsRepository.SHOW_FACET.name(), ViewsRepository.SHOW_FACET.forClass())).
                    set(ViewsRepository.FACET_ENTRIES, model.get(ViewsRepository.FACET_ENTRIES.name(), ViewsRepository.FACET_ENTRIES.forClass())).
                    set(CompositeCrawler.CHECKPOINT, model.get(CompositeCrawler.CHECKPOINT.name(), CompositeCrawler.CHECKPOINT.forClass())).
                    set(RecordDefinition.SUBFEED, model.get(RecordDefinition.SUBFEED.name(), RecordDefinition.SUBFEED.forClass())).
                    set(RecordDefinition.PRIORITY, model.get(RecordDefinition.PRIORITY.name(), RecordDefinition.PRIORITY.forClass())).
                    set(RecordDefinition.RECORD_DEFINITION, convert(model.get("record", Model.class))));
        }
    }


}
