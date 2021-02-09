name := "tiny-web-crawler"

version := "0.1"

scalaVersion := "2.13.4"

val http4sVersion = "0.21.15"
val circeVersion  = "0.13.0"
val log4jVersion  = "2.14.0"
val slf4jVersion  = "1.7.30"

libraryDependencies ++= Seq(
  "io.monix"              %% "monix"               % "3.3.0",
  "org.http4s"            %% "http4s-dsl"          % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"            %% "http4s-circe"        % http4sVersion,
  "io.circe"              %% "circe-generic"       % circeVersion,
  "io.circe"              %% "circe-literal"       % circeVersion,
  "org.slf4j"             % "slf4j-api"            % slf4jVersion,
  "org.slf4j"             % "slf4j-simple"         % slf4jVersion,
  "com.github.pureconfig" %% "pureconfig"          % "0.14.0",
  "com.google.guava" % "guava" % "30.1-jre"
  //  "org.scalatest" % "scalatest_2.12"           % "3.2.3" % Test,
//  "org.mockito"   %% "mockito-scala-scalatest" % "1.16.15" % Test
)

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
