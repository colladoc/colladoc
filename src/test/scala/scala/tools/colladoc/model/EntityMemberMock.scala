package scala.tools.colladoc.model

import org.specs.mock._
import org.apache.lucene.store.RAMDirectory
import scala.tools.colladoc.lib.util.NameUtils._
import tools.nsc.doc.model._

/**
 * Created by IntelliJ IDEA.
 * User: rumi
 * Date: 17/02/11
 * Time: 23:54
 * To change this template use File | Settings | File Templates.
 */
import mapper.{CommentToString, Comment}

  trait EntityMemberMock extends AnyRef with JMocker with ClassMocker{
    // Package
    var mockPackage: Package =_
    val packageName = "Mocked Package"

    // MemberEntity
    val entityName = "EntityName"
    val entityLookUp = "entityLookUp"

    def construct ={
       mockPackage = mock[Package]
    }
    def defaultExpectationsForPackage={
         exactly(1).of(mockPackage).name willReturn(packageName.toLowerCase)
         one(mockPackage).qualifiedName willReturn packageName
    }
    def expectationsForEmptyPackage = {
       expect {
          defaultExpectationsForPackage
          one(mockPackage).members willReturn(List[MemberEntity]())
        }
    }
    def expectationsForPackageWithEntity(memberEntity : MemberEntity) = {
       expect {
          defaultExpectationsForPackage
          one(mockPackage).members willReturn(List[MemberEntity](memberEntity))
        }
    }
    def expectationsForAnyMemberEntity(mockEntity: MemberEntity) = {
       expect {
          one(mockEntity).name willReturn entityName
          one(mockEntity).qualifiedName willReturn entityName
          allowingMatch("members")
        }
    }
  }