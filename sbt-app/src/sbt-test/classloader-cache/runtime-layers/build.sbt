val layeringStrategyTest = (project in file(".")).settings(
  name := "layering-strategy-test",
  scalaVersion := "2.12.19",
  organization := "sbt",
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.16",
)
