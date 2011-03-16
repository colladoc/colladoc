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
object ParseAndSearchLambdaParamsTests  extends SpecificationWithJUnit
{
  import SearchIndex._

  private var directory:RAMDirectory = null

  doBeforeSpec {
    directory = new RAMDirectory();

    // Lucene 4 Init
    val config = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40))
    val writer = new IndexWriter(directory, config)

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

    // We do leave this to make sure we dont match them:
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

    // Test matching these:
    addDef("meth11", 1, "()>Unit", "Unit")
    addDef("meth12", 1, "(Int)>Double", "Double")
    addDef("meth13", 1, "(Double)>String", "String")
    addDef("meth14", 1, "(Int,String)>List[Int]", "List[Int]")
    addDef("meth15", 1, "(Double,List[String])>List[Double]", "List[Double]")
    addDef("meth16", 1, "(Double,List[Int],Double)>List[List[Int]]", "List[List[Int]]")
    addDef("meth17", 1, "(Double,List[Int],Double)>Unit", "Unit")
    addDef("meth18", 1, "(Double,Double,Int,Int)>Unit", "Unit")
    addDef("meth19", 1, "(String,Double,Int,Long)>Boolean", "Boolean")
    addDef("meth20", 1, "(String,List[Double],Array[Int],Map[Long,String],Long)>Boolevard", "Boolevard")


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
    val lambdified = "(" + q + ") => _"
    val searchQuery = ScoogleParser.parse(lambdified)
    val luceneQuery = LuceneQuery.toLuceneQuery(searchQuery)
    val collector = TopScoreDocCollector.create(100, true)
    new IndexSearcher(directory, true).search(luceneQuery, collector);

    totalResults mustBe collector.getTotalHits
  }

  def parseAndRetrieveResultsForLambdaParamsWith =
    addToSusVerb("parse and retrieve results for lamda params with/that")

  "Query Layer" should parseAndRetrieveResultsForLambdaParamsWith {
    // We want to share the index throughout all examples.
    shareVariables

    "no restrictions" in { expect("=> _", 10) }

    "no params" in { expect("() => _", 1) }

    "returns concrete" in { expect("=> Double", 1) }

    "returns wildcard" in { expect("=> Bool_", 2) }

    // NOTE: Unless we use regular expression queries, searchinf for (_) within a type will translate to a greedy *
    "any param" in { expect("(_) => _", 10) }

    "any params" in { expect("(*) => _", 10) }

    // NOTE: Stars are translated to * wildcards and not collapsed to a single *, thsi differs from the param behavior.
    "many any params" in { expect("(*, *, *) => _", 5) }

    // Note: Greedy star here, expected 2 but actual 7
    "any two params" in { expect("(_, _) => _", 7) }

    // Note: Star takes the third position here, expected 7 but actual 5
    "at least two params" in { expect("(_, _, *) => _", 5) }

    "concrete" in { expect("(Int) => _", 1) }

    "concrete (2)" in { expect("(Double) => _", 1) }

    "two param concrete" in { expect("(Int String) => _", 1) }

    // Note: Star takes the second position here, expected 2 but actual 1
    "concrete param then any param" in { expect("(Int, *) => _", 1) }

    "mixture of concrete params and any params" in { expect("(Double, _, Int, _) => _", 1) }

    // Note: Star takes the first and third positions here, expected 4 but actual 2
    "star param, concrete param, star param" in { expect("(*, Int, *) => _", 2) }

    // Note: Star takes the third position here, expected 3 but actual 2
    "mixture of concrete, wildcard and star params" in { expect("(Double, List_, *) => _", 2) }

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