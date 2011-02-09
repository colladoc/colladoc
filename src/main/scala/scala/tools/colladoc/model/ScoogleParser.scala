/**
 * Created by IntelliJ IDEA.
 * User: paskov
 * Date: 2/6/11
 * Time: 5:47 AM
 * To change this template use File | Settings | File Templates.
 */

import scala.util.parsing.combinator._
import lexical.{Scanners, StdLexical}
import syntactical.{StdTokenParsers, StandardTokenParsers}
import util.parsing.input.CharSequenceReader
import util.parsing.syntax.StdTokens

abstract sealed trait Query

case class Word(word:String) extends Query
case class ExactWord(exact:String) extends Word(exact)
//case class Params extends Word

case class Comment(words:List[Word]) extends Query

case class Entity(name:Word) extends Query
case class Class(className:Word, base:Option[Word]) extends Entity(className)
case class Object(objectName:Word, base:Option[Word]) extends Entity(objectName)
case class Trait(traitName:Word, base:Option[Word]) extends Entity(traitName)
case class Package(packageName:Word) extends  Entity(packageName)
case class Extends(name:Word) extends Query

case class Def(methodName:Word, ret:Option[Word]) extends Entity(methodName)
case class Val(valName:Word) extends Entity(valName)
case class Var(varName:Word) extends  Entity(varName)


case class And(q:List[Query]) extends Query
case class Or(q:List[Query]) extends Query
case class Not(q:Query) extends Query

case class Group(q:Query) extends Query

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

  def words:Parser[List[Word]] = rep1(word)

  def manyWords:Parser[Comment] = phrase(rep1(word)) ^^ {Comment(_)}

  def singleWord = phrase(word) ^^ {w => Or(List(Comment(List(w)), Entity(w)))}

  def comment():Parser[Comment] = "//" ~ words ^^ {case _ ~ w => Comment(w)}

  def `extends` = opt(("extends" ~> word))

  def `class` = "class" ~> word ~ `extends` ^^ {case c~e => Class(c, e)}

  def `object` = "object" ~> word ~ `extends` ^^ {case o~e => Object(o, e)}

  def justExtends = "extends" ~> word ^^ {Extends(_)}

  def `trait` = "trait" ~> word ~ `extends` ^^ {case t~e => Trait(t, e)}

  def `package` = "package" ~> word ^^ {Package(_)}

  def returnType = opt(":" ~> word)

  def params = "(" ~> words <~ ")"

  def `def` = "def" ~> word ~ returnType ^^ {case i~r => Def(i, r)}

  def group:Parser[Group] = "(" ~> expr <~ ")" ^^ {Group(_)}

  def term:Parser[Query] = not | group |  comment | `class` | `trait` | `package` |`object` | justExtends | `def` | word

  def or:Parser[Or] = (term ~ (((("or"|"||") ~> term)+) ^^ {a:List[Query] => a})) ^^ {case h ~ t => Or(h::t)}

  def not:Parser[Not] = ("not" | "!") ~> term ^^ {Not(_)}

  def and:Parser[And] = (term ~ (((("and"|"&&") ~> term)+) ^^ {a:List[Query] => a})) ^^ {case h ~ t => And(h::t)}

  def expr:Parser[Query] = or | and  | term

  def query = singleWord | manyWords | expr

  def parse(q:String) = {
      (phrase(query)(new CharSequenceReader(q))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => println("Fail: " + msg); msg
      case Error(msg, _) => println("Error:" + msg); msg
    }
  }
}