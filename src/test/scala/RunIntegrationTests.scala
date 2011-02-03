import _root_.org.mortbay.jetty.Server
import _root_.org.mortbay.jetty.webapp.WebAppContext
import junit.framework.{Test, TestSuite}
import org.mortbay.jetty.nio._
import tools.colladoc.IntegrationTests

object RunIntegrationTests extends Application {
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(8080)
  server.setConnectors(Array(scc))

  val context = new WebAppContext()
  context.setServer(server)
  context.setContextPath("/")
  context.setWar("src/main/webapp")

  server.addHandler(context)

  try {
    server.start()

    Thread.sleep(1)

    _root_.junit.textui.TestRunner.run(suite)

    server.stop()
    server.join()
  } catch {
    case exc: Exception => {
      exc.printStackTrace()
      System.exit(100)
    }
  }

  def suite: Test = {
    val suite = new TestSuite(classOf[IntegrationTests])
    suite
  }

}
