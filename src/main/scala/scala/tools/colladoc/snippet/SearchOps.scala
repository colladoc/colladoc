package scala.tools.colladoc.snippet

import org.apache.lucene.util.Version
import tools.colladoc.model.SearchIndex
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.{TopScoreDocCollector, IndexSearcher}
import tools.colladoc.page.Search
import tools.nsc.doc.model.MemberEntity
import xml._
import net.liftweb.util.Helpers._
import net.liftweb.http.StatefulSnippet
import net.liftweb.http.SHtml._
import net.liftweb.http.js.{JE, JsCmds}
import scala.tools.colladoc.search._
import org.apache.lucene.search.{Query}

/**
 * Search snippet.
 */
class SearchOps extends StatefulSnippet{
  import tools.colladoc.lib.DependencyFactory._

  val dispatch: DispatchIt ={ case "show" => show _
                              case "body" => body _}

  var searchValue = "b*"

  lazy val searchPage = new Search(model.vend.rootPackage)



  /** Return history title. */
  def title(xhtml: NodeSeq): NodeSeq =
    Text(searchPage.title)

  def show(xhtml:NodeSeq):NodeSeq = {
      <xml:group>
      <label for="searchText">Search :</label>
      { text(searchValue, v => searchValue = v) % ("size" -> "10") % ("id" -> "searchText") }
      { submit("Go", () => body _) }
    </xml:group>

  }

  /** Return history body. */
  def body(xhtml: NodeSeq): NodeSeq =
    bind("search", searchPage.body,
         "results" -> search(searchValue))

  def search(query : String) :NodeSeq = {

    println("Searching for: " + query)
    ScoogleParser.parse(query) match
    {
      case SyntaxError(msg) => errorToHtml(msg)
      case searchQuery:SearchQuery => displayResults(LuceneQuery.toLuceneQuery(searchQuery))
    }
  }

  def displayResults(query:Query):NodeSeq =
  {
    var searcher : IndexSearcher = null
    try {
      val hitsPerPage = 10
      val collector = TopScoreDocCollector.create(hitsPerPage, true)
      searcher = new IndexSearcher(index.vend.luceneDirectory, true)

      println("Lucene Query: " + query.toString)

      searcher.search(query, collector)

      // Collect the entities that were returned
      val entityResults = collector.topDocs().scoreDocs.map((hit) => {
        val doc = searcher.doc(hit.doc)
        val entitylookupKey = Integer.parseInt(doc.get(SearchIndex.entityLookupField))
        val entityResult = index.vend.entityLookup.get(entitylookupKey)
        entityResult
      })

      println("Results: " + entityResults.toString)
      resultsToHtml(entityResults)
    }
    finally {
      if (searcher != null) {
        searcher.close()
      }
    }
  }

  /** Render search results **/
  def resultsToHtml(members : Array[MemberEntity]) = {
    <div id="searchResults">
      {
        searchPage.membersToHtml(members)
      }
    </div>
  }

  def errorToHtml(msg : String) = {
    <div id="searchResults">
      {
        msg
      }
    </div>
  }
}