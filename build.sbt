name := "collection.vf"

organization := "org.vitalstar"

version := "1.0"

scalaVersion := "2.11.11"
val sparkVersion = "2.3.1"

parallelExecution in Test := false

//This is for spark-fast-tests.
resolvers += "jitpack" at "https://jitpack.io"

fork in run := true

connectInput in run := true

dependencyOverrides += "com.google.guava" % "guava" % "20.0"


libraryDependencies ++=
  Seq(//This version of swagger-akka-http is using 10.1.3 spray-json
      "org.scala-lang.modules" %% "scala-async" % "0.9.2" withSources() withJavadoc(),
      "com.github.swagger-akka-http" % "swagger-akka-http_2.11" % "1.0.0" withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.6" withSources() withJavadoc(),
      "org.json4s" %% "json4s-native" % "3.2.11" withSources() withJavadoc(),
      "org.json4s" %% "json4s-jackson" % "3.2.11" withSources() withJavadoc(),

      //=====Test=====
      "com.typesafe.akka" % "akka-http-testkit_2.11" % "10.1.3" % "test",
      "junit" % "junit" % "4.10" % "test",

      // Simulate Coderpad.io settings
      "com.chuusai"     %% "shapeless"                    % "2.3.2",
      "org.scalacheck"  %% "scalacheck"                   % "1.13.4",
      "org.scalactic"   %% "scalactic"                    % "3.0.1",
      "org.scalamock"   %% "scalamock-scalatest-support"  % "3.5.0",
      "org.scalatest"   %% "scalatest"                    % "3.0.1",
      "org.scalaz"      %% "scalaz-core"                  % "7.2.12",
      "org.typelevel"   %% "cats"                         % "0.9.0"

  )

assemblyMergeStrategy in assembly := {
      case "application.conf" => MergeStrategy.concat
      case "version.conf" => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
      case PathList("META-INF", "services", "org.apache.spark.sql.sources.DataSourceRegister") => MergeStrategy.filterDistinctLines
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
}

// turn off testing
// sbt command line: 'set test in assembly := {}'
// test in assembly := {}
