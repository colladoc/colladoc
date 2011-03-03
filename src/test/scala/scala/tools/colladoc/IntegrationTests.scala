package scala.tools.colladoc

import lib.DependencyFactory
import org.specs.Specification
import org.mortbay.jetty.Server
import org.mortbay.jetty.webapp.WebAppContext
import org.openqa.selenium.server.{RemoteControlConfiguration, SeleniumServer}
import com.thoughtworks.selenium.DefaultSelenium
import util.TestProps

object IntegrationTests extends Specification {
  private val pageLoadTimeoutInMs = "30000"

  private var server : Server = null
  private var selenium : DefaultSelenium = null
  private var seleniumServer : SeleniumServer = null

  doBeforeSpec {
    val GUI_PORT             = 8080
    val SELENIUM_SERVER_PORT = 4444

    // Setting up the jetty instance which will be running Colladoc for the
    // duration of the tests.
    server  = new Server(GUI_PORT)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")
    server.addHandler(context)
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

  "a user" should {
    "be shown the root package page after navigating to Colladoc" in {
      selenium.open("/")
      selenium.waitForPageToLoad(pageLoadTimeoutInMs)

      selenium.getTitle() mustMatch "_root_"
    }

//    "be taken to the results page after entering a search query" in {
//      selenium.open("/")
//      selenium.waitForPageToLoad(pageLoadTimeoutInMs)
//
//      selenium.typeKeys("svalue", "class _")
//      selenium.click("searchbtn")
//
//      selenium.getTitle() mustMatch "Search"
//    }
  }

  doAfterSpec {
    // Close everything when done
    selenium.close()
    selenium.stop()
    server.stop()
    seleniumServer.stop()
  }
}
