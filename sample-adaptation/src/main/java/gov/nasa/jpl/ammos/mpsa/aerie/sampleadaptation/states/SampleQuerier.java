package gov.nasa.jpl.ammos.mpsa.aerie.sampleadaptation.states;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.MerlinAdaptation;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.events.SimulationEvent;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.ActivityMapper;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.SerializedValue;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.ActivityEffectEvaluator;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.ActivityEvent;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.ActivityModel;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.ActivityModelApplicator;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.ActivityModelQuerier;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities.DynamicActivityModelQuerier;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.constraints.ConstraintViolation;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.activities.DynamicReactionContext;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine.activities.ReactionContext;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline.History;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline.Query;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline.SimulationTimeline;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.utilities.DynamicCell;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.DynamicStateQuery;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.StateQuery;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.model.CumulableEffectEvaluator;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.model.CumulableStateApplicator;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.model.RegisterState;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.model.SettableEffectEvaluator;
import gov.nasa.jpl.ammos.mpsa.aerie.contrib.models.independent.model.SettableStateApplicator;
import gov.nasa.jpl.ammos.mpsa.aerie.sampleadaptation.events.SampleEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.utilities.DynamicCell.setDynamic;

public class SampleQuerier<T> implements MerlinAdaptation.Querier<T, SimulationEvent<SampleEvent>> {
    // Create two DynamicCells to provide ReactionContext and StateContext to modeling code
    private final static DynamicCell<ReactionContext<?, SimulationEvent<SampleEvent>, Activity>> reactionContext = DynamicCell.create();
    private final static DynamicCell<SampleQuerier<?>.StateQuerier> stateContext = DynamicCell.create();

    // Define a function to take a state name and provide questions that can be asked based on current context
    public static final Function<String, StateQuery<SerializedValue>> query = (name) ->
        new DynamicStateQuery<>(() -> stateContext.get().getRegisterQuery(name));

    // Provide access to activity information based on the current context.
    public static final ActivityModelQuerier activityQuerier =
        new DynamicActivityModelQuerier(() -> stateContext.get().getActivityQuery());

    // Provide direct access to methods on the context stored in the dynamic cell.
    // e.g. instead of `reactionContext.get().spawn(act)`, just use `ctx.spawn(act)`.
    public static final ReactionContext<?, SimulationEvent<SampleEvent>, Activity> ctx = new DynamicReactionContext<>(() -> reactionContext.get());

    // Maintain a map of Query objects for each state (by name)
    // This allows queries on states to be tracked and cached for convenience
    private final Set<String> stateNames = new HashSet<>();
    private final Map<String, Query<T, RegisterState<SerializedValue>>> settables = new HashMap<>();
    private final Map<String, Query<T, RegisterState<Double>>> cumulables = new HashMap<>();

    // Model the durations of (and relationships between) activities.
    private final ActivityMapper activityMapper;
    private final Query<T, ActivityModel> activityModel;

    public SampleQuerier(final ActivityMapper activityMapper, final SimulationTimeline<T, SimulationEvent<SampleEvent>> timeline) {
        this.activityMapper = activityMapper;

        this.activityModel = timeline.register(
            new ActivityEffectEvaluator().filterContramap(SampleEvent::asActivity).filterContramap(SimulationEvent::asAdaptationEvent),
            new ActivityModelApplicator());

        // Register a Query object for each settable state
        for (final var entry : SampleMissionStates.factory.getSettableStates().entrySet()) {
            final var name = entry.getKey();
            final var initialValue = entry.getValue();

            if (this.stateNames.contains(name)) throw new RuntimeException("State \"" + name + "\" already defined");
            this.stateNames.add(name);

            final var query = timeline.register(
                new SettableEffectEvaluator(name).filterContramap(SampleEvent::asIndependent).filterContramap(SimulationEvent::asAdaptationEvent),
                new SettableStateApplicator(initialValue));

            this.settables.put(name, query);
        }

        // Register a Query object for each cumulable state
        for (final var entry : SampleMissionStates.factory.getCumulableStates().entrySet()) {
            final var name = entry.getKey();
            final var initialValue = entry.getValue();

            if (this.stateNames.contains(name)) throw new RuntimeException("State \"" + name + "\" already defined");
            this.stateNames.add(name);

            final var query = timeline.register(
                new CumulableEffectEvaluator(name).filterContramap(SampleEvent::asIndependent).filterContramap(SimulationEvent::asAdaptationEvent),
                new CumulableStateApplicator(initialValue));

            this.cumulables.put(name, query);
        }
    }

    @Override
    public void runActivity(final ReactionContext<T, SimulationEvent<SampleEvent>, Activity> ctx, final String activityId, final Activity activity) {
        // Run the activity in the context of the given reaction context as well as the established state queries.
        // The activity can affect the simulation by emitting events against the reaction context,
        // and can query states to change its behavior based on simulation state.
        stateContext.setWithin(new StateQuerier(ctx::now), () ->
            reactionContext.setWithin(ctx, () -> {
                // Signal the beginning of the activity.
                ctx.emit(SimulationEvent.ofAdaptationEvent(SampleEvent.activity(ActivityEvent.startActivity(activityId, this.activityMapper.serializeActivity(activity).get()))));

                // Run the entirety of the activity.
                activity.modelEffects();

                // Signal the end of the activity, but only after all of its children have also completed.
                ctx.waitForChildren();
                ctx.emit(SimulationEvent.ofAdaptationEvent(SampleEvent.activity(ActivityEvent.endActivity(activityId))));
            }));
    }

    @Override
    public Set<String> states() {
        final var states = new HashSet<String>();
        states.addAll(this.cumulables.keySet());
        states.addAll(this.settables.keySet());
        return states;
    }

    @Override
    public SerializedValue getSerializedStateAt(final String name, final History<T, SimulationEvent<SampleEvent>> history) {
        return this.getRegisterQueryAt(name, history).get();
    }

    @Override
    public List<ConstraintViolation> getConstraintViolationsAt(final History<T, SimulationEvent<SampleEvent>> history) {
        return setDynamic(stateContext, new StateQuerier(() -> history), () -> {
            final var violations = new ArrayList<ConstraintViolation>();

            for (final var violableConstraint : SampleMissionStates.violableConstraints) {
                // Set the constraint's getWindows method within the context of the history and evaluate it
                final var violationWindows = violableConstraint.getWindows();
                if (violationWindows.isEmpty()) continue;

                violations.add(new ConstraintViolation(violationWindows, violableConstraint));
            }

            return violations;
        });
    }

    public StateQuery<SerializedValue> getRegisterQueryAt(final String name, final History<T, SimulationEvent<SampleEvent>> history) {
        if (this.settables.containsKey(name)) return this.settables.get(name).getAt(history);
        else if (this.cumulables.containsKey(name)) return StateQuery.from(this.cumulables.get(name).getAt(history), SerializedValue::of);
        else throw new RuntimeException("State \"" + name + "\" is not defined");
    }

    // An inner class to maintain a supplier for current history to pass to the SampleQuerier
    public final class StateQuerier {
        // Provides the most up-to-date event history at the time of each request
        private final Supplier<History<T, SimulationEvent<SampleEvent>>> historySupplier;

        public StateQuerier(final Supplier<History<T, SimulationEvent<SampleEvent>>> historySupplier) {
            this.historySupplier = historySupplier;
        }

        // Get a queryable object representing the named state.
        public StateQuery<SerializedValue> getRegisterQuery(final String name) {
            return SampleQuerier.this.getRegisterQueryAt(name, this.historySupplier.get());
        }

        // Get a queryable object representing simulated activities.
        public ActivityModelQuerier getActivityQuery() {
            return SampleQuerier.this.activityModel.getAt(this.historySupplier.get());
        }
    }
}