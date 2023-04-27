package gov.nasa.jpl.aerie.merlin.server.remotes.postgres;

import gov.nasa.jpl.aerie.json.JsonParser;
import gov.nasa.jpl.aerie.merlin.driver.engine.ProfileSegment;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.intellij.lang.annotations.Language;

public final class AppendProfileSegmentsAction implements AutoCloseable {
  private final @Language("SQL") String sql =
      """
      insert into profile_segment (dataset_id, profile_id, start_offset, dynamics, is_gap)
      values (?, ?, ?::interval, ?::json, ?)
    """;
  private final PreparedStatement statement;

  public AppendProfileSegmentsAction(final Connection connection) throws SQLException {
    this.statement = connection.prepareStatement(sql);
  }

  public <Dynamics> Duration apply(
      final long datasetId,
      final ProfileRecord profileRecord,
      final List<ProfileSegment<Optional<Dynamics>>> segments,
      final JsonParser<Dynamics> dynamicsP)
      throws SQLException {
    var accumulatedOffset = profileRecord.duration();
    for (final var pair : segments) {
      final var duration = pair.extent();
      final var dynamics = pair.dynamics();

      this.statement.setLong(1, datasetId);
      this.statement.setLong(2, profileRecord.id());
      PreparedStatements.setDuration(this.statement, 3, accumulatedOffset);
      if (dynamics.isPresent()) {
        this.statement.setString(4, serializeDynamics(dynamics.get(), dynamicsP));
        this.statement.setBoolean(5, false);
      } else {
        this.statement.setString(4, "null");
        this.statement.setBoolean(5, true);
      }

      this.statement.addBatch();

      accumulatedOffset = Duration.add(accumulatedOffset, duration);
    }

    final var results = this.statement.executeBatch();
    for (final var result : results) {
      if (result == Statement.EXECUTE_FAILED) throw new FailedInsertException("profile_segment");
    }

    return accumulatedOffset;
  }

  private <Dynamics> String serializeDynamics(
      final Dynamics dynamics, final JsonParser<Dynamics> dynamicsP) {
    return dynamicsP.unparse(dynamics).toString();
  }

  @Override
  public void close() throws SQLException {
    this.statement.close();
  }
}
