name := "pekko-grpc-starter"
organization := "io.github.andres-luque-eng"
version := sys.props.getOrElse("version", "0.1.0-SNAPSHOT")
scalaVersion := "3.10.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed"  % "1.1.2",
  "org.apache.pekko" %% "pekko-stream"       % "1.1.2",
  "org.apache.pekko" %% "pekko-http"         % "1.1.0",
  "org.apache.pekko" %% "pekko-grpc-runtime" % "1.1.1",
  "ch.qos.logback"    % "logback-classic"    % "1.5.12",
  "org.scalameta"    %% "munit"              % "1.1.0" % Test
)

// GitHub Packages Apache Maven Registry Configuration (Free on GitHub Free)
publishTo := Some("GitHub Packages" at "https://maven.pkg.github.com/andres-luque-eng/pekko-grpc-starter")
publishMavenStyle := true
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  sys.env.getOrElse("GITHUB_ACTOR", "andres-luque-eng"),
  sys.env.getOrElse("GITHUB_TOKEN", "")
)
