package scala.tools.colladoc.model
import mapper.{CommentToString}
import tools.nsc.doc.model._
import org.specs.SpecificationWithJUnit
import org.specs.mock._
import tools.nsc.doc.model.MemberEntity
import org.apache.lucene.store.{RAMDirectory, Directory}
import tools.nsc.doc.model.Package
import org.apache.lucene.document.Document
import tools.nsc.doc.model
import org.apache.lucene.index.{IndexWriter, IndexReader}
import org.apache.lucene.index.{IndexWriter, IndexReader}
import net.liftweb.common.Logger

object SearchIndexTests extends SpecificationWithJUnit
       with EntityMemberMock
       with DocTemplateEntityMock{
   var directory: Directory = _
  "Search Index" should {
    doBefore {
      directory = new RAMDirectory
      construct
    }
    "Use the FSDirectory that is given to it on creation" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      index.directory must beEqualTo(directory)
    }

    "Index the root package" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqualTo(1)
      docs(0).get(SearchIndex.nameField) mustEqual packageName.toLowerCase
      docs(0).get(SearchIndex.commentField) mustEqual defaultUserComment
    }

    "Index Entity: name, entityId and user-defined comment" in {
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntityWithUserComment(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.nameField) mustEqual entityName.toLowerCase
      docs(1).get(SearchIndex.commentField) mustEqual defaultUserComment
      docs(1).get(SearchIndex.entityLookupField) mustEqual mockEntity.hashCode.toString
    }

    "Index Entities without comments" in {
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntityWithoutComment(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.nameField) mustEqual entityName.toLowerCase
      docs(1).get(SearchIndex.commentField) mustEqual ""
      docs(1).get(SearchIndex.entityLookupField) mustEqual mockEntity.hashCode.toString
    }

    "Index Entities with default comments" in {
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntityWithComment(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.nameField) mustEqual entityName.toLowerCase
      docs(1).get(SearchIndex.commentField) mustEqual defaultComment
      docs(1).get(SearchIndex.entityLookupField) mustEqual mockEntity.hashCode.toString
    }

    "Index class:  visibility, parentClass, Traits that extends  " in {
      expect {
        defaultExpectationsForPackage
        one(mockPackage).members.willReturn(List[MemberEntity](mockClass))
        expectationsForDocTemplateEntity(mockClass)
        expectationsForAnyMemberEntityWithUserComment(mockClass)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.extendsField) mustEqual parentClassName.toLowerCase
      docs(1).get(SearchIndex.typeField)  mustEqual SearchIndex.classField
    }

     "Index trait:  visibility, parentClass, Traits that extends  " in {
      expect {
        defaultExpectationsForPackage
        one(mockPackage).members.willReturn(List[MemberEntity](mockTrait))
        expectationsForDocTemplateEntity(mockTrait)
        expectationsForAnyMemberEntityWithUserComment(mockTrait)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.extendsField) mustEqual parentClassName.toLowerCase
      docs(1).get(SearchIndex.withsField)  mustEqual traitTemplateEntityName.toLowerCase
      docs(1).get(SearchIndex.typeField)  mustEqual SearchIndex.traitField
    }

     "Index Object:  visibility, parentClass, Traits that extends  " in {
      expect {
        defaultExpectationsForPackage
        one(mockPackage).members.willReturn(List[MemberEntity](mockObject))
        expectationsForDocTemplateEntity(mockObject)
        expectationsForAnyMemberEntityWithUserComment(mockObject)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.extendsField) mustEqual parentClassName.toLowerCase
      docs(1).get(SearchIndex.withsField)  mustEqual traitTemplateEntityName.toLowerCase
      docs(1).get(SearchIndex.typeField)  mustEqual SearchIndex.objectField
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

<<<<<<< HEAD
    "Add valsOrVars field to package documents" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs(0).get(SearchIndex.valvarField) must notBeNull
    }

    "Add defs field to package documents" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory, commentMapper)

=======
    "Only index each entity once" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn(packageName)
        allowingMatch(mockRootPackage, "comment")

        // This entity returns itself as a member, putting us in a situation where
        // we could index it twice (or infinitely) if we are not careful.
        one(mockRootPackage).members willReturn(List[MemberEntity](mockRootPackage))
      }

      val index = new SearchIndex(directory)
      index.index(mockRootPackage)
>>>>>>> 778c7561aab78f1a96cb27d30af2786e8a2bf751
      val docs = getAllDocs(directory)

      docs.length must beEqual(1)
    }

<<<<<<< HEAD
    "Reindex document when the comment is updated" in {

      // 1. check the initial comment for the entity
     val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntityWithUserComment(mockEntity)
=======
//    "Index all inherited classes and their parents" in {
//      // TODO: Implement this...
//      assert(false)
//    }

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
>>>>>>> 778c7561aab78f1a96cb27d30af2786e8a2bf751
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      var docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.commentField) mustEqual defaultUserComment

      // 2. Change the comment
      expect { one(commentMapper).latestToString(mockEntity) willReturn(Some(updatedUserComment)) }

      index.reindexEntityComment(mockEntity)

      // 3. Verify that the document exist
      // 4. And the new comment is indexed
      docs = getAllDocs(directory)
      docs(1).get(SearchIndex.commentField) mustEqual updatedUserComment

    }
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