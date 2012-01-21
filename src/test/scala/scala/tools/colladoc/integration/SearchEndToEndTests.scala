package scala.tools.colladoc.integration

import org.specs2.mutable._
import org.specs2.specification._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.openqa.selenium.server.{RemoteControlConfiguration, SeleniumServer}
import com.thoughtworks.selenium.DefaultSelenium
import tools.colladoc.lib.DependencyFactory
import tools.colladoc.util.TestProps

trait SearchEndToEndTests extends Specification {
  private val pageLoadTimeoutInMs = "30000"

  private var server : Server = null
  private var selenium : DefaultSelenium = null
  private var seleniumServer : SeleniumServer = null

  override def map(fs: => Fragments) = Step(startSelenium) ^ fs ^ Step(stopSelenium)

  def startSelenium = {
    val GUI_PORT             = 8080
    val SELENIUM_SERVER_PORT = 4444

    // Setting up the jetty instance which will be running Colladoc for the
    // duration of the tests.
    server  = new Server(GUI_PORT)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")
    server.setHandler(context)
    server.start()

    // We use the test props for this integration test so that we don't depend
    // on any external .props files being in the correct state.
    DependencyFactory.props.default.set(TestProps.props)

    // Setting up the Selenium Server for the duration of the tests
    val rc = new RemoteControlConfiguration()
    rc.setPort(SELENIUM_SERVER_PORT)
    seleniumServer = new SeleniumServer(rc)
    seleniumServer.boot()
    seleniumServer.start()
    seleniumServer.getPort()

    // Setting up the Selenium Client for the duration of the tests
    selenium = new DefaultSelenium("localhost",
      SELENIUM_SERVER_PORT,
      "*firefox",
      "http://localhost:"+GUI_PORT+"/")
    selenium.start()
  }

  def stopSelenium = {
    // Close everything when done
    selenium.close()
    selenium.stop()
    server.stop()
    seleniumServer.stop()
  }

  // TODO: setup selenium in the right way!
  /*"a user" should {
    // We want to share the same selenium objects throughout all examples.
    shareVariables

    "be shown the history page after navigating to Colladoc" in {
      selenium.open("/")
      selenium.waitForPageToLoad(pageLoadTimeoutInMs)

      selenium.getTitle() mustMatch "History"
    }

    "be taken to the results page after entering a search query [c1]" in {
      selenium.open("/")
      selenium.waitForPageToLoad(pageLoadTimeoutInMs)

      enterSearchQuery("class _")

      // Give the results page a litle time to load
      Thread.sleep(3000)

      selenium.getTitle() mustMatch "Search"
    }

    "not be taken anywhere if he clicks the search button with an empty query [c2]" in {
      selenium.open("/")
      selenium.waitForPageToLoad(pageLoadTimeoutInMs)

      enterSearchQuery("")

      // Just to be sure that nothing is happening.
      // TODO: At this rate we'll end up having lots of sleeps in the code...
      // There must be a more deterministic wait that we can do.
      Thread.sleep(3000)

      selenium.getTitle() mustMatch "Search"
    }
  }*/

  private def enterSearchQuery(q : String) = {
    selenium.`type`("svalue", q)
    selenium.click("searchbtn")
  }
}
