import sbtassembly.AssemblyKeys.assembly

lazy val root = project
  .in(file("."))
  .settings(
    name := "brapi",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.0" % Test,

      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,

      "com.softwaremill.sttp.client4" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client4" %% "circe" % sttpVersion,

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "ch.qos.logback" % "logback-classic" % "1.5.32",
    ),

    assembly / mainClass := Some("bootstrap"),
    assembly / assemblyJarName := s"${name.value}-${version.value}.jar",
    Test / testOptions += Tests.Filter(!_.endsWith("E2ESuite")),

    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
val scala3Version = "3.8.3"
val sttpVersion = "4.0.23"
val circeVersion = "0.14.15"
val tapirVersion = "1.13.16"
