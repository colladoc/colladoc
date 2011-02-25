package scala.tools.colladoc.model
import scala.tools.colladoc.lib.util.NameUtils._
import java.io.File
import java.util.HashMap
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{Field, Document}
import tools.nsc.doc.model._
import org.apache.lucene.store.{Directory, FSDirectory}
import tools.colladoc.search.AnyParams
import org.apache.lucene.index.{TermDocs, Term, IndexReader, IndexWriter}
import tools.colladoc.utils.Timer

object SearchIndex {

  /** The type is a package */
  val packageField = "package"

  /** The type is a class */
  val classField = "class"

  /** The type is a trait */
  val traitField = "trait"

  /** The type is an object */
  val objectField = "object"

  /** The document is a def */
  val defField = "def"

  /** The document is a val */
  val valField = "val"

  /** The document is a var */
  val varField = "var"
  val isLazyValField = "isLazyVal"

  /** Members have a return type */
  val returnsField = "return"
  val typeParamsCountField = "typeparamscount"
  val visibilityField = "visibility"

  val methodParamsCount = "methodParamsCount"
  val methodParams = "methodParams"

  /** All documents have a name */
  val nameField = "name"

  /** Every entity has a type */
  val typeField = "type"

  /** All documents contain a comments field */
  val commentField = "comment"
  val entityLookupField = "entityLookup"

  /** Entities that extend something has this field */
  val extendsField = "extends"
  val valvarField = "valvar"
  val defsField = "defs"
}

class SearchIndex(indexDirectory : Directory) {
  import SearchIndex._
  val entityLookup = new HashMap[Int, MemberEntity]()
  var directory = indexDirectory
  def this() = this(FSDirectory.open(new File("lucene-index")))

  private def getDocumentsByMember(member:MemberEntity,reader : IndexReader) : TermDocs = {
    val number = reader.docFreq(new Term(entityLookupField, member.hashCode.toString))
    println("number:" + number)
    println(member.hashCode.toString)
    val docsToBeModified = reader.termDocs(new Term(entityLookupField,  member.hashCode.toString))
    docsToBeModified
  }

  private def updateDocumentComments(docs : List[Document],
                                     member : MemberEntity,
                                     writer : IndexWriter){
        docs.foreach(doc =>{
        doc.removeField(commentField)
        val newDoc = addCommentToDocument(member, doc)
        writer.addDocument(newDoc)
    })
  }

  private def removeDocuments(member : MemberEntity, directory : Directory) : List[Document] = {
    val reader = IndexReader.open(directory, false)
    val docs = getDocumentsByMember(member, reader)
    var removeDocs = List[Document]()
    while(docs.next()){

    val doc = reader.document(docs.doc())
    reader.deleteDocument(docs.doc())
    removeDocs = doc :: removeDocs
    }
    reader.close
    removeDocs
  }


  def reindexEntityComment(member : MemberEntity){
    Timer.go
    var reader : IndexReader = null
    var writer : IndexWriter = null

    try{
      println("Start")
      var docsToBeModified =  removeDocuments(member, directory)
      writer = new IndexWriter(directory,
                               new StandardAnalyzer(Version.LUCENE_30),
                               IndexWriter.MaxFieldLength.UNLIMITED)
      updateDocumentComments(docsToBeModified, member, writer)

    }
    finally {
      if (writer != null) { writer.optimize(); writer.close() }
      Timer.stop
    }
  }

  def index(rootPackage : Package){
    var writer : IndexWriter = null
    try {
      writer = new IndexWriter(directory,
                               new StandardAnalyzer(Version.LUCENE_30),
                               IndexWriter.MaxFieldLength.UNLIMITED)

      // Clear any previously indexed data.
      writer.deleteAll()

      indexMembers(rootPackage :: Nil, writer)

      writer.optimize()
    }
    finally {
      if (writer != null) {
        writer.close()
      }
    }
  }

  private def indexMembers(members : List[MemberEntity], writer : IndexWriter) : Unit = {
    if (members.isEmpty) {
      throw new IllegalArgumentException()
    }

    // Index another member
    val member = members.head
    indexMember(member, writer)

    // Add this entity's members to the list of members to index.
    val additionalMembers = member match {
      case doc : DocTemplateEntity =>
        doc.members
      case _ => Nil
    }

    val remainingMembers = members.tail ::: additionalMembers

    // Finally, the recursive step, index the remainig members...
    // NOTE: Tail call recursion is REQUIRED here because of the depth of
    // scaladoc models for large code bases
    if (!remainingMembers.isEmpty) {
      indexMembers(remainingMembers, writer)
    } 
  }

  private def indexMember(member : MemberEntity, writer : IndexWriter) : Unit = {
    val doc : Document = member match {
      case pkg : Package =>
        createPackageDocument(pkg)
      case cls : Class =>
        createClassDocument(cls)
      case trt : Trait =>
        createTraitDocument(trt)
      case obj : Object =>
        createObjectDocument(obj)
      case df : Def =>
        createDefDocument(df)
      case value : Val =>
        createValDocument(value)
      case _ =>
        new Document
    }

    // Make sure that every entity at least has a field for their name to enable
    // general searches ([q1])
    doc.add(new Field(nameField,
                      member.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))


    // Add the entity to our lookup and store the lookup key as a field so that
    // we can recover the entity later.
    val lookupKey = member.hashCode()
    entityLookup.put(lookupKey, member)
    doc.add(new Field(entityLookupField,
                      lookupKey.toString(),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addCommentToDocument(member, doc)
    // Write the appropriate member information to the current document.
    // TODO (asb10): Miro - please remove this after implementing member specific search.
//    member match {
//      case mbr : DocTemplateEntity =>
//        mbr.members.foreach((m) => {
//          m match {
//            case df: Def =>
//              addValueToDefsField(df, doc)
//            case value : Val =>
//              addValueToValVarField(value, doc)
//            case _ => { }
//          }
//        })
//      case _ => { }
//    }

    // Fianlly, index the document for this entity.
    writer.addDocument(doc)
  }

  private def addCommentToDocument(member : MemberEntity, doc:Document): Document = {
 // Each entity will have a comment, only the last comment is indexed:
    //val comment = mapper.Comment.latest(member.uniqueName) match { case Some(str) =>str.comment.is; case _ => ""}
    //doc.add(new Field(commentField, comment, Field.Store.YES, Field.Index.ANALYZED))
    doc
  }

  private def createPackageDocument(pkg : Package) = {
    val doc = new Document
    doc.add(new Field(packageField,
                      pkg.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    doc.add(new Field(typeField,
                      packageField,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    // Scala allows package objects (http://www.scala-lang.org/docu/files/packageobjects/packageobjects.html)
    // so packages can have vals and fields.
    addValVarField("", doc)
    addDefsField("", doc)

    doc
  }

  private def createClassDocument(cls : Class) = {
    createClassOrTraitDocument(classField, cls)
  }

  private def createTraitDocument(trt : Trait) = {
    createClassOrTraitDocument(traitField, trt)
  }

  private def createClassOrTraitDocument(primaryField : String,
                                         classOrTrait : DocTemplateEntity) = {
    val doc = new Document
    doc.add(new Field(typeField,
                      primaryField,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    classOrTrait.parentType match {case Some(parent) => doc.add(new Field(extendsField, parent.name, Field.Store.YES, Field.Index.NOT_ANALYZED))
                                   case _ => {}}
    addVisibilityField(classOrTrait.visibility, doc)
    addValVarField("", doc)
    addDefsField("", doc)

    doc
  }

  private def createObjectDocument(obj : Object) = {
    createClassOrTraitDocument(objectField, obj)
  }

  private def createDefDocument(df : Def) = {
    val doc = new Document

    doc.add(new Field(typeField,
                      defField,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))



    addTypeParamsCountField(df.typeParams, doc)
    addVisibilityField(df.visibility, doc)
    addReturnsField(df.resultType, doc)

    val params:List[ValueParam] = df.valueParams.flatten(l => l)

    val paramNames = params.map(_.name);

    val fieldValue = paramNames.mkString(" ")

    doc.add(new Field(methodParams, fieldValue, Field.Store.YES, Field.Index.ANALYZED))

    doc.add(new Field(methodParamsCount, params.size.toString, Field.Store.YES, Field.Index.NOT_ANALYZED))

    doc
  }

  private def createValDocument(valOrVar : Val) = {
    val valOrVarFieldKey = if (valOrVar.isVar) varField else valField
    val doc = new Document

    doc.add(new Field(typeField,
                      valOrVarFieldKey,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    doc.add(new Field(isLazyValField,
                      valOrVar.isLazyVal.toString(),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    addReturnsField(valOrVar.resultType, doc)

    doc
  }

  private def addTypeParamsCountField(typeParams : List[TypeParam],
                                      doc : Document) = {
    doc.add(new Field(typeParamsCountField,
                      typeParams.length.toString(),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }

  private def addVisibilityField(visibility : Visibility, doc : Document) = {
    val vis = if (visibility.isPublic) "public"
              else if (visibility.isProtected) "protected"
              else "private"

    doc.add(new Field(visibilityField,
                      vis,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }

  private def addReturnsField(returnType : TypeEntity, doc : Document) = {
    doc.add(new Field(returnsField,
                      returnType.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }

  private def addValVarField(value : String, doc : Document) = {

    if (doc.getField(valvarField) != null){
      doc.removeField(valvarField)
    }

    doc.add( new Field(valvarField,
                        value, Field.Store.YES, Field.Index.NOT_ANALYZED));
  }

  private def addValueToValVarField( value : Val, doc : Document) = {

    var curValue : String = doc.getField(valvarField).stringValue()

    curValue = curValue + value.name + ":" + value.resultType.name + ";"

    println(curValue)

    addValVarField(curValue, doc)

  }

  private def addDefsField(value : String, doc : Document) = {

    if (doc.getField(defsField) != null){
      doc.removeField(defsField)
    }

    doc.add( new Field(defsField,
                        value, Field.Store.YES, Field.Index.NOT_ANALYZED));
  }

  private def addValueToDefsField( value : Def, doc : Document) = {

    var curValue : String = doc.getField(defsField).stringValue()

    var valueParams : String = ""
    val valueParamsListSize : Int =  value.valueParams.size;
    var curListValueParamsSize : Int = 0;

    //println("valueParamsListSize = " + valueParamsListSize)
    if (valueParamsListSize>0){

      curValue += value.name + "("

      if (valueParamsListSize>0){
        for (j <- 0 until valueParamsListSize){

          curListValueParamsSize = value.valueParams(j).size

          //println( curListValueParamsSize)

          var delimeter = ""
          curListValueParamsSize match {
            case size if (size == 0) => {}
            case size if (size == 1) => {

              if (j>0)
                delimeter = ", "
              curValue +=  delimeter + value.valueParams(j)(0).resultType.name
            }
            case size if (size > 1) => {

              if (j>0)
                delimeter = ","

              curValue += delimeter + value.valueParams(j)(0).resultType.name

              for ( i <- 1 until curListValueParamsSize) {
                curValue += "," + value.valueParams(0)(i).resultType.name
              }
            }
          }
        }
      }

      curValue += "):" + value.resultType.name + ";"

      //println(curValue)

      addDefsField(curValue, doc)
    }

  }
}