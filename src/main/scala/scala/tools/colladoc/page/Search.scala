package scala.tools.colladoc.page

import net.liftweb.http.js.{JE, JsCmds, JsExp}
import net.liftweb.http.js.JE._
import xml._
import collection.mutable.HashMap
import tools.nsc.doc.model._

class Search(rootPack: Package) extends scala.tools.colladoc.page.Template(rootPack) {
  /** Page title. */
  override val title = "Search"

  /** Page body. */
  override val body =
<body class="value" onload="windowTitle();" scroll="no">
      <div id="definition">
            <img src="/images/search_big.png"/>
            <h1><search:count/> for: </h1><h1><span  id="searchValue" style="white-space:wrap;"><i>&laquo;<lift:SearchOps.sText/>&raquo;</i></span></h1>
            <p><a href="#" id="linkURL" style="font-size:10px;color:#ffffff;" target="_blank">Permalink</a></p>
          </div>
	       <search:header />

         <div style="bottom:0;display:block;position:absolute;width:100%;overflow:auto;top:180px;" id="searchPanel">
            <div id="template">

                   <search:results />

             </div>
         </div>
       <div id="tooltip" ></div>
    </body>

 def bodyHeader(xhtml:NodeSeq):NodeSeq = {

    <div id="mbrsel">
    <div id='textfilter'><span class='pre'/><span class='input'><input type='text' accesskey='/'/></span><span class='post'/></div>
    {
      <div id="symboltype">
        <span class="filtertype">Symbol</span>
        <ol>
          <li class="package in">Package</li>
          <li class="type in">Type</li>
          <li class="object in">Object</li>
          <li class="class in">Class</li>
          <li class="trait in">Trait</li>
          <li class="constructor in">Constructor</li>
          <li class="def in">Def</li>
          <li class="val in">Val</li>
          <li class="var in">Var</li>
        </ol>
      </div>
    }
    {
      <div id="visbl">
        <span class="filtertype">Visibility</span>
        <ol><li class="public out">Public</li><li class="all in">All</li></ol>
      </div>
    }
    {
      <div id="impl">
        <span class="filtertype">Impl.</span>
        <ol><li class="concrete in">Concrete</li><li class="abstract in">Abstract</li></ol>
      </div>
    }
  </div>

}

  /**
   * The search message is not displayed since it is never helpful.
   * If the error messages of the QueryParser and the QueryTranslator improve, then they can be displayed.
   */
 def bodyHelp(searchValue:String, errorMessage:String): NodeSeq = {

    val updateTop = JsRaw("$('#searchPanel').css('top','80px');")

    <script type="text/javascript">{ Unparsed(updateTop.toJsCmd) }</script>

   <div style="pagging: 3px; top: 0;" id="noResults">
     <div id = "helpTemplate">
      <h3>Here are some sample queries to get you started:</h3>
     </div>
     <div style = "background-color: #E5E5E5" >
       <ul style="margin: 0px 30px; padding: 4pt" class = "nodecoration">
         <li style="margin: 10px 30px;"><a href="/search?q=any" >any</a>
          searches for everything that has the word any in its name, definition or comment
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=any_">any_</a>
          searches for everything that starts with any
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=//_any">//_any</a>
          searches for all comments that contain a word that ends with any
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
         <li style="margin: 10px 30px;"><a href="/search?q=var _: Int">var _: Int</a>
          searches for all values or variables of type Int, vars are displayed before the vals
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def toString">def toString</a>
          searches for all methods with name toString
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def toString : String">def toString : String</a>
          searches for all methods with name toString and return type String
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def _(_) : Boolean">def _(_) : Boolean</a>
          searches for all methods with one argument and  returnType Boolean
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def _(Int, _)">def _(Int, _)</a>
          searches for all methods with arguments and the first is of type Int
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def _(_, *)">def _(_, *)</a>
          searches for all methods with one or more arguments
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def _(Iterable[_]):Int">def _(Iterable[_]):Int</a>
          searches for all methods that take an Iterable and return Int
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=(Iterable[_]) %3D> Int">(Iterable[_]) =&gt; Int</a>
          equvalent to the above, lambda syntax can also be used for searching for methods.
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=%3D> (_, _)">=&gt; (_, _)</a>
          searches for all methods that return a tuple with two elements.
         </li>
         <li style="margin: 10px 30px;"><a href="/search?q=def _((_) %3D> _)">def _((_) =&gt; _)</a>
          searches for all methods that have one as first parameter a method that takes and returns any value.
         </li>
       </ul>
       <FORM method="GET" action="http://www.google.com/search">
         <div style="padding: 10px 50px 10px;">
              <span style="position:relative;top:2pt">Or search with </span>
              <INPUT TYPE="hidden" name="q" value={"Scala " + searchValue}/>
              <INPUT TYPE="hidden" name="hl" value="en"/>
              <INPUT type="image" src="/images/google_logo.png" height="20px" style="vertical-align:middle;"/>
         </div>
       </FORM>
   </div>
   </div>
 }
  /**
   * Renders list of comments to its xhtml representation.
   * @param cmts list of comments
   * @return xhtml comments representation
   */
  def resultsToHtml(results: Iterable[MemberEntity]): NodeSeq = {
    // Groups members by containing type.
    def aggregateMembers(mbrs: Iterable[MemberEntity]) = {
      val containingTypeMap = new HashMap[DocTemplateEntity, List[MemberEntity]] {
        override def default(key: DocTemplateEntity) = Nil
      }

      for (mbr <- mbrs) {
        val tpl = mbr match {
          case tpl: DocTemplateEntity => tpl
          case _ => mbr.inTemplate
        }

        // Add this member to the current list of members for this type.
        containingTypeMap(tpl) = (mbr :: containingTypeMap(tpl))
      }

      containingTypeMap
    }

    <xml:group>
      {
        aggregateMembers(results) flatMap { case (containingType, mbrs) =>

          <div class={"searchResult" +
                    (if (containingType.isTrait || containingType.isClass) " type"
                    else " value")
                }>
            <h4 class="definition">
              <a href={ relativeLinkTo(containingType) }>
                  <img src={ relativeLinkTo{List(kindToString(containingType) + ".png", "lib")} }/>
              </a>
              <span>
                {
                  if (containingType.isRootPackage) "root package"
                  else containingType.qualifiedName }
              </span>
            </h4>

            <div>
              { membersToHtml(mbrs) }
            </div>
          </div>
        }
      }
    </xml:group>
  }

  /**
   *  Renders sequence of member entities to its xhtml representation.
   * @param mbrs sequence of member entities
   * @return xhtml comments representation
   */
  def membersToHtml(mbrs: Iterable[MemberEntity]): NodeSeq = {
    val valueMembers = mbrs collect {
      case (tpl: TemplateEntity) if tpl.isObject || tpl.isPackage => tpl
      case (mbr: MemberEntity) if mbr.isDef || mbr.isVal || mbr.isVar => mbr
    }
    val typeMembers = mbrs collect {
      case (tpl: TemplateEntity) if tpl.isTrait || tpl.isClass => tpl
      case (mbr: MemberEntity) if mbr.isAbstractType || mbr.isAliasType => mbr
    }
    val constructors = mbrs collect { case (mbr: MemberEntity) if mbr.isConstructor => mbr }
    <xml:group>
      { if (typeMembers.isEmpty) NodeSeq.Empty else
          <div id="types" class="types members">
            <ol>{ typeMembers map { memberToHtml(_) } }</ol>
          </div>
      }
      { if (constructors.isEmpty) NodeSeq.Empty else
          <div id="constructors" class="members">
            <ol>{ constructors map {memberToHtml(_) } }</ol>
          </div>
      }
      { if (valueMembers.isEmpty) NodeSeq.Empty else
          <div id="values" class="values members">
            <ol>{ valueMembers map { memberToHtml(_) } }</ol>
          </div>
      }
    </xml:group>
  }

  override def signature(mbr: MemberEntity,
                         isSelf: Boolean,
                         isReduced: Boolean = false): NodeSeq = {
    mbr match {
      case ctor : Constructor =>
        // Since we are showing constructors on the "root package page" we need
        // to do our own work to make sure that they show with the name of their
        // containing type.
        constructorSignature(ctor)
      case _ =>
        super.signature(mbr, isSelf, isReduced)
    }
  }

  private def constructorSignature(ctor : Constructor) : NodeSeq = {
    val hasLinks = true
    def constructorParamsToHtml(vlsss: List[List[ValueParam]]): NodeSeq = {
      def constructorParam0(vl: ValueParam): NodeSeq =
        // notice the }{ in the next lines, they are necessary to avoid a undesired withspace in output
        <span name={ vl.name }>{ Text(vl.name + ": ") }{ typeToHtml(vl.resultType, hasLinks) }{
          if(!vl.defaultValue.isEmpty) {
            defaultValueToHtml(vl.defaultValue.get);
          }
          else NodeSeq.Empty
        }</span>
      def constructorParams0(vlss: List[ValueParam]): NodeSeq = vlss match {
        case Nil => NodeSeq.Empty
        case vl :: Nil => constructorParam0(vl)
        case vl :: vls => constructorParam0(vl) ++ Text(", ") ++ constructorParams0(vls)
      }
      def constructorImplicitCheck(vlss: List[ValueParam]): NodeSeq = vlss match {
        case vl :: vls => if(vl.isImplicit) { <span class="implicit">implicit </span> } else Text("")
        case _ => Text("")
      }
      vlsss map {
        vlss =>
          <span class="params">
            ({constructorImplicitCheck(vlss) ++ constructorParams0(vlss) })
          </span>
      }
    }

    <xml:group>
      <h4 class="signature">
        <span class="kind">{ kindToString(ctor) }</span>
        <span class="symbol">
          <span class={"name" + (if (ctor.deprecation.isDefined) " deprecated" else "") }>
            { ctor.inTemplate.name }
          </span>
          { constructorParamsToHtml(ctor.valueParams) }
        </span>
      </h4>
    </xml:group>
  }
}
