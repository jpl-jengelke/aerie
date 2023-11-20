package gov.nasa.jpl.aerie.scheduler.goals;

import gov.nasa.jpl.aerie.constraints.model.EvaluationEnvironment;
import gov.nasa.jpl.aerie.constraints.model.SimulationResults;
import gov.nasa.jpl.aerie.merlin.driver.ActivityDirectiveId;
import gov.nasa.jpl.aerie.scheduler.conflicts.Conflict;
import gov.nasa.jpl.aerie.scheduler.model.Plan;
import gov.nasa.jpl.aerie.scheduler.model.SchedulingActivityDirectiveId;
import gov.nasa.jpl.aerie.scheduler.solver.optimizers.Optimizer;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptionGoal extends Goal {


  private List<Goal> goals;
  private Optimizer optimizer;

  public List<Goal> getSubgoals() {
    return goals;
  }

  public boolean hasOptimizer(){
    return optimizer != null;
  }

  public Optimizer getOptimizer(){
    return optimizer;
  }

  @Override
  public java.util.Collection<Conflict> getConflicts(Plan plan,
                                                     final SimulationResults simulationResults,
                                                     final Optional<BidiMap<SchedulingActivityDirectiveId, ActivityDirectiveId>> mapSchedulingIdsToActivityIds,
                                                     final EvaluationEnvironment evaluationEnvironment) {
    throw new NotImplementedException("Conflict detection is performed at solver level");
  }

  public static class Builder extends Goal.Builder<OptionGoal.Builder> {

    final List<Goal> goals = new ArrayList<>();

    Optimizer optimizer;

    public Builder or(Goal goal) {
      goals.add(goal);
      return getThis();
    }

    public Builder optimizingFor(Optimizer s) {
      optimizer = s;
      return getThis();
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public OptionGoal build() {
      OptionGoal dg = new OptionGoal();
      super.fill(dg);
      dg.goals = goals;
      dg.name = name;
      dg.optimizer = optimizer;
      return dg;
    }
  }
}
