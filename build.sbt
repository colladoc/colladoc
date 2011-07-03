seq(WebPlugin.webSettings :_*)

name := "Colladoc"

version := "1.0"

organization := "scala.tools.colladoc"

scalaHome := Some(file("scala/build/pack"))

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

libraryDependencies ++= {
  val liftVersion = "2.4-M2"
  Seq(
    "net.liftweb" % "lift-webkit_2.9.0-1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-mapper_2.9.0-1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-openid_2.9.0-1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-widgets_2.9.0-1" % liftVersion % "compile->default"
  )
}

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6.2" % "compile",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
  "ch.qos.logback" % "logback-classic" % "0.9.26",
  "junit" % "junit" % "4.8.2" % "test->default",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test->default",
  "org.jmock" % "jmock-legacy" %"2.5.1" % "test->default",
  "cglib" % "cglib" %"2.1_3" % "test->default",
  "org.objenesis" % "objenesis" %"1.0" % "test->default",
  "org.scala-tools.testing" % "specs_2.8.1" % "1.6.6" % "test->default",
  "org.seleniumhq.selenium" % "selenium" % "2.0b1" % "test->default",
  "org.seleniumhq.selenium" % "selenium-server" % "2.0b1" % "test->default",
  "com.h2database" % "h2" % "1.2.144" % "runtime",
  "postgresql" % "postgresql" % "8.4-701.jdbc4" % "runtime"
 )

resolvers += ScalaToolsSnapshots
