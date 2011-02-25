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
    ScoogleParser.parse("bau") match {
      case Word("bau") => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleWord_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("bau"))
    Assert.assertEquals("name:bau comment:bau", result)
  }

  def testSingleExactWord()
  {
    ScoogleParser.parse("\"bau\"") match {
      case ExactWord("bau") => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleComment()
  {
    ScoogleParser.parse("//wow") match {
      case Comment(List(Word("wow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleComment_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("//wow"))
    Assert.assertEquals("comment:wow", result)
  }

  def testSingleCommentWithSpace()
  {
    ScoogleParser.parse("// wow") match {
      case Comment(List(Word("wow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleCommentWithSpace_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("// wow"))
    Assert.assertEquals("comment:wow", result)
  }

  def testSimpleClassWow()
  {
    ScoogleParser.parse("class Wow") match {
      case Class(Word("Wow"),None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleClass_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Wow"))
    Assert.assertEquals("+type:class +name:Wow", result)
  }

  def testClassEndsWith()
  {
    ScoogleParser.parse("class _Wow") match {
      case Class(EndWith("Wow"),None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassEndsWith_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class _Wow"))
    Assert.assertEquals("+type:class +name:*Wow", result)
  }

  def testClassStartsWith()
  {
    ScoogleParser.parse("class Wow_") match {
      case Class(StartWith("Wow"),None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassStartsWith_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Wow_"))
    Assert.assertEquals("+type:class +name:Wow*", result)
  }

  def testClassWithExtends()
  {
    ScoogleParser.parse("class Robot extends Cloneable") match {
      case Class(Word("Robot"), Some(Word("Cloneable"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Robot extends Cloneable"))
    Assert.assertEquals("+type:class +name:Robot +extends:Cloneable", result)
  }

  def testTraitWithExtends()
  {
    ScoogleParser.parse("trait Robot extends Cloneable") match {
      case Trait(Word("Robot"), Some(Word("Cloneable"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testTraitWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Robot extends Cloneable"))
    Assert.assertEquals("+type:trait +name:Robot +extends:Cloneable", result)
  }

  def testObjectWithExtends()
  {
    ScoogleParser.parse("object Robot extends Cloneable") match {
      case Object(Word("Robot"), Some(Word("Cloneable"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testObjectWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("object Robot extends Cloneable"))
    Assert.assertEquals("+type:object +name:Robot +extends:Cloneable", result)
  }

  def testJustExtends()
  {
    ScoogleParser.parse("extends Cloneable") match {
      case Extends(Word("Cloneable")) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testJustExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("extends Cloneable"))
    Assert.assertEquals("extends:Cloneable", result)
  }

  def testSimpleObject()
  {
    ScoogleParser.parse("object Iterable") match {
      case Object(Word("Iterable"), None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleObject_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("object Iterable"))
    Assert.assertEquals("+type:object +name:Iterable", result)
  }

  def testSimpleTrait()
  {
      ScoogleParser.parse("trait Cloneable") match {
        case Trait(Word("Cloneable"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleTrait_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Cloneable"))
    Assert.assertEquals("+type:trait +name:Cloneable", result)
  }

  def testSimplePackage()
  {
    ScoogleParser.parse("package org.junit") match {
      case Package(Word("org.junit")) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimplePackage_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("package org.junit"))
    Assert.assertEquals("+type:package +name:org.junit", result)
  }

  def testSimpleGroupOr()
  {
    ScoogleParser.parse("(class First or class Second)") match {
      case Group(Or(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleGroupOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(class First or class Second)"))
    Assert.assertEquals("(+type:class +name:First) (+type:class +name:Second)", result)
  }

  def testClassGroupFirstAndClassSecond()
  {
    ScoogleParser.parse("(class First and class Second)") match {
      case Group(And(List(Class(Word("First"), None), Class(Word("Second"), None)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassGroupFirstAndClassSecond_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(class First and class Second)"))
    Assert.assertEquals("+(+type:class +name:First) +(+type:class +name:Second)", result)
  }

  def testOr()
  {
    ScoogleParser.parse("class First or class Second") match {
      case Or(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testOrWithPipe()
  {
      ScoogleParser.parse("class First || class Second") match {
        case Or(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class First or class Second"))
    Assert.assertEquals("(+type:class +name:First) (+type:class +name:Second)", result)
  }

  def testAnd()
  {
      ScoogleParser.parse("class First and class Second") match {
        case And(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAndWithAmpersands()
  {
      ScoogleParser.parse("class First && class Second") match {
        case And(List(Class(Word("First"), None), Class(Word("Second"), None))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAnd_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class First and class Second"))
    Assert.assertEquals("+(+type:class +name:First) +(+type:class +name:Second)", result)
  }

  def testOrWithiinWords()
  {
    ScoogleParser.parse("bau or wow") match {
      case Or(List(Word("bau"), Word("wow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testOrWithiinWords_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("bau or wow"))
    Assert.assertEquals("(name:bau comment:bau) (name:wow comment:wow)", result)
  }

  def testManyWords()
  {
    ScoogleParser.parse("wow bau") match {
      case Comment(List(Word("wow"), Word("bau"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testManyWords_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("bau wow"))
    Assert.assertEquals("comment:bau comment:wow", result)
  }

  def complexQuery1()
  {
    ScoogleParser.parse("(trait Robot and // copy) or extends Cloneable") match {
      case Or(List(
        Group(And(List(Trait(Word("Robot"), None), Comment(List(Word("copy")))))),
        Extends(Word("Cloneable")))
      ) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def contrivedPipesAndAmpersandsCanReplaceBooleans()
  {
      ScoogleParser.parse("(trait Robot && // copy) || extends Cloneable") match {
        case Or(List(
          Group(And(List(Trait(Word("Robot"), None), Comment(List(Word("copy")))))),
          Extends(Word("Cloneable")))
        ) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def complexQuery1_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(trait Robot and // copy) or extends Cloneable"))
    Assert.assertEquals("(+type:trait +name:Robot +comment:copy) extends:Cloneable", result)
  }

  def testSimpleDef()
  {
      ScoogleParser.parse("def Now") match {
        case Def(Word("Now"), List(), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleDef_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def Now"))
    Assert.assertEquals("+type:def +name:Now", result)
  }

  def testDefReturn()
  {
    ScoogleParser.parse("def test:Int") match {
      case Def(Word("test"), List(), Some(Word("Int"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefReturn_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def test:Int"))
    Assert.assertEquals("+type:def +name:test +return:Int", result)
  }

  def testSimpleNot()
  {
    ScoogleParser.parse("not def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Word("Int")))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNotWithExclamation()
  {
    ScoogleParser.parse("! def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Word("Int")))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("not def test:Int"))
    Assert.assertEquals("-(+type:def +name:test +return:Int)", result)
  }

  def testDoubleNot()
  {
    ScoogleParser.parse("not not def test:Int") match {
      case Not(Not(Def(Word("test"), List(), Some(Word("Int"))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDoubleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("not not def test:Int"))
    Assert.assertEquals("-(-(+type:def +name:test +return:Int))", result)
  }

  def testDefWithAnyName()
  {
    ScoogleParser.parse("def _ : Int") match {
      case Def(AnyWord(), List(), Some(Word("Int"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithAnyName_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _ : Int"))
    Assert.assertEquals("+type:def +return:Int", result)
  }

  def testValWithReturnType()
  {
      ScoogleParser.parse("val test:Int") match {
        case Val(Word("test"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testValWithReturnType_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("val test:Int"))
    Assert.assertEquals("+(type:val type:var) +name:test +return:Int", result)
  }

  def testSimpleVar()
  {
      ScoogleParser.parse("var test") match {
        case Var(Word("test"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleVar_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("var test"))
    Assert.assertEquals("+(type:var type:val) +name:test", result)
  }

  def testVarWithReturnType()
  {
      ScoogleParser.parse("var test:Int") match {
        case Var(Word("test"), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testVarWithReturnType_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("var test:Int"))
    Assert.assertEquals("+(type:var type:val) +name:test +return:Int", result)
  }

  def testDefWithEmptyParams()
  {
      ScoogleParser.parse("def _() : Int") match {
        case Def(AnyWord(), List(List()), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithEmptyParams_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _() : Int"))
    Assert.assertEquals("+type:def +methodParamsCount:0 +return:Int", result)
  }

  def testDefWithCurriedEmptyParams()
  {
      ScoogleParser.parse("def _()() : Int") match {
        case Def(AnyWord(), List(List(),List()), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithCurriedEmptyParams_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _() : Int"))
    Assert.assertEquals("+type:def +methodParamsCount:0 +return:Int", result)
  }

  def testDefWithConcreteParam()
  {
      ScoogleParser.parse("def _(Int) : Int") match {
        case Def(AnyWord(), List(List(Word("Int"))), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithConcreteParam_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _(Int) : Int"))
    Assert.assertEquals("+type:def +methodParamsCount:0 +return:Int", result)
  }

  def testDefWithMultipleConcreteParam()
  {
      ScoogleParser.parse("def _(Int, String) : Int") match {
        case Def(AnyWord(), List(List(Word("Int"), Word("String"))), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParam()
  {
      ScoogleParser.parse("def _(Int, String, *) : Int") match {
        case Def(AnyWord(), List(List(Word("Int"), Word("String"), AnyParams())), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurried()
  {
      ScoogleParser.parse("def _(Int, String, *)(_, *) : Int") match {
        case Def(AnyWord(), List(
                              List(Word("Int"), Word("String"), AnyParams()),
                              List(AnyWord(), AnyParams())
                           ), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurriedNoSeparators()
  {
      ScoogleParser.parse("def _(Int String *)(_ *) : Int") match {
        case Def(AnyWord(), List(
                              List(Word("Int"), Word("String"), AnyParams()),
                              List(AnyWord(), AnyParams())
                           ), Some(Word("Int"))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testSimpleVal()
  {
      ScoogleParser.parse("val test") match {
        case Val(Word("test"), None) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def contrivedQuery2()
  {
      ScoogleParser.parse("""//can "be copied" or (trait Robot and def replicate(_ Model *)(_, Blueprint): Robot) or (extends Cloneable && val archetype""") match {
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

  def testCommentEatsManyKeywordsButHasKeywords()
  {
    ScoogleParser.parse("// class _Wow or object _ test_ _meow_") match {
      case Comment(List(Word("class"), EndWith("Wow"), Word("or"), Word("object"), AnyWord(), StartWith("test"), Contains("meow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testCommentEatsManyKeywordsButHasKeywords_query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("// class _Wow or object _ test_ _meow_"))
     Assert.assertEquals("comment:class comment:*Wow comment:or comment:object comment:test* comment:*meow*", result)
  }

  def testCommentEatsKeyword()
  {
    ScoogleParser.parse("// ala and bala") match {
      case Comment(List(Word("ala"), Word("and"), Word("bala"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testCommentEatsKeyword_query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("// ala and bala"))
     Assert.assertEquals("comment:ala comment:and comment:bala", result)
  }

  def testContains()
  {
    ScoogleParser.parse("class _c_ extends _e_") match {
      case Class(Contains("c"), Some(Contains("e"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testContains_query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class _c_ extends _e_"))
     Assert.assertEquals("+type:class +name:*c* +extends:*e*", result)
  }


  // TODO: Test strange identifiers
  // TODO: Test invalid syntax
  // TODO: Test nonsensical queries (x-category)
}


























