package gov.nasa.jpl.aerie.timeline

fun interface BinaryOperation<in Left, in Right, out Out> {
  operator fun invoke(l: Left?, r: Right?, i: Interval): Out

  companion object {
    @JvmStatic fun <Left, Right, Out> cases(
        left: (Left & Any, Interval) -> Out,
        right: (Right & Any, Interval) -> Out,
        combine: (Left & Any, Right & Any, Interval) -> Out
    ) = BinaryOperation<Left, Right, Out> { l, r, i ->
      if (l != null && r != null) combine(l, r, i)
      else if (l != null) left(l, i)
      else if (r != null) right(r, i)
      else throw BinaryOperationBothNullException()
    }

    @JvmStatic fun <Left, Right, Out> singleFunction(f: (Left?, Right?, Interval) -> Out) = BinaryOperation(f)

    @JvmStatic fun <Left, Right, Out> combineOrNull(f: (Left & Any, Right & Any, Interval) -> Out) = BinaryOperation<Left, Right, Out?> { l, r, i ->
      if (l == null || r == null) null
      else f(l, r, i)
    }

    @JvmStatic fun <V> combineOrIdentity(f: (V & Any, V & Any, Interval) -> V) = BinaryOperation<V, V, V> { l, r, i ->
      if (l != null && r != null) f(l, r, i)
      else l ?: r ?: throw BinaryOperationBothNullException()
    }

    @JvmStatic fun <In, Out> fold(
        convert: (In & Any, Interval) -> Out,
        combine: (new: In & Any, acc: Out & Any, Interval) -> Out
    ) = BinaryOperation<In, Out, Out> { new, acc, i ->
      if (acc != null && new != null) combine(new, acc, i)
      else if (new != null) convert(new, i)
      else acc ?: throw BinaryOperationBothNullException()
    }
  }

  class BinaryOperationBothNullException: Exception("Both arguments to binary operation were null.")
}
