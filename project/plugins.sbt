resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalameta"               % "sbt-scalafmt"        % "2.0.6")
addSbtPlugin("org.scala-js"                % "sbt-scalajs"         % "1.0.1")
addSbtPlugin("ch.epfl.scala"               % "sbt-scalajs-bundler" % "0.17.0")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"       % "1.0.0-beta21")

libraryDependencies ++= Seq(
  "com.dorkbox" % "Notify" % "3.7"
)
