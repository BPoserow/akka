/**
 * Copyright (C) 2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.stream.scaladsl2

import org.reactivestreams.Subscriber

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.annotation.unchecked.uncheckedVariance
import scala.util.Try

/**
 * A `Sink` is a set of stream processing steps that has one open input and an attached output.
 * Can be used as a `Subscriber`
 */
trait Sink[-In] {
  /**
   * Connect this `Sink` to a `Source` and run it. The returned value is the materialized value
   * of the `Source`, e.g. the `Subscriber` of a [[SubscriberTap]].
   */
  def runWith(source: KeyedSource[In])(implicit materializer: FlowMaterializer): source.MaterializedType =
    source.connect(this).run().get(source)

  /**
   * Connect this `Sink` to a `Source` and run it. The returned value is the materialized value
   * of the `Source`, e.g. the `Subscriber` of a [[SubscriberTap]].
   */
  def runWith(source: SimpleSource[In])(implicit materializer: FlowMaterializer): Unit =
    source.connect(this).run()
}

object Sink {
  /**
   * Helper to create [[Sink]] from `Subscriber`.
   */
  def apply[T](subscriber: Subscriber[T]): Sink[T] = SubscriberDrain(subscriber)

  /**
   * Creates a `Sink` by using an empty [[FlowGraphBuilder]] on a block that expects a [[FlowGraphBuilder]] and
   * returns the `UndefinedSource`.
   */
  def apply[T]()(block: FlowGraphBuilder ⇒ UndefinedSource[T]): Sink[T] =
    createSinkFromBuilder(new FlowGraphBuilder(), block)

  /**
   * Creates a `Sink` by using a FlowGraphBuilder from this [[PartialFlowGraph]] on a block that expects
   * a [[FlowGraphBuilder]] and returns the `UndefinedSource`.
   */
  def apply[T](graph: PartialFlowGraph)(block: FlowGraphBuilder ⇒ UndefinedSource[T]): Sink[T] =
    createSinkFromBuilder(new FlowGraphBuilder(graph.graph), block)

  private def createSinkFromBuilder[T](builder: FlowGraphBuilder, block: FlowGraphBuilder ⇒ UndefinedSource[T]): Sink[T] = {
    val in = block(builder)
    builder.partialBuild().toSink(in)
  }

  /**
   * A `Sink` that immediately cancels its upstream after materialization.
   */
  def cancelled[T]: Sink[T] = CancelDrain

  /**
   * A `Sink` that materializes into a `Future` of the first value received.
   */
  def future[T] = FutureDrain[T]

  /**
   * A `Sink` that materializes into a [[org.reactivestreams.Publisher]].
   * that can handle one [[org.reactivestreams.Subscriber]].
   */
  def publisher[T] = PublisherDrain[T]

  /**
   * A `Sink` that materializes into a [[org.reactivestreams.Publisher]]
   * that can handle more than one [[org.reactivestreams.Subscriber]].
   */
  def publisher[T](initialBufferSize: Int, maximumBufferSize: Int) =
    FanoutPublisherDrain[T](initialBufferSize, maximumBufferSize)

  /**
   * A `Sink` that will consume the stream and discard the elements.
   */
  def ignore = BlackholeDrain

  /**
   * A `Sink` that will invoke the given procedure for each received element. The sink is materialized
   * into a [[scala.concurrent.Future]] will be completed with `Success` when reaching the
   * normal end of the stream, or completed with `Failure` if there is an error is signaled in
   * the stream..
   */
  def foreach[T](f: T ⇒ Unit) = ForeachDrain(f)

  /**
   * A `Sink` that will invoke the given function for every received element, giving it its previous
   * output (or the given `zero` value) and the element as input.
   * The returned [[scala.concurrent.Future]] will be completed with value of the final
   * function evaluation when the input stream ends, or completed with `Failure`
   * if there is an error is signaled in the stream.
   */
  def fold[U, T](zero: U)(f: (U, T) ⇒ U) = FoldDrain(zero)(f)

  /**
   * A `Sink` that when the flow is completed, either through an error or normal
   * completion, apply the provided function with [[scala.util.Success]]
   * or [[scala.util.Failure]].
   */
  def onComplete[T](callback: Try[Unit] ⇒ Unit) = OnCompleteDrain[T](callback)
}

/**
 * A `Sink` that does not need to create a user-accessible object during materialization.
 */
trait SimpleSink[-In] extends Sink[In]

/**
 * A `Sink` that will create an object during materialization that the user will need
 * to retrieve in order to access aspects of this sink (could be a completion Future
 * or a cancellation handle, etc.)
 */
trait KeyedSink[-In] extends Sink[In] {
  type MaterializedType
}
