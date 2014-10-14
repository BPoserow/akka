/**
 * Copyright (C) 2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.stream.scaladsl2

/**
 * Returned by [[RunnableFlow#run]] and [[FlowGraph#run]] and can be used to retrieve the materialized
 * `Source` inputs or `Sink` outputs, e.g. [[SubscriberTap]] or [[PublisherDrain]].
 */
trait MaterializedMap {

  /**
   * Retrieve a materialized `Source`, e.g. the `Subscriber` of a [[SubscriberTap]].
   */
  def get(key: KeyedSource[_]): key.MaterializedType

  /**
   * Retrieve a materialized `Sink`, e.g. the `Publisher` of a [[PublisherDrain]].
   */
  def get(key: KeyedSink[_]): key.MaterializedType
}
