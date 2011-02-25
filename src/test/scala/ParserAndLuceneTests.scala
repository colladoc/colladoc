import junit.framework.TestCase
import org.apache.lucene.analysis.core.{WhitespaceAnalyzer, SimpleAnalyzer}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{NumericField, Field, Document}
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.search.spans._
import org.apache.lucene.search.BooleanClause.Occur
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
class ParserAndLuceneTests  extends TestCase
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
    val searchQuery = ScoogleParser.parse(q)
    val luceneQuery = LuceneQuery.toLuceneQuery(searchQuery)
    val collector = TopScoreDocCollector.create(100, true)
    new IndexSearcher(directory, true).search(luceneQuery, collector);
    Assert.assertEquals("\nQuery: " + LuceneQuery.toLuceneQueryString(searchQuery) + "\n" + searchQuery, totalResults, collector.getTotalHits);
  }

  def testNoRestrictions() = expect("def _", 10);

  def testNoParams() = expect("def _()", 1);

  def testReturnsConcrete() = expect("def _:Double", 1);

  def testReturnsWidlcard() = expect("def _:Bool_", 2);

  def testAnyParam() = expect("def _(_)", 2);

  def testAnyParams() = expect("def _(*)", 10);

  def testManyAnyParams() = expect("def _(*, *, *)", 10);

  def testAnyTwoParams() = expect("def _(_, _)", 2);

  def testAtLeastTwoParams() = expect("def _(_, _, *)", 7);

  def testConcrete() = expect("def _(Int)", 1);

  def testConcrete2() = expect("def _(Double)", 1);

  def testTwoConcrete() = expect("def _(Int String)", 1);

  def testConcreteThenAny() = expect("def _(Int, *)", 2);

  def testConcreteAnyConcreteAny() = expect("def _(Double, _, Int, _)", 1);

  def testStarConcreteStar() = expect("def _(*, Int, *)", 4);

  def testConcreteWildcardStar() = expect("def _(Double, List_, *)", 3);

  def testConcreteGenericStar() = expect("def _(Double, List[Int], *)", 2);

  def testConcreteStarConcrete() = expect("def _(String * Long)", 2);

  def testThreeBlanksWithSpaces() = expect("def _(String _ _ _ Long)", 1);

  def testWildcardGenerics() = expect("def _(String List_ Array_ Map_ Long)", 1);

  def testInnerWildcardGenerics() = expect("def _(String List[_] Array[_] Map[_, _] Long)", 1);

  def testManyConcrete() = expect("def _( String, List[Double], Array[Int], Map[Long,String], Long)", 1);

  def testConcreteStarConcrete2() = expect("def _(Double, *): Unit", 2);
}