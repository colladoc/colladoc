package scala.tools.colladoc

import junit.framework.Assert._
import junit.framework.{Assert, TestCase}
import com.thoughtworks.selenium.DefaultSelenium
import org.junit.{After, Before, Test}

///**
// * Created by IntelliJ IDEA.
// * User: Miro Paskov
// * Date: 2/2/11
// * Time: 2:11 PM
// * To change this template use File | Settings | File Templates.
// */

class IntegrationTests extends TestCase {

  var s: DefaultSelenium = null

  @Before
  override def setUp() {
    s = new DefaultSelenium("localhost", 4444, """*firefox""", "http://localhost:8080/");
    s.start();
  }

  @Test
  def testSearchForExactObject() = {
    s.open("/")
    s.`type`("//input[@type='text']", "object Foopa")
    s.selectFrame("template")

    assertTrue(s.isTextPresent("class Barpa extends AnyRef"));
  }

  @After
  override def tearDown() {
    s.stop();
  }
}
