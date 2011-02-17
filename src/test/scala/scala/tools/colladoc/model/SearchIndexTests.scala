package scala.tools.colladoc.model

import tools.nsc.doc.model._
import org.specs.SpecificationWithJUnit
import org.specs.mock.JMocker
import tools.nsc.doc.model.MemberEntity
import org.apache.lucene.store.{RAMDirectory, Directory}
import org.apache.lucene.index.IndexReader
import tools.nsc.doc.model.Package
import org.apache.lucene.document.Document

object SearchIndexTests extends SpecificationWithJUnit with JMocker {
  "Search Index" should {
    "Use the FSDirectory that is given to it on creation" in {
      val expectedDirectory : Directory = new RAMDirectory
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn("foo")
        allowingMatch(mockRootPackage, "comment")
        one(mockRootPackage).members willReturn(List[MemberEntity]())
      }

      val index = new SearchIndex(mockRootPackage, expectedDirectory)
      index.luceneDirectory must beEqualTo(expectedDirectory)
    }

    "Index the root package" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn(packageName)
        allowingMatch(mockRootPackage, "comment")
        one(mockRootPackage).members willReturn(List[MemberEntity]())
      }
      val index = new SearchIndex(mockRootPackage, directory)
      val docs = getAllDocs(directory)
      docs.length must beEqualTo(1)
      docs(0).get(SearchIndex.nameField) mustEqual packageName
    }

    "Add valsOrVars field to package documents" in {
      val directory = new RAMDirectory
      val packageName = "foo"
      val mockRootPackage = mock[Package]
      expect {
        exactly(2).of(mockRootPackage).name willReturn(packageName)
        allowingMatch(mockRootPackage, "comment")
        one(mockRootPackage).members willReturn(List[MemberEntity]())
      }

      val index = new SearchIndex(mockRootPackage, directory)

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

      val index = new SearchIndex(mockRootPackage, directory)

      val docs = getAllDocs(directory)
      docs(0).get(SearchIndex.defsField) must notBeNull
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