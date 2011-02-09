import junit.framework.{TestSuite, TestCase}
import org.junit.Assert

/**
 * User: Miroslav Paskov
 * Date: 2/6/11
 * Time: 5:54 AM
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
        case Or(List(Comment(List(Word("bau"))), Entity(Word("bau")))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleExactWord()
  {
      new ScoogleParser().parse("\"bau\"") match {
        case Or(List(Comment(List(ExactWord("bau"))), Entity(ExactWord("bau")))) => ()
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

  def testSimpleClassWow()
  {
      new ScoogleParser().parse("class Wow") match {
        case Class(Word("Wow"),None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimplePackage()
  {
      new ScoogleParser().parse("package org.junit") match {
        case Package(Word("org.junit")) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleObject()
  {
      new ScoogleParser().parse("object Iterable") match {
        case Object(Word("Iterable"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleTrait()
  {
      new ScoogleParser().parse("trait Cloneable") match {
        case Trait(Word("Cloneable"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleDef()
  {
      new ScoogleParser().parse("def Now") match {
        case Def(Word("Now"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testGroupClassFirstOrClassSecond()
  {
      new ScoogleParser().parse("(class First or class Second)") match {
        case Group(Or(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassGroupFirstAndClassSecond()
  {
      new ScoogleParser().parse("(class First and class Second)") match {
        case Group(And(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testOr()
  {
      new ScoogleParser().parse("class First or class Second") match {
        case Or(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testOrWithPipe()
  {
      new ScoogleParser().parse("class First || class Second") match {
        case Or(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAnd()
  {
      new ScoogleParser().parse("class First and class Second") match {
        case And(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAndWithAmpersands()
  {
      new ScoogleParser().parse("class First && class Second") match {
        case And(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testOrWithiinWords()
  {
      new ScoogleParser().parse("bau or wow") match {
        case Or(List(Word("bau"), Word("wow"))) => ()
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

  def testTraitWithExtends()
  {
      new ScoogleParser().parse("trait Robot extends Cloneable") match {
        case Trait(Word("Robot"), Some(Word("Cloneable"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testObjectWithExtends()
  {
      new ScoogleParser().parse("object Robot extends Cloneable") match {
        case Object(Word("Robot"), Some(Word("Cloneable"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassWithExtends()
  {
      new ScoogleParser().parse("class Robot extends Cloneable") match {
        case Class(Word("Robot"), Some(Word("Cloneable"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testJustExtends()
  {
      new ScoogleParser().parse("extends Cloneable") match {
        case Extends(Word("Cloneable")) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def contrivedQuery1()
  {
      new ScoogleParser().parse("(trait Robot and // copy) or extends Cloneable") match {
        case Or(List(
          Group(And(List(Trait(Word("Robot"), None), Comment(List(Word("copy")))))),
          Extends(Word("Cloneable")))
        ) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def contrivedPipesAndAmpersandsCanReplaceBooleans()
  {
      new ScoogleParser().parse("(trait Robot && // copy) || extends Cloneable") match {
        case Or(List(
          Group(And(List(Trait(Word("Robot"), None), Comment(List(Word("copy")))))),
          Extends(Word("Cloneable")))
        ) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefReturn()
  {
      new ScoogleParser().parse("def test:Int") match {
        case Def(Word("test"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleNot()
  {
      new ScoogleParser().parse("not def test:Int") match {
        case Not(Def(Word("test"), Some(Word("Int")))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDoubleNot()
  {
      new ScoogleParser().parse("not not def test:Int") match {
        case Not(Not(Def(Word("test"), Some(Word("Int"))))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testNotWithExclamation()
  {
      new ScoogleParser().parse("! def test:Int") match {
        case Not(Def(Word("test"), Some(Word("Int")))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithAnyName()
  {
      new ScoogleParser().parse("def _ : Int") match {
        case Def(Word("_"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }
}
