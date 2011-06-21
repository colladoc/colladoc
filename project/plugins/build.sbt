seq(
  libraryDependencies <+= sbtVersion("com.github.siasia" %% "xsbt-web-plugin" % _),
  libraryDependencies <+= sbtVersion("com.github.siasia" %% "xsbt-proguard-plugin" % _)
)

resolvers ++= Seq(
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "Proguard plugin repo" at "http://siasia.github.com/maven2"
)
