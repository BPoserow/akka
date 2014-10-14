/**
 * Copyright (C) 2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.stream.javadsl

import akka.stream.javadsl
import akka.stream.scaladsl2

abstract class Tap[+Out] extends javadsl.SourceAdapter[Out] {
  def delegate: scaladsl2.Source[Out]
}

final case class SimpleSource[+Out](override val delegate: scaladsl2.Source[Out]) extends Tap[Out] {
  override def asScala: scaladsl2.SimpleTap[Out] = super.asScala.asInstanceOf[scaladsl2.SimpleTap[Out]]
}

final case class KeyedSource[+Out, T](override val delegate: scaladsl2.Source[Out]) extends Tap[Out] {
  override def asScala: scaladsl2.KeyedTap[Out] = super.asScala.asInstanceOf[scaladsl2.KeyedTap[Out]]
}
