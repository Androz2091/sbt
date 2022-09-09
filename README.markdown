sbt-projectmatrix
=================

cross building using subprojects.

This is an experimental plugin that implements better cross building.

setup
-----

**Requirements**: Requires sbt 1.2.0 or above.

In `project/plugins.sbt`:

```scala
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.0")

// add also the following for Scala.js support
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1")
```

usage
-----

### building against multiple Scala versions

After adding sbt-projectmatrix to your build, here's how you can set up a matrix with two Scala versions.

```scala
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core"
  )
  .jvmPlatform(scalaVersions = Seq("2.13.3", "2.12.12"))
```

This will create subprojects `core` and `core2_12`.
Unlike `++` style stateful cross building, these will build in parallel.

### two matrices

It gets more interesting if you have more than one matrix.

```scala
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

// uncomment if you want root
// lazy val root = (project in file("."))
//   .aggregate(core.projectRefs ++ app.projectRefs: _*)
//   .settings(
//   )

lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core"
  )
  .jvmPlatform(scalaVersions = Seq("2.13.3", "2.12.12"))

lazy val app = (projectMatrix in file("app"))
  .dependsOn(core)
  .settings(
    name := "app"
  )
  .jvmPlatform(scalaVersions = Seq("2.13.3"))
```

This is an example where `core` builds against Scala 2.12 and 2.13, but app only builds for one of them.

### Scala.js support

[Scala.js](http://scala-js.org/) support was added in sbt-projectmatrix 0.2.0.
To use this, you need to setup sbt-scalajs as well:

```scala
lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core"
  )
  .jsPlatform(scalaVersions = Seq("2.12.12", "2.11.12"))
```

This will create subprojects `coreJS2_11` and `coreJS2_12`.

### Scala Native support

[Scala Native](http://scala-native.org) support will be added in upcoming release.
To use this, you need to setup sbt-scala-native` as well:

```scala
lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core"
  )
  .nativePlatform(scalaVersions = Seq("2.11.12"))
```

This will create subproject `coreNative2_11`.

### parallel cross-library building

The rows can also be used for parallel cross-library building.
For example, if you want to build against Config 1.2 and Config 1.3, you can do something like this:

In `project/ConfigAxis.scala`:

```scala
import sbt._

case class ConfigAxis(idSuffix: String, directorySuffix: String) extends VirtualAxis.WeakAxis {
}
```

In `build.sbt`:

```scala
ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val config12 = ConfigAxis("Config1_2", "config1.2")
lazy val config13 = ConfigAxis("Config1_3", "config1.3")

lazy val scala212 = "2.12.10"
lazy val scala211 = "2.11.12"

lazy val app = (projectMatrix in file("app"))
  .settings(
    name := "app"
  )
  .customRow(
    scalaVersions = Seq(scala212, scala211),
    axisValues = Seq(config12, VirtualAxis.jvm),
    _.settings(
      moduleName := name.value + "_config1.2",
      libraryDependencies += "com.typesafe" % "config" % "1.2.1"
    )
  )
  .customRow(
    scalaVersions = Seq(scala212, scala211),
    axisValues = Seq(config13, VirtualAxis.jvm),
    _.settings(
      moduleName := name.value + "_config1.3",
      libraryDependencies += "com.typesafe" % "config" % "1.3.3"
    )
  )
```

This will create `appConfig1_22_11`, `appConfig1_22_12`, and `appConfig1_32_12` respectively producing `app_config1.3_2.12`, `app_config1.2_2.11`, and `app_config1.2_2.12` artifacts.

### referencing the generated subprojects

You might want to reference to one of the projects within `build.sbt`.

```scala
lazy val core12 = core.jvm("2.12.8")

lazy val appConfig12_212 = app.finder(config13, VirtualAxis.jvm)("2.12.8")
```

In the above `core12` returns `Project` type.

### accessing axes from subprojects

Each generated subproject can access the values for all the axes using `virtualAxes` key:

```scala
lazy val platformTest = settingKey[String]("")

lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core"
  )
  .jsPlatform(scalaVersions = Seq("2.12.12", "2.11.12"))
  .jvmPlatform(scalaVersion = Seq("2.12.12", "2.13.3"))
  .settings(
    platformTest := {
      if(virtualAxes.value.contains(VirtualAxis.jvm))
        "JVM project"
      else
        "JS project"
    }
  )
```

credits
-------

- The idea of representing cross build using subproject was pionieered by Tobias Schlatter's work on Scala.js plugin, which was later expanded to [ sbt-crossproject](https://github.com/portable-scala/sbt-crossproject). However, this only addresses the platform (JVM, JS, Native) cross building.
- [sbt-cross](https://github.com/lucidsoftware/sbt-cross) written by Paul Draper in 2015 implements cross building across Scala versions.

license
-------

MIT License
