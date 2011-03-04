package scala.tools.colladoc.snippet

import tools.colladoc.model.SearchIndex
import tools.colladoc.page.Search
import tools.nsc.doc.model.MemberEntity
import xml._
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.{JE, JsCmds, JsExp}
import net.liftweb.http.js.JE._
import scala.{ xml}
import net.liftweb.http.{S, SHtml, RequestVar, StatefulSnippet}
import scala.tools.colladoc.search._
import org.apache.lucene.search._
import scala.collection.JavaConversions._

import org.apache.lucene.search.IterablePaging.TotalHitsRef
import org.apache.lucene.search.IterablePaging
import net.liftweb.common.Empty

/**
 * Search snippet.
 */
class SearchOps extends StatefulSnippet{
  import tools.colladoc.lib.DependencyFactory._
  object queryRequestVar extends RequestVar[String](S.param("q") openOr "")
  object pageRequestVar extends RequestVar[String](S.param("page") openOr "1")

  var hasMember:Boolean = false

  // TODO: Maybe we should set this value in the SearchPage?

  val pageNo:Int = (pageRequestVar.is).toInt
  val resultsPerPage = 30
  var resultsCount = 0


  val dispatch: DispatchIt ={ case "show" => show _
                              case "body" => body _
                              case "sText" => sText

                             }

  def sText (xhtml:NodeSeq): NodeSeq = {
    {scala.xml.Unparsed(searchValue)}
  }
  var searchValue = queryRequestVar.is

  lazy val searchPage = new Search(model.vend.rootPackage)

  /** Return history title. */
  def title(xhtml: NodeSeq): NodeSeq =
    Text(searchPage.title)


  def show(xhtml:NodeSeq):NodeSeq = {
      <xml:group>
      <label for="searchText">Search :</label>
      { text(searchValue, v => searchValue = v) % ("size" -> "10") % ("id" -> "searchText") }
      { submit("Go", () => S.redirectTo("search", () => queryRequestVar(searchValue))) }
      </xml:group>

  }

  /** Return history body. */
  def body(xhtml: NodeSeq): NodeSeq = {

    bind("search", searchPage.body,
         "results" -> search(searchValue),
         if (hasMember) {"header" -> searchPage.bodyHeader _ } else {"header" -> <div/>}
    )
  }
  def search(query : String) :NodeSeq =
  {
    println("Searching for: " + query)
    if(query.trim ne "")
    {
      ScoogleParser.parse(query) match
      {
        case SyntaxError(msg) => errorToHtml(msg)
        case searchQuery:SearchQuery => displayResults(LuceneQuery.toLuceneQuery(searchQuery))
      }
    }
    else
    {
	    errorToHtml("We dont like empty queries")
    }
  }

  def displayResults(query:Query):NodeSeq =
  {
    println("test")
    var searcher : IndexSearcher = null
    try {

      searcher = new IndexSearcher(index.vend.directory, true)
      println("Lucene Query: " + query.toString)
      searchResults(searcher, query, pageNo)
    }
    finally {
      if (searcher != null) { searcher.close() }
    }
  }
  def searchResults(searcher : IndexSearcher, query : Query, pageNumber : Int)={
    val totalHitsRef = new TotalHitsRef()
		val paging = new IterablePaging(searcher, query, 1000)
    val itemsPerPage = resultsPerPage
    val skipPages = (pageNumber - 1)* itemsPerPage

		val entityResults = paging.skipTo(skipPages).gather(itemsPerPage).
                        totalHits(totalHitsRef).
                        map((hit) => {
        val doc = searcher.doc(hit.doc)
        val entitylookupKey = Integer.parseInt(searcher.doc(hit.doc).
                                      get(SearchIndex.entityLookupField))
        val entityResult = index.vend.entityLookup.get(entitylookupKey)

        entityResult
      })

      println("Results: " + totalHitsRef.totalHits())
      resultsToHtml(entityResults)

  }



  /** Render search results **/
  def resultsToHtml(members : Iterable[MemberEntity]) = {
    resultsCount = members.size

    <div id="searchResults">
      {
        if (members.nonEmpty) {
        hasMember=true
          searchPage.resultsToHtml(members)

        } else {
         hasMember=false
         //background-color: #E5E5E5
         <div style="pagging: 3px;font-family: monospace;font-size: 12pt; top: 0; margin: -100px 0px;"
              id="noResults">
           <div style="margin: 0px 0px -45px 0px;">
             <img src="/images/no_search_results.png" border="0" width="80px" heigh="80px"/>
             <div style="margin: 0px 0px 0px 100px;position:relative; top: -40pt">
             Your search returned no matches
              <img src="/images/wonderingFace.jpg" border="0" width="20px" heigh="20px"/>
              <div style="text-align: right;margin: 0px 500px 0px 0px;"><br><i>... but don't give up </i></br></div>
             </div></div>
           <div id = "helpTemplate">
            <h3>Here are some sample queries to get you started:</h3>
           </div>
           <div style = "background-color: #E5E5E5">
             <ul style="margin: 0px 30px;font-size: 11pt;padding: 4pt" class = "nodecoration">
               <li style="margin: 10px 30px;"><a href="/search?q=foo" >foo</a>
                searches for everything that has the word foo in its name, definition or comment
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=_foo_">foo_</a>
                searches for everything that starts with foo
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=//_foo">//foo</a>
                searches for all comments that contain a word that end with foo
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=class AnyRef">class AnyRef</a>
                searches for all classes with name AnyRef
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=trait _">trait _</a>
                searches for all traits
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=object _">object _</a>
                searches for all objects
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=class A_ || class B_">class A_ || class B_</a>
                searches for all classes that starts with A or B
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=class  _ extends _ with _">class  _ extends _ with _</a>
                searches for all classes that extend a class and implement a trait
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=var _">var _: Int</a>
                searches for all values or variables of type Int, vars are displayed before the vals
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=val _">val _: Int</a>
                the same as above, but vals will be displayed before the vars
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=def toString">def toString</a>
                searches for all methods with name toString
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=def toString : String">def toString : String</a>
                searches for all methods with name toString and return type String
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=def _(_) : Boolen">def _(_) : Boolen</a>
                searches for all methods with one argument and  returnType Boolen and
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=def _(Int, _)">def _(Int, _)</a>
                searches for all methods with arguments and the first is of type Int
               </li>
               <li style="margin: 10px 30px;"><a href="/search?q=def _(_, *)">def _(_, *)</a>
                searches for all methods with one or more arguments
               </li>
             </ul>
           <p style="padding: 10px 50px 10px;"> For more query syntax samples, please refer to the <a href="/syntax.html" onclick="window.open(this.href, 'newWindow', 'height=600, width=500, left=50, top=50, toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no,'); return false">Syntax reference</a></p>
             <FORM method="GET" action="http://www.google.com/search?q=123">
               <div style="padding: 10px 50px 10px;">
                    <span style="position:relative;top:-5pt">Still nothing... try with </span>
                    <INPUT TYPE="hidden" name="q" value={searchValue}/>
                    <INPUT TYPE="hidden" name="hl" value="en"/>
                    <INPUT type="image" src="/images/google_logo.gif" height="30px"/>
               </div>
             </FORM>
         </div>
         </div>

        }
      }
    </div>
  }

  def errorToHtml(msg : String) = {

    <div id="searchResults">
         <div style="margin:25px 50px;"> Error with the syntax: {msg}
           <br/>
		       <br/>
             For supported query syntax samples, please refer to the <a href="/syntax.html" onclick="window.open(this.href, 'newWindow', 'height=600, width=500, left=50, top=50, toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no,'); return false">Syntax reference</a>
         </div>
    </div>
  }
}

