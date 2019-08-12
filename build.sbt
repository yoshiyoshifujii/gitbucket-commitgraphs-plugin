val Organization = "me.huzi.gitbucket"
val ProjectName = "gitbucket-commitgraphs-plugin"
val ProjectVersion = "4.23.1"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

organization := Organization
name := ProjectName
version := ProjectVersion
scalaVersion := "2.13.0"
gitbucketVersion := "4.32.0"

scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps", "-Ydelambdafy:method", "-target:jvm-1.8")
javacOptions in compile ++= Seq("-target", "8", "-source", "8")

useJCenter := true
