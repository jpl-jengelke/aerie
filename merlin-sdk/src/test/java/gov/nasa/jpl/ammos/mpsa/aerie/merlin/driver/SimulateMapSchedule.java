package gov.nasa.jpl.ammos.mpsa.aerie.merlin.driver;

import gov.nasa.jpl.ammos.mpsa.aerie.merlin.protocol.TaskSpecType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.sample.generated.FooAdaptationFactory;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.SerializedActivity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.SerializedValue;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration.MILLISECONDS;
import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration.SECOND;
import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration.SECONDS;
import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration.duration;

public class SimulateMapSchedule {
  public static void main(final String[] args) {
    try {
      simulateWithMapSchedule();
    } catch (final TaskSpecType.UnconstructableTaskSpecException ex) {
      ex.printStackTrace();
    }
  }

  private static
  void simulateWithMapSchedule()
  throws TaskSpecType.UnconstructableTaskSpecException
  {
    final var adaptation = new FooAdaptationFactory().instantiate();
    final var schedule = Map.of(
        UUID.randomUUID().toString(), Pair.of(
            duration(0, MILLISECONDS),
            new SerializedActivity(
                "foo",
                Map.of("x", SerializedValue.of(1),
                       "y", SerializedValue.of("test_1")))),
        UUID.randomUUID().toString(), Pair.of(
            duration(50, MILLISECONDS),
            new SerializedActivity(
                "foo",
                Map.of("x", SerializedValue.of(2),
                       "y", SerializedValue.of("spawn")))),
        UUID.randomUUID().toString(), Pair.of(
            duration(50, MILLISECONDS),
            new SerializedActivity(
                "foo",
                Map.of("x", SerializedValue.of(2),
                       "y", SerializedValue.of("test")))),
        UUID.randomUUID().toString(), Pair.of(
            duration(150, MILLISECONDS),
            new SerializedActivity(
                "foo",
                Map.of("x", SerializedValue.of(2),
                       "y", SerializedValue.of("test_2")))),
        UUID.randomUUID().toString(), Pair.of(
            duration(150, MILLISECONDS),
            new SerializedActivity(
                "foo",
                Map.of("x", SerializedValue.of(3),
                       "y", SerializedValue.of("test_3"))))
    );
    final var startTime = Instant.now();
    final var simulationDuration = duration(5, SECONDS);
    final var samplingPeriod = duration(1, SECOND);

    final var simulationResults = SimulationDriver.simulate(
        adaptation,
        schedule,
        startTime,
        simulationDuration,
        samplingPeriod);

    simulationResults.timelines.forEach((name, samples) -> System.out.format("%s: %s\n", name, samples));
  }
}