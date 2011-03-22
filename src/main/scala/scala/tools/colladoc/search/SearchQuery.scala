package scala.tools.colladoc.search

/**
 * Author: Miroslav Paskov
 * Date: 2/9/11
 * Time: 3:22 PM
 */

/**
 * Base trait for all search queries. They are created by the QueryParser and represent a
 * search-engine independent query tree.
 */
abstract sealed trait SearchQuery

/**
 * Base trait for all identifiers - for type names, package or member names as well as comment content.
 *
 * Note: Indeed words in comments are treated as indetifiers for simplification. The allowed identifier syntax is
 * broad enough to include the majority of words in the comments.
 */
abstract sealed trait Identifier extends SearchQuery

/**
 * Base trait for all queries that can be part of a list of method arguments.
 *
 * Essentially the ParamType is any type or the AnyParams. The AnyParams query is the only ParamType which is
 * not a type since it cannot be used outside the context of a list of types.
 */
abstract sealed trait ParamType extends SearchQuery

/**
 * Base trait for all queries that represent a type literal.
 */
abstract sealed trait Type extends ParamType

/**
 * A Word is an identifier that does not include wildcards, e.g. 'SomeName'.
 */
case class Word(identifier:String) extends Identifier

/**
 * An ExactWord is an identifier that is surrounded by quotes. It can contain spaces. Technically this type of query
 * only makes sence inc comments but its use us not restricted in other places. There no results will be resutrned.
 */
case class ExactWord(exact:String) extends Identifier

/**
 * An EndWith is wildcard identifier that can start with anything but must end with the given string.
 */
case class EndWith(exact:String) extends Identifier

/**
 * A StartWith is a wildcard identifier that can end with anything but starts with the given string.
 */
case class StartWith(exact:String) extends Identifier

/**
 * Contains is a wildcard identifier that must contain the given string.
 */
case class Contains(exact:String) extends Identifier

/**
 * AnyWord is a single wildcard indetifier that can include any content.
 */
case class AnyWord() extends Identifier

/**
 * Comment is a query made up from a list of identifiers (words).
 */
case class Comment(words:List[Identifier]) extends SearchQuery

/**
 * A simple type is Type wchich is not a Tple of a Func. It has an Identifier and a list of generic parameters.
 */
case class SimpleType(id:Identifier, generics:List[Type] = List()) extends Type

/**
 * A Tuple is made up from a list of types.
 */
case class Tuple(elements:List[ParamType]) extends Type

/**
 * A Func is a query for a lambda (anonymous) function type.
 */
case class Func(args:List[List[ParamType]], ret:Type) extends Type

/**
 * AnyParams is a special ParamType which acts as a wildcard in a list of ParamTypes meaning 'zero or more types'.
 */
case class AnyParams() extends ParamType

/**
 * Entity is a query that represents any class, object, trait or package.
 */
case class Entity(name:Type) extends SearchQuery

/**
 * The Class Query represents any Scala class definition - it has an identifier and an optional base class and traits.
 */
case class Class(className:Identifier, base:Option[Type] = None, withs:List[Type] = List()) extends SearchQuery

/**
 * The Object is similar to a Class.
 */
case class Object(objectName:Identifier, base:Option[Type] = None, withs:List[Type] = List()) extends SearchQuery

/**
 * The Trait is very similar to a Class in its structure but represents traits.
 */
case class Trait(traitName:Identifier, base:Option[Type] = None, withs:List[Type] = List()) extends SearchQuery

/**
 * The Package Query represents searching for scala packages.
 */
case class Package(packageName:Identifier) extends  SearchQuery

/**
 * The Extends Query represents searching for anything that extends the given type.
 */
case class Extends(name:Type) extends SearchQuery

/**
 * The Withs Query represents searching for anythings that mixes in a the given trait types.
 */
case class Withs(ids:List[Type]) extends SearchQuery

/**
 * A Query that represents searching for a method with the given identifier, parameter list and return type.
 */
case class Def(methodName:Identifier, params:List[List[ParamType]] = List(), defReturn:Option[Type] = None) extends SearchQuery

/**
 * Query that represents searching for a val member of an entity.
 */
case class Val(valName:Identifier, valReturn:Option[Type] = None) extends SearchQuery

/**
 * Query that represents searching for a var member of an entity.
 */
case class Var(varName:Identifier, varReturn:Option[Type] = None) extends SearchQuery

/**
 * A composite boolean query that matches something only of it is matched by all the contained queries.
 */
case class And(queries:List[SearchQuery]) extends SearchQuery

/**
 * A composite boolean query that matches anything that one of its contained queries matches.
 */
case class Or(queries:List[SearchQuery]) extends SearchQuery

/**
 * A Query that represents a negation of the contained query.
 */
case class Not(q:SearchQuery) extends SearchQuery

/**
 * A query used together with the boolean queries to change precedence, very much like brackets in operations.
 */
case class Group(q:SearchQuery) extends SearchQuery

/**
 * This is a query that indicates that there was an error in the parser. It is not expected that this query will be
 * translated to anything.
 */
case class SyntaxError(msg:String) extends SearchQuery