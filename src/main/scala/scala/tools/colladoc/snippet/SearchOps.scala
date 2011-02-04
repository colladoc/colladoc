package scala.tools.colladoc.snippet

import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.util.Version
import tools.colladoc.model.SearchIndex
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.{TopScoreDocCollector, IndexSearcher}
import tools.colladoc.page.Search
import xml.NodeSeq
import tools.nsc.doc.model.MemberEntity
import tools.colladoc.lib.DependencyFactory
import xml._
import net.liftweb.util.Helpers._

/**
 * Search snippet.
 */
class SearchOps {
  lazy val searchPage = new Search(DependencyFactory.model.vend.rootPackage)

  /** Return history title. */
  def title(xhtml: NodeSeq): NodeSeq =
    Text(searchPage.title)

  /** Return history body. */
  def body(xhtml: NodeSeq): NodeSeq =
    bind("search", searchPage.body,
         "results" -> search("foo"))

  def search(query : String) = {
    // TODO: Add custom QueryParser that will handle our proposed query syntax
    val parser = new QueryParser(Version.LUCENE_30,
                                 SearchIndex.nameFieldKey,
                                 new StandardAnalyzer(Version.LUCENE_30))
    val q = parser.parse(query)

    var searcher : IndexSearcher = null
    try {
      val hitsPerPage = 10
      val collector = TopScoreDocCollector.create(hitsPerPage, true)
      searcher = new IndexSearcher(SearchIndex.luceneDirectory, true)
      searcher.search(q, collector)

      // Collect the entities that were returned
      val entityResults = collector.topDocs().scoreDocs.map((hit) => {
        val doc = searcher.doc(hit.doc)
        val entitylookupKey = Integer.parseInt(doc.get(SearchIndex.entityLookupKey))
        val entityResult = SearchIndex.entityLookup.get(entitylookupKey)
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