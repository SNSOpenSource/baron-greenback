package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.lazyrecords.parser.StandardParser;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequences;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PrimaryViewShortcutPolicyTest {

    private PrimaryViewShortcutPolicy policy;
    private RecordsService recordsService;
    private RecordsModelRepository modelRepository;
    private Keyword<String> keyword = Keyword.constructors.keyword("foo", String.class);
    private BaronGreenbackRecords records = BaronGreenbackRecords.records(new MemoryRecords(new StringMappings().add(Model.class, new ModelMapping())));

    @Before
    public void createPolicy() {
        modelRepository = new RecordsModelRepository(records);
        recordsService = new RecordsService(records, modelRepository, new PredicateBuilder(new StandardParser()));

        policy = new PrimaryViewShortcutPolicy(recordsService, modelRepository);
    }

    @Test
    public void shouldShortcutWhenThereIsOnlyOneItem() throws Exception {
        createView(someDefinition(), "someView", true);

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("someView", "foo:value"), is(true));
    }

    @Test
    public void shouldNotShortcutWhenThereAreTwoItems() throws Exception {
        createView(someDefinition(), "viewName", true);

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));
        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("viewName", "foo:value"), is(false));
    }

    @Test
    public void shouldNotShortcutToInvisibleViews() throws Exception {
        createView(someDefinition(), "someView", false);

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("someView", "foo:value"), is(false));
    }

    @Test
    public void shouldNotShortcutWithNoData() throws Exception {
        createView(someDefinition(), "someView", true);

        assertThat(policy.shouldShortcut("someView", "foo:value"), is(false));
    }

    @Test
    public void shouldNotShortcutWhenThereAreMultipleMatchingTopLevelViews() throws Exception {
        createView(someDefinition(), "view1", true);
        createView(someDefinition(), "view2", true);

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("view1", "foo:value"), is(false));
    }

    @Test
    public void shouldShortcutWhenThereAreChildViews() throws Exception {
        createView(someDefinition(), "view1", true);
        createView(someDefinition(), "view2", true, some("view1"));

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("view1", "foo:value"), is(true));
    }

    @Test
    public void shouldShortcutWhenTheParentViewIsInvisibleButChildrenAreNot() throws Exception {
        createView(someDefinition(), "view1", false);
        createView(someDefinition(), "view2", true, some("view1"));

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("view2", "foo:value"), is(true));
    }

    @Test
    public void shouldNotShortcutWhenThereAreNoResultsInCurrentView() throws Exception {
        createView(someDefinition(), "view1", true, none(String.class), some("foo:bar"));
        createView(someDefinition(), "view2", true);

        records.value().add(someDefinition(), Record.constructors.record(keyword, "value"));

        assertThat(policy.shouldShortcut("view1", "foo:value"), is(false));
    }

    private Definition someDefinition() {
        return Definition.constructors.definition("definitionName", keyword);
    }


    private void createView(Definition definition, String viewName, boolean visible) {
        createView(definition, viewName, visible, Option.<String>none());
    }

    private void createView(Definition definition, String viewName, boolean visible, Option<String> parent) {
        createView(definition, viewName, visible, parent, none(String.class));
    }

    private void createView(Definition definition, String viewName, boolean visible, Option<String> parent, Option<String> query) {
        Model view = ViewsRepository.viewModel(Sequences.<Keyword<?>>sequence(keyword), viewName, parent, definition.name(), query.getOrElse(""), visible, "");
        modelRepository.set(UUID.randomUUID(), view);
    }
}
