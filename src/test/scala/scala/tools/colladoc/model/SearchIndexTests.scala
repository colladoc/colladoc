package scala.tools.colladoc.model

import tools.nsc.doc.model._
import org.specs.SpecificationWithJUnit
import tools.nsc.doc.model.MemberEntity
import org.apache.lucene.store.{RAMDirectory, Directory}
import tools.nsc.doc.model.Package
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader

object SearchIndexTests extends SpecificationWithJUnit with EntityMemberMock with DocTemplateEntityMock {
  var directory: Directory = _
  "Search Index" should {
    doBefore {
      directory = new RAMDirectory
      construct()
    }

    "Use the FSDirectory that is given to it on creation" in {
      expect {
        expectationsForEmptyPackage
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      index.directory must beEqualTo(directory)
    }

    "Index the root package" in {
      expect {
        expectationsForEmptyPackage
      }

      new SearchIndex(mockPackage, directory, commentMapper)
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

      new SearchIndex(mockPackage, directory, commentMapper)
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

      new SearchIndex(mockPackage, directory, commentMapper)
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

      new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.nameField) mustEqual entityName.toLowerCase
      docs(1).get(SearchIndex.commentField) mustEqual defaultComment
      docs(1).get(SearchIndex.entityLookupField) mustEqual mockEntity.hashCode.toString
    }

    "Index class: visibility, parentClass, traits that extends" in {
      expect {
        defaultExpectationsForPackage()
        one(mockPackage).members.willReturn(List[MemberEntity](mockClass))
        one(mockClass).linearizationTemplates.willReturn(List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))
        expectationsForDocTemplateEntity(mockClass)
        expectationsForAnyMemberEntityWithUserComment(mockClass)
      }

      new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.withsField) mustEqual traitTemplateEntityName.toLowerCase
      docs(1).get(SearchIndex.typeField) mustEqual SearchIndex.classField
    }

    "Index trait: visibility, parentClass, traits that extends" in {
      expect {
        defaultExpectationsForPackage()
        one(mockPackage).members.willReturn(List[MemberEntity](mockTrait))
        one(mockTrait).linearizationTemplates.willReturn(List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))
        expectationsForDocTemplateEntity(mockTrait)
        expectationsForAnyMemberEntityWithUserComment(mockTrait)
      }

      new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.withsField) mustEqual traitTemplateEntityName.toLowerCase
      docs(1).get(SearchIndex.typeField) mustEqual SearchIndex.traitField
    }

    "Index object: visibility, parentClass, traits that extends" in {
      expect {
        defaultExpectationsForPackage()
        one(mockPackage).members.willReturn(List[MemberEntity](mockObject))
        one(mockObject).linearizationTemplates.willReturn(List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))
        expectationsForDocTemplateEntity(mockObject)
        expectationsForAnyMemberEntityWithUserComment(mockObject)
      }

      new SearchIndex(mockPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.visibilityField) mustEqual classVisibility
      docs(1).get(SearchIndex.withsField) mustEqual traitTemplateEntityName.toLowerCase
      docs(1).get(SearchIndex.typeField) mustEqual SearchIndex.objectField
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

    "Only index each entity once" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(1).of(mockRootPackage).name willReturn (packageName.toLowerCase)
        one(commentMapper).latestToString(mockRootPackage) willReturn (Some(defaultUserComment))

        // This entity returns itself as a member, putting us in a situation where
        // we could index it twice (or infinitely) if we are not careful.
        one(mockRootPackage).members willReturn (List[MemberEntity](mockRootPackage))
      }

      new SearchIndex(mockRootPackage, directory, commentMapper)
      val docs = getAllDocs(directory)

      docs.length must beEqual(1)
    }

    //  "Index all inherited classes and their parents" in {
    //    // TODO: Implement this...
    //    assert(false)
    //  }

    "Reindex document when the comment is updated" in {
      // 1. check the initial comment for the entity
      val mockEntity = mock[MemberEntity]
      expect {
        expectationsForPackageWithEntity(mockEntity)
        expectationsForAnyMemberEntityWithUserComment(mockEntity)
      }

      val index = new SearchIndex(mockPackage, directory, commentMapper)
      var docs = getAllDocs(directory)

      docs.length must beEqual(2)
      docs(1).get(SearchIndex.commentField) mustEqual defaultUserComment

      // 2. Change the comment
      expect {
        one(commentMapper).latestToString(mockEntity) willReturn (Some(updatedUserComment))
      }

      index.reindexEntityComment(mockEntity)

      // 3. Verify that the document exist
      // 4. And the new comment is indexed
      docs = getAllDocs(directory)
      docs(1).get(SearchIndex.commentField) mustEqual updatedUserComment

    }
  }

  private def getAllDocs(dir: Directory) = {
    var docs = List[Document]()
    var reader: IndexReader = null
    try {
      reader = IndexReader.open(dir, true)
      for (i <- 0 until reader.numDocs()) {
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