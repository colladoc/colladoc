seq(
  libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10")),
  libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))
)

resolvers ++= Seq(
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "Proguard plugin repo" at "http://siasia.github.com/maven2"
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "0.11.1-SNAPSHOT")
