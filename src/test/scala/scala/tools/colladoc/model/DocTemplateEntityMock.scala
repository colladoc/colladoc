package scala.tools.colladoc.model

import org.specs.mock._
import tools.nsc.doc.model._

/**
 * Mock for the Classes, traits and Objects
 * @author: rumi
 */
trait DocTemplateEntityMock extends AnyRef with JMocker with ClassMocker {
  val mockClass = mock[Class]
  val mockTrait = mock[Trait]
  val mockObject = mock[Object]
  val parentClass = mock[TypeEntity]
  val classVisibility = "public"
  val parentClassName = "ParentClass"
  val mockVisibility = mock[Visibility]
  val traitTemplateEntity = mock[TemplateEntity]
  val traitTemplateEntityName = "TestTrait"
  val notTemplateEntityName = "TestNotTrait"
  val notTraitTemplateEntity = mock[TemplateEntity]
  val notTraitTemplateEntityName = "NotTestTrait"

  def expectationsForDocTemplateEntity(mockEntity: DocTemplateEntity) {
    expect {
      one(mockEntity).linearizationTemplates willReturn (List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))

      // From all TemplateEntities for the class(all with) get the name only for the one that are Traits
      // Make sure that if EntityTemplate is not a trait its name is not added
      one(traitTemplateEntity).name willReturn traitTemplateEntityName
      one(notTraitTemplateEntity).name willReturn notTraitTemplateEntityName
      allowing(traitTemplateEntity).isTrait willReturn true
      allowing(notTraitTemplateEntity).isTrait willReturn false

      // Get the visibility of the class
      one(mockEntity).visibility willReturn mockVisibility
      one(mockVisibility).isPublic willReturn true
    }
  }
}