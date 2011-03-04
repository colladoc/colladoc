package scala.tools.colladoc.util

import java.io.File


object TestProps {
  lazy val props = {
    // Figure out the current directory so that we can set up the classpath and
    // sourcepath relative to the test directory.
    // NOTE: This ends up being colladoc/ folder.
    val currentDirectory = new File(".").getAbsolutePath.replace('\\','/')
    val sourcepath = currentDirectory + "/src/test/scala/scala/tools/colladoc/testfiles"
    val classpathDir = currentDirectory + "/scala/build/pack/lib/"
    val classpath = {
      val separator = if (System.getProperty("os.name").toLowerCase().indexOf("win")>=0) ";"
                      else ":";
      classpathDir + "scala-compiler.jar" + separator +
                    classpathDir + "scala-library.jar"
    }

    println(sourcepath)

    println(classpath)

    Map[String, String]("-doc-title" -> "Colladoc",
                        "-doc-version" -> "1.0-SNAPSHOT",
                        "-sourcepath" -> sourcepath,
                        "-classpath" -> classpath)
  }
}