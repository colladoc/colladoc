package scala.tools.colladoc.page

import tools.nsc.doc.model.{TemplateEntity, MemberEntity, Package, DocTemplateEntity}
import xml.NodeSeq

class Search(rootPack: Package) extends Template(rootPack) {
  /** Page title. */
  override val title = "Search"

  /** Page body. */
  override val body =
  <body class = "search">
    <div id = "results">
        <h3>Results</h3>
        <search:results />
    </div>
  </body>

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
      { if (constructors.isEmpty) NodeSeq.Empty else
          <div id="constructors" class="members">
            <ol>{ constructors map { memberToHtml(_) } }</ol>
          </div>
      }
      { if (typeMembers.isEmpty) NodeSeq.Empty else
          <div id="types" class="types members">
            <ol>{ typeMembers map { memberToHtml(_) } }</ol>
          </div>
      }
      { if (valueMembers.isEmpty) NodeSeq.Empty else
          <div id="values" class="values members">
            <ol>{ valueMembers map { memberToHtml(_) } }</ol>
          </div>
      }
    </xml:group>
  }
}