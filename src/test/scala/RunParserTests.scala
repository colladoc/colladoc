import junit.framework.{TestSuite, TestCase}
import org.junit.Assert
import tools.colladoc.search._

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
        case Word("bau") => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleWord_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("bau"))
    Assert.assertEquals("name:bau comment:bau", result)
  }

  def testSingleExactWord()
  {
      new ScoogleParser().parse("\"bau\"") match {
        case ExactWord("bau") => ()
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

  def testSingleComment_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("//wow"))
    Assert.assertEquals("comment:wow", result)
  }

  def testSingleCommentWithSpace()
  {
      new ScoogleParser().parse("// wow") match {
        case Comment(List(Word("wow"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSingleCommentWithSpace_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("// wow"))
    Assert.assertEquals("comment:wow", result)
  }

  def testSimpleClassWow()
  {
      new ScoogleParser().parse("class Wow") match {
        case Class(Word("Wow"),None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleClass_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class Wow"))
    Assert.assertEquals("+type:class +name:Wow", result)
  }

  def testClassEndsWith()
  {
      new ScoogleParser().parse("class _Wow") match {
        case Class(EndWith("Wow"),None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassEndsWith_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class _Wow"))
    Assert.assertEquals("+type:class +name:*Wow", result)
  }

  def testClassStartsWith()
  {
      new ScoogleParser().parse("class Wow_") match {
        case Class(StartWith("Wow"),None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassStartsWith_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class Wow_"))
    Assert.assertEquals("+type:class +name:Wow*", result)
  }

  def testClassWithExtends()
  {
      new ScoogleParser().parse("class Robot extends Cloneable") match {
        case Class(Word("Robot"), Some(Word("Cloneable"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testClassWithExtends_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class Robot extends Cloneable"))
    Assert.assertEquals("+type:class +name:Robot +extends:Cloneable", result)
  }

  def testTraitWithExtends()
  {
      new ScoogleParser().parse("trait Robot extends Cloneable") match {
        case Trait(Word("Robot"), Some(Word("Cloneable"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testTraitWithExtends_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("trait Robot extends Cloneable"))
    Assert.assertEquals("+type:trait +name:Robot +extends:Cloneable", result)
  }

  def testObjectWithExtends()
  {
    new ScoogleParser().parse("object Robot extends Cloneable") match {
      case Object(Word("Robot"), Some(Word("Cloneable"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testObjectWithExtends_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("object Robot extends Cloneable"))
    Assert.assertEquals("+type:object +name:Robot +extends:Cloneable", result)
  }

  def testJustExtends()
  {
    new ScoogleParser().parse("extends Cloneable") match {
      case Extends(Word("Cloneable")) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testJustExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("extends Cloneable"))
    Assert.assertEquals("extends:Cloneable", result)
  }

  def testSimpleObject()
  {
    new ScoogleParser().parse("object Iterable") match {
      case Object(Word("Iterable"), None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleObject_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("object Iterable"))
    Assert.assertEquals("+type:object +name:Iterable", result)
  }

  def testSimpleTrait()
  {
      new ScoogleParser().parse("trait Cloneable") match {
        case Trait(Word("Cloneable"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleTrait_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("trait Cloneable"))
    Assert.assertEquals("+type:trait +name:Cloneable", result)
  }

  def testSimplePackage()
  {
      new ScoogleParser().parse("package org.junit") match {
        case Package(Word("org.junit")) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimplePackage_Query()
  {
    var result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("package org.junit"))
    Assert.assertEquals("+type:package +name:org.junit", result)
  }

  def testSimpleGroupOr()
  {
      new ScoogleParser().parse("(class First or class Second)") match {
        case Group(Or(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleGroupOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("(class First or class Second)"))
    Assert.assertEquals("(+type:class +name:First) (+type:class +name:Second)", result)
  }

  def testClassGroupFirstAndClassSecond()
  {
    new ScoogleParser().parse("(class First and class Second)") match {
      case Group(And(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassGroupFirstAndClassSecond_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("(class First and class Second)"))
    Assert.assertEquals("+(+type:class +name:First) +(+type:class +name:Second)", result)
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

  def testOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class First or class Second"))
    Assert.assertEquals("(+type:class +name:First) (+type:class +name:Second)", result)
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

  def testAnd_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("class First and class Second"))
    Assert.assertEquals("+(+type:class +name:First) +(+type:class +name:Second)", result)
  }

  def testOrWithiinWords()
  {
    new ScoogleParser().parse("bau or wow") match {
      case Or(List(Word("bau"), Word("wow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testOrWithiinWords_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("bau or wow"))
    Assert.assertEquals("(name:bau comment:bau) (name:wow comment:wow)", result)
  }

  def testManyWords()
  {
    new ScoogleParser().parse("wow bau") match {
      case Comment(List(Word("wow"), Word("bau"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testManyWords_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("bau wow"))
    Assert.assertEquals("comment:bau comment:wow", result)
  }

  def complexQuery1()
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

  def complexQuery1_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("(trait Robot and // copy) or extends Cloneable"))
    Assert.assertEquals("(+type:trait +name:Robot +comment:copy) extends:Cloneable", result)
  }

  def testSimpleDef()
  {
      new ScoogleParser().parse("def Now") match {
        case Def(Word("Now"), List(), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleDef_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("def Now"))
    Assert.assertEquals("+type:def +name:Now", result)
  }

  def testDefReturn()
  {
    new ScoogleParser().parse("def test:Int") match {
      case Def(Word("test"), List(), Some(Word("Int"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefReturn_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("def test:Int"))
    Assert.assertEquals("+type:def +name:test +return:Int", result)
  }

  def testSimpleNot()
  {
    new ScoogleParser().parse("not def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Word("Int")))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNotWithExclamation()
  {
    new ScoogleParser().parse("! def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Word("Int")))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("not def test:Int"))
    Assert.assertEquals("-(+type:def +name:test +return:Int)", result)
  }

  def testDoubleNot()
  {
    new ScoogleParser().parse("not not def test:Int") match {
      case Not(Not(Def(Word("test"), List(), Some(Word("Int"))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDoubleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(new ScoogleParser().parse("not not def test:Int"))
    Assert.assertEquals("-(-(+type:def +name:test +return:Int))", result)
  }

  def testDefWithAnyName()
  {
    new ScoogleParser().parse("def _ : Int") match {
      case Def(Word("_"), List(), Some(Word("Int"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testValWithReturnType()
  {
      new ScoogleParser().parse("val test:Int") match {
        case Val(Word("test"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleVar()
  {
      new ScoogleParser().parse("var test") match {
        case Var(Word("test"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testVarWithReturnType()
  {
      new ScoogleParser().parse("var test:Int") match {
        case Var(Word("test"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithEmptyParams()
  {
      new ScoogleParser().parse("def _() : Int") match {
        case Def(Word("_"), List(List()), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithCurriedEmptyParams()
  {
      new ScoogleParser().parse("def _()() : Int") match {
        case Def(Word("_"), List(List(),List()), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithConcreteParam()
  {
      new ScoogleParser().parse("def _(Int) : Int") match {
        case Def(Word("_"), List(List(Word("Int"))), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteParam()
  {
      new ScoogleParser().parse("def _(Int, String) : Int") match {
        case Def(Word("_"), List(List(Word("Int"), Word("String"))), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParam()
  {
      new ScoogleParser().parse("def _(Int, String, *) : Int") match {
        case Def(Word("_"), List(List(Word("Int"), Word("String"), AnyParams())), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurried()
  {
      new ScoogleParser().parse("def _(Int, String, *)(_, *) : Int") match {
        case Def(Word("_"), List(
                              List(Word("Int"), Word("String"), AnyParams()),
                              List(Word("_"), AnyParams())
                           ), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurriedNoSeparators()
  {
      new ScoogleParser().parse("def _(Int String *)(_ *) : Int") match {
        case Def(Word("_"), List(
                              List(Word("Int"), Word("String"), AnyParams()),
                              List(Word("_"), AnyParams())
                           ), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleVal()
  {
      new ScoogleParser().parse("val test") match {
        case Val(Word("test"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def contrivedQuery2()
  {
      new ScoogleParser().parse("""//can "be copied" or (trait Robot and def replicate(_ Model *)(_, Blueprint): Robot) or (extends Cloneable && val archetype""") match {
        case Or(List(
          Comment(List(Word("Can"), ExactWord("be copied"))),
          Group(And(List(
            Trait(Word("Robot"), None),
            Def(
              Word("replicate"),
              List(List(Word("_"), Word("Model"), AnyParams()), List(Word("_"), Word("Blueprint"))),
              Some(Word("Robot")))))),
          Group(And(List(
            Extends(Word("Cloneable")),
            Val(Word("archetype"), None))))
        )) => ()
        case e => Assert.fail(e.toString)
    }
  }

  // TODO: Test strange identifiers
  // TODO: Test invalid syntax
  // TODO: Test nonsensical queries (x-category)
}


























