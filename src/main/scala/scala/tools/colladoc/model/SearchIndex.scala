package scala.tools.colladoc.model

import java.io.File
import java.util.HashMap
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{Field, Document}
import tools.nsc.doc.Universe
import tools.nsc.doc.model._

object SearchIndex {
  val packageField = "package"
  val classField = "class"
  val traitField = "trait"
  val objectField = "object"
  val defField = "def"
  val valField = "val"
  val varField = "var"
  val isLazyValField = "isLazyVal"
  val returnsField = "returns"
  val typeParamsCountField = "typeparamscount"
  val visibilityField = "visibility"
  val nameField = "name"
  val entityLookupField = "entityLookup"
}

class SearchIndex(universe : Universe, directory : FSDirectory) {
  import SearchIndex._

  def this(universe : Universe) = this(universe,
                                       FSDirectory.open(new File("lucene-index")))

  val entityLookup = new HashMap[Int, MemberEntity]()

  val luceneDirectory = construct(universe, directory)

  private def construct(universe : Universe,
                        directory : FSDirectory) = {
    var writer : IndexWriter = null
    try {
      writer = new IndexWriter(directory,
                               new StandardAnalyzer(Version.LUCENE_30),
                               IndexWriter.MaxFieldLength.UNLIMITED)

      // Clear any previously indexed data.
       writer.deleteAll()

      indexMember(universe.rootPackage, writer)

      writer.optimize()
    }
    finally {
      if (writer != null) {
        writer.close()
      }
    }

    directory
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
    doc.add(new Field(packageField,
                      pkg.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))

    doc
  }

  private def createClassDocument(cls : Class) = {
    createClassOrTraitDocument(classField, cls)
  }

  private def createTraitDocument(trt : Trait) = {
    createClassOrTraitDocument(traitField, trt)
  }

  private def createClassOrTraitDocument(primaryField : String,
                                         classOrTrait : Trait) = {
    val doc = new Document
    doc.add(new Field(primaryField,
                      classOrTrait.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addTypeParamsCountField(classOrTrait.typeParams, doc)
    addVisibilityField(classOrTrait.visibility, doc)

    doc
  }

  private def createObjectDocument(obj : Object) = {
    val doc = new Document
    doc.add(new Field(objectField,
                      obj.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addVisibilityField(obj.visibility, doc)

    doc
  }

  private def createDefDocument(df : Def) = {
    val doc = new Document
    doc.add(new Field(defField,
                      df.name,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED))
    addTypeParamsCountField(df.typeParams, doc)
    addVisibilityField(df.visibility, doc)
    addReturnsField(df.resultType, doc)

    doc
  }

  private def createValDocument(valOrVar : Val) = {
    val valOrVarFieldKey = if (valOrVar.isVar) varField else valField
    val doc = new Document

    doc.add(new Field(valOrVarFieldKey,
                      valOrVar.name,
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
}