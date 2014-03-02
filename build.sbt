name := "play-jekyll-bootstrap"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.pegdown" % "pegdown" % "1.4.2"
)     

play.Project.playScalaSettings

templatesImport += "org.joda.time._"