package scala.tools.colladoc.search

import java.util.{ArrayList}
import org.apache.lucene.index.Term
import org.apache.lucene.search._
import org.apache.lucene.search.BooleanClause.Occur

/**
 * User: Miroslav Paskov
 * Date: 2/9/11
 * Time: 3:25 PM
 */

object LuceneQuery
{
  /** All documents contain a comments field */
  val COMMENTS = "comment"

  /** All documents have a name */
  val NAME = "name"

  /** Every entity has a type */
  val TYPE = "type"

  /** The type is a class */
  val CLASS = "class"

  /** The type is an object */
  val OBJECT = "object"

  /** The type is a trait */
  val TRAIT = "trait"

  /** The type is a package */
  val PACKAGE = "package"

  /** Entities that extend something has this field */
  val EXTENDS = "extends"

  /** Members have a return type */
  val RETURN = "return"

  val DEF = "def"
  val VAL = "val"
  val VAR = "var"


  implicit def convertToArrayList[T](l:List[T]):ArrayList[T] =
  {
    val array = new ArrayList[T](l.size)
    for(item <- l) { array.add(item) }
    array
  }

  def toLuceneQueryString(syntaxQuery:SearchQuery):String =
  {
    transform(syntaxQuery).toString()
  }

  def toLuceneQuery(syntaxQuery:SearchQuery):Query =
  {
    transform(syntaxQuery)
  }

  def transform(syntaxQuery:SearchQuery):Query =
  {
    // TODO: Categorize the queries!

    syntaxQuery match
    {
      case And(queries) => transformAnd(queries map transform)
      case Or(queries) => transformOr(queries map transform)
      case Not(query) => transformNot(transform(query))
      case Group(query) => transformGroup(transform(query))

      case Comment(words) => transformComment(words map transformWord(COMMENTS))

      case Class(id, ext) => transformType(CLASS)(id, ext)
      case Object(id, ext) => transformType(OBJECT)(id, ext)
      case Trait(id, ext) => transformType(TRAIT)(id, ext)

      case Extends(id) => transformExtends(id)
      case Package(id) => transformPackage(id)

      case Def(id, params, ret) => transformDef(id, ret)

      case word:Word => transformRootWord(word)
    }
  }

  def transformVar(id:Identifier, ret:Option[Identifier]):Query =
  {
    ret match {
      case None => transformAnd(List(typeTermQuery(VAL), transformWord(NAME)(id)))
      case Some(retId) => transformAnd(List(typeTermQuery(DEF), transformWord(NAME)(id), transformWord(RETURN)(retId)))
    }
  }

  def transformDef(id:Identifier, ret:Option[Identifier]):Query =
  {
    ret match {
      case None => transformAnd(List(typeTermQuery(DEF), transformWord(NAME)(id)))
      case Some(retId) => transformAnd(List(typeTermQuery(DEF), transformWord(NAME)(id), transformWord(RETURN)(retId)))
    }
  }

  def transformPackage(id:Identifier):Query =
  {
    transformAnd(List(
      typeTermQuery(PACKAGE),
      transformWord(NAME)(id)
    ))
  }

  def transformExtends(id:Identifier):Query =
  {
    transformWord(EXTENDS)(id)
  }

  def transformType(typeName:String)(id:Identifier, ext:Option[Identifier]):Query =
  {
    ext match {
      case None => transformAnd(List(typeTermQuery(typeName), transformWord(NAME)(id)))
      case Some(base) => transformAnd(List(typeTermQuery(typeName), transformWord(NAME)(id), transformWord(EXTENDS)(base)))
    }
  }

  def typeTermQuery(str:String):Query =
  {
    new TermQuery(new Term(TYPE, str))
  }

  def transformRootWord(word:Word) : Query =
  {
    transformOr(List(
      transformWord(NAME)(word),
      transformWord(COMMENTS)(word)
    ))
  }

  def transformWord(columnName:String)(id:Identifier) : Query =
  {
    id match {
      case Word(id) =>  new TermQuery(new Term(columnName, id))
      case ExactWord(str) => {
        val result = new PhraseQuery()
        result.add(new Term(columnName, str))
        result
      }
      case EndWith(str) => new WildcardQuery(new Term(columnName, "*" + str))
      case StartWith(str) => new PrefixQuery(new Term(columnName, str))
      case AnyParams() => new PhraseQuery()
    }
  }

  def transformComment(words:List[Query]) : Query =
  {
    val result = new BooleanQuery();
    words foreach {result.add(_, Occur.SHOULD)}
    result
  }

  def transformGroup(query:Query):Query = query

  def transformNot(query:Query):Query =
  {
    val result = new BooleanQuery();
    result.add(query, Occur.MUST_NOT)
    result
  }

  def transformOr(queries:List[Query]):Query =
  {
    val result = new BooleanQuery();
    queries foreach {result.add(_, Occur.SHOULD)}
    result
  }

  def transformAnd(queries:List[Query]):Query =
  {
    val result = new BooleanQuery();
    queries foreach {result.add(_, Occur.MUST)}
    result
  }

  // NOTE: All entities should contain their package:! Dotted packages (wow.test) are not separated)

}