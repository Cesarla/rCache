lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"
lazy val playWsV = "1.1.8"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.cesarla",
      scalaVersion    := "2.12.5"
    )),
    name := "rcache",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.play" %% "play-ws-standalone-json" % playWsV,
      "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0" excludeAll ExclusionRule(
        organization = "com.typesafe.play"),
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )
