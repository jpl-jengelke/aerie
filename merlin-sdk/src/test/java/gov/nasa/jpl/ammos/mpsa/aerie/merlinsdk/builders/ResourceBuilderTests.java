package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.builders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.HashSet;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.LinearCombinationResource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.Resource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Time;
import org.junit.Test;

public class ResourceBuilderTests {

  @Test
  public void testResourceBuilderCanSetName() {
    String name = "peel";
    ResourceBuilder builder = new ResourceBuilder().withName(name);
    Resource resource = builder.getResource();
    assertSame(resource.getName(), name);
  }

  @Test
  public void testResourceBuilderCanSetType() {
    ResourceBuilder builder = new ResourceBuilder().ofType(Integer.class);
    Resource resource = builder.getResource();
    assertSame(resource.getType(), Integer.class);
  }

  @Test
  public void testResourceBuilderCanSetInitialValue() {
    Object val = 3;
    ResourceBuilder builder = new ResourceBuilder().withInitialValue(val);
    Resource resource = builder.getResource();
    assertSame(resource.getCurrentValue(), val);
  }

  @Test
  public void testResourceBuilderCanSetSubsystem() {
    String subsystem= "peel";
    ResourceBuilder builder = new ResourceBuilder().forSubsystem(subsystem);
    Resource resource = builder.getResource();
    assertSame(resource.getSubsystem(), subsystem);
  }

  @Test
  public void testResourceBuilderCanSetUnits() {
    String units = "sections";
    ResourceBuilder builder = new ResourceBuilder().withUnits(units);
    Resource resource = builder.getResource();
    assertSame(resource.getUnits(), units);
  }

  @Test
  public void testResourceBuilderCanSetInterpolation() {
    String interpolation = "linear";
    ResourceBuilder builder = new ResourceBuilder().withInterpolation(interpolation);
    Resource resource = builder.getResource();
    assertSame(resource.getInterpolation(), interpolation);
  }

  @Test
  public void testResourceBuilderCanSetAllowedValues() {
    HashSet<Integer> allowedValues = new HashSet<>();
    ResourceBuilder builder = new ResourceBuilder().withAllowedValues(allowedValues);
    Resource resource = builder.getResource();
    assertSame(resource.getAllowedValues(), allowedValues);
  }

  // TODO: Move to Resource tests when they exist
  @Test
  public void testResourceBuilderCanSetAnAllowedValue() {
    Integer value = 3;
    HashSet<Integer> allowedValues = new HashSet<>();
    allowedValues.add(value);
    ResourceBuilder builder = new ResourceBuilder().withAllowedValues(allowedValues);
    Resource resource = builder.getResource();
    resource.setValue(value);
    assertSame(resource.getCurrentValue(), value);
  }

  // TODO: Move to Resource tests when they exist
  @Test
  public void testResourceBuilderCannotSetAnUnspecififedValue() {
    Integer value = 3;
    HashSet<Integer> allowedValues = new HashSet<>(); // No allowed values
    ResourceBuilder builder = new ResourceBuilder().withAllowedValues(allowedValues);
    Resource resource = builder.getResource();

    // Expect throw
    boolean caughtError = false;
    try {
      resource.setValue(value);
    } catch (RuntimeException e) {
      caughtError = true;
    }

    assertSame(resource.getCurrentValue(), null);
    assertSame(caughtError, true);
  }

  @Test
  public void testResourceBuilderCanSetMinimum() {
    Integer min = 7;
    ResourceBuilder builder = new ResourceBuilder().withMin(min);
    Resource resource = builder.getResource();
    assertSame(resource.getMinimum(), min);
  }

  @Test
  public void testResourceBuilderCanSetMaximum() {
    Integer max = 7;
    ResourceBuilder builder = new ResourceBuilder().withMax(max);
    Resource resource = builder.getResource();
    assertSame(resource.getMaximum(), max);
  }

  @Test
  public void testResourceBuilderCanSetFrozen() {
    boolean isFrozen = false;
    ResourceBuilder builder = new ResourceBuilder().isFrozen(isFrozen);
    Resource resource = builder.getResource();
    assertSame(resource.isFrozen(), isFrozen);
  }

  @Test
  public void testResourceBuilderLinearCombinationFromDoubleResources() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue(1200.0)
                .withUnits("mW")
                .getResource();
    Resource powerDraw2 = new ResourceBuilder()
                .withName("Power_Draw_2")
                .withInitialValue(2.2)
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 0.001)
                .withTerm(powerDraw2, 1.0)
                .withUnits("W")
                .getResource();
    assertEquals(3.4, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

  @Test
  public void testResourceBuilderLinearCombinationFromIntegerResources() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue(1)
                .withUnits("W")
                .getResource();
    Resource powerDraw2 = new ResourceBuilder()
                .withName("Power_Draw_2")
                .withInitialValue(2)
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 1.0)
                .withTerm(powerDraw2, 1.0)
                .withUnits("W")
                .getResource();
    assertEquals(3.0, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResourceBuilderLinearCombinationFromStringResources() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue("A")
                .withUnits("W")
                .getResource();
    Resource powerDraw2 = new ResourceBuilder()
                .withName("Power_Draw_2")
                .withInitialValue("B")
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 1)
                .withTerm(powerDraw2, 1)
                .withUnits("W")
                .getResource();
  }

  @Test
  public void testLinearCombinationResourcePropertyChange() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue(1.2)
                .withUnits("W")
                .getResource();
    Resource powerDraw2 = new ResourceBuilder()
                .withName("Power_Draw_2")
                .withInitialValue(2.2)
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 1)
                .withTerm(powerDraw2, 1)
                .withUnits("W")
                .getResource();
    assertEquals(3.4, (double) totalPowerDraw.getCurrentValue(), 1e-15);
    powerDraw1.setValue(0.2);
    assertEquals(2.4, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

  @Test
  public void testResourceBuilderLinearCombinationFromOneResource() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue(1.2)
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 1.0)
                .withUnits("W")
                .getResource();
    assertEquals(1.2, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

  @Test
  public void testResourceBuilderLinearCombinationFromTenResources() {
    ResourceBuilder totalPowerDrawBuilder = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withUnits("W");
    for (int i = 1; i <= 10; i++) {
        Resource powerDraw = new ResourceBuilder()
                .withName("Power_Draw_"+i)
                .withInitialValue((double) i)
                .withUnits("W")
                .getResource();
        totalPowerDrawBuilder.withTerm(powerDraw, 1.0);
    }
    Resource totalPowerDraw = totalPowerDrawBuilder.getResource();
    assertEquals(55.0, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

  @Test
  public void testLinearCombinationResourceManyPropertyChanges() {
    Resource powerDraw1 = new ResourceBuilder()
                .withName("Power_Draw_1")
                .withInitialValue(1.2)
                .withUnits("W")
                .getResource();
    Resource powerDraw2 = new ResourceBuilder()
                .withName("Power_Draw_2")
                .withInitialValue(2.2)
                .withUnits("W")
                .getResource();
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withTerm(powerDraw1, 1)
                .withTerm(powerDraw2, 1)
                .withUnits("W")
                .getResource();
    assertEquals(3.4, (double) totalPowerDraw.getCurrentValue(), 1e-15);
    for (int i = 0; i < 10; i++) {
        powerDraw1.setValue((double)i);
        assertEquals(2.2+i, (double) totalPowerDraw.getCurrentValue(), 1e-15);
    }
  }

  @Test
  public void testResourceBuilderLinearCombinationFromZeroResources() {
    Resource totalPowerDraw = new ResourceBuilder(LinearCombinationResource.class)
                .withName("Total_Power_Draw")
                .withUnits("W")
                .getResource();
    assertEquals(0.0, (double) totalPowerDraw.getCurrentValue(), 1e-15);
  }

}
