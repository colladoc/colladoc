import junit.framework.{TestSuite, TestCase}
import org.junit.Assert

/**
 * Created by IntelliJ IDEA.
 * User: paskov
 * Date: 2/6/11
 * Time: 5:54 AM
 * To change this template use File | Settings | File Templates.
 */

object RunParserTests extends Application
{
    _root_.junit.textui.TestRunner.run(new TestSuite(classOf[ScoogleParserTests]))
}

class ScoogleParserTests extends TestCase
{
  def testSingleWord()
  {
      new ScoogleParser().parse("bau") match {
        case Or(Comment(List(Word("bau"))), Entity(Word("bau"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleExactWord()
  {
      new ScoogleParser().parse("\"bau\"") match {
        case Or(Comment(List(ExactWord("bau"))), Entity(ExactWord("bau"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testManyWords()
  {
      new ScoogleParser().parse("wow bau") match {
        case Comment(List(Word("wow"), Word("bau"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleComment()
  {
      new ScoogleParser().parse("//wow") match {
        case Comment(List(Word("wow"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleCommentWithSpace()
  {
      new ScoogleParser().parse("// wow") match {
        case Comment(List(Word("wow"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testOrWithiinWords()
  {
      new ScoogleParser().parse("bau or wow") match {
        //case Comment(List(Word("bau"), Word("or"), Word("wow"))) => ()
        case Or(Word("bau"), Word("wow")) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassWow()
  {
      new ScoogleParser().parse("class Wow") match {
        case Class(Word("Wow")) => ()
        case e => Assert.fail(e.toString)
    }
  }
//
//  def testDefNow()
//  {
//      new ScoogleParser().parse("def Now") match {
//        case Def(Word("Now")) => ()
//        case e => Assert.fail(e.toString)
//    }
//  }
//
//  def testClassBauOrClassWow()
//  {
//      new ScoogleParser().parse("(class Bau or class Wow)") match {
//        case Or(Class(Word("Bau")), Class(Word("Wow"))) => ()
//        case e => Assert.fail(e.toString)
//    }
//  }

}