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
import org.specs.SpecificationWithJUnit

/**
 * User: Miroslav Paskov
 * Date: 2/20/11
 * Time: 3:06 PM
 */

/**
 * There is not enough documentation on how the more advanced Lucene Queries work.
 * Here are examples ot the ones we use.
 */
object LuceneRegressionTests  extends SpecificationWithJUnit
{
  val TextField = "text"
  val TeamField = "team"
  val CountField = "count"

  private var directory:RAMDirectory = null

  doBeforeSpec {
    directory = new RAMDirectory();

    // Lucene 4
    val config = new IndexWriterConfig(Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40))
    val writer = new IndexWriter(directory, config)

    // Lucene 3
    //val writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.UNLIMITED)

    val doc = new Document();

    doc.add(new Field(TeamField, "Rumi Akil Alek Jamil Miro", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field(TextField, "It was the best of times, it was the worst of times.", Field.Store.YES, Field.Index.ANALYZED));

    writer.addDocument(doc);

    def addCountDoc(count:Int)
    {
      val doc = new Document();
      doc.add(new NumericField(CountField).setIntValue(count))
      writer.addDocument(doc);
    }

    addCountDoc(0);
    addCountDoc(1);
    addCountDoc(2);
    addCountDoc(3);
    addCountDoc(4);
    addCountDoc(5);

    writer.optimize();
    writer.close();
  }

  implicit def convertToArrayList[T](l:List[T]):ArrayList[T] =
  {
    val array = new ArrayList[T](l.size)
    for(item <- l) { array.add(item) }
    array
  }

  def or(queries:List[Query]):Query =
  {
    val result = new BooleanQuery();
    queries.filter(_ != null) foreach {result.add(_, Occur.SHOULD)}
    result
  }

  def and(queries:List[Query]):Query =
  {
    val result = new BooleanQuery();
    queries.filter(_ != null) foreach {result.add(_, Occur.MUST)}
    result
  }

  def expect(q:Query, totalResults:Int)
  {
     val c = TopScoreDocCollector.create(100, true)
     new IndexSearcher(directory, true).search(q, c);
     Assert.assertEquals(totalResults, c.getTotalHits);
  }

  // We want to share the index throughout all examples.
  shareVariables

  def execute = addToSusVerb("execute")

  "Lucene" should execute {
    "simple term query" in { expect(new TermQuery(new Term(TeamField, "rumi")), 1) }

    "simple or term query" in { expect(or(List(new TermQuery(new Term(TeamField, "jamil")))), 1) }

    "simple span term query" in { expect( new SpanTermQuery(new Term(TeamField, "akil")), 1) }

    "simple near span term query" in { expect( new SpanNearQuery(List(
      new SpanTermQuery(new Term(TeamField, "akil")),
      new SpanTermQuery(new Term(TeamField, "alek"))
    ).toArray, 0, true) , 1) }

    "near span term too far away" in { expect( new SpanNearQuery(List(
      new SpanTermQuery(new Term(TeamField, "akil")),
      new SpanTermQuery(new Term(TeamField, "jamil"))
    ).toArray, 0, true) , 0) }

    "near span term order matters" in { expect( new SpanNearQuery(List(
      new SpanTermQuery(new Term(TeamField, "alek")),
      new SpanTermQuery(new Term(TeamField, "akil"))
    ).toArray, 0, true) , 0) }

    // Note that the word is third, param position is 2, 3
    "exact position span query" in { expect( new SpanPositionRangeQuery(
        new SpanTermQuery(new Term(TeamField, "alek")),
      2, 3), 1) }

    "exact position wrong position query" in { expect( new SpanPositionRangeQuery(
        new SpanTermQuery(new Term(TeamField, "alek")),
      3, 4), 0) }

    "range query" in { expect(NumericRangeQuery.newIntRange(CountField, 0, 10, true, true), 6) }
  }

  doAfterSpec {
    directory.close();
  }
}