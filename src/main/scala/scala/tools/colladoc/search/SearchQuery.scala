package scala.tools.colladoc.search

/**
 * Author: Miroslav Paskov
 * Date: 2/9/11
 * Time: 3:22 PM
 */

/**
 * The base class for all queries that will be parsed for searching.
 *
 * NOTE: TThere is no class hierarchy because extending case classes may be deprecated.
 */
abstract sealed trait SearchQuery

/**
 * Base class for all identifiers - for type, package or member names as well as comment content.
 */
abstract sealed trait Identifier extends SearchQuery

case class Word(identifier:String) extends Identifier
case class ExactWord(exact:String) extends Identifier
case class EndWith(exact:String) extends Identifier
case class StartWith(exact:String) extends Identifier
case class Contains(exact:String) extends Identifier
case class AnyWord() extends Identifier
case class AnyParams() extends Identifier

case class Comment(words:List[Identifier]) extends SearchQuery

case class Entity(name:Identifier) extends SearchQuery
case class Class(className:Identifier, base:Option[Identifier]) extends SearchQuery
case class Object(objectName:Identifier, base:Option[Identifier]) extends SearchQuery
case class Trait(traitName:Identifier, base:Option[Identifier]) extends SearchQuery
case class Package(packageName:Identifier) extends  SearchQuery
case class Extends(name:Identifier) extends SearchQuery

case class Def(methodName:Identifier, params:List[List[Identifier]], defReturn:Option[Identifier]) extends SearchQuery
case class Val(valName:Identifier, valReturn:Option[Identifier]) extends SearchQuery
case class Var(varName:Identifier, varReturn:Option[Identifier]) extends SearchQuery


case class And(queries:List[SearchQuery]) extends SearchQuery
case class Or(queries:List[SearchQuery]) extends SearchQuery
case class Not(q:SearchQuery) extends SearchQuery

case class Group(q:SearchQuery) extends SearchQuery

case class SyntaxError(msg:String) extends SearchQuery