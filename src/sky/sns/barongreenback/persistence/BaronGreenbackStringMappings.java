package com.googlecode.barongreenback.persistence;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Sequences.sequence;

public class BaronGreenbackStringMappings implements Value<StringMappings> {
    private StringMappings mappings;

    private BaronGreenbackStringMappings(StringMappings mappings, PersistentTypes persistentTypes) {
        this.mappings = mappings.add(Model.class, new ModelMapping());
        final Sequence<Pair<Class<?>, StringMapping>> additionalMappings = sequence(persistentTypes.mappings()).map(asStringMapping());
        
        for (Pair<Class<?>, StringMapping> mapping : additionalMappings) {
            this.mappings = this.mappings.add(mapping.first(), mapping.second());
        }
    }

    private Function1<Pair<Class<?>, Callable1<StringMappings, StringMapping>>, Pair<Class<?>, StringMapping>> asStringMapping() {
        return Callables.second(Callables.<StringMappings, StringMapping>callWith(this.mappings));
    }

    public static BaronGreenbackStringMappings baronGreenbackStringMappings(StringMappings mappings, PersistentTypes persistentTypes) {
        return new BaronGreenbackStringMappings(mappings, persistentTypes);
    }

    @Override
    public StringMappings value() {
        return mappings;
    }
}
