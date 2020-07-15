package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.eventgraph;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Window;

import java.util.List;
import java.util.function.Supplier;

public final class DynamicActivityModelQuerier implements ActivityModelQuerier {
    private final Supplier<ActivityModelQuerier> querier;

    public DynamicActivityModelQuerier(final Supplier<ActivityModelQuerier> querier) {
        this.querier = querier;
    }

    @Override
    public List<String> getActivitiesOfType(final String activityType) {
        return this.querier.get().getActivitiesOfType(activityType);
    }

    @Override
    public Window getCurrentInstanceWindow(final String activityId) {
        return this.querier.get().getCurrentInstanceWindow(activityId);
    }

    @Override
    public List<Window> getTypeWindows(final String activityType) {
        return this.querier.get().getTypeWindows(activityType);
    }
}