val Organization = "me.huzi.gitbucket"
val ProjectName = "gitbucket-commitgraphs-plugin"
val ProjectVersion = "4.9.0"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

organization := Organization
name := ProjectName
version := ProjectVersion
scalaVersion := "2.11.8"

resolvers ++= Seq(
  "bkromhout-maven" at "https://dl.bintray.com/bkromhout/maven/"
)

libraryDependencies ++= Seq(
  "io.github.gitbucket" %% "gitbucket"          % "4.9.0" % "provided",
  "com.typesafe.play"   %% "twirl-compiler"     % "1.0.4" % "provided",
  "javax.servlet"        % "javax.servlet-api"  % "3.1.0" % "provided"
)

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps", "-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8")
javacOptions in compile ++= Seq("-target", "8", "-source", "8")
