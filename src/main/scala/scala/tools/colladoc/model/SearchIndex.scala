package scala.tools.colladoc.model

import java.io.File
import java.util.HashMap
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{Field, Document}
import tools.nsc.doc.model._
import org.apache.lucene.store.{Directory, FSDirectory}

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
      writer = new IndexWriter(directory,
                               new StandardAnalyzer(Version.LUCENE_30),
                               IndexWriter.MaxFieldLength.UNLIMITED)

      // Clear any previously indexed data.
       writer.deleteAll()

      indexRootPackege(rootPackage, writer)

      writer.optimize()
    }
    finally {
      if (writer != null) {
        writer.close()
      }
    }

    directory
  }

  private def indexRootPackege( rootPackege : Package, writer : IndexWriter) = {
          indexMember(rootPackage, writer, null)
  }

  private def indexMember(member : MemberEntity, writer : IndexWriter, parentDoc : Document) : Unit = {
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
        addValueToDefsField(df, parentDoc)
        createDefDocument(df)
      case value : Val =>
        addValueToValVarField(value, parentDoc)
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

    // Each entity will have a comment:
    val comment = member.comment match { case Some(str) => str.body.toString; case None => ""}
    doc.add(new Field(commentField, comment, Field.Store.YES, Field.Index.ANALYZED))



    // Finally, index any members of this entity.
    member match {
      case mbr : DocTemplateEntity =>
        mbr.members.foreach((m) => {
          indexMember(m, writer, doc)
        })
      case _ => {
      }
    }

    // Index the document for this entity.
    writer.addDocument(doc)
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

    classOrTrait.parentType match {case Some(parent) => doc.add(new Field(extendsField, parent.name, Field.Store.YES, Field.Index.NOT_ANALYZED))}
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