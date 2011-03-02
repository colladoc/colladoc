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

  val keywords = List("class", "def", "trait", "package", "object", "or", "||", "&&", "and", "not", "!", "val", "var", "extends", "with", "_")

  def notkeyword[T](p: => Parser[T]) = Parser { in =>
    p(in) match {
      case Success(res, in) if keywords contains res => Failure("Should not be keyword.", in)
      case e => e
    }
  }

  def charExcept(cs: Char*) = elem("", ch => (cs forall (ch !=)))

  // WARNING: Words containing dots (.) are allowed!
  def identifierOrKeyword = """[^ :)(\[\],"`]+""".r

  def identifier = notkeyword(identifierOrKeyword)

  def startsWithIdentifier = """[^ :)(\[\],"`]+_""".r

  def nonApostrophy = "[^`]+".r

  def stringLit = '\"' ~ rep( charExcept('\"', '\n', EofCh) ) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => (chars mkString "") }

  def word =  ( "`" ~> nonApostrophy <~ "`" ^^ {Word(_)}
              | "_\\b".r ^^ {s => AnyWord()}
              | "_" ~> startsWithIdentifier ^^ {s => Contains(s.substring(0, s.length-1))}
              | "_" ~> identifier ^^ {EndWith(_)}
              | startsWithIdentifier ^^ {s => StartWith(s.substring(0, s.length-1))}
              | identifier ^^ {Word(_)}
              | stringLit ^^ {ExactWord(_)})

  def wordOrKeyword =   ( "`" ~> nonApostrophy <~ "`" ^^ {Word(_)}
                        | "_\\b".r ^^ {s => AnyWord()}
                        | "_" ~> startsWithIdentifier ^^ {s => Contains(s.substring(0, s.length-1))}
                        | "_" ~> identifierOrKeyword ^^ {EndWith(_)}
                        | startsWithIdentifier ^^ {s => StartWith(s.substring(0, s.length-1))}
                        | identifierOrKeyword ^^ {Word(_)}
                        | stringLit ^^ {ExactWord(_)})

  def words:Parser[List[Identifier]] = rep1(word)

  def anyParam:Parser[ParamType] = "*" ^^ {a => AnyParams()}

  def paramType:Parser[ParamType] = (anyParam | `type`)

  def paramTypes:Parser[List[ParamType]] = repsep(paramType, opt(","))

  def manyWords:Parser[Comment] = phrase(rep1(word)) ^^ {Comment(_)}

  def comment():Parser[Comment] = "//" ~ (wordOrKeyword*) ^^ {case _ ~ w => Comment(w)}

  def generics:Parser[List[Type]] = "[" ~> repsep(`type`, opt(",")) <~ "]"

  def `type`:Parser[Type] = ( word ~ generics ^^ {case i~g => SimpleType(i, g)}
               | word ^^ {SimpleType(_, List())}
               | "(" ~> rep1sep ( `paramType`, opt(",") ) <~ ")" ^^ {Tuple(_)}
              )

  def `extends` = opt(("extends" ~> `type`))

  def withs = ("with" ~> `type`)*

  def `class` = "class" ~> word ~ `extends` ~ withs ^^ {case c~e~w => Class(c, e, w)}

  def `val` = "val" ~> word ~ returnType ^^ {case v~r => Val(v, r)}

  def `var` = "var" ~> word ~ returnType ^^ {case v~r => Var(v, r)}

  def `object` = "object" ~> word ~ `extends` ~ withs ^^ {case o~e~w => Object(o, e, w)}

  def justExtends = "extends" ~> `type` ^^ {Extends(_)}

  def justWiths = rep1("with" ~> `type`) ^^ {Withs(_)}

  def `trait` = "trait" ~> word ~ `extends` ~ withs ^^ {case t~e~w => Trait(t, e, w)}

  def `package` = "package" ~> word ^^ {Package(_)}

  def returnType = opt(":" ~> `type`)

  def params = "(" ~> paramTypes <~ ")"

  def curriedParams:Parser[List[List[ParamType]]] = params*

  def `def` = "def" ~> word ~ curriedParams ~ returnType ^^ {case i~p~r => Def(i, p, r)}

  def group:Parser[Group] = "(" ~> expr <~ ")" ^^ {Group(_)}

  def term:Parser[SearchQuery] = not | group |  comment | `class` | `val` | `var` | `trait` | `package` |`object` | justWiths | justExtends | `def` | word

  def or:Parser[Or] = (term ~ (((("or"|"||") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => Or(h::t)}

  def not:Parser[Not] = ("not" | "!") ~> term ^^ {Not(_)}

  def and:Parser[And] = (term ~ (((("and"|"&&") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => And(h::t)}

  def expr:Parser[SearchQuery] = or | and  | term

  def query = phrase(expr) | manyWords

  def parse(q:String) : SearchQuery = {
      (phrase(query)(new CharSequenceReader(q.toLowerCase))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => SyntaxError(msg)
      case Error(msg, _) => SyntaxError(msg)
    }
  }
}