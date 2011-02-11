package scala.tools.colladoc.search

/**
 * Author: Miroslav Paskov
 * Date: 2/6/11
 * Time: 5:47 AM
 */

import scala.util.parsing.combinator._
import util.parsing.input.CharSequenceReader

object  ScoogleParser extends RegexParsers{

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

  def startsWithIdentifier = """[a-zA-Z_][\w\._]*_""".r

  def stringLit = '\"' ~ rep( charExcept('\"', '\n', EofCh) ) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => (chars mkString "") }

  def word = ( "_" ~> identifier ^^ {EndWith(_)}
              | startsWithIdentifier ^^ {s => StartWith(s.substring(0, s.length-1))}
              | identifier ^^ {Word(_)}
              | stringLit ^^ {ExactWord(_)})

  def words:Parser[List[Identifier]] = rep1(word)

  def anyParam:Parser[Identifier] = "*" ^^ {a => AnyParams()}

  def wordOrStar:Parser[Identifier] = (anyParam | word)

  def wordsOrStar:Parser[List[Identifier]] = repsep(wordOrStar, opt(","))

  def manyWords:Parser[Comment] = phrase(rep1(word)) ^^ {Comment(_)}


  //def singleWord = phrase(word) ^^ {w => Or(List(Comment(List(w)), Entity(w)))}

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

  def term:Parser[SearchQuery] = not | group |  comment | `class` | `val` | `var` | `trait` | `package` |`object` | justExtends | `def` | word

  def or:Parser[Or] = (term ~ (((("or"|"||") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => Or(h::t)}

  def not:Parser[Not] = ("not" | "!") ~> term ^^ {Not(_)}

  def and:Parser[And] = (term ~ (((("and"|"&&") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => And(h::t)}

  def expr:Parser[SearchQuery] = or | and  | term

  def query = phrase(expr) | manyWords

  def parse(q:String) : SearchQuery = {
      (phrase(query)(new CharSequenceReader(q))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => SyntaxError(msg)
      case Error(msg, _) => SyntaxError(msg)
    }
  }
}