package scala.tools.colladoc.model

import org.specs2.mock._
import tools.nsc.doc.model._
import tools.nsc.doc.model.comment.{Body, Comment}
import mapper.CommentToString

/**
 * Mock for entity member.
 * @author rumi
 */
trait EntityMemberMock extends Mockito {
  // Package
  var mockPackage: Package = _
  val packageName = "Mocked Package"

  // MemberEntity
  val entityName = "EntityName"
  val entityLookUp = "entityLookUp"

  //comment values
  val defaultUserComment = "testDefaultUserComment"
  val updatedUserComment = "testUserUpdatedComment"
  val defaultComment = "testDefaultComment"

  var commentMapper: CommentToString = _

  def construct() {
    mockPackage = mock[Package]
    commentMapper = mock[CommentToString]
  }

  def defaultExpectationsForPackage() {
    exactly(1).of(mockPackage).name willReturn (packageName.toLowerCase)
    one(commentMapper).latestToString(mockPackage) willReturn (Some(defaultUserComment))
  }

  def expectationsForEmptyPackage = {
    defaultExpectationsForPackage()
    one(mockPackage).members willReturn (List[MemberEntity]())
  }

  def expectationsForPackageWithEntity(memberEntity: MemberEntity) = {
    defaultExpectationsForPackage()
    one(mockPackage).members willReturn (List[MemberEntity](memberEntity))
  }

  def expectationsForAnyMemberEntityWithUserComment(mockEntity: MemberEntity) = {
    one(mockEntity).name willReturn entityName
    one(commentMapper).latestToString(mockEntity) willReturn (Some(defaultUserComment))
    allowingMatch("members")
  }

  def expectationsForAnyMemberEntityWithComment(mockEntity: MemberEntity) = {
    val entityComment: Comment = new TestComment()
    one(mockEntity).name willReturn entityName
    one(commentMapper).latestToString(mockEntity) willReturn (None)
    one(mockEntity).comment willReturn (Some(entityComment))
    allowingMatch("members")
  }

  def expectationsForAnyMemberEntityWithoutComment(mockEntity: MemberEntity) = {
    one(mockEntity).name willReturn entityName
    one(commentMapper).latestToString(mockEntity) willReturn (None)
    one(mockEntity).comment willReturn (None)
    allowingMatch("members")
  }

  // Although Comment class can be mocked, expectations cannot be applied to the toString method
  // This is due to JMock restriction since toString is a method from the Objects class
  class TestComment extends Comment {
    def body: scala.tools.nsc.doc.model.comment.Body = Body(Nil)
    def authors: scala.List[scala.tools.nsc.doc.model.comment.Body] = Nil
    def see: scala.List[scala.tools.nsc.doc.model.comment.Body] = Nil
    def result: scala.Option[scala.tools.nsc.doc.model.comment.Body] = None
    def throws: scala.collection.Map[scala.Predef.String, scala.tools.nsc.doc.model.comment.Body] = Map()
    def valueParams: scala.collection.Map[scala.Predef.String, scala.tools.nsc.doc.model.comment.Body] = Map()
    def typeParams: scala.collection.Map[scala.Predef.String, scala.tools.nsc.doc.model.comment.Body] = Map()
    def version: scala.Option[scala.tools.nsc.doc.model.comment.Body] = None
    def since: scala.Option[scala.tools.nsc.doc.model.comment.Body] = None
    def todo: scala.List[scala.tools.nsc.doc.model.comment.Body] = Nil
    def deprecated: scala.Option[scala.tools.nsc.doc.model.comment.Body] = None
    def note: scala.List[scala.tools.nsc.doc.model.comment.Body] = Nil
    def example: scala.List[scala.tools.nsc.doc.model.comment.Body] = Nil
    def source: scala.Option[scala.Predef.String] = None
    def constructor: scala.Option[scala.tools.nsc.doc.model.comment.Body] = None
    override def toString() = defaultComment
  }
}

