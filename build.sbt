lazy val root = (project in file(".")).settings(
  Seq(
    name := "OTRL-coding",
    version := "0.1",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.1.4",
      "org.scalatest" %% "scalatest" % "3.2.0" % Test
    )
  )
)

lazy val checkScalaStyle = taskKey[Unit]("checkScalaStyle")
checkScalaStyle := scalastyle.in(Test).dependsOn(scalastyle.in(Compile).toTask("")).toTask("").value

(test in Test) := ((test in Test) dependsOn checkScalaStyle).value

scalastyleFailOnWarning := true
scalastyleFailOnError := true
