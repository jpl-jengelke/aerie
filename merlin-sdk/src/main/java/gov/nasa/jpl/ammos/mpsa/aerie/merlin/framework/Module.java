package gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework;

import gov.nasa.jpl.ammos.mpsa.aerie.merlin.protocol.TaskSpecType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.timeline.History;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.timeline.Query;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.discrete.DiscreteResource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.real.RealCondition;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.real.RealResource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.SerializedValue;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Module<$Schema> {
  private final Context<$Schema> context;

  protected Module(final Context<$Schema> context) {
    this.context = Objects.requireNonNull(context);
  }

  protected Module(final ResourcesBuilder.Cursor<$Schema> builder) {
    this(builder.getRootContext());
  }


  protected final History<? extends $Schema> now() {
    return this.context.now();
  }


  protected final <Event> void emit(final Event event, final Query<? super $Schema, Event, ?> query) {
    this.context.emit(event, query);
  }

  protected final <Spec> String spawn(final Spec spec, final TaskSpecType<? super $Schema, Spec> type) {
    return this.context.spawn(spec, type);
  }

  @Deprecated
  protected final String spawn(final String type, final Map<String, SerializedValue> arguments) {
    return this.context.spawn(type, arguments);
  }

  protected final <Spec> void call(final Spec spec, final TaskSpecType<? super $Schema, Spec> type) {
    this.waitFor(this.spawn(spec, type));
  }

  protected final <Spec> String defer(final Duration duration, final Spec spec, final TaskSpecType<? super $Schema, Spec> type) {
    return this.context.defer(duration, spec, type);
  }

  protected final <Spec> String defer(final long quantity, final Duration unit, final Spec spec, final TaskSpecType<? super $Schema, Spec> type) {
    return this.defer(unit.times(quantity), spec, type);
  }


  protected final void delay(final Duration duration) {
    this.context.delay(duration);
  }

  protected final void delay(final long quantity, final Duration unit) {
    this.delay(unit.times(quantity));
  }

  protected final void waitFor(final String id) {
    this.context.waitFor(id);
  }

  protected final void waitFor(
      final RealResource<? super History<? extends $Schema>> resource,
      final RealCondition condition)
  {
    this.context.waitFor(resource, condition);
  }

  protected final <T> void waitFor(
      final DiscreteResource<? super History<? extends $Schema>, T> resource,
      final Set<T> condition)
  {
    this.context.waitFor(resource, condition);
  }
}
