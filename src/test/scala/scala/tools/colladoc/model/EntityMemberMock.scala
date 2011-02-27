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
       //allowingMatch(mockPackage, "comment")
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
        allowingMatch("members")
      }
  }

}

trait ClassMock extends AnyRef with JMocker with ClassMocker {
  val mockClass = mock[Class]
  val parentClass = mock[TypeEntity]
  val classVisibility = "public"
  val parentClassName = "ParentClass"
  val mockVisibility = mock[Visibility]
  val traitTemplateEntity = mock[TemplateEntity]
  val traitTemplateEntityName = "TestTrait"
  val notTemplateEntityName = "TestNotTrait"
  val notTraitTemplateEntity = mock[TemplateEntity]
  val notTraitTemplateEntityName = "NotTestTrait"

  def expectationsForClass(){
    expect {

      // Get the parent of the class and all withs for the class
      one(mockClass).parentType willReturn Some(parentClass)
      one(mockClass).linearizationTemplates willReturn(List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))

      // From all TemplateEntities for the class(all with) get the name only for teh one that are Traits
      // Make sure that if EntityTenplate is not a trait its name is not added
      one(traitTemplateEntity).name willReturn traitTemplateEntityName
      one(traitTemplateEntity).isTrait willReturn true
      one(notTraitTemplateEntity).isTrait willReturn false

      // Get the name of the parent class
      one(parentClass).name willReturn parentClassName

      //get the visibility of the class
      one(mockClass).visibility willReturn mockVisibility
      one(mockVisibility).isPublic willReturn true
    }
  }
}