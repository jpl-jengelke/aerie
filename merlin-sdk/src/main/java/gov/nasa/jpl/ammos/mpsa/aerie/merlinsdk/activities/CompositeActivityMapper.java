package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.ValueSchema;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.annotations.ActivityType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.serialization.SerializedActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;

public class CompositeActivityMapper implements ActivityMapper {
  private final Map<String, ActivityMapper> activityMappers;
  private final Map<String, Map<String, ValueSchema>> activitySchemas;

  public CompositeActivityMapper(final Map<String, ActivityMapper> activityMappers) {
    this.activityMappers = activityMappers;
    this.activitySchemas = immutableActivitySchemas(activityMappers);
  }

  private Optional<ActivityMapper> lookupMapper(final String activityType) {
    return Optional.ofNullable(this.activityMappers.get(activityType));
  }


  @Override
  public Map<String, Map<String, ValueSchema>> getActivitySchemas() {
    return this.activitySchemas;
  }

  @Override
  public Optional<Activity> deserializeActivity(final SerializedActivity activity) {
    final String activityType = activity.getTypeName();
    return lookupMapper(activityType).flatMap(m -> m.deserializeActivity(activity));
  }

  @Override
  public Optional<SerializedActivity> serializeActivity(final Activity activity) {
    final String activityType = activity.getClass().getAnnotation(ActivityType.class).name();
    return lookupMapper(activityType).flatMap(m -> m.serializeActivity(activity));
  }

  private static Map<String, Map<String, ValueSchema>> immutableActivitySchemas(final Map<String, ActivityMapper> activityMappers) {
    final Map<String, Map<String, ValueSchema>> clonedSchemas = new HashMap<>();

    for (final var mapper : activityMappers.values()) {
      for (final var activityEntry : mapper.getActivitySchemas().entrySet()) {
        final String activityName = activityEntry.getKey();
        final Map<String, ValueSchema> activityParameters = new HashMap<>(activityEntry.getValue());

        clonedSchemas.put(activityName, activityParameters);
      }
    }

    return unmodifiableMap(clonedSchemas);
  }
}