package gov.nasa.jpl.aerie.merlin.driver.timeline;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SparseLiveCells extends LiveCells {

  private int quickAddress0 = -1;
  private LiveCell quickCell0;

  private int quickAddress1 = -1;
  private LiveCell quickCell1;

  private int quickAddress2 = -1;
  private LiveCell quickCell2;

  private int quickAddress3 = -1;
  private LiveCell quickCell3;

  private Map<Query<?>, LiveCell<?>> cellMap;

  public SparseLiveCells(final EventSource source) {
    this(source, null);
  }

  public SparseLiveCells(final EventSource source, final LiveCells parent) {
    super(source, parent);
  }

  @Override
  protected <State> void write(final Query<State> query, final LiveCell<State> cell) {
    if (quickAddress0 == -1 || quickAddress0 == query.address) {
      quickAddress0 = query.address;
      quickCell0 = cell;
    } else if (quickAddress1 == -1 || quickAddress1 == query.address) {
      quickAddress1 = query.address;
      quickCell1 = cell;
    } else if (quickAddress2 == -1 || quickAddress2 == query.address) {
      quickAddress2 = query.address;
      quickCell2 = cell;
    } else if (quickAddress3 == -1 || quickAddress3 == query.address) {
      quickAddress3 = query.address;
      quickCell3 = cell;
    } else {
      if (cellMap == null) {
        cellMap = new HashMap<>();
      }
      cellMap.put(query, cell);
    }
  }

  @Override
  protected <State> LiveCell<State> read(Query<State> query) {
    LiveCell ret = null;
    if (query.address == quickAddress0) {
      ret = quickCell0;
    } else if (query.address == quickAddress1) {
      ret = quickCell1;
    } else if (query.address == quickAddress2) {
      ret = quickCell2;
    } else if (query.address == quickAddress3) {
      ret = quickCell3;
    } else if (cellMap != null) {
      // SAFETY: By the invariant, if there is an entry for this query, it is of type Cell<State>.
      ret = cellMap.get(query);
    }
    @SuppressWarnings("unchecked")
    final var cell = (LiveCell<State>)ret;
    return cell;
  }
}
