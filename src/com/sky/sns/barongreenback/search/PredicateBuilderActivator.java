package sky.sns.barongreenback.search;

import sky.sns.barongreenback.shared.BaronGreenbackRequestScope;

import java.util.concurrent.Callable;

public class PredicateBuilderActivator implements Callable<PredicateBuilder> {
    private final BaronGreenbackRequestScope requestScope;

    public PredicateBuilderActivator(BaronGreenbackRequestScope requestScope) {
        this.requestScope = requestScope;
    }

    @Override
    public PredicateBuilder call() throws Exception {
        return requestScope.value().create(PredicateBuilder.class);
    }
}
