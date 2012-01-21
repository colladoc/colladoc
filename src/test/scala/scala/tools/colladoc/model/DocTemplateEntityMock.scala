package scala.tools.colladoc.model

import org.specs2.mock._
import tools.nsc.doc.model._

/**
 * Mock for the Classes, traits and Objects
 * @author: rumi
 */
trait DocTemplateEntityMock extends Mockito {
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
    there was one(mockEntity).linearizationTemplates returns (List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))

    // From all TemplateEntities for the class(all with) get the name only for the one that are Traits
    // Make sure that if EntityTemplate is not a trait its name is not added
    there was one(traitTemplateEntity).name returns traitTemplateEntityName
    there was one(notTraitTemplateEntity).name returns notTraitTemplateEntityName
    traitTemplateEntity.isTrait returns true
    notTraitTemplateEntity.isTrait returns false

    // Get the visibility of the class
    there was one(mockEntity).visibility returns mockVisibility
    there was one(mockVisibility).isPublic returns true
  }
}