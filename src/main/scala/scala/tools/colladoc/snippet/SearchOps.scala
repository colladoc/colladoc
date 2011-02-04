package scala.tools.colladoc.snippet

import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.util.Version
import tools.colladoc.model.SearchIndex
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.{TopScoreDocCollector, IndexSearcher}

/**
 * Search snippet.
 */
class SearchOps {
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

      // TODO: How will we give the results back to the page?
      // Maybe like HistoryOps.memberToHtml
      entityResults
    }
    finally {
      if (searcher != null) {
        searcher.close()
      }
    }
  }
}