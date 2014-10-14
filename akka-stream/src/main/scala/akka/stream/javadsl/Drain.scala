/**
 * Copyright (C) 2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.stream.javadsl

import akka.stream.scaladsl2.FlowMaterializer

import akka.stream.javadsl
import akka.stream.scaladsl2

abstract class Drain[-In] extends javadsl.SinkAdapter[In] {
  protected def delegate: scaladsl2.Sink[In]

  override def runWith[T](source: javadsl.KeyedSource[In, T], materializer: FlowMaterializer): T = {
    val sSource = source.asScala
    sSource.connect(asScala).run()(materializer).get(sSource).asInstanceOf[T]
  }

  override def runWith(source: javadsl.SimpleSource[In], materializer: FlowMaterializer): Unit = {
    source.asScala.connect(asScala).run()(materializer)
  }
}

final case class SimpleSink[-In](override val delegate: scaladsl2.Sink[In]) extends javadsl.Drain[In] {
  override def asScala: scaladsl2.SimpleDrain[In] = super.asScala.asInstanceOf[scaladsl2.SimpleDrain[In]]
}

final case class KeyedSink[-In, M](override val delegate: scaladsl2.Sink[In]) extends javadsl.Drain[In] {
  override def asScala: scaladsl2.KeyedSink[In] = super.asScala.asInstanceOf[scaladsl2.KeyedSink[In]]
}
