scalacOptions ++= Seq( "-unchecked", "-deprecation" )

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Dependency Resolution
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M7")

// Code Quality
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0") // scalastyle

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7") // stats

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4") // dependencyUpdates

addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1")
