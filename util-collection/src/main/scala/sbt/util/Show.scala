/*
 * sbt
 * Copyright 2011 - 2018, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * Licensed under Apache License 2.0 (see LICENSE)
 */

package sbt.util

trait Show[A]:
  def show(a: A): String
end Show

object Show:
  def apply[A](f: A => String): Show[A] = a => f(a)

  def fromToString[A]: Show[A] = _.toString
end Show
