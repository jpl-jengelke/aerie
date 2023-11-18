package gov.nasa.jpl.aerie.merlin.driver.timeline;

public final class Query<State> {
  public static int nextAddress;
  public final int address;
  public Query() {
    address = nextAddress++;
  }
}
