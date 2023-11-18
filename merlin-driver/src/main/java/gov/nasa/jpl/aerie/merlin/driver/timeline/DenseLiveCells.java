package gov.nasa.jpl.aerie.merlin.driver.timeline;

public class DenseLiveCells extends LiveCells {

  public static final int DEF_PAGE_SIZE = 4096;
  public static final int DEF_INITIAL_NUM_PAGES = 1;

  private LiveCell[][] pageTable;

  private final int pageSize;

  public DenseLiveCells(final EventSource source) {
    this(DEF_PAGE_SIZE, DEF_INITIAL_NUM_PAGES, source);
  }

  public DenseLiveCells(final EventSource source, final LiveCells parent) {
    this(DEF_PAGE_SIZE, DEF_INITIAL_NUM_PAGES, source, parent);
  }

  public DenseLiveCells(int pageSize, int initialNumPages, final EventSource source) {
    this(pageSize, initialNumPages, source, null);
  }

  public DenseLiveCells(int pageSize, int initialNumPages, final EventSource source, final LiveCells parent) {
    super(source, parent);
    this.pageSize = pageSize;
    if (initialNumPages > 0) {
      pageTable = new LiveCell[initialNumPages][];
      for (int i = 0; i < initialNumPages; i++) {
        pageTable[i] = new LiveCell[pageSize];
      }
    }
  }

  @Override
  protected <State> void write(final Query<State> query, final LiveCell<State> cell) {
    final int page = query.address / pageSize;
    final int numPages = pageTable != null ? pageTable.length : 0;
    if (page >= numPages) {
      final var newPageTable = new LiveCell[Math.max(page + 1, 2 * numPages)][];
      if (pageTable != null) {
        System.arraycopy(pageTable, 0, newPageTable, 0, numPages);
      }
      pageTable = newPageTable;
    }
    if (pageTable[page] == null) {
      pageTable[page] = new LiveCell[pageSize];
    }
    if (pageTable[page][query.address % pageSize] == null) {
    }
    pageTable[page][query.address % pageSize] = cell;
  }

  @Override
  protected <State> LiveCell<State> read(Query<State> query) {
    final int page = query.address / pageSize;
    if (pageTable == null || page >= pageTable.length || pageTable[page] == null) {
      return null;
    }
    // SAFETY: By the invariant, if there is an entry for this query, it is of type Cell<State>.
    @SuppressWarnings("unchecked")
    final var cell = (LiveCell<State>)pageTable[page][query.address % pageSize];
    return cell;
  }
}
