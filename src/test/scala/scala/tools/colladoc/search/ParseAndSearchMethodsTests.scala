package scala.tools.colladoc.search

import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import org.apache.lucene.document.{NumericField, Field, Document}
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import java.util.ArrayList
import org.specs.SpecificationWithJUnit
import tools.colladoc.model.SearchIndex

/**
 * For queries involving more complex transformation, like method queries we test only the end result and not
 * the lucene query syntax.
 */
object ParseAndSearchMethodsTests  extends SpecificationWithJUnit
{
  import SearchIndex._

  private var directory:RAMDirectory = null

  doBeforeSpec {
    directory = new RAMDirectory();

    // Lucene 4 Init
    val config = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40))
    val writer = new IndexWriter(directory, config)

    // Lucene 3 Init
    //val writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.UNLIMITED)

    def addDef(name:String, count:Int, params:String, ret:String) =
    {
      val doc = new Document();
      doc.add(new Field(typeField, defField, Field.Store.YES, Field.Index.NOT_ANALYZED))
      doc.add(new Field(nameField, name.toLowerCase, Field.Store.YES, Field.Index.NOT_ANALYZED))
      doc.add(new NumericField(methodParamsCount).setIntValue(count))
      doc.add(new Field(methodParams, params.toLowerCase, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS))
      doc.add(new Field(returnsField, ret.toLowerCase, Field.Store.YES, Field.Index.ANALYZED))
      writer.addDocument(doc);
    }

    addDef("meth01", 0, "", "Unit")
    addDef("meth02", 1, "Int", "Double")
    addDef("meth03", 1, "Double", "String")
    addDef("meth04", 2, "Int String", "List[Int]")
    addDef("meth05", 2, "Double List[String]", "List[Double]")
    addDef("meth06", 3, "Double List[Int] Double", "List[List[Int]]")
    addDef("meth07", 3, "Double List[Int] Double", "Unit")
    addDef("meth08", 4, "Double Double Int Int", "Unit")
    addDef("meth09", 4, "String Double Int Long", "Boolean")
    addDef("meth10", 5, "String List[Double] Array[Int] Map[Long,String] Long", "Boolevard")
    // 10 methods in total

    writer.commit()
    writer.optimize()
    writer.close()
  }

  implicit def convertToArrayList[T](l:List[T]):ArrayList[T] =
  {
    val array = new ArrayList[T](l.size)
    for(item <- l) { array.add(item) }
    array
  }

  def expect(q:String, totalResults:Int)
  {
    val searchQuery = ScoogleParser.parse(q)
    val luceneQuery = LuceneQuery.toLuceneQuery(searchQuery)
    val collector = TopScoreDocCollector.create(100, true)
    new IndexSearcher(directory, true).search(luceneQuery, collector)

    totalResults mustBe collector.getTotalHits
  }

  def parseAndRetrieveResultsForDefsWith =
    addToSusVerb("parse and retrieve results for defs with/that")
  def parseAndRetrieveResultsForDefsLambdaSyntaxWith =
    addToSusVerb("parse and retrieve results for defs(Lambda Syntax) with/that")

  // We want to share the index throughout all examples.
  shareVariables

  "Query Layer" should parseAndRetrieveResultsForDefsWith {

    "no restrictions" in { expect("def _", 10) }

    "no params" in { expect("def _()", 1) }

    "returns concrete" in { expect("def _:Double", 1) }

    "returns wildcard" in { expect("def _:Bool_", 2) }

    "any param" in { expect("def _(_) :_", 2) }

    "any params" in { expect("def _(*) :_", 10) }

    "many any params" in { expect("def _(*, *, *) :_", 10) }

    "any two params" in { expect("def _(_, _) :_", 2) }

    "at least two params" in { expect("def _(_, _, *) :_", 7) }

    "concrete" in { expect("def _(Int) :_", 1) }

    "concrete (2)" in { expect("def _(Double) :_", 1) }

    "two param concrete" in { expect("def _(Int String) :_", 1) }

    "concrete param then any param" in { expect("def _(Int, *) :_", 2) }

    "mixture of concrete params and any params" in { expect("def _(Double, _, Int, _) :_", 1) }

    "star param, concrete param, star param" in { expect("def _(*, Int, *) :_", 4) }

    "mixture of concrete, wildcard and star params" in { expect("def _(Double, List_, *) :_", 3) }

    "concrete param, generic param and star param" in { expect("def _(Double, List[Int], *) :_", 2) }

    "concrete param, star param, concrete param" in { expect("def _(String * Long) :_", 2) }

    "three wildcard params with spaces" in { expect("def _(String _ _ _ Long) :_", 1) }

    "wildcard generics" in { expect("def _(String List_ Array_ Map_ Long) :_", 1) }

    "inner wildcard generics" in { expect("def _(String List[_] Array[_] Map[_, _] Long) :_", 1) }

    "many concrete params" in { expect("def _( String, List[Double], Array[Int], Map[Long,String], Long) :_", 1) }

    "concrete param, star param (2)" in { expect("def _(Double, *) :Unit", 2) }
  }

  // Lambda def syntax:
  "Query Layer" should parseAndRetrieveResultsForDefsLambdaSyntaxWith {

    "no restrictions" in { expect("=> _", 10) }

    "no params" in { expect("() => _", 1) }

    "returns concrete" in { expect("=> Double", 1) }

    "returns wildcard" in { expect("=> Bool_", 2) }

    "any param" in { expect("(_) => _", 2) }

    "any params" in { expect("(*) => _", 10) }

    "many any params" in { expect("(*, *, *) => _", 10) }

    "any two params" in { expect("(_, _) => _", 2) }

    "at least two params" in { expect("(_, _, *) => _", 7) }

    "concrete" in { expect("(Int) => _", 1) }

    "concrete (2)" in { expect("(Double) => _", 1) }

    "two param concrete" in { expect("(Int String) => _", 1) }

    "concrete param then any param" in { expect("(Int, *) => _", 2) }

    "mixture of concrete params and any params" in { expect("(Double, _, Int, _) => _", 1) }

    "star param, concrete param, star param" in { expect("(*, Int, *) => _", 4) }

    "mixture of concrete, wildcard and star params" in { expect("(Double, List_, *) => _", 3) }

    "concrete param, generic param and star param" in { expect("(Double, List[Int], *) => _", 2) }

    "concrete param, star param, concrete param" in { expect("(String * Long) => _", 2) }

    "three wildcard params with spaces" in { expect("(String _ _ _ Long) => _", 1) }

    "wildcard generics" in { expect("(String List_ Array_ Map_ Long) => _", 1) }

    "inner wildcard generics" in { expect("(String List[_] Array[_] Map[_, _] Long) => _", 1) }

    "many concrete params" in { expect("( String, List[Double], Array[Int], Map[Long,String], Long) => _", 1) }

    "concrete param, star param (2)" in { expect("(Double, *) => Unit", 2) }
  }

  doAfterSpec {
    directory.close();
  }
}