scalacOptions ++= Seq( "-unchecked", "-deprecation" )

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Workaround for https://github.com/coursier/coursier/issues/450
classpathTypes += "maven-plugin"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.18")

// SBT-Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Scala.js
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.24")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8")

// App Packaging
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")

// Benchmarking
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.3")

// Dependency Resolution
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M7")

// Code Quality
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0") // scalastyle

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.9") // scapegoat

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7") // stats

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2") // dependencyGraph

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4") // dependencyUpdates

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2") // scalariformFormat

addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1")
