import junit.framework.TestCase
import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import org.apache.lucene.document.{NumericField, Field, Document}
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.junit.Assert
import java.util.ArrayList
import tools.colladoc.model.SearchIndex
import tools.colladoc.search.{LuceneQuery, ScoogleParser}

/**
 * User: Miroslav Paskov
 * Date: 2/23/11
 * Time: 3:06 PM
 */

/**
 * For queries involving more complex transformation, like method queries we test only the end result and not
 * the lucene query syntax.
 */
class ParseAndSearchLambdaParams  extends TestCase
{
  import SearchIndex._

  private var directory:RAMDirectory = null

  override def setUp()
  {
    super.setUp()

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

  override def tearDown()
  {
    super.tearDown()
    directory.close();
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
    Assert.assertEquals("\nQuery: " + LuceneQuery.toLuceneQueryString(searchQuery) + "\n" + searchQuery, totalResults, collector.getTotalHits);
  }

  def testNoRestrictionsLambda() = expect("=> _", 10);

  def testNoParamsLambda() = expect("() => _", 1);

  def testReturnsConcreteLambda() = expect("=> Double", 1);

  def testReturnsWidlcardLambda() = expect("=> Bool_", 2);

  // NOTE: Unless we use regular expression queries, searchinf for (_) within a type will translate to a greedy *
  def testAnyParamLambda() = expect("(_) => _", 10);

  def testAnyParamsLambda() = expect("(*) => _", 10);

  // NOTE: Stars are translated to * wildcards and not collapsed to a single *, thsi differs from the param behavior.
  def testManyAnyParamsLambda() = expect("(*, *, *) => _", 5);

  // Note: Greedy star here, expected 2 but actual 7
  def testAnyTwoParamsLambda() = expect("(_, _) => _", 7);

  // Note: Star takes the third position here, expected 7 but actual 5
  def testAtLeastTwoParamsLambda() = expect("(_, _, *) => _", 5);

  def testConcreteLambda() = expect("(Int) => _", 1);

  def testConcrete2Lambda() = expect("(Double) => _", 1);

  def testTwoConcreteLambda() = expect("(Int String) => _", 1);

  // Note: Star takes the second position here, expected 2 but actual 1
  def testConcreteThenAnyLambda() = expect("(Int, *) => _", 1);

  def testConcreteAnyConcreteAnyLambda() = expect("(Double, _, Int, _) => _", 1);

  // Note: Star takes the first and third positions here, expected 4 but actual 2
  def testStarConcreteStarLambda() = expect("(*, Int, *) => _", 2);

  // Note: Star takes the third position here, expected 3 but actual 2
  def testConcreteWildcardStarLambda() = expect("(Double, List_, *) => _", 2);

  def testConcreteGenericStarLambda() = expect("(Double, List[Int], *) => _", 2);

  def testConcreteStarConcreteLambda() = expect("(String * Long) => _", 2);

  def testThreeBlanksWithSpacesLambda() = expect("(String _ _ _ Long) => _", 1);

  def testWildcardGenericsLambda() = expect("(String List_ Array_ Map_ Long) => _", 1);

  def testInnerWildcardGenericsLambda() = expect("(String List[_] Array[_] Map[_, _] Long) => _", 1);

  def testManyConcreteLambda() = expect("( String, List[Double], Array[Int], Map[Long,String], Long) => _", 1);

  def testConcreteStarConcrete2Lambda() = expect("(Double, *) => Unit", 2);


}