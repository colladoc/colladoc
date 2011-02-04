package scala.tools.colladoc.model

import java.io.File
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{Field, Document}
import tools.nsc.doc.Universe
import tools.nsc.doc.model._
import java.util.HashMap

object SearchIndex {
  // TODO: Will we use a RAMDirectory once the vertical slice is hooked up?
  // I guess not since there is no guarantee on the size of the index (could be huge).
  lazy val luceneDirectory = FSDirectory.open(new File("lucene-index"))

  val entityLookup = new HashMap[Int, MemberEntity]()

  val packageFieldKey = "package"
  val classFieldKey = "class"
  val traitFieldKey = "trait"
  val objectFieldKey = "object"
  val defFieldKey = "def"
  val valFieldKey = "val"
  val varFieldKey = "var"
  val isLazyValFieldKey = "isLazyVal"
  val returnsFieldKey = "returns"
  val typeParamsCountFieldKey = "typeparamscount"
  val visibilityFieldKey = "visibility"
  val nameFieldKey = "name"
  val entityLookupKey = "entityLookup"

  def construct(universe : Universe) : Unit = {
    var writer : IndexWriter = null
    try {
      writer = new IndexWriter(luceneDirectory,
                               new StandardAnalyzer(Version.LUCENE_30),
                               IndexWriter.MaxFieldLength.UNLIMITED)

      // Clear the previous index.
       writer.deleteAll()

      indexMember(universe.rootPackage, writer)

      writer.optimize()
    }
    finally {
      if (writer != null) {
        writer.close()
      }
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
    doc.add(new Field(nameFieldKey,
                      member.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))


    // Add the entity to our lookup and store the lookup key as a field so that
    // we can recover the entity later.
    val lookupKey = member.hashCode()
    entityLookup.put(lookupKey, member)
    doc.add(new Field(entityLookupKey,
                      lookupKey.toString(),
                      Field.Store.YES,
                      Field.Index.NO))

    // Index the document for this entity.
    writer.addDocument(doc)

    // Finally, index any members of this entity.
    member match {
      case mbr : DocTemplateEntity =>
        mbr.members.foreach((m) => {
          indexMember(m, writer)
        })
      case _ => {
      }
    }
  }

  private def createPackageDocument(pkg : Package) = {
    val doc = new Document
    doc.add(new Field(packageFieldKey,
                      pkg.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    doc
  }

  private def createClassDocument(cls : Class) = {
    createClassOrTraitDocument(classFieldKey, cls)
  }

  private def createTraitDocument(trt : Trait) = {
    createClassOrTraitDocument(traitFieldKey, trt)
  }

  private def createClassOrTraitDocument(primaryFieldKey : String,
                                         classOrTrait : Trait) = {
    val doc = new Document
    doc.add(new Field(primaryFieldKey,
                      classOrTrait.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addTypeParamsCountField(classOrTrait.typeParams, doc)
    addVisibilityField(classOrTrait.visibility, doc)

    doc
  }

  private def createObjectDocument(obj : Object) = {
    val doc = new Document
    doc.add(new Field(objectFieldKey,
                      obj.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addVisibilityField(obj.visibility, doc)

    doc
  }

  private def createDefDocument(df : Def) = {
    val doc = new Document
    doc.add(new Field(defFieldKey,
                      df.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addTypeParamsCountField(df.typeParams, doc)
    addVisibilityField(df.visibility, doc)
    addReturnsField(df.resultType, doc)

    doc
  }

  private def createValDocument(valOrVar : Val) = {
    val valOrVarFieldKey = if (valOrVar.isVar) varFieldKey else valFieldKey
    val doc = new Document

    doc.add(new Field(valOrVarFieldKey,
                      valOrVar.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    doc.add(new Field(isLazyValFieldKey,
                      valOrVar.isLazyVal.toString(),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addReturnsField(valOrVar.resultType, doc)

    doc
  }

  private def addTypeParamsCountField(typeParams : List[TypeParam],
                                      doc : Document) = {
    doc.add(new Field(typeParamsCountFieldKey,
                      typeParams.length.toString(),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }

  private def addVisibilityField(visibility : Visibility, doc : Document) = {
    val vis = if (visibility.isPublic) "public"
              else if (visibility.isProtected) "protected"
              else "private"

    doc.add(new Field(visibilityFieldKey,
                      vis,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }

  private def addReturnsField(returnType : TypeEntity, doc : Document) = {
    doc.add(new Field(returnsFieldKey,
                      returnType.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
  }
}