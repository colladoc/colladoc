package scala.tools.colladoc.search

/**
 * Author: Miroslav Paskov
 * Date: 2/6/11
 * Time: 5:47 AM
 */

import scala.util.parsing.combinator._
import util.parsing.input.CharSequenceReader

/**
 * The QueryParser parses strings to produce a search-independent SearchQuery structure.
 *
 * Combinatorial Parsers are used to parse the user string.
 */
object QueryParser extends RegexParsers{

  final val EofCh = '\032'

  val keywords = List("class", "def", "trait", "package", "object", "or", "||", "&&", "and", "not", "!", "val", "var", "extends", "with", "_", "=>")

  def notkeyword[T](p: => Parser[T]) = Parser { in =>
    p(in) match {
      case Success(res, in) if keywords contains res => Failure("Should not be keyword.", in)
      case e => e
    }
  }

  def charExcept(cs: Char*) = elem("", ch => (cs forall (ch !=)))

  /**
   * An identifier or a keyword can be anything that does not contain :)(][,"` which are terminal symbols. Please note
   * that an identifier can contain a . which is not true in Scala.
   */
  def identifierOrKeyword = """[^ :)(\[\],"`]+""".r

  /**
   * This identifier is is the same as the identifierOrKeyword but ends with an udnerscore. This is due to the greediness
   * of the regular identifier. Only the last underscore of an identifier should be treated as wildcard.
   *
   * NOTE: There could be other ways to work around this problem.
   */
  def startsWithIdentifier = """[^ :)(\[\],"`]+_""".r

  /**
   * An identifier is an identifier or a keyword which is not a keyword.
   */
  def identifier = notkeyword(identifierOrKeyword)

  /**
   * Matches any sequence of characters that does not contain an apostrophy.
   */
  def nonApostrophy = "[^`]+".r

  /**
   * Matches a string literal. This definition is more than what is required, but ok.
   */
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
               | curriedParams ~ ("=>" ~> `type`) ^^ {case params~ret => Func(params, ret)}
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

  /**
   * Returns a Def SearchQuery specified using the lambda syntax.
   */
  def funcDef = curriedParams ~ ("=>" ~> `type`) ^^ {case p~r => Def(AnyWord(), p, Some(r))}

  def group:Parser[Group] = "(" ~> expr <~ ")" ^^ {Group(_)}

  def term:Parser[SearchQuery] = not | funcDef | group |  comment | `class` | `val` | `var` | `trait` | `package` |`object` | justWiths | justExtends | `def` | word

  def or:Parser[Or] = (term ~ (((("or"|"||") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => Or(h::t)}

  def not:Parser[Not] = ("not" | "!") ~> term ^^ {Not(_)}

  def and:Parser[And] = (term ~ (((("and"|"&&") ~> term)+) ^^ {a:List[SearchQuery] => a})) ^^ {case h ~ t => And(h::t)}

  def expr:Parser[SearchQuery] = or | and  | term

  /**
   *  A search query is either a structured query or just a sequence of search terms:
   */
  def query = phrase(expr) | manyWords

  /**
   * Parses the given string to create a SearchQuery structure.
   *
   * @param q The search query string.
   *
   * @returns A SearchQuery representing the given string.
   */
  def parse(searchString:String) : SearchQuery = {
      (phrase(query)(new CharSequenceReader(searchString.toLowerCase))) match {
      case Success(ord, _) => ord
      case Failure(msg, _) => SyntaxError(msg)
      case Error(msg, _) => SyntaxError(msg)
    }
  }
}