package scala.tools.colladoc.search

import org.junit.Assert
import org.specs.SpecificationWithJUnit
import org.specs.util.Configuration

object ScoogleParserTests extends SpecificationWithJUnit
{
  implicit def idToType(id:Identifier):Type =
  {
    SimpleType(id, List())
  }

  def simpleType(str:String) : Type = SimpleType(Word(str), List())

  def parse = addToSusVerb("parse")


  // Used to store and restore the config that was being used before this test
  // runs
  val oldConfig = Configuration.config

  doBeforeSpec {
    object configuration extends Configuration {
      // Since we converted these examples from JUnit tests, most don't have any
      // Specs expectations. Therefore, we need to override the default Specs
      // behaviour.
      override def examplesWithoutExpectationsMustBePending = false
    }

    Configuration.config = configuration
  }

  "QueryParser" should parse {
    "single word" in {
      QueryParser.parse("bau") match {
        case Word("bau") => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single word (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("bau"))
      Assert.assertEquals("name:bau comment:bau", result)
    }

    "single exact word" in {
      QueryParser.parse("\"bau\"") match {
        case ExactWord("bau") => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single comment" in {
      QueryParser.parse("//wow") match {
        case Comment(List(Word("wow"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single comment (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("//wow"))
      Assert.assertEquals("comment:wow", result)
    }

    "single comment with space" in {
      QueryParser.parse("// wow") match {
        case Comment(List(Word("wow"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single comment with space (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("// wow"))
      Assert.assertEquals("comment:wow", result)
    }

    "simple class" in {
      QueryParser.parse("class Wow") match {
        case Class(Word("wow"), None, List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple class (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class Wow"))
      Assert.assertEquals("+type:class +name:wow", result)
    }

    "class ends with" in {
      QueryParser.parse("class _Wow") match {
        case Class(EndWith("wow"), None, List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "class ends with (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class _Wow"))
      Assert.assertEquals("+type:class +name:*wow", result)
    }

    "class starts with" in {
      QueryParser.parse("class Wow_") match {
        case Class(StartWith("wow"), None, List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "class starts with (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class Wow_"))
      Assert.assertEquals("+type:class +name:wow*", result)
    }

    "class with extends" in {
      QueryParser.parse("class Robot extends Cloneable") match {
        case Class(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "class with extends (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class Robot extends Cloneable"))
      Assert.assertEquals("+type:class +name:robot +extends:cloneable", result)
    }

    "trait with extends" in {
      QueryParser.parse("trait Robot extends Cloneable") match {
        case Trait(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "trait with extends (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("trait Robot extends Cloneable"))
      Assert.assertEquals("+type:trait +name:robot +extends:cloneable", result)
    }

    "object with extends" in {
      QueryParser.parse("object Robot extends Cloneable") match {
        case Object(Word("robot"), Some(SimpleType(Word("cloneable"), List())), List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "object with extends (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("object Robot extends Cloneable"))
      Assert.assertEquals("+type:object +name:robot +extends:cloneable", result)
    }

    "just extends" in {
      QueryParser.parse("extends Cloneable") match {
        case Extends(SimpleType(Word("cloneable"), List())) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "just extends (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("extends Cloneable"))
      Assert.assertEquals("extends:cloneable", result)
    }

    "simple object" in {
      QueryParser.parse("object Iterable") match {
        case Object(Word("iterable"), None, List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple object (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("object Iterable"))
      Assert.assertEquals("+type:object +name:iterable", result)
    }

    "simple trait" in {
      QueryParser.parse("trait Cloneable") match {
        case Trait(Word("cloneable"), None, List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple trait (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("trait Cloneable"))
      Assert.assertEquals("+type:trait +name:cloneable", result)
    }

    "simple package" in {
      QueryParser.parse("package org.junit") match {
        case Package(Word("org.junit")) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple package (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("package org.junit"))
      Assert.assertEquals("+type:package +name:org.junit", result)
    }

    "simple group or" in {
      QueryParser.parse("(class First or class Second)") match {
        case Group(Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List())))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple group or (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("(class First or class Second)"))
      Assert.assertEquals("(+type:class +name:first) (+type:class +name:second)", result)
    }

    "class group first and class second" in {
      QueryParser.parse("(class First and class Second)") match {
        case Group(And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List())))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "class group first and class second (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("(class First and class Second)"))
      Assert.assertEquals("+(+type:class +name:first) +(+type:class +name:second)", result)
    }

    "or" in {
      QueryParser.parse("class First or class Second") match {
        case Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "||" in {
      QueryParser.parse("class First || class Second") match {
        case Or(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "or (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class First or class Second"))
      Assert.assertEquals("(+type:class +name:first) (+type:class +name:second)", result)
    }

    "and" in {
        QueryParser.parse("class First and class Second") match {
          case And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "&&" in {
        QueryParser.parse("class First && class Second") match {
          case And(List(Class(Word("first"), None, List()), Class(Word("second"), None, List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "and (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class First and class Second"))
      Assert.assertEquals("+(+type:class +name:first) +(+type:class +name:second)", result)
    }

    "or within words" in {
      QueryParser.parse("bau or wow") match {
        case Or(List(Word("bau"), Word("wow"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "or within words (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("bau or wow"))
      Assert.assertEquals("(name:bau comment:bau) (name:wow comment:wow)", result)
    }

    "many words" in {
      QueryParser.parse("wow bau") match {
        case Comment(List(Word("wow"), Word("bau"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "many words (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("bau wow"))
      Assert.assertEquals("comment:bau comment:wow", result)
    }

    "complex query" in {
      QueryParser.parse("(trait Robot and // copy) or extends Cloneable") match {
        case Or(List(
          Group(And(List(Trait(Word("robot"), None, List()), Comment(List(Word("copy")))))),
          Extends(SimpleType(Word("cloneable"), List())))
        ) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "contrived ||'s and &&'s can replace booleans" in {
      QueryParser.parse("(trait Robot && // copy) || extends Cloneable") match {
        case Or(List(
          Group(And(List(Trait(Word("robot"), None, List()), Comment(List(Word("copy")))))),
          Extends(SimpleType(Word("cloneable"), List())))
        ) => ()
        case e => Assert.fail(e.toString)
      }
    }

    // TODO: Investigate this test.
    // This test wasn't running before we converted parser tests to Specs and now
    // it fails.
//    "complex query (Query)" in {
//      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("(trait Robot and // copy) or extends Cloneable"))
//      Assert.assertEquals("(+type:trait +name:robot +comment:copy) extends:cloneable", result)
//    }

    "simple def" in {
      QueryParser.parse("def Now") match {
        case Def(Word("now"), List(), None) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple def (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("def Now"))
      Assert.assertEquals("+type:def +name:now", result)
    }

    "def return" in {
      QueryParser.parse("def test:Int") match {
        case Def(Word("test"), List(), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def return (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("def test:Int"))
      Assert.assertEquals("+type:def +name:test +return:int", result)
    }

    "simple not" in {
      QueryParser.parse("not def test:Int") match {
        case Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List())))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "!" in {
      QueryParser.parse("! def test:Int") match {
        case Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List())))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple not (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("not def test:Int"))
      Assert.assertEquals("-(+type:def +name:test +return:int)", result)
    }

    "double not" in {
      QueryParser.parse("not not def test:Int") match {
        case Not(Not(Def(Word("test"), List(), Some(SimpleType(Word("int"), List()))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "double not (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("not not def test:Int"))
      Assert.assertEquals("-(-(+type:def +name:test +return:int))", result)
    }

    "def with any name" in {
      QueryParser.parse("def _ : Int") match {
        case Def(AnyWord(), List(), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def with any name (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("def _ : Int"))
      Assert.assertEquals("+type:def +return:int", result)
    }

    "val with return type" in {
        QueryParser.parse("val test:Int") match {
          case Val(Word("test"), Some(SimpleType(Word("int"), List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "val with return type (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("val test:Int"))
      Assert.assertEquals("+(type:val type:var) +name:test +return:int", result)
    }

    "simple var" in {
      QueryParser.parse("var test") match {
        case Var(Word("test"), None) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple var (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("var test"))
      Assert.assertEquals("+(type:var type:val) +name:test", result)
    }

    "var with return type" in {
      QueryParser.parse("var test:Int") match {
        case Var(Word("test"), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "var with return type (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("var test:Int"))
      Assert.assertEquals("+(type:var type:val) +name:test +return:int", result)
    }

    "def with empty params" in {
      QueryParser.parse("def _() : Int") match {
        case Def(AnyWord(), List(List()), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def with empty params (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("def _() : Int"))
      Assert.assertEquals("+type:def +methodParamsCount:[0 TO 0] +return:int", result)
    }

    "def with curried empty params" in {
      QueryParser.parse("def _()() : Int") match {
        case Def(AnyWord(), List(List(), List()), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def with curried empty params (Query)" in {
      val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("def _() : Int"))
      Assert.assertEquals("+type:def +methodParamsCount:[0 TO 0] +return:int", result)
    }

    "def with concrete param" in {
      QueryParser.parse("def _(Int) : Int") match {
        case Def(AnyWord(), List(List(SimpleType(Word("int"), List()))), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def with multiple concrete param" in {
        QueryParser.parse("def _(Int, String) : Int") match {
          case Def(AnyWord(), List(List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()))), Some(SimpleType(Word("int"), List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "def with multiple concrete and any param" in {
      QueryParser.parse("def _(Int, String, *) : Int") match {
        case Def(AnyWord(), List(List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams())), Some(SimpleType(Word("int"), List()))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "def with multiple concrete and any param curried" in {
        QueryParser.parse("def _(Int, String, *)(_, *) : Int") match {
          case Def(AnyWord(), List(
                                List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams()),
                                List(SimpleType(AnyWord(), List()), AnyParams())
                             ), Some(SimpleType(Word("int"), List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "def with multiple concreate and any param curried (no serparators)" in {
        QueryParser.parse("def _(Int String *)(_ *) : Int") match {
          case Def(AnyWord(), List(
                                List(SimpleType(Word("int"), List()), SimpleType(Word("string"), List()), AnyParams()),
                                List(SimpleType(AnyWord(), List()), AnyParams())
                             ), Some(SimpleType(Word("int"), List()))) => ()
          case e => Assert.fail(e.toString)
      }
    }

    "simple val" in {
        QueryParser.parse("val test") match {
          case Val(Word("test"), None) => ()
          case e => Assert.fail(e.toString)
      }
    }

  ////// NOTE: This does not compile with Java 1.6 (23) on 32 bit but does on x64 :)
  //////  def contrivedQuery2()
  //////  {
  //////      QueryParser.parse("""//can "be copied" or (trait Robot and def replicate(_ Model *)(_, Blueprint): Robot) or (extends Cloneable && val archetype""") match {
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

    "comment eats many keywords but has keywords" in {
      QueryParser.parse("// class _Wow or object _ test_ _meow_") match {
        case Comment(List(Word("class"), EndWith("wow"), Word("or"), Word("object"), AnyWord(), StartWith("test"), Contains("meow"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "comment eats many keywords but has keywords (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("// class _Wow or object _ test_ _meow_"))
       Assert.assertEquals("comment:class comment:*wow comment:or comment:object comment:test* comment:*meow*", result)
    }

    "comment eats keyword" in {
      QueryParser.parse("// ala and bala") match {
        case Comment(List(Word("ala"), Word("and"), Word("bala"))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "comment eats keyword (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("// ala and bala"))
       Assert.assertEquals("comment:ala comment:and comment:bala", result)
    }

    "contains" in {
      QueryParser.parse("class _c_ extends _e_") match {
        case Class(Contains("c"), Some(SimpleType(Contains("e"),_)), List()) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "contains (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("class _c_ extends _e_"))
       Assert.assertEquals("+type:class +name:*c* +extends:*e*", result)
    }

    "simple generic" in {
      QueryParser.parse("extends List[Int]") match {
        case Extends(SimpleType(Word("list"), List(SimpleType(Word("int"), _)))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple generic (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("extends List[Int]"))
       Assert.assertEquals("extends:list[int]", result)
    }

    "nested generic" in {
      QueryParser.parse("extends List[Array[Int]]") match {
        case Extends(SimpleType(Word("list"), List(SimpleType(Word("array"), List(SimpleType(Word("int"), _)))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "nested generic (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("extends List[Array[Int]]"))
       Assert.assertEquals("extends:list[array[int]]", result)
    }

    "multiple generics" in {
      QueryParser.parse("extends Map[String,Int]") match {
        case Extends(SimpleType(Word("map"), List(SimpleType(Word("string"), _), SimpleType(Word("int"), _)))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "multiple generics (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("extends Map[String,Int]"))
       Assert.assertEquals("extends:map[string,int]", result)
    }

    "multiple generics with space" in {
      QueryParser.parse("extends Map[String Int]") match {
        case Extends(SimpleType(Word("map"), List(SimpleType(Word("string"), _), SimpleType(Word("int"), _)))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single with" in {
      QueryParser.parse("with Wow") match {
        case Withs(List(SimpleType(Word("wow"), _))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "single with (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with Wow"))
       Assert.assertEquals("+withs:wow", result)
    }

    "multiple withs" in {
      QueryParser.parse("with Wow with List[Wow]") match {
        case Withs(List(SimpleType(Word("wow"), _), SimpleType(Word("list"), List(SimpleType(Word("wow"), _))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "multiple withs (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with Wow with List[Wow]"))
       Assert.assertEquals("+withs:wow +withs:list[wow]", result)
    }

    "full trait" in {
      QueryParser.parse("trait Bau_ extends List[_] with Wow with Set[Wow]") match {
        case Trait(
              StartWith("bau"),
              Some(SimpleType(Word("list"), List(SimpleType(AnyWord(), _)))),
              List(SimpleType(Word("wow"), _), SimpleType(Word("set"), List(SimpleType(Word("wow"), _))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "full trait (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("trait Bau_ extends List[_] with Wow with Set[Wow]"))
       Assert.assertEquals("+type:trait +name:bau* +extends:list[*] +(+withs:wow +withs:set[wow])", result)
    }

    "exact name '~'" in {
      QueryParser.parse("with `~`") match {
        case Withs(List(SimpleType(Word("~"), _))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "exact name '~' (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with `~`"))
       Assert.assertEquals("+withs:~", result)
    }

    "characters in name" in {
      QueryParser.parse("with ~*=><") match {
        case Withs(List(SimpleType(Word("~*=><"), _))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "characters in name (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with ~*=><"))
       Assert.assertEquals("+withs:~*=><", result)
    }

    "keyword in name" in {
      QueryParser.parse("with `def`") match {
        case Withs(List(SimpleType(Word("def"), _))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "keyword in name (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with `def`"))
       Assert.assertEquals("+withs:def", result)
    }

    "no wild cards in exact name" in {
      QueryParser.parse("with `_:`") match {
        case Withs(List(SimpleType(Word("_:"), _))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "no wild card in exact name (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with `_:`"))
       Assert.assertEquals("+withs:_:", result)
    }

    "simple tuple" in {
      QueryParser.parse("with (Int)") match {
        case Withs(List(Tuple(List(SimpleType(Word("int"), _))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "simple tuple (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with (Int)"))
       Assert.assertEquals("+withs:(int)", result)
    }

    "multi tuple" in {
      QueryParser.parse("with (Int, String)") match {
        case Withs(List(Tuple(List(
          SimpleType(Word("int"), _),
          SimpleType(Word("string"), _)
        )))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "multi tuple no ','" in {
      QueryParser.parse("with (Int String)") match {
        case Withs(List(Tuple(List(
          SimpleType(Word("int"), _),
          SimpleType(Word("string"), _)
        )))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "multi tuple (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with (Int, String)"))
       Assert.assertEquals("+withs:(int,string)", result)
    }

    "nested tuple" in {
      QueryParser.parse("with (Int, (String, Wow))") match {
        case Withs(List(Tuple(List(
          SimpleType(Word("int"), _),
          Tuple(List(
            SimpleType(Word("string"), _),
            SimpleType(Word("wow"), _)
        )))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "nested tuple no ','" in {
      QueryParser.parse("with (Int(String Wow))") match {
        case Withs(List(Tuple(List(
          SimpleType(Word("int"), _),
          Tuple(List(
            SimpleType(Word("string"), _),
            SimpleType(Word("wow"), _)
        )))))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "nested tuple (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with (Int, (String, Wow))"))
       Assert.assertEquals("+withs:(int,(string,wow))", result)
    }

    "tuple of generics" in {
      QueryParser.parse("with (Map[Int, Wow], List[String])") match {
        case Withs(List(Tuple(List(
          SimpleType(Word("map"), List(SimpleType(Word("int"), _), SimpleType(Word("wow"), _))),
          SimpleType(Word("list"), List(SimpleType(Word("string"), _)))
        )))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "tuple of generics (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with (Map[Int, Wow], List[String])"))
       Assert.assertEquals("+withs:(map[int,wow],list[string])", result)
    }

    "generic tuple" in {
      QueryParser.parse("with Map[(Int, Wow), (String)]") match {
        case Withs(List(SimpleType(Word("map"), List(
          Tuple(List(SimpleType(Word("int"),_), SimpleType(Word("wow"), _))),
          Tuple(List(SimpleType(Word("string"), _)))
        )))) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "generic tuple (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with Map[(Int, Wow), (String)]"))
       Assert.assertEquals("+withs:map[(int,wow),(string)]", result)
    }

    "wildcard tuple" in {
      QueryParser.parse("with (_, Wow_) with (_String, *)") match {
        case Withs(List(
          Tuple(List(SimpleType(AnyWord(), _), SimpleType(StartWith("wow"), _))),
          Tuple(List(SimpleType(EndWith("string"), _), AnyParams()))
        )) => ()
        case e => Assert.fail(e.toString)
      }
    }

    "wildcard tuple (Query)" in {
       val result = LuceneQueryTranslator.toLuceneQueryString(QueryParser.parse("with (_, Wow_) with (_String, *)"))
       Assert.assertEquals("+withs:(*,wow*) +withs:(*string,*)", result)
    }

    // TODO: Test invalid syntax
    // TODO: Test nonsensical queries (x-category)
  }

  doAfterSpec {
    // Restore the config that was being used before this test.
    Configuration.config = oldConfig
  }
}