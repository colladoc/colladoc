seq(webSettings :_*)

name := "Colladoc"

version := "1.0"

organization := "scala.tools.colladoc"

scalaHome := Some(file("scala/build/pack"))

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

libraryDependencies ++= {
  val liftVersion = "2.4"
  Seq(
    "net.liftweb" % "lift-webkit_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-mapper_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-openid_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-widgets_2.9.1" % liftVersion % "compile->default"
  )
}

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.6.2" % "compile",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "test,container",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "compile",
  "junit" % "junit" % "4.10" % "test->default",
  "org.mockito" % "mockito-all" % "1.9.0" % "test->default",
  "cglib" % "cglib" %"2.2.2" % "test->default",
  "org.specs2" % "specs2_2.9.1" % "1.7.1" % "test->default",
  "org.seleniumhq.selenium" % "selenium-server" % "2.17.0" % "test->default",
  "com.h2database" % "h2" % "1.2.144" % "runtime",
  "postgresql" % "postgresql" % "8.4-701.jdbc4" % "runtime"
)

resolvers ++= Seq(
  "snapshots" at "http://scala-tools.org/repo-snapshots",
  "releases" at "http://scala-tools.org/repo-releases",
  "Jetty Repo" at "http://repo1.maven.org/maven2/org/mortbay/jetty",
  "Java.net Repo" at "http://download.java.net/maven/2/"
)
