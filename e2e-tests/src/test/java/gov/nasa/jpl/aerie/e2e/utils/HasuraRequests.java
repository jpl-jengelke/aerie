package gov.nasa.jpl.aerie.e2e.utils;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.RequestOptions;
import gov.nasa.jpl.aerie.e2e.types.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hasura API request functions
 */
public class HasuraRequests implements AutoCloseable {
  private static final String hasuraAdminSecret = System.getenv("HASURA_GRAPHQL_ADMIN_SECRET");
  private static final Map<String, String> defaultHeaders = Map.of("x-hasura-role", "aerie_admin", "x-hasura-user-id", "Aerie Legacy");

  private final APIRequestContext request;

  public HasuraRequests(Playwright playwright) {
    request = playwright.request().newContext(
            new APIRequest.NewContextOptions()
                    .setBaseURL(BaseURL.HASURA.url)
                    .setTimeout(0)
    );
  }

  @Override
  public void close(){
    request.dispose();
  }

  /**
   * Make a request to Hasura as the `Aerie Legacy` user using the role `aerie_admin`
   * @param query the GQL query or mutation to be executed
   * @param variables a JsonObject containing the query variables for the query
   * @return a JsonObject containing the response from Hasura
   * @throws IOException if the response status is not 200
   */
  private JsonObject makeRequest(GQL query, JsonObject variables)
  throws IOException {
    return makeRequest( query, variables, defaultHeaders);
  }

  /**
   * Make a request to Hasura using custom headers
   * @param query the GQL query or mutation to be executed
   * @param variables a JsonObject containing the query variables for the query
   * @param headers a Map containing the custom headers
   * @return a JsonObject containing the response from Hasura
   * @throws IOException if the response status is not 200
   */
  private JsonObject makeRequest(
      GQL query,
      JsonObject variables,
      Map<String, String> headers
  ) throws IOException {
    // Build Payload
    final String data = Json.createObjectBuilder()
                            .add("query", query.query)
                            .add("variables", variables)
                            .build()
                            .toString(); // Payloads must be JSON Strings and not JSON Objects

    // Set Up Request
    final RequestOptions options = RequestOptions.create()
            .setData(data)
            .setHeader("x-hasura-admin-secret", hasuraAdminSecret);
    headers.forEach(options::setHeader);

    final var response = request.post("/v1/graphql", options);

    // Process Response
    if(!response.ok()){
      throw new IOException(response.statusText());
    }

    try(final var reader = Json.createReader(new StringReader(response.text()))){
      final JsonObject bodyJson = reader.readObject();
      if(bodyJson.containsKey("errors")){
        System.err.println("Errors in response: \n" + bodyJson.get("errors"));
        throw new RuntimeException(bodyJson.toString());
      }
      return bodyJson.getJsonObject("data");
    }
  }

  //region Mission Model
  public int createMissionModel(int jarId, String name, String mission, String version)
  throws IOException, InterruptedException
  {
    final var insertModelBuilder = Json.createObjectBuilder()
                                     .add("jar_id", jarId)
                                     .add("name", name)
                                     .add("mission", mission)
                                     .add("version", version);
    final var variables = Json.createObjectBuilder().add("model", insertModelBuilder).build();
    final var data = makeRequest(GQL.CREATE_MISSION_MODEL, variables).getJsonObject("insert_mission_model_one");
    // Delay 1.25s to guarantee all events associated with model upload have finished
    // Necessary for TS compilation
    Thread.sleep(1250);
    return data.getInt("id");
  }

  public void deleteMissionModel(int id) throws IOException {
    makeRequest(GQL.DELETE_MISSION_MODEL, Json.createObjectBuilder().add("id", id).build());
  }

  public EffectiveModelArguments getEffectiveModelArguments(
      int modelId,
      JsonObject modelArgs
  ) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("modelId", modelId)
                              .add("modelArgs", modelArgs)
                              .build();
    final var results = makeRequest(GQL.GET_EFFECTIVE_MODEL_ARGUMENTS, variables)
        .getJsonObject("getModelEffectiveArguments");
    return EffectiveModelArguments.fromJSON(results);
  }

  public List<ResourceType> getResourceTypes(int missionModelId) throws IOException {
    final var variables = Json.createObjectBuilder().add("missionModelId", missionModelId).build();
    final var data = makeRequest(GQL.GET_RESOURCE_TYPES, variables);
    return data.getJsonArray("resource_type").getValuesAs(ResourceType::fromJSON);
  }

  public List<ActivityType> getActivityTypes(int missionModelId) throws IOException {
    final var variables = Json.createObjectBuilder().add("missionModelId", missionModelId).build();
    final var data = makeRequest(GQL.GET_ACTIVITY_TYPES, variables);
    return data.getJsonArray("activity_type").getValuesAs(ActivityType::fromJSON);
  }
  //endregion

  //region Plan
  public int createPlan(int modelId, String name, String duration, String startTime) throws IOException {
    return createPlan(modelId, name, duration, startTime, defaultHeaders);
  }

  public int createPlan(
      int modelId,
      String name,
      String duration,
      String startTime,
      Map<String, String> headers)
  throws IOException {
    final var insertPlanBuilder = Json.createObjectBuilder()
            .add("model_id", modelId)
            .add("name", name)
            .add("duration", duration)
            .add("start_time", startTime);
    final var variables = Json.createObjectBuilder().add("plan", insertPlanBuilder).build();
    return makeRequest(GQL.CREATE_PLAN, variables, headers).getJsonObject("insert_plan_one").getInt("id");
  }

  public Plan getPlan(int planId) throws IOException {
    final var variables = Json.createObjectBuilder().add("id", planId).build();
    final var plan = makeRequest(GQL.GET_PLAN, variables).getJsonObject("plan");
    return Plan.fromJSON(plan);
  }

  public int getPlanRevision(int planId) throws IOException {
    final var variables = Json.createObjectBuilder().add("id", planId).build();
    return makeRequest(GQL.GET_PLAN_REVISION, variables).getJsonObject("plan").getInt("revision");
  }

  public void deletePlan(int planId) throws IOException {
    final var variables = Json.createObjectBuilder().add("id", planId).build();
    makeRequest(GQL.DELETE_PLAN, variables);
  }

  public int insertActivity(int planId, String type, String startOffset, JsonObject arguments) throws IOException {
    final var insertActivityBuilder = Json.createObjectBuilder()
                                          .add("plan_id", planId)
                                          .add("type", type)
                                          .add("start_offset", startOffset)
                                          .add("arguments", arguments);
    final var variables = Json.createObjectBuilder().add("activityDirectiveInsertInput", insertActivityBuilder).build();
    return makeRequest(GQL.CREATE_ACTIVITY_DIRECTIVE, variables).getJsonObject("createActivityDirective").getInt("id");
  }

  public void deleteActivity(int planId, int activityId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("id", activityId)
                              .build();
    makeRequest(GQL.DELETE_ACTIVITY_DIRECTIVE, variables);
  }

  public EffectiveActivityArguments getEffectiveActivityArguments(
      int modelId,
      String activityType,
      JsonObject activityArguments
  ) throws IOException {
    final var effectiveArgs =  getEffectiveActivityArgumentsBulk(modelId, List.of(Pair.of(activityType, activityArguments)));
    assert(effectiveArgs.size() == 1);
    return effectiveArgs.get(0);
  }

  public List<EffectiveActivityArguments> getEffectiveActivityArgumentsBulk(
      int modelId,
      List<Pair<String, JsonObject>> activities
  ) throws IOException {
    final var activitiesBuilder = Json.createArrayBuilder();
    activities.forEach(pair -> activitiesBuilder.add(Json.createObjectBuilder()
                                                            .add("activityTypeName", pair.getLeft())
                                                            .add("activityArguments", pair.getRight())));
    final var variables = Json.createObjectBuilder()
                              .add("modelId", modelId)
                              .add("activities", activitiesBuilder)
                              .build();
    return makeRequest(GQL.GET_EFFECTIVE_ACTIVITY_ARGUMENTS_BULK, variables)
        .getJsonArray("getActivityEffectiveArgumentsBulk")
        .getValuesAs(EffectiveActivityArguments::fromJSON);
  }

  public Map<Long, ActivityValidation> getActivityValidations(final int planId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("planId", planId)
                              .build();
    final JsonArray response = makeRequest(GQL.GET_ACTIVITY_VALIDATIONS, variables)
        .getJsonArray("activity_directive_validations");
    final var res = new HashMap<Long, ActivityValidation>();
    for (final var object : response) {
      res.put(
          (long) object.asJsonObject().getInt("directive_id"),
          ActivityValidation.fromJSON(object.asJsonObject()));
    }
    return res;
  }

  //endregion

  //region Simulation
  private SimulationResponse simulate(int planId) throws IOException {
    final var variables = Json.createObjectBuilder().add("plan_id", planId).build();
    return SimulationResponse.fromJSON(makeRequest(GQL.SIMULATE, variables).getJsonObject("simulate"));
  }

  /**
   * Simulate the specified plan with a timeout of 30 seconds
   */
  public SimulationResponse awaitSimulation(int planId) throws IOException {
    return awaitSimulation(planId, 30);
  }

  /**
   * Simulate the specified plan with a set timeout
   * @param planId the plan to simulate
   * @param timeout the length of the timeout, in seconds
   */
  public SimulationResponse awaitSimulation(int planId, int timeout) throws IOException {
    for(int i = 0; i < timeout; ++i){
      final var response = simulate(planId);
        switch (response.status()) {
          case "pending", "incomplete" -> {
            try {
              Thread.sleep(1000); // 1s
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
          case "complete" -> {
            return response;
          }
          default -> throw new IOException("Simulation returned bad status " + response.status() + " with reason " +response.reason());
        }
    }
    throw new TimeoutError("Simulation timed out after " + timeout + " seconds");
  }

  public int getSimulationId(int planId) throws IOException {
    final var variables = Json.createObjectBuilder().add("plan_id", planId).build();
    return makeRequest(GQL.GET_SIMULATION_ID, variables).getJsonArray("simulation").getJsonObject(0).getInt("id");
  }

  public int insertAndAssociateSimTemplate(int modelId, String description, JsonObject arguments, int simConfigId)
  throws IOException
  {
    final var insertSimTemplateBuilder = Json.createObjectBuilder()
                                             .add("model_id", modelId)
                                             .add("description", description)
                                             .add("arguments", arguments);
    final var insertVariables = Json.createObjectBuilder().add("simulationTemplate", insertSimTemplateBuilder).build();
    final var templateId = makeRequest(GQL.INSERT_SIMULATION_TEMPLATE, insertVariables)
        .getJsonObject("template")
        .getInt("id");

    final var assignVariables = Json.createObjectBuilder()
                                    .add("simulation_id", simConfigId)
                                    .add("simulation_template_id", templateId)
                                    .build();
    makeRequest(GQL.ASSIGN_TEMPLATE_TO_SIMULATION, assignVariables);
    return templateId;
  }

  public void deleteSimTemplate(int templateId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("templateId", templateId)
                              .build();
    makeRequest(GQL.DELETE_SIMULATION_PRESET, variables);
  }

  public void updateSimBounds(int planId, String simStartTime, String simEndTime) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("simulation_start_time", simStartTime)
                              .add("simulation_end_time", simEndTime)
                              .build();
    makeRequest(GQL.UPDATE_SIMULATION_BOUNDS, variables).getJsonObject("update_simulation");
  }
  //endregion

  //region Scheduling
  private SchedulingResponse schedule(int schedulingSpecId) throws IOException {
    final var variables = Json.createObjectBuilder().add("specificationId", schedulingSpecId).build();
    final var data = makeRequest(GQL.SCHEDULE, variables).getJsonObject("schedule");
    return SchedulingResponse.fromJSON(data);
  }

  /**
   * Run scheduling on the specified scheduling specification with a timeout of 30 seconds
   */
  public SchedulingResponse awaitScheduling(int schedulingSpecId) throws IOException {
    return awaitScheduling(schedulingSpecId, 30);
  }

  /**
   * Run scheduling on the specified scheduling specification with a timeout of 30 seconds
   * @param timeout the length of the timeout, in seconds
   */
  public SchedulingResponse awaitScheduling(int schedulingSpecId, int timeout) throws IOException {
    for(int i = 0; i < timeout; ++i){
      final var response = schedule(schedulingSpecId);
        switch (response.status()) {
          case "pending", "incomplete" -> {
            try {
              Thread.sleep(1000); // 1s
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
          case "complete" -> {
            return response;
          }
          default -> throw new IOException("Scheduling returned bad status " + response.status() + " with reason " +response.reason());
        }
    }
    throw new TimeoutError("Scheduling timed out after " + timeout + " seconds");
  }

  public int insertSchedulingGoal(String name, int modelId, String definition) throws IOException {
    return insertSchedulingGoal(name, modelId, definition, "");
  }

  public int insertSchedulingGoal(String name, int modelId, String definition, String description) throws IOException {
    final var schedulingGoalInputBuilder = Json.createObjectBuilder()
                                          .add("name", name)
                                          .add("model_id", modelId)
                                          .add("definition", definition)
                                          .add("description", description);
    final var variables = Json.createObjectBuilder().add("goal", schedulingGoalInputBuilder).build();
    return makeRequest(GQL.CREATE_SCHEDULING_GOAL, variables).getJsonObject("goal").getInt("id");
  }

  public void deleteSchedulingGoal(int goalId) throws IOException {
    final var variables = Json.createObjectBuilder().add("goalId", goalId).build();
    makeRequest(GQL.DELETE_SCHEDULING_GOAL, variables);
  }

  public int insertSchedulingSpecification(
      int planId,
      int planRevision,
      String horizonStart,
      String horizonEnd,
      JsonObject simArguments,
      boolean analysisOnly
  ) throws IOException {
    final var schedulingSpecInputBuilder = Json.createObjectBuilder()
                                               .add("plan_id", planId)
                                               .add("plan_revision", planRevision)
                                               .add("horizon_start", horizonStart)
                                               .add("horizon_end", horizonEnd)
                                               .add("simulation_arguments", simArguments)
                                               .add("analysis_only", analysisOnly);
    final var variables = Json.createObjectBuilder().add("scheduling_spec", schedulingSpecInputBuilder).build();
    return makeRequest(GQL.INSERT_SCHEDULING_SPECIFICATION, variables).getJsonObject("scheduling_spec").getInt("id");
  }

  public void updatePlanRevisionSchedulingSpec(int planId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("planId", planId)
                              .add("planRev", getPlanRevision(planId))
                              .build();
    makeRequest(GQL.UPDATE_SCHEDULING_SPECIFICATION_PLAN_REVISION, variables);
  }

  public void createSchedulingSpecGoal(int goalId, int specificationId, int priority) throws IOException {
    final var schedulingSpecGoalInsertBuilder = Json.createObjectBuilder()
                                                    .add("goal_id", goalId)
                                                    .add("specification_id", specificationId)
                                                    .add("priority", priority);
    final var variables = Json.createObjectBuilder().add("spec_goal", schedulingSpecGoalInsertBuilder).build();
    makeRequest(GQL.CREATE_SCHEDULING_SPEC_GOAL, variables);
  }

  public SchedulingDSLTypesResponse getSchedulingDslTypeScript(int missionModelId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("missionModelId", missionModelId)
                              .build();
    return SchedulingDSLTypesResponse.fromJSON(makeRequest(GQL.GET_SCHEDULING_DSL_TYPESCRIPT, variables)
                                                   .getJsonObject("schedulingDslTypescript"));
  }

  public SchedulingDSLTypesResponse getSchedulingDslTypeScript(int missionModelId, int planId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("missionModelId", missionModelId)
                              .add("planId", planId)
                              .build();
    return SchedulingDSLTypesResponse.fromJSON(makeRequest(GQL.GET_SCHEDULING_DSL_TYPESCRIPT, variables)
                                                   .getJsonObject("schedulingDslTypescript"));
  }
  //endregion

  //region Simulation Datasets
  public SimulationDataset getSimulationDataset(int simDatasetId) throws IOException {
    final var data = makeRequest(GQL.GET_SIMULATION_DATASET, Json.createObjectBuilder().add("id", simDatasetId).build())
            .getJsonObject("simulationDataset");
    return SimulationDataset.fromJSON(data);
  }
  public SimulationDataset getSimulationDatasetByDatasetId(int datasetId) throws IOException {
    final var data = makeRequest(
            GQL.GET_SIMULATION_DATASET_BY_DATASET_ID,
            Json.createObjectBuilder().add("id", datasetId).build())
            .getJsonArray("simulation_dataset");
    assert(data.size() == 1);
    return SimulationDataset.fromJSON(data.getJsonObject(0));
  }
  public Map<String, List<ProfileSegment>> getProfiles(int datasetId) throws IOException {
    final var variables = Json.createObjectBuilder().add("datasetId", datasetId).build();
    final var profiles = makeRequest(GQL.GET_PROFILES, variables).getJsonArray("profile");

    // Process Profile Map
    final var map = new HashMap<String, List<ProfileSegment>>();
    for(final var entry : profiles) {
      final JsonObject e = entry.asJsonObject();
      final String name = e.getString("name");
      map.put(name, e.getJsonArray("profile_segments").getValuesAs(ProfileSegment::fromJSON));
    }
    return map;
  }

  public Map<String, Topic> getTopicsEvents(int datasetId) throws IOException {
    final var variables = Json.createObjectBuilder().add("datasetId", datasetId).build();
    final var topics = makeRequest(GQL.GET_TOPIC_EVENTS, variables).getJsonArray("topic");
    final var topicList = topics.getValuesAs(Topic::fromJSON);
    // Collect into map for ease of use
    return topicList.stream().collect(Collectors.toMap(Topic::name, Function.identity()));
  }

  public int insertSimDataset(
      int simId,
      String simStartTime,
      String simEndTime,
      String status,
      JsonObject simArguments,
      int planRevision
  ) throws IOException {
    final var insertSimDatasetBuilder = Json.createObjectBuilder()
                                          .add("simulation_id", simId)
                                          .add("simulation_start_time", simStartTime)
                                          .add("simulation_end_time", simEndTime)
                                          .add("status", status)
                                          .add("arguments", simArguments)
                                          .add("plan_revision", planRevision);
    final var variables = Json.createObjectBuilder().add("simulationDataset", insertSimDatasetBuilder).build();
    // Only the Hasura Admin role may insert into this table
    return makeRequest(GQL.INSERT_SIMULATION_DATASET, variables, Map.of("x-hasura-role", "admin"))
        .getJsonObject("simulation_dataset")
        .getInt("dataset_id");
  }

  public void insertProfile(
      int datasetId,
      String name,
      String duration,
      JsonObject type,
      List<ProfileSegment> segments
  ) throws IOException
  {
    final var hasuraAdminHeader = Map.of("x-hasura-role", "admin");
    // Insert Profile
    final var profileVariables = Json.createObjectBuilder()
                                     .add("datasetId", datasetId)
                                     .add("duration", duration)
                                     .add("name", name)
                                     .add("type", type)
                                     .build();
    final int profileId = makeRequest(GQL.INSERT_PROFILE, profileVariables, hasuraAdminHeader)
        .getJsonObject("insert_profile_one")
        .getInt("id");

    // Insert Profile Segments
    final var segmentsBuilder = Json.createArrayBuilder();
    segments.forEach(s -> segmentsBuilder.add(s.toJSON(datasetId, profileId)));
    final var segmentVariables = Json.createObjectBuilder()
                                     .add("segments", segmentsBuilder)
                                     .build();
    makeRequest(GQL.INSERT_PROFILE_SEGMENTS, segmentVariables, hasuraAdminHeader);
  }
  //endregion

  //region External Datasets
  public int insertExternalDataset(
      int planId,
      String datasetStartTimestamp,
      List<ExternalDataset.ProfileInput> profileSet
  ) throws IOException {
    final var profileSetBuilder = Json.createObjectBuilder();
    profileSet.forEach(e -> profileSetBuilder.add(e.name(), e.toJSON()));
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("simulation_dataset_id", JsonValue.NULL)
                              .add("dataset_start", datasetStartTimestamp)
                              .add("profile_set",profileSetBuilder)
                              .build();
    return makeRequest(GQL.ADD_EXTERNAL_DATASET, variables)
        .getJsonObject("addExternalDataset")
        .getInt("datasetId");
  }

  public int insertExternalDataset(
      int planId,
      int simulationDatasetId,
      String datasetStartTimestamp,
      List<ExternalDataset.ProfileInput> profileSet) throws IOException {
        final var profileSetBuilder = Json.createObjectBuilder();
    profileSet.forEach(e -> profileSetBuilder.add(e.name(), e.toJSON()));
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("simulation_dataset_id", simulationDatasetId)
                              .add("dataset_start", datasetStartTimestamp)
                              .add("profile_set",profileSetBuilder)
                              .build();
    return makeRequest(GQL.ADD_EXTERNAL_DATASET, variables)
        .getJsonObject("addExternalDataset")
        .getInt("datasetId");
  }

  public void extendExternalDataset(int datasetId, List<ExternalDataset.ProfileInput> profileSet) throws IOException {
    final var profileSetBuilder = Json.createObjectBuilder();
    profileSet.forEach(e -> profileSetBuilder.add(e.name(), e.toJSON()));
    final var variables = Json.createObjectBuilder()
                              .add("dataset_id", datasetId)
                              .add("profile_set", profileSetBuilder)
                              .build();
    makeRequest(GQL.EXTEND_EXTERNAL_DATASET, variables);
  }

  public ExternalDataset getExternalDataset(int planId, int datasetId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("dataset_id", datasetId)
                              .build();
    final var dataset = makeRequest(GQL.GET_EXTERNAL_DATASET, variables).getJsonObject("externalDataset");
    return ExternalDataset.fromJSON(dataset);
  }

  public void deleteExternalDataset(int planId, int datasetId) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("plan_id", planId)
                              .add("dataset_id", datasetId)
                              .build();
    makeRequest(GQL.DELETE_EXTERNAL_DATASET, variables);
  }
  //endregion

  //region Constraints
  public List<ConstraintRecord> checkConstraints(int planID) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("planId", planID)
                              .add("simulationDatasetId", JsonValue.NULL)
                              .build();
    final var constraintResults = makeRequest(GQL.CHECK_CONSTRAINTS, variables).getJsonArray("constraintViolations");
    return constraintResults.getValuesAs(e -> ConstraintRecord.fromJSON(e.asJsonObject()));
  }

  public List<ConstraintRecord> checkConstraints(int planID, int simulationDatasetID) throws IOException {
    final var variables = Json.createObjectBuilder()
                              .add("planId", planID)
                              .add("simulationDatasetId", simulationDatasetID)
                              .build();
    final var constraintResults = makeRequest(GQL.CHECK_CONSTRAINTS, variables).getJsonArray("constraintViolations");
    return constraintResults.getValuesAs(e -> ConstraintRecord.fromJSON(e.asJsonObject()));
  }

  public List<CachedConstraintRun> getConstraintRuns(int simulationDatasetId) throws IOException {
    final var variables = Json.createObjectBuilder().add("simulationDatasetId", simulationDatasetId).build();
    final var cachedRuns = makeRequest(GQL.GET_CONSTRAINT_RUNS, variables).getJsonArray("constraint_run");
    return cachedRuns.getValuesAs(e -> CachedConstraintRun.fromJSON(e.asJsonObject()));
  }

  public int insertPlanConstraint(String name, int planId, String definition, String description) throws IOException {
    final var constraintInsertBuilder = Json.createObjectBuilder()
                                            .add("name", name)
                                            .add("plan_id", planId)
                                            .add("definition", definition)
                                            .add("description", description);
    final var variables = Json.createObjectBuilder().add("constraint", constraintInsertBuilder).build();
    return makeRequest(GQL.INSERT_CONSTRAINT, variables).getJsonObject("constraint").getInt("id");
  }

  public void updateConstraint(int constraintId, String definition) throws IOException{
    final var variables = Json.createObjectBuilder()
                              .add("constraintId", constraintId)
                              .add("constraintDefinition", definition)
                              .build();
    makeRequest(GQL.UPDATE_CONSTRAINT, variables);
  }

  public void deleteConstraint(int constraintId) throws IOException {
    final var variables = Json.createObjectBuilder().add("id", constraintId).build();
    makeRequest(GQL.DELETE_CONSTRAINT, variables);
  }
  //endregion

  //region User and Roles
  public void createUser(User user) throws IOException {
    final var userInsertBuilder = Json.createObjectBuilder()
                                      .add("username", user.name())
                                      .add("default_role", user.defaultRole());
    final var allowedRolesBuilder = Json.createObjectBuilder();
    for(final var role : user.allowedRoles()) {
      allowedRolesBuilder.add("username", user.name());
      allowedRolesBuilder.add("allowed_role", role);
    }

    final var variables = Json.createObjectBuilder()
                              .add("user", userInsertBuilder)
                              .add("allowed_roles", allowedRolesBuilder)
                              .build();
    makeRequest(GQL.CREATE_USER, variables);
  }

  public void deleteUser(User user) throws IOException {
    final var variables = Json.createObjectBuilder().add("username", user.name()).build();
    makeRequest(GQL.DELETE_USER, variables);
  }

  public void addPlanCollaborator(User user, int planId) throws IOException {
    final var planCollabBuilder = Json.createObjectBuilder().add("planId", planId).add("collaborator", user.name());
    final var variables = Json.createObjectBuilder().add("planCollaboratorInsertInput", planCollabBuilder).build();
    makeRequest(GQL.ADD_PLAN_COLLABORATOR, variables);
  }

  public ActionPermissionsSet getActionPermissionsForRole(String role) throws IOException {
    final var variables = Json.createObjectBuilder().add("role", role).build();
    final var permissions = makeRequest(GQL.GET_ROLE_ACTION_PERMISSIONS, variables).getJsonObject("permissions");
    return ActionPermissionsSet.fromJSON(permissions.getJsonObject("action_permissions"));
  }

  public void updateActionPermissionsForRole(String role, ActionPermissionsSet permissions) throws IOException{
    final var variables = Json.createObjectBuilder()
                              .add("role", role)
                              .add("action_permissions", permissions.toJSON())
                              .build();
    makeRequest(GQL.UPDATE_ROLE_ACTION_PERMISSIONS, variables);
  }
  //endregion
}


