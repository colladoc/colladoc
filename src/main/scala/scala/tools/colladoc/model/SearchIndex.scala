package scala.tools.colladoc.model

import java.io.File
import java.util.HashMap
import org.apache.lucene.util.Version
import tools.nsc.doc.model._
import org.apache.lucene.store.{Directory, FSDirectory}
import tools.colladoc.search.AnyParams
import org.apache.lucene.index.{IndexWriterConfig, IndexWriter}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.core.{WhitespaceAnalyzer, KeywordAnalyzer}
import org.apache.lucene.document.{NumericField, Field, Document}


//import org.apache.lucene.index.{IndexWriter}
//import org.apache.lucene.analysis.standard.StandardAnalyzer

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

  val withsField = "withs"
  val valvarField = "valvar"
  val defsField = "defs"
}

class SearchIndex(rootPackage : Package, directory : Directory) {
  import SearchIndex._

  def this(rootPackage : Package) = this(rootPackage,
                                         FSDirectory.open(new File("lucene-index")))

  val entityLookup = new HashMap[Int, MemberEntity]()

  val luceneDirectory = construct(rootPackage, directory)

  private def construct(rootPackage : Package,
                        directory : Directory) = {
    var writer : IndexWriter = null
    try {

      val config = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40))
      writer = new IndexWriter(directory, config)

      //writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.UNLIMITED)

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

    directory
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
                      member.name.toLowerCase,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))


    // Add the entity to our lookup and store the lookup key as a field so that
    // we can recover the entity later.
    val lookupKey = member.hashCode()
    entityLookup.put(lookupKey, member)
    doc.add(new Field(entityLookupField,
                      lookupKey.toString(),
                      Field.Store.YES,
                      Field.Index.NO))

    // Each entity will have a comment:
    val comment = member.comment match { case Some(str) => str.body.toString; case _ => ""}
    doc.add(new Field(commentField, comment.toLowerCase, Field.Store.YES, Field.Index.ANALYZED))

    // Fianlly, index the document for this entity.
    writer.addDocument(doc)
  }

  private def createPackageDocument(pkg : Package) = {
    val doc = new Document
    doc.add(new Field(typeField,
                      packageField,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    // Scala allows package objects (http://www.scala-lang.org/docu/files/packageobjects/packageobjects.html)
    // so packages can have vals and fields.
    //addValVarField("", doc)
    //addDefsField("", doc)

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

    classOrTrait.parentType match {case Some(parent) => doc.add(new Field(extendsField, parent.name.toLowerCase, Field.Store.YES, Field.Index.NOT_ANALYZED))
                                   case _ => {}}


    addVisibilityField(classOrTrait.visibility, doc)


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

    val paramTypes = params.map(_.resultType.name);

    val fieldValue = paramTypes.mkString(" ")

    println("methodParams: " + fieldValue )

    val pField = new Field(methodParams, fieldValue.toLowerCase, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS)

    pField.setOmitTermFreqAndPositions(false)

    doc.add(pField)

    println(pField.isStorePositionWithTermVector)
    println(pField.isTermVectorStored)
    println(pField.isIndexed)

    doc.add(new NumericField(methodParamsCount).setIntValue(params.size))

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
                      returnType.name.toLowerCase,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }
}