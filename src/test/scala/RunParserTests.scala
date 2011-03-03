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
  suite.addTestSuite(classOf[ParseAndSearchMethods])
  suite.addTestSuite(classOf[ParseAndSearchLambdaParams])
  suite.addTestSuite(classOf[LuceneRegressionTests])
  suite.addTestSuite(classOf[ScoogleParserTests])

  _root_.junit.textui.TestRunner.run(suite)
}

class ScoogleParserTests extends TestCase
{
  implicit def idToType(id:Identifier):Type =
  {
    SimpleType(id, List())
  }

  def testSingleWord()
  {
    ScoogleParser.parse("bau") match {
      case Word("bau") => ()
      case e => Assert.fail(e.toString)
    }
  }

  def simpleType(str:String) : Type = SimpleType(Word(str), List())

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
      case Class(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
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
      case Trait(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
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
      case Object(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
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
      case Extends(SimpleType(Word("cloneable"), List())) => ()
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
        Extends(SimpleType(Word("cloneable"), List())))
      ) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def contrivedPipesAndAmpersandsCanReplaceBooleans()
  {
    ScoogleParser.parse("(trait Robot && // copy) || extends Cloneable") match {
      case Or(List(
        Group(And(List(Trait(Word("robot"), None, List()), Comment(List(Word("copy")))))),
        Extends(SimpleType(Word("cloneable"), List())))
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
      case Def(Word("test"), List(), Some(SimpleType(Word("int"), List()))) => ()
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
      case Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List())))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNotWithExclamation()
  {
    ScoogleParser.parse("! def test:Int") match {
      case Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List())))) => ()
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
      case Not(Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List()))))) => ()
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
      case Def(AnyWord(), List(), Some(SimpleType(Word("int"), List()))) => ()
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
        case Val(Word("test"), Some(SimpleType(Word("int"), List()))) => ()
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
      case Var(Word("test"), Some(SimpleType(Word("int"), List()))) => ()
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
      case Def(AnyWord(), List(List()), Some(SimpleType(Word("int"), List()))) => ()
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
      case Def(AnyWord(), List(List(), List()), Some(SimpleType(Word("int"), List()))) => ()
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
      case Def(AnyWord(), List(List(SimpleType(Word("int"), List()))), Some(SimpleType(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteParam()
  {
      ScoogleParser.parse("def _(Int, String) : Int") match {
        case Def(AnyWord(), List(List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()))), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParam()
  {
    ScoogleParser.parse("def _(Int, String, *) : Int") match {
      case Def(AnyWord(), List(List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams())), Some(SimpleType(Word("int"), List()))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurried()
  {
      ScoogleParser.parse("def _(Int, String, *)(_, *) : Int") match {
        case Def(AnyWord(), List(
                              List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams()),
                              List(SimpleType(AnyWord(), List()), AnyParams())
                           ), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
    }
  }

  def testDefWithMultipleConcreteAndAnyParamCurriedNoSeparators()
  {
      ScoogleParser.parse("def _(Int String *)(_ *) : Int") match {
        case Def(AnyWord(), List(
                              List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams()),
                              List(SimpleType(AnyWord(), List()), AnyParams())
                           ), Some(SimpleType(Word("int"), List()))) => ()
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
      case Class(Contains("c"), Some(SimpleType(Contains("e"),_)), List()) => ()
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
      case Extends(SimpleType(Word("list"), List(SimpleType(Word("int"), _)))) => ()
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
      case Extends(SimpleType(Word("list"), List(SimpleType(Word("array"), List(SimpleType(Word("int"), _)))))) => ()
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
      case Extends(SimpleType(Word("map"), List(SimpleType(Word("string"), _), SimpleType(Word("int"), _)))) => ()
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
      case Extends(SimpleType(Word("map"), List(SimpleType(Word("string"), _), SimpleType(Word("int"), _)))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSingleWith()
  {
    ScoogleParser.parse("with Wow") match {
      case Withs(List(SimpleType(Word("wow"), _))) => ()
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
      case Withs(List(SimpleType(Word("wow"), _), SimpleType(Word("list"), List(SimpleType(Word("wow"), _))))) => ()
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
            Some(SimpleType(Word("list"), List(SimpleType(AnyWord(), _)))),
            List(SimpleType(Word("wow"), _), SimpleType(Word("set"), List(SimpleType(Word("wow"), _))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testFullTrait_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("trait Bau_ extends List[_] with Wow with Set[Wow]"))
     Assert.assertEquals("+type:trait +name:bau* +extends:list[*] +(+withs:wow +withs:set[wow])", result)
  }

  def testExactNameTildaCharacter()
  {
    ScoogleParser.parse("with `~`") match {
      case Withs(List(SimpleType(Word("~"), _))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testExactNameTildaCharacter_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with `~`"))
     Assert.assertEquals("+withs:~", result)
  }

  def testCharactersInName()
  {
    ScoogleParser.parse("with ~*=><") match {
      case Withs(List(SimpleType(Word("~*=><"), _))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testCharactersInName_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with ~*=><"))
     Assert.assertEquals("+withs:~*=><", result)
  }

  def testKeywordInName()
  {
    ScoogleParser.parse("with `def`") match {
      case Withs(List(SimpleType(Word("def"), _))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testKeywordInName_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with `def`"))
     Assert.assertEquals("+withs:def", result)
  }

  def testNoWildcardsInExactName()
  {
    ScoogleParser.parse("with `_:`") match {
      case Withs(List(SimpleType(Word("_:"), _))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNoWildcardsInExactName_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with `_:`"))
     Assert.assertEquals("+withs:_:", result)
  }

  def testSimpleTuple()
  {
    ScoogleParser.parse("with (Int)") match {
      case Withs(List(Tuple(List(SimpleType(Word("int"), _))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testSimpleTuple_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with (Int)"))
     Assert.assertEquals("+withs:(int)", result)
  }

  def testMultiTuple()
  {
    ScoogleParser.parse("with (Int, String)") match {
      case Withs(List(Tuple(List(
        SimpleType(Word("int"), _),
        SimpleType(Word("string"), _)
      )))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testMultiTupleNoComma()
  {
    ScoogleParser.parse("with (Int String)") match {
      case Withs(List(Tuple(List(
        SimpleType(Word("int"), _),
        SimpleType(Word("string"), _)
      )))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testMultiTuple_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with (Int, String)"))
     Assert.assertEquals("+withs:(int,string)", result)
  }

  def testNestedTuple()
  {
    ScoogleParser.parse("with (Int, (String, Wow))") match {
      case Withs(List(Tuple(List(
        SimpleType(Word("int"), _),
        Tuple(List(
          SimpleType(Word("string"), _),
          SimpleType(Word("wow"), _)
      )))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNestedTupleNoCommas()
  {
    ScoogleParser.parse("with (Int(String Wow))") match {
      case Withs(List(Tuple(List(
        SimpleType(Word("int"), _),
        Tuple(List(
          SimpleType(Word("string"), _),
          SimpleType(Word("wow"), _)
      )))))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testNestedTuple_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with (Int, (String, Wow))"))
     Assert.assertEquals("+withs:(int,(string,wow))", result)
  }

  def testTupleOfGenerics()
  {
    ScoogleParser.parse("with (Map[Int, Wow], List[String])") match {
      case Withs(List(Tuple(List(
        SimpleType(Word("map"), List(SimpleType(Word("int"), _), SimpleType(Word("wow"), _))),
        SimpleType(Word("list"), List(SimpleType(Word("string"), _)))
      )))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testTupleOfGenerics_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with (Map[Int, Wow], List[String])"))
     Assert.assertEquals("+withs:(map[int,wow],list[string])", result)
  }

  def testGenericTuple()
  {
    ScoogleParser.parse("with Map[(Int, Wow), (String)]") match {
      case Withs(List(SimpleType(Word("map"), List(
        Tuple(List(SimpleType(Word("int"),_), SimpleType(Word("wow"), _))),
        Tuple(List(SimpleType(Word("string"), _)))
      )))) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testGenericTuple_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with Map[(Int, Wow), (String)]"))
     Assert.assertEquals("+withs:map[(int,wow),(string)]", result)
  }

  def testWildcardTuple()
  {
    ScoogleParser.parse("with (_, Wow_) with (_String, *)") match {
      case Withs(List(
        Tuple(List(SimpleType(AnyWord(), _), SimpleType(StartWith("wow"), _))),
        Tuple(List(SimpleType(EndWith("string"), _), AnyParams()))
      )) => ()
      case e => Assert.fail(e.toString)
    }
  }

  def testWildcardTuple_Query()
  {
     val result = LuceneQuery.toLuceneQueryString(ScoogleParser.parse("with (_, Wow_) with (_String, *)"))
     Assert.assertEquals("+withs:(*,wow*) +withs:(*string,*)", result)
  }


  // TODO: Test invalid syntax
  // TODO: Test nonsensical queries (x-category)
}


























