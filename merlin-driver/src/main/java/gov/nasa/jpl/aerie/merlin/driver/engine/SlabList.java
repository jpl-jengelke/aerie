package gov.nasa.jpl.aerie.merlin.driver.engine;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An append-only list comprising a chain of fixed-size slabs.
 *
 * The fixed-size slabs allow for better cache locality when traversing the list forward,
 * and the chain of links allows for cheap extension when a slab reaches capacity.
 */
public final class SlabList<T> implements Iterable<T> {

  /** ~4 KiB of elements (or at least, references thereof). */
  private static final int SLAB_SIZE = 1024;

  private final Slab<T> head = new Slab<>();

  /*derived*/
  private Slab<T> tail = head;

  /*derived*/
  private int size = 0;

  public void append(final T element) {
    if (tail.numElements == SLAB_SIZE) {
      tail = tail.next = new Slab<>();
    }
    tail.elements[tail.numElements++] = element;
    size++;
  }

  public int size() {
    return size;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof SlabList<?> other) {
      if (size != other.size) {
        return false;
      }
      final var oit = other.iterator();
      for (final var it = iterator(); it.hasNext(); ) {
        Object e = it.next();
        Object oe = oit.next();
        if (e == null) {
          if (oe != null) {
            return false;
          }
        } else {
          if (!e.equals(oe)) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    for (final var it = iterator(); it.hasNext(); ) {
      T e = it.next();
      hash = hash * 31 + (e != null ? e.hashCode() : 0);
    }
    return hash;
  }

  @Override
  public String toString() {
    final var sb = new StringBuilder();
    sb.append(SlabList.class.getSimpleName());
    sb.append("[");
    final var it = iterator();
    for (int i = 0; i < size; i++) {
      sb.append(it.next().toString());
      if (i < size - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Returns an iterator that is stable through appends.
   *
   * If hasNext() returns false and then additional elements are added to the list,
   * the iterator can be reused to continue from where it left off.
   */
  @Override
  public SlabIterator iterator() {
    return new SlabIterator();
  }

  public final class SlabIterator implements Iterator<T> {

    private Slab<T> slab = SlabList.this.head;
    private int index = 0;

    private SlabIterator() {}

    @Override
    public boolean hasNext() {
      return index < slab.numElements || slab.next != null;
    }

    @Override
    public T next() {
      if (!hasNext()) throw new NoSuchElementException();
      if (index == SLAB_SIZE) {
        slab = slab.next;
        index = 0;
      }
      return slab.elements[index++];
    }
  }

  private class Slab<T> {
    public final T[] elements;
    public int numElements;
    public Slab<T> next;
    @SuppressWarnings("unchecked")
    public Slab() {
      elements = (T[])(new Object[SLAB_SIZE]);
    }
  }
}
