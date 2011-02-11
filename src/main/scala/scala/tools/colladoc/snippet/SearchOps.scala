package scala.tools.colladoc.snippet

import org.apache.lucene.queryParser.QueryParser
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
import net.liftweb.http.js.{JE, JsCmds, JsExp}
import net.liftweb.http.js.JE._

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
      { submit("Go", () => body _) % ("id" -> "searchcmd")}
      </xml:group>

  }

  /** Return history body. */
  def body(xhtml: NodeSeq): NodeSeq =
    bind("search", searchPage.body,
         "results" -> search(searchValue))

  def search(query : String) = {
    // TODO: Add custom QueryParser that will handle our proposed query syntax
    val parser = new QueryParser(Version.LUCENE_30,
                                 SearchIndex.nameField,
                                 new StandardAnalyzer(Version.LUCENE_30))
    val q = parser.parse(query)

    var searcher : IndexSearcher = null
    try {
      val hitsPerPage = 10
      val collector = TopScoreDocCollector.create(hitsPerPage, true)
      searcher = new IndexSearcher(index.vend.luceneDirectory, true)
      searcher.search(q, collector)

      // Collect the entities that were returned
      val entityResults = collector.topDocs().scoreDocs.map((hit) => {
        val doc = searcher.doc(hit.doc)
        val entitylookupKey = Integer.parseInt(doc.get(SearchIndex.entityLookupField))
        val entityResult = index.vend.entityLookup.get(entitylookupKey)
        entityResult
      })

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
}