package gov.nasa.jpl.aerie.scheduler;

import gov.nasa.jpl.aerie.constraints.time.Segment;
import gov.nasa.jpl.aerie.constraints.time.Windows;
import gov.nasa.jpl.aerie.constraints.tree.*;
import gov.nasa.jpl.aerie.merlin.driver.MissionModel;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import gov.nasa.jpl.aerie.scheduler.constraints.activities.ActivityExpression;
import gov.nasa.jpl.aerie.scheduler.constraints.timeexpressions.TimeAnchor;
import gov.nasa.jpl.aerie.scheduler.constraints.timeexpressions.TimeExpressionConstant;
import gov.nasa.jpl.aerie.scheduler.goals.CoexistenceGoal;
import gov.nasa.jpl.aerie.scheduler.constraints.resources.StateQueryParam;
import gov.nasa.jpl.aerie.scheduler.goals.ChildCustody;
import gov.nasa.jpl.aerie.scheduler.model.PlanInMemory;
import gov.nasa.jpl.aerie.scheduler.model.PlanningHorizon;
import gov.nasa.jpl.aerie.scheduler.model.Problem;
import gov.nasa.jpl.aerie.scheduler.model.SchedulingActivityDirective;
import gov.nasa.jpl.aerie.scheduler.simulation.SimulationFacade;
import gov.nasa.jpl.aerie.scheduler.solver.PrioritySolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.aerie.constraints.time.Interval.Inclusivity.Exclusive;
import static gov.nasa.jpl.aerie.constraints.time.Interval.Inclusivity.Inclusive;
import static gov.nasa.jpl.aerie.constraints.time.Interval.interval;
import static gov.nasa.jpl.aerie.merlin.protocol.types.Duration.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulationFacadeTest {

  MissionModel<?> missionModel;
  Problem problem;
  SimulationFacade facade;
  //concrete named time points used to setup tests and validate expectations
  private static final Instant t0h = TestUtility.timeFromEpochMillis(0);
  private static final Instant t1h = TestUtility.timeFromEpochMillis(1000);
  private static final Instant t1_5h = TestUtility.timeFromEpochMillis(1500);
  private static final Instant t2h = TestUtility.timeFromEpochMillis(2000);
  private static final Instant tEndh = TestUtility.timeFromEpochMillis(5000);

  //hard-coded range of scheduling/simulation operations
  private static final PlanningHorizon horizon = new PlanningHorizon(t0h, tEndh);

  //concrete named time points used to setup tests and validate expectations
  private static final Duration t0 = horizon.toDur(t0h);
  private static final Duration t1 = horizon.toDur(t1h);
  private static final Duration t1_5 = horizon.toDur(t1_5h);
  private static final Duration t2 = horizon.toDur(t2h);
  private static final Duration tEnd = horizon.toDur(tEndh);

  /** fetch reference to the fruit resource in the mission model **/
  private RealResource getFruitRes() {
    return new RealResource("/fruit");
  }

  /** fetch reference to the conflicted marker on the flag resource in the mission model **/
  private DiscreteResource getFlagConflictedRes() {
    return new DiscreteResource("/flag/conflicted");
  }

  /** fetch reference to the flag resource in the mission model **/
  private DiscreteResource getFlagRes() {
    return new DiscreteResource("/flag");
  }

  /** fetch reference to the plant resource in the mission model **/
  private DiscreteResource getPlantRes() {
    return new DiscreteResource("/plant");
  }

  @BeforeEach
  public void setUp() {
    missionModel = SimulationUtility.getBananaMissionModel();
    final var schedulerModel = SimulationUtility.getBananaSchedulerModel();
    facade = new SimulationFacade(horizon, missionModel, schedulerModel);
    problem = new Problem(missionModel, horizon, facade, schedulerModel);
  }

  @AfterEach
  public void tearDown() {
    missionModel = null;
    problem = null;
    facade = null;
  }

  /** constructs an empty plan with the test model/horizon **/
  private PlanInMemory makeEmptyPlan() {
    return new PlanInMemory();
  }

  /**
   * constructs a simple test plan with one peel and bite
   *
   * expected activity/resource histories:
   * <pre>
   * time: |t0--------|t1--------|t2-------->
   * acts:            |peel(fS)  |bite(0.1)
   * peel: |4.0-------|3.0------------------>
   * fruit:|4.0-------|3.0-------|2.9------->
   * </pre>
   **/
  private PlanInMemory makeTestPlanP0B1() {
    final var plan = makeEmptyPlan();
    final var actTypeBite = problem.getActivityType("BiteBanana");
    final var actTypePeel = problem.getActivityType("PeelBanana");

    var act1 = SchedulingActivityDirective.of(actTypePeel, t1, null, Map.of("peelDirection", SerializedValue.of("fromStem")), null, true);
    plan.add(act1);

    var act2 = SchedulingActivityDirective.of(actTypeBite, t2, null, Map.of("biteSize", SerializedValue.of(0.1)), null, true);
    plan.add(act2);

    return plan;
  }

  @Test
  public void associationToExistingSatisfyingActivity(){
    final var plan = makeEmptyPlan();
    final var actTypePeel = problem.getActivityType("PeelBanana");
    final var actTypeBite = problem.getActivityType("BiteBanana");

    var act1 = SchedulingActivityDirective.of(actTypePeel, t1, t2, Map.of("peelDirection", SerializedValue.of("fromStem")), null, true);
    plan.add(act1);

    final var goal = new CoexistenceGoal.Builder()
        .forEach(ActivityExpression.ofType(actTypePeel))
        .forAllTimeIn(new WindowsWrapperExpression(new Windows(false).set(horizon.getHor(), true)))
        .thereExistsOne(new ActivityExpression.Builder().
                           ofType(actTypeBite)
                           .withArgument("biteSize", SerializedValue.of(0.1))
                           .build())
        .startsAt(TimeAnchor.END)
        .aliasForAnchors("its a me")
        .withinPlanHorizon(horizon)
        .build();

    problem.setGoals(List.of(goal));
    problem.setInitialPlan(plan);
    final var solver = new PrioritySolver(problem);
    final var plan1 = solver.getNextSolution();
    assertTrue(plan1.isPresent());

    final var actAssociatedInFirstRun = plan1.get().getEvaluation().forGoal(goal).getAssociatedActivities();
    assertEquals(1, actAssociatedInFirstRun.size());

    problem.setInitialPlan(plan1.get());
    final var solver2 = new PrioritySolver(problem);
    final var plan2 = solver2.getNextSolution();
    assertTrue(plan2.isPresent());

    final var actAssociatedInSecondRun = plan2.get().getEvaluation().forGoal(goal).getAssociatedActivities();
    assertEquals(1, actAssociatedInSecondRun.size());
    assertTrue(actAssociatedInFirstRun.iterator().next().equalsInProperties(actAssociatedInSecondRun.iterator().next()));
    assertEquals(2, problem.getSimulationFacade().countSimulationRestarts());
  }

  @Test
  public void getValueAtTimeDoubleOnSimplePlanMidpoint() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    final var stateQuery = new StateQueryParam(getFruitRes().name, new TimeExpressionConstant(t1_5));
    final var actual = stateQuery.getValue(facade.getLatestConstraintSimulationResults().get(), null, horizon.getHor());
    assertEquals(SerializedValue.of(3.0), actual);
  }

  @Test
  public void getValueAtTimeDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    final var stateQuery = new StateQueryParam(getFruitRes().name, new TimeExpressionConstant(t2));
    final var actual = stateQuery.getValue(facade.getLatestConstraintSimulationResults().get(), null, horizon.getHor());
    assertEquals(SerializedValue.of(2.9), actual);
    assertEquals(1, problem.getSimulationFacade().countSimulationRestarts());
  }

  @Test
  public void whenValueAboveDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    var actual = new GreaterThan(getFruitRes(), new RealValue(2.9)).evaluate(facade.getLatestConstraintSimulationResults().get());
    var expected = new Windows(
        Segment.of(interval(0, Inclusive, 2, Exclusive, SECONDS), true),
        Segment.of(interval(2, Inclusive,5, Exclusive, SECONDS), false)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void whenValueBelowDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    var actual = new LessThan(getFruitRes(), new RealValue(3.0)).evaluate(facade.getLatestConstraintSimulationResults().get());
    var expected = new Windows(
        Segment.of(interval(0, Inclusive, 2, Exclusive, SECONDS), false),
        Segment.of(interval(2, Inclusive, 5, Exclusive, SECONDS), true)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void whenValueBetweenDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    var actual = new And(new GreaterThanOrEqual(getFruitRes(), new RealValue(3.0)), new LessThanOrEqual(getFruitRes(), new RealValue(3.99))).evaluate(facade.getLatestConstraintSimulationResults().get());
    var expected = new Windows(
        Segment.of(interval(0, Inclusive, 1, Exclusive, SECONDS), false),
        Segment.of(interval(1, Inclusive, 2, Exclusive, SECONDS), true),
        Segment.of(interval(2, Inclusive, 5, Exclusive, SECONDS), false)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void whenValueEqualDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    var actual = new Equal<>(getFruitRes(), new RealValue(3.0)).evaluate(facade.getLatestConstraintSimulationResults().get());
    var expected = new Windows(
        Segment.of(interval(0, Inclusive, 1, Exclusive, SECONDS), false),
        Segment.of(interval(1, Inclusive, 2, Exclusive, SECONDS), true),
        Segment.of(interval(2, Inclusive, 5, Exclusive, SECONDS), false)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void whenValueNotEqualDoubleOnSimplePlan() throws SimulationFacade.SimulationException {
    facade.insertActivitiesIntoSimulation(makeTestPlanP0B1().getActivities());
    facade.computeSimulationResultsUntil(tEnd);
    var actual = new NotEqual<>(getFruitRes(), new RealValue(3.0)).evaluate(facade.getLatestConstraintSimulationResults().get());
    var expected = new Windows(
        Segment.of(interval(0, Inclusive, 1, Exclusive, SECONDS), true),
        Segment.of(interval(1, Inclusive, 2, Exclusive, SECONDS), false),
        Segment.of(interval(2, Inclusive, 5, Exclusive, SECONDS), true)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void testCoexistenceGoalWithResourceConstraint() {
    problem.setInitialPlan(makeTestPlanP0B1());

    /**
    * reminder for PB1
     * <pre>
     * time: |t0--------|t1--------|t2-------->
     * acts:            |peel(fS)  |bite(0.1)
     * peel: |4.0-------|3.0------------------>
     * fruit:|4.0-------|3.0-------|2.9------->
     * </pre>
     **/
    final var constraint = new AssignGaps<>(new And(
        new LessThanOrEqual(new RealResource("/peel"), new RealValue(3.0)),
        new LessThanOrEqual(new RealResource("/fruit"), new RealValue(2.9))
    ), new WindowsValue(false));

    final var actTypePeel = problem.getActivityType("PeelBanana");

    CoexistenceGoal cg = new CoexistenceGoal.Builder()
        .forAllTimeIn(new WindowsWrapperExpression(new Windows(false).set(horizon.getHor(), true)))
        .thereExistsOne(new ActivityExpression.Builder()
                            .ofType(actTypePeel)
                            .withArgument("peelDirection", SerializedValue.of("fromStem"))
                            .build())
        .forEach(new SpansFromWindows(constraint))
        .owned(ChildCustody.Jointly)
        .startsAt(TimeAnchor.START)
        .aliasForAnchors("its a me")
        .withinPlanHorizon(horizon)
        .build();

    problem.setGoals(List.of(cg));
    final var solver = new PrioritySolver(this.problem);
    final var plan = solver.getNextSolution().orElseThrow();
    assertTrue(TestUtility.containsActivity(plan, t2, t2, actTypePeel));
    assertEquals(2, problem.getSimulationFacade().countSimulationRestarts());
  }
}
