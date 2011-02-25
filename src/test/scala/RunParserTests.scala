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
  val suite = new TestSuite()
  suite.addTestSuite(classOf[ParserAndLuceneTests])
  suite.addTestSuite(classOf[LuceneRegressionTests])
  suite.addTestSuite(classOf[ScoogleParserTests])

  _root_.junit.textui.TestRunner.run(suite)
}

class ScoogleParserTests extends TestCase
{
  implicit def idToType(id:Identifier):Type =
  {
    Type(id, List())
  }

  def testSingleWord()
  {
    ScoogleParser.parse("bau") match {
      case Word("bau") => ()
      case e => Assert.fail(e.toString)
    }
  }

  def simpleType(str:String) : Type = Type(Word(str), List())

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
      case Class(Word("wow"), None, List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleClass_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Wow"))
    Assert.assertEquals("+type:class +name:wow", result)
  }

  def testClassEndsWith()
  {
    ScoogleParser.parse("class _Wow") match {
      case Class(EndWith("wow"), None, List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassEndsWith_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class _Wow"))
    Assert.assertEquals("+type:class +name:*wow", result)
  }

  def testClassStartsWith()
  {
    ScoogleParser.parse("class Wow_") match {
      case Class(StartWith("wow"), None, List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassStartsWith_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Wow_"))
    Assert.assertEquals("+type:class +name:wow*", result)
  }

  def testClassWithExtends()
  {
    ScoogleParser.parse("class Robot extends Cloneable") match {
      case Class(Word("robot"), Some(Type(Word("cloneable"), List())), List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class Robot extends Cloneable"))
    Assert.assertEquals("+type:class +name:robot +extends:cloneable", result)
  }

  def testTraitWithExtends()
  {
    ScoogleParser.parse("trait Robot extends Cloneable") match {
      case Trait(Word("robot"), Some(Type(Word("cloneable"), List())), List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testTraitWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Robot extends Cloneable"))
    Assert.assertEquals("+type:trait +name:robot +extends:cloneable", result)
  }

  def testObjectWithExtends()
  {
    ScoogleParser.parse("object Robot extends Cloneable") match {
      case Object(Word("robot"), Some(Type(Word("cloneable"), List())), List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testObjectWithExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("object Robot extends Cloneable"))
    Assert.assertEquals("+type:object +name:robot +extends:cloneable", result)
  }

  def testJustExtends()
  {
    ScoogleParser.parse("extends Cloneable") match {
      case Extends(Type(Word("cloneable"), List())) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testJustExtends_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("extends Cloneable"))
    Assert.assertEquals("extends:cloneable", result)
  }

  def testSimpleObject()
  {
    ScoogleParser.parse("object Iterable") match {
      case Object(Word("iterable"), None, List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleObject_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("object Iterable"))
    Assert.assertEquals("+type:object +name:iterable", result)
  }

  def testSimpleTrait()
  {
    ScoogleParser.parse("trait Cloneable") match {
      case Trait(Word("cloneable"), None, List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleTrait_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Cloneable"))
    Assert.assertEquals("+type:trait +name:cloneable", result)
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
      case Group(Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List())))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleGroupOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(class First or class Second)"))
    Assert.assertEquals("(+type:class +name:first) (+type:class +name:second)", result)
  }

  def testClassGroupFirstAndClassSecond()
  {
    ScoogleParser.parse("(class First and class Second)") match {
      case Group(And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List())))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testClassGroupFirstAndClassSecond_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(class First and class Second)"))
    Assert.assertEquals("+(+type:class +name:first) +(+type:class +name:second)", result)
  }

  def testOr()
  {
    ScoogleParser.parse("class First or class Second") match {
      case Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testOrWithPipe()
  {
    ScoogleParser.parse("class First || class Second") match {
      case Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testOr_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class First or class Second"))
    Assert.assertEquals("(+type:class +name:first) (+type:class +name:second)", result)
  }

  def testAnd()
  {
      ScoogleParser.parse("class First and class Second") match {
        case And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAndWithAmpersands()
  {
      ScoogleParser.parse("class First && class Second") match {
        case And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testAnd_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class First and class Second"))
    Assert.assertEquals("+(+type:class +name:first) +(+type:class +name:second)", result)
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
        Group(And(List(Trait(Word("robot"), None, List()), Comment(List(Word("copy")))))),
        Extends(Type(Word("cloneable"), List())))
      ) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def contrivedPipesAndAmpersandsCanReplaceBooleans()
  {
    ScoogleParser.parse("(trait Robot && // copy) || extends Cloneable") match {
      case Or(List(
        Group(And(List(Trait(Word("robot"), None, List()), Comment(List(Word("copy")))))),
        Extends(Type(Word("cloneable"), List())))
      ) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def complexQuery1_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("(trait Robot and // copy) or extends Cloneable"))
    Assert.assertEquals("(+type:trait +name:robot +comment:copy) extends:cloneable", result)
  }

  def testSimpleDef()
  {
    ScoogleParser.parse("def Now") match {
      case Def(Word("now"), List(), None) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleDef_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def Now"))
    Assert.assertEquals("+type:def +name:now", result)
  }

  def testDefReturn()
  {
    ScoogleParser.parse("def test:Int") match {
      case Def(Word("test"), List(), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefReturn_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def test:Int"))
    Assert.assertEquals("+type:def +name:test +return:int", result)
  }

  def testSimpleNot()
  {
    ScoogleParser.parse("not def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Type(Word("int"), List())))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNotWithExclamation()
  {
    ScoogleParser.parse("! def test:Int") match {
      case Not(Def(Word("test"), List(), Some(Type(Word("int"), List())))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("not def test:Int"))
    Assert.assertEquals("-(+type:def +name:test +return:int)", result)
  }

  def testDoubleNot()
  {
    ScoogleParser.parse("not not def test:Int") match {
      case Not(Not(Def(Word("test"), List(), Some(Type(Word("int"), List()))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDoubleNot_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("not not def test:Int"))
    Assert.assertEquals("-(-(+type:def +name:test +return:int))", result)
  }

  def testDefWithAnyName()
  {
    ScoogleParser.parse("def _ : Int") match {
      case Def(AnyWord(), List(), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithAnyName_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _ : Int"))
    Assert.assertEquals("+type:def +return:int", result)
  }

  def testValWithReturnType()
  {
      ScoogleParser.parse("val test:Int") match {
        case Val(Word("test"), Some(Type(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testValWithReturnType_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("val test:Int"))
    Assert.assertEquals("+(type:val type:var) +name:test +return:int", result)
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
      case Var(Word("test"), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testVarWithReturnType_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("var test:Int"))
    Assert.assertEquals("+(type:var type:val) +name:test +return:int", result)
  }

  def testDefWithEmptyParams()
  {
    ScoogleParser.parse("def _() : Int") match {
      case Def(AnyWord(), List(List()), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithEmptyParams_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _() : Int"))
    Assert.assertEquals("+type:def +methodParamsCount:[0 TO 0] +return:int", result)
  }

  def testDefWithCurriedEmptyParams()
  {
    ScoogleParser.parse("def _()() : Int") match {
      case Def(AnyWord(), List(List(), List()), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithCurriedEmptyParams_Query()
  {
    val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("def _() : Int"))
    Assert.assertEquals("+type:def +methodParamsCount:[0 TO 0] +return:int", result)
  }

  def testDefWithConcreteParam()
  {
    ScoogleParser.parse("def _(Int) : Int") match {
      case Def(AnyWord(), List(List(Type(Word("int"), List()))), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteParam()
  {
      ScoogleParser.parse("def _(Int, String) : Int") match {
        case Def(AnyWord(), List(List(Type(Word("int"), List()), Type(Word("string"), List()))), Some(Type(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParam()
  {
    ScoogleParser.parse("def _(Int, String, *) : Int") match {
      case Def(AnyWord(), List(List(Type(Word("int"), List()), Type(Word("string"), List()), AnyParams())), Some(Type(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurried()
  {
      ScoogleParser.parse("def _(Int, String, *)(_, *) : Int") match {
        case Def(AnyWord(), List(
                              List(Type(Word("int"), List()), Type(Word("string"), List()), AnyParams()),
                              List(Type(AnyWord(), List()), AnyParams())
                           ), Some(Type(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurriedNoSeparators()
  {
      ScoogleParser.parse("def _(Int String *)(_ *) : Int") match {
        case Def(AnyWord(), List(
                              List(Type(Word("int"), List()), Type(Word("string"), List()), AnyParams()),
                              List(Type(AnyWord(), List()), AnyParams())
                           ), Some(Type(Word("int"), List()))) => ()
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

////// NOTE: This does not compile with Java 1.6 (23) on 32 bit but does on x64 :)
//////  def contrivedQuery2()
//////  {
//////      ScoogleParser.parse("""//can "be copied" or (trait Robot and def replicate(_ Model *)(_, Blueprint): Robot) or (extends Cloneable && val archetype""") match {
//////        case Or(List(
//////          Comment(List(Word("can"), ExactWord("be copied"))),
//////          Group(And(List(
//////            Trait(Word("robot"), None),
//////            Def(
//////              Word("replicate"),
//////              List(List(Word("_"), Word("model"), AnyParams()), List(Word("_"), Word("blueprint"))),
//////              Some(Word("robot")))))),
//////          Group(And(List(
//////            Extends(Word("cloneable")),
//////            Val(Word("archetype"), None))))
//////        )) => ()
//////        case e => Assert.fail(e.toString)
//////    }
//////  }

  def testCommentEatsManyKeywordsButHasKeywords()
  {
    ScoogleParser.parse("// class _Wow or object _ test_ _meow_") match {
      case Comment(List(Word("class"), EndWith("wow"), Word("or"), Word("object"), AnyWord(), StartWith("test"), Contains("meow"))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testCommentEatsManyKeywordsButHasKeywords_query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("// class _Wow or object _ test_ _meow_"))
     Assert.assertEquals("comment:class comment:*wow comment:or comment:object comment:test* comment:*meow*", result)
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
      case Class(Contains("c"), Some(Type(Contains("e"),_)), List()) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testContains_query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("class _c_ extends _e_"))
     Assert.assertEquals("+type:class +name:*c* +extends:*e*", result)
  }

  def testSimpleGeneric()
  {
    ScoogleParser.parse("extends List[Int]") match {
      case Extends(Type(Word("list"), List(Type(Word("int"), _)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleGeneric_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("extends List[Int]"))
     Assert.assertEquals("extends:list[int]", result)
  }

  def testNestedGeneric()
  {
    ScoogleParser.parse("extends List[Array[Int]]") match {
      case Extends(Type(Word("list"), List(Type(Word("array"), List(Type(Word("int"), _)))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNestedGeneric_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("extends List[Array[Int]]"))
     Assert.assertEquals("extends:list[array[int]]", result)
  }

  def testMultipleGeneric()
  {
    ScoogleParser.parse("extends Map[String,Int]") match {
      case Extends(Type(Word("map"), List(Type(Word("string"), _), Type(Word("int"), _)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testMultipleGeneric_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("extends Map[String,Int]"))
     Assert.assertEquals("extends:map[string,int]", result)
  }

  def testMultipleGenericWithSpace()
  {
    ScoogleParser.parse("extends Map[String Int]") match {
      case Extends(Type(Word("map"), List(Type(Word("string"), _), Type(Word("int"), _)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleWith()
  {
    ScoogleParser.parse("with Wow") match {
      case Withs(List(Type(Word("wow"), _))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleWith_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with Wow"))
     Assert.assertEquals("+withs:wow", result)
  }

  def testMultipleWith()
  {
    ScoogleParser.parse("with Wow with List[Wow]") match {
      case Withs(List(Type(Word("wow"), _), Type(Word("list"), List(Type(Word("wow"), _))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testMultipleWith_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with Wow with List[Wow]"))
     Assert.assertEquals("+withs:wow +withs:list[wow]", result)
  }

  def testFullTrait()
  {
    ScoogleParser.parse("trait Bau_ extends List[_] with Wow with Set[Wow]") match {
      case Trait(
            StartWith("bau"),
            Some(Type(Word("list"), List(Type(AnyWord(), _)))),
            List(Type(Word("wow"), _), Type(Word("set"), List(Type(Word("wow"), _))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testFullTrait_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Bau_ extends List[_] with Wow with Set[Wow]"))
     Assert.assertEquals("+type:trait +name:bau* +extends:list[*] +(+withs:wow +withs:set[wow])", result)
  }

  // TODO: Test strange identifiers
  // TODO: Test invalid syntax
  // TODO: Test nonsensical queries (x-category)
}


























