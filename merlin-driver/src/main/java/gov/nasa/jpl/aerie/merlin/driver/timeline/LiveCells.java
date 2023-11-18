package gov.nasa.jpl.aerie.merlin.driver.timeline;

import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;

import java.util.Optional;

// INVARIANT: Every Query<T> maps to a LiveCell<T>; that is, the type parameters are correlated.
public abstract class LiveCells {

  private final EventSource source;
  private final LiveCells parent;

  public LiveCells(final EventSource source) {
    this(source, null);
  }

  public LiveCells(final EventSource source, final LiveCells parent) {
    this.source = source;
    this.parent = parent;
  }

  public <State> Optional<State> getState(final Query<State> query) {
    return getCell(query).map(Cell::getState);
  }

  public Optional<Duration> getExpiry(final Query<?> query) {
    return getCell(query).flatMap(Cell::getExpiry);
  }

  public <State> void put(final Query<State> query, final Cell<State> cell) {
    write(query, new LiveCell<>(cell, this.source.cursor()));
  }

  private <State> Optional<Cell<State>> getCell(final Query<State> query) {
    // First, check if we have this cell already.
    {
      final var cell = read(query);

      if (cell != null) return Optional.of(cell.get());
    }

    // Otherwise, go ask our parent for the cell.
    if (this.parent == null) return Optional.empty();
    final var cell$ = this.parent.getCell(query);
    if (cell$.isEmpty()) return Optional.empty();

    final var cell = new LiveCell<>(cell$.get().duplicate(), this.source.cursor());

    // SAFETY: The query and cell share the same State type parameter.
    write(query, cell);

    return Optional.of(cell.get());
  }

  protected abstract <State> void write(final Query<State> query, final LiveCell<State> cell);

  protected abstract <State> LiveCell<State> read(Query<State> query);
}
