package gov.nasa.jpl.aerie.contrib.models;

import gov.nasa.jpl.aerie.contrib.cells.linear.LinearAccumulationEffect;
import gov.nasa.jpl.aerie.contrib.cells.linear.LinearIntegrationCell;
import gov.nasa.jpl.aerie.merlin.framework.Cell;
import gov.nasa.jpl.aerie.merlin.framework.resources.real.RealResource;
import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics;

import java.util.function.Function;

public final class Accumulator implements RealResource {
  private final Cell<LinearAccumulationEffect, LinearIntegrationCell> cell;

  public final Rate rate = new Rate();

  public Accumulator() {
    this(0.0, 0.0);
  }

  public Accumulator(final double initialVolume, final double initialRate) {
    this.cell = LinearIntegrationCell.allocate(initialVolume, initialRate, Function.identity());
  }

  @Override
  public RealDynamics getDynamics() {
    return this.cell.get().getVolume();
  }

  @Deprecated
  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }


  public final class Rate implements RealResource {
    @Override
    public RealDynamics getDynamics() {
      return Accumulator.this.cell.get().getRate();
    }

    public void add(final double delta) {
      Accumulator.this.cell.emit(LinearAccumulationEffect.addRate(delta));
    }

    @Deprecated
    @Override
    public boolean equals(final Object obj) {
      return super.equals(obj);
    }
  }
}
