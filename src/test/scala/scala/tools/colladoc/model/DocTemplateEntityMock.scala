import org.specs.mock._
import scala.tools.colladoc.lib.util.NameUtils._
import tools.nsc.doc.model._

/**
 * Created by IntelliJ IDEA.
 * User: rumi
 * Date: 17/02/11
 * Time: 23:54
 * To change this template use File | Settings | File Templates.
 */
// Mock for the Classes, traits and Objects
package scala.tools.colladoc.model{
import mapper.{CommentToString, Comment}
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

    def expectationsForDocTemplateEntity(mockEntity: DocTemplateEntity){
      expect {

        // Get the parent of the class and all withs for the class
        one(mockEntity).parentType willReturn Some(parentClass)
        one(mockEntity).linearizationTemplates willReturn(List[TemplateEntity](traitTemplateEntity, notTraitTemplateEntity))

        // From all TemplateEntities for the class(all with) get the name only for the one that are Traits
        // Make sure that if EntityTenplate is not a trait its name is not added
        one(traitTemplateEntity).name willReturn traitTemplateEntityName
        one(traitTemplateEntity).isTrait willReturn true
        one(notTraitTemplateEntity).isTrait willReturn false

        // Get the name of the parent class
        one(parentClass).name willReturn parentClassName

        //get the visibility of the class
        one(mockEntity).visibility willReturn mockVisibility
        one(mockVisibility).isPublic willReturn true
      }
    }
  }
}
