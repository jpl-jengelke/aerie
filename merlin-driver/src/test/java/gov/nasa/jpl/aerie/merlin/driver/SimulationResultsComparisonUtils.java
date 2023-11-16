package gov.nasa.jpl.aerie.merlin.driver;

import gov.nasa.jpl.aerie.merlin.driver.engine.ProfileSegment;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SimulationResultsComparisonUtils {
  public static void assertEqualsSimResultsClipper(final SimulationResults expected, final SimulationResults simulationResults2){
    assertEquals(expected.unfinishedActivities, simulationResults2.unfinishedActivities);
    assertEquals(expected.topics, simulationResults2.topics);
    assertEqualsTSA(convertSimulatedActivitiesToTree(expected), convertSimulatedActivitiesToTree(simulationResults2));
    for(final var discreteProfile: simulationResults2.discreteProfiles.entrySet()){
      final var filteredActualProfileElements = new ArrayList<ProfileSegment<SerializedValue>>();
      discreteProfile.getValue().getRight().forEach(a -> filteredActualProfileElements.add(new ProfileSegment<>(a.extent(), removeFieldsFromSerializedValue(a.dynamics(), List.of("uuid")))));
      final var filteredExpectedProfileElements = new ArrayList<ProfileSegment<SerializedValue>>();
      expected.discreteProfiles.get(discreteProfile.getKey()).getRight().forEach(a -> filteredExpectedProfileElements.add(new ProfileSegment<>(a.extent(), removeFieldsFromSerializedValue(a.dynamics(), List.of("uuid")))));
      if(!filteredExpectedProfileElements.equals(filteredActualProfileElements)){
        fail();
      }
    }
    for(final var realProfile: simulationResults2.realProfiles.entrySet()){
      final var profileElements = realProfile.getValue().getRight();
      final var expectedProfileElements = expected.realProfiles.get(realProfile.getKey()).getRight();
      if(!profileElements.equals(expectedProfileElements)) {
        fail();
      }
    }
  }

  public static SerializedValue removeFieldsFromSerializedValue(
      SerializedValue serializedValue,
      final Collection<String> fieldsToRemove){
    final var visitor = new SerializedValue.Visitor<SerializedValue>(){
      @Override
      public SerializedValue onNull() {
        return SerializedValue.NULL;
      }

      @Override
      public SerializedValue onNumeric(final BigDecimal value) {
        return SerializedValue.of(value);
      }

      @Override
      public SerializedValue onBoolean(final boolean value) {
        return SerializedValue.of(value);
      }

      @Override
      public SerializedValue onString(final String value) {
        return SerializedValue.of(value);
      }

      @Override
      public SerializedValue onMap(final Map<String, SerializedValue> value) {
        final var newVal = new HashMap<String, SerializedValue>();
        for(final var entry: value.entrySet()){
          if(!fieldsToRemove.contains(entry.getKey())){
            newVal.put(entry.getKey(), removeFieldsFromSerializedValue(entry.getValue(), fieldsToRemove));
          }
        }
        return SerializedValue.of(newVal);
      }

      @Override
      public SerializedValue onList(final List<SerializedValue> value) {
        final var newList = new ArrayList<SerializedValue>();
        for(final var val : value){
          newList.add(removeFieldsFromSerializedValue(val, fieldsToRemove));
        }
        return SerializedValue.of(newList);
      }
    };
    return serializedValue.match(visitor);
  }

  public static Set<TreeSimulatedActivity> convertSimulatedActivitiesToTree(final SimulationResults simulationResults){
    return simulationResults.simulatedActivities.values().stream().map(simulatedActivity -> TreeSimulatedActivity.fromSimulatedActivity(
        simulatedActivity,
        simulationResults)).collect(Collectors.toSet());
  }

  public static void assertEqualsTSA(final Set<TreeSimulatedActivity> expected,
                                     final Set<TreeSimulatedActivity> actual){
    assertEquals(expected.size(), actual.size());
    for(final var inB: actual){
      if(!expected.contains(inB)){
        fail();
      }
    }
  }

  public record TreeSimulatedActivity(StrippedSimulatedActivity activity,
                                      Set<TreeSimulatedActivity> children){
    public static TreeSimulatedActivity fromSimulatedActivity(SimulatedActivity simulatedActivity, SimulationResults simulationResults){
      final var stripped = StrippedSimulatedActivity.fromSimulatedActivity(simulatedActivity);
      final HashSet<TreeSimulatedActivity> children = new HashSet<>();
      for(final var childId: simulatedActivity.childIds()) {
        final var child = fromSimulatedActivity(simulationResults.simulatedActivities.get(childId), simulationResults);
        children.add(child);
      }
      return new TreeSimulatedActivity(stripped, children);
    }
  }

  public record StrippedSimulatedActivity(
      String type,
      Map<String, SerializedValue> arguments,
      Instant start,
      Duration duration,
      SerializedValue computedAttributes
  ){
    public static StrippedSimulatedActivity fromSimulatedActivity(SimulatedActivity simulatedActivity){
      return new StrippedSimulatedActivity(
          simulatedActivity.type(),
          simulatedActivity.arguments(),
          simulatedActivity.start(),
          simulatedActivity.duration(),
          simulatedActivity.computedAttributes()
      );
    }
  }
}
