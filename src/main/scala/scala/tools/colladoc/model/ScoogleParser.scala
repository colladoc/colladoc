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

abstract sealed class Query

case class Word(word:String) extends Query
case class ExactWord(exact:String) extends Word(exact)

case class Comment(words:List[Word]) extends Query

case class Entity(name:Word) extends Query
case class Class(className:Word) extends Entity(className)
case class Def(methodName:Word) extends Entity(methodName)
case class Val(valName:Word) extends Entity(valName)
case class Var(varName:Word) extends  Entity(varName)
case class Package(packageName:Word) extends  Entity(packageName)

case class And(left:Query, right:Query) extends Query
case class Or(left:Query, right:Query) extends Query
case class Not(q:Query) extends Query

//case class Comment(words:List[Word]) extends Query
//case class General(List) extends Query

class ScoogleParser extends RegexParsers{

  //lexical.delimiters ++= List("(", ")", "//")
  //lexical.reserved += ("class", "or", "def")

  final val EofCh = '\032'

  def chrExcept(cs: Char*) = elem("", ch => (cs forall (ch !=)))

  def ident = "[a-zA-Z]\\w*".r

  def stringLit = '\"' ~ rep( chrExcept('\"', '\n', EofCh) ) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => (chars mkString "") }

  def word = (log(ident)("Word") ^^ {Word(_)}
              | log(stringLit)("Exact Word") ^^ {ExactWord(_)}
            )

  def words:Parser[List[Word]] = log(rep1(word))("Words")

  def generalWord = word ^^ {w => println("General Word: " + w); Or(Comment(List(w)), Entity(w))}

  def generalWords:Parser[Comment] = words ^^ {ws => Comment(ws)}

  def comment:Parser[Comment] = log("//" ~ words )("Comment") ^^ {case _ ~ w => Comment(w)}

  def or:Parser[Or] = query ~ "or" ~ query ^^ {case (left ~ p ~ right) => Or(left, right) }

  def classQ = "class" ~> word ^^ {Class(_)}

  //def defawdawQ:Parser[Def] = "def" ~> word ^^ {Def(_)}

  def ors = query ~ ("or" ~ query)+

  def query = log(comment)("Query.Comment") | log(word)("Query.Word") | log(or)("Query.Or")

  def rootQuery:Parser[Query] =  log(query)("Root.Query") | log(phrase(generalWord))("Root.GeneralWord") | log(generalWords)("Root.GeneralWords")

  //def group = "(" ~> query <~ ")"

  def parse(q:String) = {
      println("-----------------------------------------------: " + q)
      (phrase(rootQuery)(new CharSequenceReader(q))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => println("Fail: " + msg); msg
      case Error(msg, _) => println("Error:" + msg); msg
    }
  }


//  def expr: Parser[Any] = term~rep("+"~term | "-"~term)
//  def term: Parser[Any] = factor~rep("*"~factor | "/"~factor)
//  def factor: Parser[Any] = floatingPointNumber | "("~expr~")"
}