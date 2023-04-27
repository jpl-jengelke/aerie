package gov.nasa.jpl.aerie.contrib.serialization.mappers;

import gov.nasa.jpl.aerie.merlin.framework.Result;
import gov.nasa.jpl.aerie.merlin.framework.ValueMapper;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import gov.nasa.jpl.aerie.merlin.protocol.types.ValueSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class PrimitiveShortArrayValueMapper implements ValueMapper<short[]> {
  @Override
  public ValueSchema getValueSchema() {
    return ValueSchema.ofSeries(ValueSchema.INT);
  }

  @Override
  public Result<short[], String> deserializeValue(SerializedValue serializedValue) {
    var elementMapper = new ShortValueMapper();
    return serializedValue
        .asList()
        .map(
            (Function<List<SerializedValue>, Result<List<SerializedValue>, String>>)
                Result::success)
        .orElseGet(() -> Result.failure("Expected list, got " + serializedValue.toString()))
        .match(
            serializedElements -> {
              final short[] elements = new short[serializedElements.size()];
              int index = 0;
              for (final var serializedElement : serializedElements) {
                final var result = elementMapper.deserializeValue(serializedElement);
                if (result.getKind() == Result.Kind.Failure)
                  return result.mapSuccess(_left -> null);

                // SAFETY: `result` must be a Success variant.
                elements[index++] = result.getSuccessOrThrow();
              }
              return Result.success(elements);
            },
            Result::failure);
  }

  @Override
  public SerializedValue serializeValue(short[] elements) {
    final var serializedElements = new ArrayList<SerializedValue>(elements.length);
    for (final var element : elements) {
      serializedElements.add(SerializedValue.of(element));
    }
    return SerializedValue.of(serializedElements);
  }
}
