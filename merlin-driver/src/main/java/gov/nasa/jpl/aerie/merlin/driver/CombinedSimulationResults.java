package gov.nasa.jpl.aerie.merlin.driver;

import gov.nasa.jpl.aerie.merlin.driver.engine.ProfileSegment;
import gov.nasa.jpl.aerie.merlin.driver.timeline.EventGraph;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import gov.nasa.jpl.aerie.merlin.protocol.types.ValueSchema;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CombinedSimulationResults implements SimulationResultsInterface {

  protected SimulationResultsInterface nr = null;
  protected SimulationResultsInterface or = null;

  public CombinedSimulationResults(SimulationResultsInterface newSimulationResults,
                                   SimulationResultsInterface oldSimulationResults) {
    this.nr = newSimulationResults;
    this.or = oldSimulationResults;
  }



  @Override
  public Instant getStartTime() {
    return ObjectUtils.min(nr.getStartTime(), or.getStartTime());
  }

  @Override
  public Duration getDuration() {
    return Duration.minus(ObjectUtils.max(Duration.addToInstant(nr.getStartTime(), nr.getDuration()),
                                          Duration.addToInstant(or.getStartTime(), or.getDuration())),
                          getStartTime());
  }

  @Override
  public Map<String, Pair<ValueSchema, List<ProfileSegment<RealDynamics>>>> getRealProfiles() {
    return Stream.of(or.getRealProfiles(), nr.getRealProfiles()).flatMap(m -> m.entrySet().stream())
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (p1, p2) -> mergeProfiles(or.getStartTime(), nr.getStartTime(), p1, p2)));
  }

  // We need to pass startTimes for both to know from where they are offset?  We don't want to assume that the two
  // simulations had the same timeframe.
  static <D> Pair<ValueSchema, List<ProfileSegment<D>>> mergeProfiles(Instant t1, Instant t2,
                                                                             Pair<ValueSchema, List<ProfileSegment<D>>> p1,
                                                                             Pair<ValueSchema, List<ProfileSegment<D>>> p2) {
    // We assume that the two ValueSchemas are the same and don't check for the sake of minimizing computation.
    return Pair.of(p1.getLeft(), mergeSegmentLists(t1, t2, p1.getRight(), p2.getRight()));
  }

  private static <D> List<ProfileSegment<D>> mergeSegmentLists(Instant t1, Instant t2,
                                                               List<ProfileSegment<D>> list1,
                                                               List<ProfileSegment<D>> list2) {
    Duration offset = Duration.minus(t2, t1);
    var s1 = list1.stream();
    var s2 = list2.stream();
    final Duration[] elapsed = {Duration.ZERO, Duration.ZERO};
    if (offset.isNegative()) {
      elapsed[0] = elapsed[0].minus(offset);
    } else {
      elapsed[1] = elapsed[1].plus(offset);
    }
    var ss1 = s1.map(p -> {
      var r =  Triple.of(elapsed[0], 1, p);
      elapsed[0] = elapsed[0].plus(p.extent());
      return r;
    });
    var ss2 = s2.map(p -> {
      var r =  Triple.of(elapsed[1], 0, p);
      elapsed[1] = elapsed[1].plus(p.extent());
      final Triple<Duration, Integer, ProfileSegment<D>> r1 = r;
      return r1;
    });
    final Triple<Duration, Integer, ProfileSegment<D>> tripleNull = Triple.of(null, null, null);
    var sorted = Stream.concat(Stream.of(ss1, ss2).flatMap(s -> s).sorted(), Stream.of(tripleNull));
    final Triple<Duration, Integer, ProfileSegment<D>>[] last = new Triple[] {null};
    //final Duration[] lastExtent = new Duration[] {null};
    var sss = sorted.map(t -> {
      final var oldLast = last[0];
      last[0] = t;
      if (oldLast == null) {
        return null;
      }
      if (t == null || t.getLeft() == null) {
        return oldLast.getRight();
      }
      Duration extent = t.getLeft().minus(oldLast.getLeft());

      if (extent.isEqualTo(Duration.ZERO) && !oldLast.getMiddle().equals(t.getMiddle())) {
//        System.out.println("skipping " + t);
        last[0] = oldLast;
        return null;
      }
//      System.out.println("keeping " + t);
//      last[0] = t;
      //lastExtent[0] = t.getRight().extent();
      var p = new ProfileSegment<D>(extent, oldLast.getRight().dynamics());
      return p;
    });
//    System.out.println("last[0] " + last[0]);
//    var rsss = Stream.concat(sss, Stream.of(last[0] == null ? null : last[0].getRight())).filter(Objects::nonNull);
    var rsss = sss.filter(Objects::nonNull);

    return rsss.toList();
  }

  private static void testMergeSegmentLists() {
    ProfileSegment<Integer> p1 = new ProfileSegment<>(Duration.of(2, Duration.MINUTES), 0);
    ProfileSegment<Integer> p2 = new ProfileSegment<>(Duration.of(5, Duration.MINUTES), 1);
    ProfileSegment<Integer> p3 = new ProfileSegment<>(Duration.of(5, Duration.MINUTES), 2);

    ProfileSegment<Integer> p0 = new ProfileSegment<>(Duration.of(15, Duration.MINUTES), 0);
    Instant t = Instant.ofEpochSecond(366L * 24 * 3600 * 60);
    var list1 = List.of(p1, p2, p3);
    System.out.println(list1);
    var list2 = List.of(p0);
    System.out.println(list2);
    var list3 = mergeSegmentLists(t, t, list2, list1);
    System.out.println("merged list3");
    System.out.println(list3);
    list3 = mergeSegmentLists(t, t, list2, list1);
    System.out.println("merged list3");
    System.out.println(list3);
  }
  public static void main(final String[] args) {
    testMergeSegmentLists();
  }

  // TODO: Looking to modify interleave into a mergeSorted() to merge ProfileSegment Lists, but also need to combine elements.
  //       This wouldn't really avoid any of the messy stuff above, but there's a chance for an efficient Stream.
  public static <T extends Comparable<T>> Stream<T> interleave(Stream<? extends T> a, Stream<? extends T> b) {
    Spliterator<? extends T> spA = a.spliterator(), spB = b.spliterator();
    long s = spA.estimateSize() + spB.estimateSize();
    if(s < 0) s = Long.MAX_VALUE;  // s is negative if there's overflow from addition above
    int ch = spA.characteristics() & spB.characteristics()
             & (Spliterator.NONNULL|Spliterator.SIZED); //|Spliterator.SORTED  // if merging in order instead of interleaving
    ch |= Spliterator.ORDERED;

    return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(s, ch) {
      Spliterator<? extends T> sp1 = spA, sp2 = spB;

      @Override
      public boolean tryAdvance(final Consumer<? super T> action) {
        Spliterator<? extends T> sp = sp1;
        if(sp.tryAdvance(action)) {
          sp1 = sp2;
          sp2 = sp;
          return true;
        }
        return sp2.tryAdvance(action);
      }
    }, false);
  }

  @Override
  public Map<String, Pair<ValueSchema, List<ProfileSegment<SerializedValue>>>> getDiscreteProfiles() {
    return Stream.of(or.getDiscreteProfiles(), nr.getDiscreteProfiles()).flatMap(m -> m.entrySet().stream())
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                           (p1, p2) -> mergeProfiles(or.getStartTime(), nr.getStartTime(), p1, p2)));
  }

  @Override
  public Map<SimulatedActivityId, SimulatedActivity> getSimulatedActivities() {
    var combined = new HashMap<>(or.getSimulatedActivities());
    combined.putAll(nr.getSimulatedActivities());
    return combined;
  }

  @Override
  public Map<SimulatedActivityId, UnfinishedActivity> getUnfinishedActivities() {
    var combined = new HashMap<>(or.getUnfinishedActivities());
    combined.putAll(nr.getUnfinishedActivities());
    return combined;
  }

  @Override
  public List<Triple<Integer, String, ValueSchema>> getTopics() {
    // WARNING: Assuming the same topics in old and new!!!
    return nr.getTopics();
  }

  @Override
  public Map<Duration, List<EventGraph<Pair<Integer, SerializedValue>>>> getEvents() {
    var ors = or.getEvents().entrySet().stream().map(e -> Pair.of(e.getKey().plus(Duration.minus(or.getStartTime(),getStartTime())), e.getValue()));
    var nrs = nr.getEvents().entrySet().stream().map(e -> Pair.of(e.getKey().plus(Duration.minus(nr.getStartTime(),getStartTime())), e.getValue()));
    // overwrite old with new where at the same time
    return Stream.of(ors, nrs).flatMap(s -> s)
                 .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (list1, list2) -> list2));
  }

  @Override
  public String toString() {
    return makeString();
  }
}