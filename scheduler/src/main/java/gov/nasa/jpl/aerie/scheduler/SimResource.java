package gov.nasa.jpl.aerie.scheduler;

import gov.nasa.jpl.aerie.constraints.model.SimulationResults;
import gov.nasa.jpl.aerie.constraints.time.Windows;
import gov.nasa.jpl.aerie.constraints.tree.And;
import gov.nasa.jpl.aerie.constraints.tree.DiscreteResource;
import gov.nasa.jpl.aerie.constraints.tree.DiscreteValue;
import gov.nasa.jpl.aerie.constraints.tree.Equal;
import gov.nasa.jpl.aerie.constraints.tree.Expression;
import gov.nasa.jpl.aerie.constraints.tree.GreaterThan;
import gov.nasa.jpl.aerie.constraints.tree.GreaterThanOrEqual;
import gov.nasa.jpl.aerie.constraints.tree.LessThan;
import gov.nasa.jpl.aerie.constraints.tree.LessThanOrEqual;
import gov.nasa.jpl.aerie.constraints.tree.NotEqual;
import gov.nasa.jpl.aerie.constraints.tree.RealResource;
import gov.nasa.jpl.aerie.constraints.tree.RealValue;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Class mocking the behavior of an externally defined state and implementing ExternalState interface
 * @param <T> the type of the variable managed by the state
 */
public class SimResource<T extends Comparable<T>> implements
        ExternalState<T> {

    /** the identifier of this resource for use in properly querying the simulation results */
    private String name;

    /** reference to simulation results that contextualize queries to this resource */
    private SimulationResults simResults;

    /** the scheduler time stamp at the beginning of the horizon, used to contextualize offset-duration results */
    private Time horizonStart;

    TreeMap<Range<Time>, T> values;

    public boolean isEmpty(){
        return values == null || values.isEmpty();
    }

    public void failIfEmpty(){
        if(isEmpty()){
            throw new IllegalArgumentException("Trying to use uninitialized resource (have you simulated before?)");
        }
    }

    public void initFromSimRes(String name, SimulationResults simResults, List<Pair<Duration, T>> fileValues, Time planningHorizonStart){
        this.name = name;
        this.simResults = simResults;
        this.horizonStart = planningHorizonStart;

        values = new TreeMap<Range<Time>, T>();
        Time start = null;
        T val =null;
        T lastVal = null;
        int i = 0;
        for(Pair<Duration, T> entry : fileValues){
            i++;
            val = entry.getValue();

           var time = planningHorizonStart.plus(gov.nasa.jpl.aerie.scheduler.Duration.fromMillis(entry.getKey().in(Duration.MILLISECOND)));
            if(start == null){
                start = time;
                lastVal = val;
            }
            if(!val.equals(lastVal) || i == fileValues.size()) {
                values.put(new Range<Time>(start, time), lastVal);
                start = time;
            }
            lastVal= val;
        }
    }

  /** convert constraint engine offset from plan start into a scheduler time point */
  private Time convertToSchedulerTime(Duration offsetDur) {
    return this.horizonStart.plus(convertToSchedulerDuration(offsetDur));
  }

  /** convert a scheduler time point to a constraint engine offset from plan start */
  private Duration convertToConstraintOffsetDuration(Time schedT ) {
    return Duration.of(schedT.minus(this.horizonStart).toMicroseconds(),Duration.MICROSECOND);
  }

  /** convert constraint engine duration to scheduler duration */
  private gov.nasa.jpl.aerie.scheduler.Duration convertToSchedulerDuration(Duration inDuration) {
    return gov.nasa.jpl.aerie.scheduler.Duration.ofMicroseconds(inDuration.in(Duration.MICROSECOND));
  }

  /** convert constraint engine windows into scheduler windows, within specified bounding windows */
  private TimeWindows convertToSchedulerWindows(Windows inWindows, TimeWindows queryBounds) {
    final var outWindows = new TimeWindows();
    for (final var inWin : inWindows) {
      final var startT = convertToSchedulerTime(inWin.start);
      final var endT = convertToSchedulerTime(inWin.end);
      outWindows.union(TimeWindows.of(startT, endT));
    }
    outWindows.intersection(queryBounds);
    return outWindows;
  }

    public T getValueAtTime(Time t){
        failIfEmpty();

        //NB: reverse iteration so that the inclusive contains() queries encounter the latest-starting range first
        //TODO: could be vastly improved by leveraging the non-overlapping data invariant and tree map floorKey queries
        for(Map.Entry<Range<Time>,T> intv : values.descendingMap().entrySet()){
            if(intv.getKey().contains(t)){
                return intv.getValue();
            }
        }
        return null;
    }

    public TimeWindows whenValueBetween(T inf, T sup, TimeWindows windows){
        failIfEmpty();

      //special case doubles are the only aerie types that can be compared with inequality constraints
      if (inf instanceof Double) {
        final var gteConstraint = new GreaterThanOrEqual(new RealResource(this.name), new RealValue((Double) inf));
        final var lteConstraint = new LessThanOrEqual(new RealResource(this.name), new RealValue((Double) sup));
        final var constraint = new And(gteConstraint,lteConstraint);
        final var satisfied = constraint.evaluate(this.simResults);
        return convertToSchedulerWindows( satisfied, windows );
      } else {
        throw new UnsupportedOperationException("aerie inequality constraints only work with real-valued resources");
      }
    }

    public TimeWindows whenValueBelow(T val, TimeWindows windows){
        failIfEmpty();

        //special case doubles are the only aerie types that can be compared with inequality constraints
        if (val instanceof Double) {
            final var constraint = new LessThan(new RealResource(this.name), new RealValue((Double) val));
            final var satisfied = constraint.evaluate(this.simResults);
            return convertToSchedulerWindows( satisfied, windows );
        } else {
            throw new UnsupportedOperationException("aerie inequality constraints only work with real-valued resources");
        }
    }

    public TimeWindows whenValueAbove(T val, TimeWindows windows){
        failIfEmpty();

      //special case doubles are the only aerie types that can be compared with inequality constraints
      if (val instanceof Double) {
        final var constraint = new GreaterThan(new RealResource(this.name), new RealValue((Double) val));
        final var satisfied = constraint.evaluate(this.simResults);
        return convertToSchedulerWindows( satisfied, windows );
      } else {
        throw new UnsupportedOperationException("aerie inequality constraints only work with real-valued resources");
      }
    }

    public TimeWindows whenValueEqual(T val, TimeWindows windows){
        failIfEmpty();

      Expression<Windows> constraint;
      if (val instanceof Double || val instanceof Float) {
        //aeire discrete double resources can be promoted to real resources even if discrete, so just do that for all doubles
        final var dblVal = ((Number) val).doubleValue();
        constraint = new Equal<>(new RealResource(this.name), new RealValue(dblVal));
      } else {
        //everything else is handled as a discrete resource
        //TODO: improve type multiplexing (type vars? or just unify type handling with aerie)
        if (val instanceof Boolean) {
          final var serVal = SerializedValue.of((Boolean) val);
          constraint = new Equal<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else if (val instanceof Long || val instanceof Integer) {
          final var serVal = SerializedValue.of(((Number) val).longValue());
          constraint = new Equal<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else if (val instanceof String) {
          final var serVal = SerializedValue.of((String) val);
          constraint = new Equal<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else {
          throw new UnsupportedOperationException("unrecognized type for aerie discrete resource equality constraint");
        }
      }
      final var satisfied = constraint.evaluate(this.simResults);
      return convertToSchedulerWindows(satisfied, windows);
    }

    @Override
    public Map<Range<Time>, T> getTimeline(TimeWindows timeDomain) {
        return values;
    }

    @Override
    public TimeWindows whenValueNotEqual(T val, TimeWindows windows) {

      Expression<Windows> constraint;
      if (val instanceof Double || val instanceof Float) {
        //aeire discrete double resources can be promoted to real resources even if discrete, so just do that for all doubles
        final var dblVal = ((Number) val).doubleValue();
        constraint = new NotEqual<>(new RealResource(this.name), new RealValue(dblVal));
      } else {
        //everything else is handled as a discrete resource
        //TODO: improve type multiplexing (type vars? or just unify type handling with aerie)
        if (val instanceof Boolean) {
          final var serVal = SerializedValue.of((Boolean) val);
          constraint = new NotEqual<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else if (val instanceof Long || val instanceof Integer) {
          final var serVal = SerializedValue.of(((Number) val).longValue());
          constraint = new NotEqual<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else if (val instanceof String) {
          final var serVal = SerializedValue.of((String) val);
          constraint = new NotEqual<>(new DiscreteResource(this.name), new DiscreteValue(serVal));
        } else {
          throw new UnsupportedOperationException("unrecognized type for aerie discrete resource inequality constraint");
        }
      }
      final var satisfied = constraint.evaluate(this.simResults);
      return convertToSchedulerWindows(satisfied, windows);
    }

    public void draw(){
        for(Map.Entry<Range<Time>, T> v : values.entrySet()){
            if(v.getValue() instanceof Boolean){
                Boolean val = (Boolean) v.getValue();
                String toPrint = "";
                if(val){
                    toPrint = "X";
                } else{
                    toPrint = "-";
                }
                int max = (int) v.getKey().getMaximum().toEpochMilliseconds()/1000;
                int min = (int) v.getKey().getMinimum().toEpochMilliseconds()/1000;

                for(int i = min; i < max; i++){
                    System.out.print(toPrint+"  ");

                }

            }
        }
        System.out.println("");


    }

}
