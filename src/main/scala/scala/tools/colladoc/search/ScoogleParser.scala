package scala.tools.colladoc.search

/**
 * User: Miroslav Paskov
 * Date: 2/6/11
 * Time: 5:54 AM
 */

import scala.util.parsing.combinator._
import util.parsing.input.CharSequenceReader

// NOTE: There is no class hierarchy because extending case classes may be deprecated in future versions of Scala.
abstract sealed trait Query
abstract sealed trait Identifier extends Query

case class Word(word:String) extends Identifier
case class ExactWord(exact:String) extends Identifier
case class AnyParams() extends Identifier

case class Comment(words:List[Identifier]) extends Query

case class Entity(name:Identifier) extends Query
case class Class(className:Identifier, base:Option[Identifier]) extends Query
case class Object(objectName:Identifier, base:Option[Identifier]) extends Query
case class Trait(traitName:Identifier, base:Option[Identifier]) extends Query
case class Package(packageName:Identifier) extends  Query
case class Extends(name:Identifier) extends Query

case class Def(methodName:Identifier, params:List[List[Identifier]], defReturn:Option[Identifier]) extends Query
case class Val(valName:Identifier, valReturn:Option[Identifier]) extends Query
case class Var(varName:Identifier, varReturn:Option[Identifier]) extends Query


case class And(q:List[Query]) extends Query
case class Or(q:List[Query]) extends Query
case class Not(q:Query) extends Query

case class Group(q:Query) extends Query

case class SyntaxError(msg:String) extends Query

class ScoogleParser extends RegexParsers{

  final val EofCh = '\032'

  val keywords = List("class", "def", "trait", "package", "object", "or", "||", "&&", "and", "not", "!", "val", "var", "extends")

  def notkeyword[T](p: => Parser[T]) = Parser { in =>
    p(in) match {
      case Success(res, in) if keywords contains res => Failure("Should not be keyword.", in)
      case e => e
    }
  }

  def charExcept(cs: Char*) = elem("", ch => (cs forall (ch !=)))

  // WARNING: Words containing dots (.) are allowed!
  def identifier = notkeyword("""[a-zA-Z_][\w\._]*""".r)

  def stringLit = '\"' ~ rep( charExcept('\"', '\n', EofCh) ) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => (chars mkString "") }

  def word = (identifier ^^ {Word(_)}
              | stringLit ^^ {ExactWord(_)})

  def words:Parser[List[Identifier]] = rep1(word)

  def anyParam:Parser[Identifier] = "*" ^^ {a => AnyParams()}

  def wordOrStar:Parser[Identifier] = (anyParam | word)

  def wordsOrStar:Parser[List[Identifier]] = repsep(wordOrStar, opt(","))

  def manyWords:Parser[Comment] = phrase(rep1(word)) ^^ {Comment(_)}

  def singleWord = phrase(word) ^^ {w => Or(List(Comment(List(w)), Entity(w)))}

  def comment():Parser[Comment] = "//" ~ words ^^ {case _ ~ w => Comment(w)}

  def `extends` = opt(("extends" ~> word))

  def `class` = "class" ~> word ~ `extends` ^^ {case c~e => Class(c, e)}

  def `val` = "val" ~> word ~ returnType ^^ {case v~r => Val(v, r)}

  def `var` = "var" ~> word ~ returnType ^^ {case v~r => Var(v, r)}

  def `object` = "object" ~> word ~ `extends` ^^ {case o~e => Object(o, e)}

  def justExtends = "extends" ~> word ^^ {Extends(_)}

  def `trait` = "trait" ~> word ~ `extends` ^^ {case t~e => Trait(t, e)}

  def `package` = "package" ~> word ^^ {Package(_)}

  def returnType = opt(":" ~> word)

  def params = "(" ~> wordsOrStar <~ ")"

  def curriedParams:Parser[List[List[Identifier]]] = params*

  def `def` = "def" ~> word ~ curriedParams ~ returnType ^^ {case i~p~r => Def(i, p, r)}

  def group:Parser[Group] = "(" ~> expr <~ ")" ^^ {Group(_)}

  def term:Parser[Query] = not | group |  comment | `class` | `val` | `var` | `trait` | `package` |`object` | justExtends | `def` | word

  def or:Parser[Or] = (term ~ (((("or"|"||") ~> term)+) ^^ {a:List[Query] => a})) ^^ {case h ~ t => Or(h::t)}

  def not:Parser[Not] = ("not" | "!") ~> term ^^ {Not(_)}

  def and:Parser[And] = (term ~ (((("and"|"&&") ~> term)+) ^^ {a:List[Query] => a})) ^^ {case h ~ t => And(h::t)}

  def expr:Parser[Query] = or | and  | term

  def query = singleWord | manyWords | expr

  def parse(q:String) = {
      (phrase(query)(new CharSequenceReader(q))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => SyntaxError(msg)
      case Error(msg, _) => SyntaxError(msg)
    }
  }
}
