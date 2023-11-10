package gov.nasa.jpl.aerie.merlin.driver;

import gov.nasa.jpl.aerie.json.BasicParsers;
import gov.nasa.jpl.aerie.merlin.driver.engine.SimulationEngine;
import gov.nasa.jpl.aerie.merlin.framework.ThreadedTask;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.InstantiationException;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGInterval;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.aerie.merlin.driver.SimulationResultsComparisonUtils.assertEqualsSimResultsClipper;
import static gov.nasa.jpl.aerie.merlin.driver.json.SerializedValueJsonParser.serializedValueP;
import static gov.nasa.jpl.aerie.merlin.protocol.types.Duration.MICROSECONDS;
import static gov.nasa.jpl.aerie.merlin.protocol.types.Duration.MINUTES;
import static gov.nasa.jpl.aerie.merlin.protocol.types.Duration.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClipperSimulationDuplicationTest {

  @BeforeAll
  static void beforeAll() {
    ThreadedTask.CACHE_READS = true;
  }

  @Test
  void testDuplicate() throws MissionModelLoader.MissionModelLoadException, FileNotFoundException,
                              InstantiationException
  {

    final var YEAR = Duration.HOUR.times(24 * 7 * 52);
    final var WEEK = Duration.HOUR.times(24 * 7);
    final var durationSim = YEAR.dividedBy(2);
    final var jsonPlan = loadClipperModelAndPlan();

    final var oneYearSimulationWithCheckpoints = SimulationDriver.simulateWithCheckpoints(
        jsonPlan.missionModel,
        jsonPlan.plan(),
        jsonPlan.planningHorizon.start,
        durationSim,
        jsonPlan.planningHorizon.start,
        durationSim,
        $ -> {},
        SimulationDriver.CachedSimulationEngine.empty(jsonPlan.missionModel()),
        SimulationDriver.periodicCheckpoints(WEEK, durationSim, WEEK));

    for(final var checkpoint : oneYearSimulationWithCheckpoints.checkpoints()){
      System.out.println("Starting simulation from checkpoint at " + checkpoint.startOffset());
      final SimulationDriver.SimulationResultsWithCheckpoints newResults = SimulationDriver.simulateWithCheckpoints(
          jsonPlan.missionModel,
          jsonPlan.plan(),
          jsonPlan.planningHorizon.start,
          durationSim,
          jsonPlan.planningHorizon.start,
          durationSim,
          $ -> {},
          checkpoint,
          SimulationDriver.desiredCheckpoints(List.of()));
      System.out.println("Comparing results for checkpoint at " + checkpoint.startOffset());
      assertEqualsSimResultsClipper(oneYearSimulationWithCheckpoints.results(), newResults.results());
    }
  }

  @Test
  void testStartFromCheckpoint()
  throws MissionModelLoader.MissionModelLoadException, FileNotFoundException, InstantiationException
  {
    final var jsonPlan = loadClipperModelAndPlan();
    final SimulationDriver.SimulationResultsWithCheckpoints results = simulateWithCheckpoints(
        SimulationDriver.CachedSimulationEngine.empty(jsonPlan.missionModel()),
        List.of(Duration.of(5, MINUTES)),
        jsonPlan.missionModel());
    final SimulationResults expected = SimulationDriver.simulate(
        jsonPlan.missionModel,
        Map.of(),
        Instant.EPOCH,
        Duration.HOUR,
        Instant.EPOCH,
        Duration.HOUR,
        $ -> {});
    assertEquals(expected, results.results());
    final SimulationDriver.SimulationResultsWithCheckpoints newResults = simulateWithCheckpoints(results.checkpoints().get(0), List.of(), jsonPlan.missionModel());
    assertEquals(expected, newResults.results());
  }


  static SimulationDriver.SimulationResultsWithCheckpoints simulateWithCheckpoints(
      final MissionModel<?> missionModel,
      final Duration periodSeconds,
      final Map<ActivityDirectiveId, ActivityDirective> schedule
  ) {
    return SimulationDriver.simulateWithCheckpoints(
        missionModel,
        schedule,
        Instant.EPOCH,
        Duration.HOUR,
        Instant.EPOCH,
        Duration.HOUR,
        $ -> {},
        SimulationDriver.CachedSimulationEngine.empty(missionModel),
        SimulationDriver.wallClockCheckpoints(periodSeconds.in(SECONDS)));
  }

  static SimulationDriver.SimulationResultsWithCheckpoints simulateWithCheckpoints(
      final MissionModel<?> missionModel,
      final SimulationDriver.CachedSimulationEngine cachedSimulationEngine,
      final List<Duration> desiredCheckpoints,
      final Map<ActivityDirectiveId, ActivityDirective> schedule
  ) {
    return SimulationDriver.simulateWithCheckpoints(
        missionModel,
        schedule,
        Instant.EPOCH,
        Duration.HOUR,
        Instant.EPOCH,
        Duration.HOUR,
        $ -> {},
        cachedSimulationEngine,
        SimulationDriver.desiredCheckpoints(desiredCheckpoints));
  }

  static SimulationDriver.SimulationResultsWithCheckpoints simulateWithCheckpoints(
      final List<Duration> desiredCheckpoints,
      final MissionModel<?> missionModel
  ) {
    return SimulationDriver.simulateWithCheckpoints(
        missionModel,
        Map.of(),
        Instant.EPOCH,
        Duration.HOUR,
        Instant.EPOCH,
        Duration.HOUR,
        $ -> {},
        SimulationDriver.CachedSimulationEngine.empty(missionModel),
        SimulationDriver.desiredCheckpoints(desiredCheckpoints));
  }

  static SimulationDriver.SimulationResultsWithCheckpoints simulateWithCheckpoints(
      final SimulationDriver.CachedSimulationEngine cachedEngine,
      final List<Duration> desiredCheckpoints,
      final MissionModel<?> missionModel
  ) {
    return SimulationDriver.simulateWithCheckpoints(
        missionModel,
        Map.of(),
        Instant.EPOCH,
        Duration.HOUR,
        Instant.EPOCH,
        Duration.HOUR,
        $ -> {},
        cachedEngine,
        SimulationDriver.desiredCheckpoints(desiredCheckpoints));
  }

  public record PlanningHorizon(Instant start, Instant end, Duration aerieStart, Duration aerieEnd){
    public static Duration durationBetween(Instant start, Instant end){
      return Duration.of(ChronoUnit.MICROS.between(start, end), Duration.MICROSECONDS);
    }
  }
  record MissionModelDescription(String name, Map<String, SerializedValue> config, Path libPath, Instant start) {}

  public record JsonPlan(PlanningHorizon planningHorizon, Map<ActivityDirectiveId, ActivityDirective> plan, MissionModel<?> missionModel){}

  public JsonPlan loadClipperModelAndPlan()
  throws MissionModelLoader.MissionModelLoadException, FileNotFoundException,
         InstantiationException
  {
    final MissionModelDescription
        CLIPPER = new MissionModelDescription(
        "eurc_30_oct.jar",
        Map.of(),
        Path.of(System.getenv("AERIE_ROOT"), "scheduler-worker", "src", "test", "resources"),
        null
    );
    final var initialPlan = loadPlanFromJson(
        Path.of(System.getenv("AERIE_ROOT"), "scheduler-worker", "src", "test", "resources",
                "cruise-rap-dev-10-30.json").toString(),
        CLIPPER);
    return initialPlan;
  }

  @Test
  public void testCheckpoint()
  throws MissionModelLoader.MissionModelLoadException, FileNotFoundException,
         InstantiationException
  {
    final var initialPlan = loadClipperModelAndPlan();
    final var simEngine1 = new SimulationEngine();
    final var simulationResults = SimulationDriver.simulate(
        initialPlan.missionModel(),
        initialPlan.plan(),
        initialPlan.planningHorizon().start(),
        initialPlan.planningHorizon().aerieEnd(),
        initialPlan.planningHorizon().start(),
        initialPlan.planningHorizon().aerieEnd());
  }

  public JsonPlan loadPlanFromJson(final String path, final MissionModelDescription missionModelDescription)
  throws MissionModelLoader.MissionModelLoadException, InstantiationException,
         FileNotFoundException
  {

    final File jsonInputFile = new File(path);
    final InputStream is = new FileInputStream(jsonInputFile);
    final JsonReader reader = Json.createReader(is);
    final JsonObject empObj = reader.readObject();
    final var planningHorizonStart = Instant.parse(empObj.getString("start_time"));
    final var planningHorizonEnd = Instant.parse(empObj.getString("end_time"));
    final var planningHorizon = new PlanningHorizon(
        planningHorizonStart,
        planningHorizonEnd,
        Duration.ZERO,
        PlanningHorizon.durationBetween(planningHorizonStart, planningHorizonEnd));
    final var activityDirectives = empObj.getJsonArray("activities");

    final var missionModel = MissionModelLoader.loadMissionModel(
        planningHorizonStart,
        SerializedValue.of(missionModelDescription.config()),
        missionModelDescription.libPath().resolve(missionModelDescription.name()),
        "",
        "");

    final var plan = new HashMap<ActivityDirectiveId, ActivityDirective>();
    for (int i = 0; i < activityDirectives.size(); i++) {
      final var jsonActivity = activityDirectives.getJsonObject(i);
      final var type = activityDirectives.getJsonObject(i).getString("type");
      final var start = jsonActivity.getString("start_offset");
      final Integer anchorId = jsonActivity.isNull("anchor_id") ? null : jsonActivity.getInt("anchor_id");
      final boolean anchoredToStart = jsonActivity.getBoolean("anchored_to_start");
      final var arguments = jsonActivity.getJsonObject("arguments");
      final var deserializedArguments = BasicParsers
          .mapP(serializedValueP)
          .parse(arguments)
          .getSuccessOrThrow();
      final var effectiveArguments = missionModel.getDirectiveTypes().directiveTypes().get(type).getInputType()
                                                 .getEffectiveArguments(deserializedArguments);
      final var merlinActivity = new ActivityDirective(
          durationFromPGInterval(start),
          type,
          effectiveArguments,
          (anchorId != null) ? new ActivityDirectiveId(anchorId) : null,
          anchoredToStart);
      final var actPK = new ActivityDirectiveId(jsonActivity.getJsonNumber("id").longValue());
      plan.put(actPK, merlinActivity);
    }
    return new JsonPlan(planningHorizon, plan, missionModel);
  }

  public static Duration durationFromPGInterval(final String pgInterval) {
    try {
      final PGInterval asInterval = new PGInterval(pgInterval);
      if(asInterval.getYears() != 0 ||
         asInterval.getMonths() != 0) throw new RuntimeException("Years or months found in a pginterval");
      final var asDuration = java.time.Duration.ofDays(asInterval.getDays())
                                               .plusHours(asInterval.getHours())
                                               .plusMinutes(asInterval.getMinutes())
                                               .plusSeconds(asInterval.getWholeSeconds())
                                               .plusNanos(asInterval.getMicroSeconds()*1000);
      return Duration.of(asDuration.toNanos()/1000, MICROSECONDS);
    }catch(SQLException e){
      throw new RuntimeException(e);
    }
  }

}
