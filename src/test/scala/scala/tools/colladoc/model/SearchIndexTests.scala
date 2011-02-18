package scala.tools.colladoc.model
import tools.nsc.doc.model._
import org.specs.SpecificationWithJUnit
import org.specs.mock._
import tools.nsc.doc.model.MemberEntity
import org.apache.lucene.store.{RAMDirectory, Directory}
import org.apache.lucene.index.IndexReader
import tools.nsc.doc.model.Package
import org.apache.lucene.document.Document
import tools.nsc.doc.model.comment.{Body, Comment}

object SearchIndexTests extends SpecificationWithJUnit with EntityMemberMock {
   var directory: Directory = _
  "Search Index" should {
    doBefore {
      directory = new RAMDirectory
      construct
    }
    "Use the FSDirectory that is given to it on creation" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory)
      //index.luceneDirectory must beEqualTo(directory)
    }

    "Index the root package" in {
      expect { expectationsForEmptyPackage }

      val index = new SearchIndex(mockPackage, directory)
      val docs = getAllDocs(directory)

      docs.length must beEqualTo(1)
      docs(0).get(SearchIndex.nameField) mustEqual packageName
    }

    // TODO: Test comments and entityLookUp
    "Index Any Entity and stores its name, entityId and comment" in {
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntity(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory)
      val docs = getAllDocs(directory)

      docs.length must beEqual(1)
      docs(0).get(SearchIndex.nameField) mustEqual entityName
    }

    "Index the classes and store their visibility, parentClass  " in {
      val mockClass = mock[Class]
      val parentClass = mock[TypeEntity]
      val classVisibility = "public"
      val parentClassName = "ParentClass"
      val mockVisibility = mock[Visibility]

      expect {
        defaultExpectationsForPackage
        one(mockPackage).members.willReturn(List[MemberEntity](mockClass))
        one(mockClass).parentType willReturn Some(parentClass)
        one(parentClass).name willReturn parentClassName
        one(mockClass).visibility willReturn mockVisibility
        one(mockVisibility).isPublic willReturn true
        expectationsForAnyMemberEntity(mockClass)
      }


      val index = new SearchIndex(mockPackage, directory)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(0).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(0).get(SearchIndex.extendsField) mustEqual parentClassName
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