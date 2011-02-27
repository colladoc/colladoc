package scala.tools.colladoc.model
import tools.nsc.doc.model._
import org.specs.SpecificationWithJUnit
import org.specs.mock._
import tools.nsc.doc.model.MemberEntity
import org.apache.lucene.store.{RAMDirectory, Directory}
import tools.nsc.doc.model.Package
import org.apache.lucene.document.Document
import tools.nsc.doc.model.comment.{Body, Comment}
import org.apache.lucene.index.{IndexWriter, IndexReader}

object SearchIndexTests extends SpecificationWithJUnit
       with EntityMemberMock
       with ClassMock{
   var directory: Directory = _
  "Search Index" should {
    doBefore {
      directory = new RAMDirectory
      construct
    }
    "Use the FSDirectory that is given to it on creation" in {
      val index = new SearchIndex(directory)
      index.directory must beEqualTo(directory)
    }

    "Index the root package" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(directory)
      index.index(mockPackage)
      val docs = getAllDocs(directory)

      docs.length must beEqualTo(1)
      docs(0).get(SearchIndex.nameField) mustEqual packageName.toLowerCase
    }

    // TODO: Test comments and entityLookUp
    "Add Any Entity and stores its name, entityId and comment" in {
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntity(mockEntity)
      }

      val index = new SearchIndex(directory)
      index.index(mockPackage)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.nameField) mustEqual entityName.toLowerCase
    }

    "Index class and store their visibility, parentClass, Traits that extends  " in {

      expect {
        defaultExpectationsForPackage
        one(mockPackage).members.willReturn(List[MemberEntity](mockClass))
        expectationsForClass
        expectationsForAnyMemberEntity(mockClass)
      }

      val index = new SearchIndex(directory)
      index.index(mockPackage)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.extendsField) mustEqual parentClassName.toLowerCase
      docs(1).get(SearchIndex.withsField)  mustEqual traitTemplateEntityName.toLowerCase
    }

    /*"Index Def and stores its number of parameters, visibility, return value" in {
      val mockDef = mock[Def]
      val mockVisibility = mock[Visibility]
      val mockReturnParam = mock[TypeParam]
      val mockParam = mock[TypeParam]
      val returnParamName = "Return Param Name"
      val defVisibility = "public"
      expect {
        expectationsForPackageWithEntity(mockDef)

        one(mockDef).typeParams willReturn(List[TypeParam](mockParam))
        one(mockDef).visibility willReturn mockVisibility
        one(mockDef).resultType willReturn mockReturnParam
        one(mockReturnParam).name willReturn returnParamName

        expectationsForAnyMemberEntity(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory)
      val docs = getAllDocs(directory)

      docs(0).get(SearchIndex.returnsField) mustEqual returnParamName
      docs(0).get(SearchIndex.typeParamsCountField) mustEqual(1)
      docs(0).get(SearchIndex.visibilityField) mustEqual defVisibility
    }*/

    "Add valsOrVars field to package documents" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn(packageName)
        allowingMatch(mockRootPackage, "comment")
        one(mockRootPackage).members willReturn(List[MemberEntity]())
      }

      val index = new SearchIndex(directory)
      index.index(mockRootPackage)
      val docs = getAllDocs(directory)
      docs(0).get(SearchIndex.valvarField) must notBeNull
    }

    "Add defs field to package documents" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn(packageName)
        allowingMatch(mockRootPackage, "comment")
        one(mockRootPackage).members willReturn(List[MemberEntity]())
      }

      val index = new SearchIndex(directory)
      index.index(mockRootPackage)

      val docs = getAllDocs(directory)
      docs(0).get(SearchIndex.defsField) must notBeNull
    }

    /*"Reindex document when the comment are updated" in {
      var directory = new RAMDirectory
      var readerMock = mock[IndexReader]
      var writerMock = mock[IndexWriter]
      var docs = mock[TermDocs]
      var doc = new Document()
      doc.add(SerachIndex.commentField, "TestComment")
      expect{
        exactly(1).of(readerMock).document willReturn(termDocs)
        exactly(1).(termDocs).next willReturn 1
        exactly
      }
    }*/
  }

  private def getAllDocs(dir : Directory) = {
    var docs = List[Document]()
    var reader : IndexReader = null
    try {
      reader = IndexReader.open(dir, true)
      for (i <- 0 until reader.numDocs() ) {
        docs = docs ::: List[Document](reader.document(i))
      }
    }
    finally {
      if (reader != null) {
        reader.close()
      }
    }
    docs
  }
}