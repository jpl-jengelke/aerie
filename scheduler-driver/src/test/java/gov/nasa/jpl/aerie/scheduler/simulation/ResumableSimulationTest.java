package gov.nasa.jpl.aerie.scheduler.simulation;

import gov.nasa.jpl.aerie.merlin.driver.ActivityDirectiveId;
import gov.nasa.jpl.aerie.merlin.driver.SerializedActivity;
import gov.nasa.jpl.aerie.merlin.driver.engine.SimulationEngine;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.scheduler.SimulationUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static gov.nasa.jpl.aerie.merlin.protocol.types.Duration.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResumableSimulationTest {
  ResumableSimulationDriver<?> resumableSimulationDriver;
  Duration endOfLastAct;

  private final Duration tenHours = Duration.of(10, Duration.HOURS);
  private final Duration fiveHours = Duration.of(5, Duration.HOURS);

  @BeforeEach
  public void init() {
    final var acts = getActivities();
    final var fooMissionModel = SimulationUtility.getFooMissionModel();
    resumableSimulationDriver = new ResumableSimulationDriver<>(fooMissionModel,tenHours);
    for (var act : acts) {
      resumableSimulationDriver.simulateActivity(act.start, act.activity, null, true, act.id);
    }
  }
  @Test
  public void simulationResultsTest(){
    final var now = Instant.now();
    //ensures that simulation results are generated until the end of the last act;
    var simResults = resumableSimulationDriver.getSimulationResults(now);
    assert(simResults.realProfiles.get("/utcClock").getRight().get(0).extent().isEqualTo(endOfLastAct));
    /* ensures that when current simulation results cover more than the asked period and that nothing has happened
    between two requests, the same results are returned */
    var simResults2 = resumableSimulationDriver.getSimulationResultsUpTo(now, Duration.of(7, SECONDS));
    assertEquals(simResults, simResults2);
  }

  @Test
  public void simulationResultsTest2(){
    /* ensures that when the passed start epoch is not equal to the one used for previously computed results, the results are re-computed */
    var simResults = resumableSimulationDriver.getSimulationResults(Instant.now());
    assert(simResults.realProfiles.get("/utcClock").getRight().get(0).extent().isEqualTo(endOfLastAct));
    var simResults2 = resumableSimulationDriver.getSimulationResultsUpTo(Instant.now(), Duration.of(7, SECONDS));
    assertNotEquals(simResults, simResults2);
  }

  @Test
  public void simulationResultsTest3(){
     /* ensures that when current simulation results cover less than the asked period and that nothing has happened
    between two requests, the results are re-computed */
    final var now = Instant.now();
    var simResults2 = resumableSimulationDriver.getSimulationResultsUpTo(now, Duration.of(7, SECONDS));
    var simResults = resumableSimulationDriver.getSimulationResults(now);
    assert(simResults.realProfiles.get("/utcClock").getRight().get(0).extent().isEqualTo(endOfLastAct));
    assertNotEquals(simResults, simResults2);
  }

  @Test
  public void durationTest(){
    final var acts = getActivities();
    var act1Dur = resumableSimulationDriver.getActivityDuration(acts.get(0).id());
    var act2Dur = resumableSimulationDriver.getActivityDuration(acts.get(1).id());
    assertTrue(act1Dur.isPresent() && act2Dur.isPresent());
    assertTrue(act1Dur.get().isEqualTo(Duration.of(2, SECONDS)));
    assertTrue(act2Dur.get().isEqualTo(Duration.of(2, SECONDS)));
  }

  @Test
  public void testStopsAtEndOfPlanningHorizon(){
    final var fooSchedulerModel = SimulationUtility.getFooSchedulerModel();
    final var activity = new TestSimulatedActivity(
        Duration.of(0, SECONDS),
        new SerializedActivity("ControllableDurationActivity", Map.of("duration", fooSchedulerModel.serializeDuration(tenHours))),
        new ActivityDirectiveId(1));
    final var fooMissionModel = SimulationUtility.getFooMissionModel();
    resumableSimulationDriver = new ResumableSimulationDriver<>(fooMissionModel, fiveHours);
    resumableSimulationDriver.initSimulation();
    resumableSimulationDriver.clearActivitiesInserted();
    resumableSimulationDriver.simulateActivity(activity.start, activity.activity, null, true, activity.id);
    assertEquals(fiveHours, resumableSimulationDriver.getCurrentSimulationEndTime());
    assert(resumableSimulationDriver.getSimulationResults(Instant.now()).unfinishedActivities.size() == 1);
  }

  private ArrayList<TestSimulatedActivity> getActivities(){
    final var acts = new ArrayList<TestSimulatedActivity>();
    var act1 = new TestSimulatedActivity(
        Duration.of(0, SECONDS),
        new SerializedActivity("BasicActivity", Map.of()),
        new ActivityDirectiveId(1));
    acts.add(act1);
    var act2 = new TestSimulatedActivity(
        Duration.of(14, SECONDS),
        new SerializedActivity("BasicActivity", Map.of()),
        new ActivityDirectiveId(2));
    acts.add(act2);

    endOfLastAct = Duration.of(16,SECONDS);
    return acts;
  }

  record TestSimulatedActivity(Duration start, SerializedActivity activity, ActivityDirectiveId id){}
}
