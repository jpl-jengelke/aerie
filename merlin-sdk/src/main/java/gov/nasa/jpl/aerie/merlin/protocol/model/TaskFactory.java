package gov.nasa.jpl.aerie.merlin.protocol.model;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A factory for creating fresh copies of a task. All tasks created by a factory must be observationally equivalent.
 *
 * @param <Input>
 *   The type of data required by a task created by this factory.
 * @param <Output>
 *   The type of data returned by a task created by this factory.
 */
public interface TaskFactory<Input, Output> {
  Task<Input, Output> create(Executor executor);

  /**
   * Create a new factory that transforms the tasks produced by this factory.
   *
   * @param transform
   *   The transformation to apply to the tasks produced by this factory.
   * @return
   *   A task factory producing transformed versions of this factory's tasks.
   * @param <I2>
   *   The new input type for transformed tasks.
   * @param <O2>
   *   The new output type for transformed tasks.
   */
  default <I2, O2> TaskFactory<I2, O2> map(final Function<Task<Input, Output>, Task<I2, O2>> transform) {
    return executor -> transform.apply(this.create(executor));
  }

  /**
   * Perform another task following this one.
   *
   * @param <X>
   *   The type of the output of the suffixed task.
   * @param suffix
   *   The task to perform following this one.
   * @return
   *   A task performing this task and the suffixed task in sequence.
   */
  default <X> TaskFactory<Input, X> andThen(final Task<Output, X> suffix) {
    return executor -> this.create(executor).andThen(suffix);
  }

  /**
   * Perform another task preceding this one.
   *
   * @param <X>
   *   The type of the output of the prefixed task.
   * @param prefix
   *   The task to perform preceding this one.
   * @return
   *   A task performing the prefixed task and this task in sequence.
   */
  default <X> TaskFactory<X, Output> butFirst(final Task<X, Input> prefix) {
    return executor -> this.create(executor).butFirst(prefix);
  }
}
