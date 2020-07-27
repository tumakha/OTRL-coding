lazy val root = (project in file(".")).settings(
  Seq(
    name := "OTRL-coding",
    version := "0.1",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.0" % Test
    )
  )
)