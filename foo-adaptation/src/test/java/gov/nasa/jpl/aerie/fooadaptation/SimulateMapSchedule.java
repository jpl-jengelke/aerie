package gov.nasa.jpl.aerie.fooadaptation;

import gov.nasa.jpl.aerie.fooadaptation.generated.GeneratedAdaptationFactory;
import gov.nasa.jpl.aerie.fooadaptation.mappers.FooValueMappers;
import gov.nasa.jpl.aerie.merlin.driver.AdaptationBuilder;
import gov.nasa.jpl.aerie.merlin.driver.SerializedActivity;
import gov.nasa.jpl.aerie.merlin.driver.SimulationDriver;
import gov.nasa.jpl.aerie.merlin.driver.json.JsonEncoding;
import gov.nasa.jpl.aerie.merlin.protocol.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.SerializedValue;
import gov.nasa.jpl.aerie.merlin.timeline.Schema;
import org.apache.commons.lang3.tuple.Pair;

import javax.json.Json;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static gov.nasa.jpl.aerie.merlin.protocol.Duration.MICROSECONDS;
import static gov.nasa.jpl.aerie.merlin.protocol.Duration.SECONDS;
import static gov.nasa.jpl.aerie.merlin.protocol.Duration.duration;

public class SimulateMapSchedule {
  public static void main(final String[] args) {
    try {
      simulateWithMapSchedule();
    } catch (final SimulationDriver.TaskSpecInstantiationException ex) {
      ex.printStackTrace();
    }
  }

  private static
  void simulateWithMapSchedule()
  throws SimulationDriver.TaskSpecInstantiationException
  {
    final var config = new Configuration();

    final var factory = new GeneratedAdaptationFactory();
    final var builder = new AdaptationBuilder<>(Schema.builder());
    factory.instantiate(FooValueMappers.configuration().serializeValue(config), builder);
    final var adaptation = builder.build();

    final var schedule = loadSchedule();
    final var startTime = Instant.now();
    final var simulationDuration = duration(25, SECONDS);

    final var simulationResults = SimulationDriver.simulate(
        adaptation,
        schedule,
        startTime,
        simulationDuration);

    simulationResults.resourceSamples.forEach((name, samples) -> {
      System.out.println(name + ":");
      samples.forEach(point -> System.out.format("\t%s\t%s\n", point.getKey(), point.getValue()));
    });

    simulationResults.simulatedActivities.forEach((name, activity) -> {
      System.out.println(name + ": " + activity.start + " for " + activity.duration);
    });
  }

  private static Map<String, Pair<Duration, SerializedActivity>> loadSchedule() {
    final var schedule = new HashMap<String, Pair<Duration, SerializedActivity>>();

    final var planJson = Json.createReader(SimulateMapSchedule.class.getResourceAsStream("plan.json")).readValue();
    for (final var scheduledActivity : planJson.asJsonArray()) {
      final var deferInMicroseconds = scheduledActivity.asJsonObject().getJsonNumber("defer").longValueExact();
      final var activityType = scheduledActivity.asJsonObject().getString("type");

      final var arguments = new HashMap<String, SerializedValue>();
      for (final var field : scheduledActivity.asJsonObject().getJsonObject("arguments").entrySet()) {
        arguments.put(field.getKey(), JsonEncoding.decode(field.getValue()));
      }

      schedule.put(
          UUID.randomUUID().toString(),
          Pair.of(
              duration(deferInMicroseconds, MICROSECONDS),
              new SerializedActivity(activityType, arguments)));
    }

    return schedule;
  }
}