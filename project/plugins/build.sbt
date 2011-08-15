seq(
  libraryDependencies += "com.github.mpeltonen" %% "sbt-idea" % "0.10.0-SNAPSHOT",
  libraryDependencies <+= sbtVersion("com.github.siasia" %% "xsbt-web-plugin" % _),
  libraryDependencies <+= sbtVersion("com.github.siasia" %% "xsbt-proguard-plugin" % _)
)

resolvers ++= Seq(
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "Proguard plugin repo" at "http://siasia.github.com/maven2"
)
