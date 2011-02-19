package scala.tools.colladoc.search

import java.util.{ArrayList}
import org.apache.lucene.index.Term
import org.apache.lucene.search._
import org.apache.lucene.search.BooleanClause.Occur
import tools.colladoc.model.SearchIndex

/**
 * User: Miroslav Paskov
 * Date: 2/9/11
 * Time: 3:25 PM
 */

object LuceneQuery
{
  import SearchIndex._

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

      case Comment(words) => transformComment(words map transformWord(commentField))

      case Class(id, ext) => transformType(classField)(id, ext)
      case Object(id, ext) => transformType(objectField)(id, ext)
      case Trait(id, ext) => transformType(traitField)(id, ext)

      case Extends(id) => transformExtends(id)
      case Package(id) => transformPackage(id)

      case Def(id, params, ret) => transformDef(id, params, ret)

      case Val(id, ret) => transformVal(id, ret)
      case Var(id, ret) => transformVar(id, ret)

      case word:Identifier => transformRootWord(word)
    }
  }

  def maybeTransformReturn(ret:Option[Identifier]) : Query =
  {
    ret match
    {
      case None => null
      case Some(retId) => transformWord(returnsField)(retId)
    }
  }

  def transformVal(id:Identifier, ret:Option[Identifier]):Query =
  {
    transformAnd(List(transformOr(List(typeTermQuery(valField), typeTermQuery(varField))), transformWord(nameField)(id), maybeTransformReturn(ret)))
  }

  def transformVar(id:Identifier, ret:Option[Identifier]):Query =
  {
    transformAnd(List(transformOr(List(typeTermQuery(varField), typeTermQuery(valField))), transformWord(nameField)(id), maybeTransformReturn(ret)))
  }

  def transformDef(id:Identifier, params:List[List[Identifier]], ret:Option[Identifier]):Query =
  {
    val flattened = params.flatten(l => l);
    var paramsCount = flattened.count(_.isInstanceOf[AnyParams]) match
    {
      case 0 => new TermQuery(new Term(methodParamsCount, flattened.size.toString))
      case _ => null
    }

    if(params.size == 0)
    {
      paramsCount = null;
    }

    transformAnd(List(typeTermQuery(defField), transformWord(nameField)(id), paramsCount, maybeTransformReturn(ret)))
  }

  def transformPackage(id:Identifier):Query =
  {
    transformAnd(List(
      typeTermQuery(packageField),
      transformWord(nameField)(id)
    ))
  }

  def transformExtends(id:Identifier):Query =
  {
    transformWord(extendsField)(id)
  }

  def transformType(typeName:String)(id:Identifier, ext:Option[Identifier]):Query =
  {
    ext match {
      case None => transformAnd(List(typeTermQuery(typeName), transformWord(nameField)(id)))
      case Some(base) => transformAnd(List(typeTermQuery(typeName), transformWord(nameField)(id), transformWord(extendsField)(base)))
    }
  }

  def typeTermQuery(str:String):Query =
  {
    new TermQuery(new Term(typeField, str))
  }

  def transformRootWord(word:Identifier) : Query =
  {
    transformOr(List(
      transformWord(nameField)(word),
      transformWord(commentField)(word)
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
      case StartWith(str) => new WildcardQuery(new Term(columnName, str + "*"))
      case Contains(str) => new WildcardQuery(new Term(columnName, "*" + str + "*"))
      case AnyWord() => null
      case AnyParams() => new PhraseQuery()
    }
  }

  def transformComment(words:List[Query]) : Query =
  {
    val result = new BooleanQuery();
    words.filter(_ != null) foreach {result.add(_, Occur.SHOULD)}
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
    queries.filter(_ != null) foreach {result.add(_, Occur.SHOULD)}
    result
  }

  def transformAnd(queries:List[Query]):Query =
  {
    val result = new BooleanQuery();
    queries.filter(_ != null) foreach {result.add(_, Occur.MUST)}
    result
  }

  // NOTE: All entities should contain their package:! Dotted packages (wow.test) are not separated)

}