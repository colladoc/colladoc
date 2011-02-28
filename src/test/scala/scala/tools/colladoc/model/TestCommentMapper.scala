package scala.tools.colladoc.model
{
import mapper.{Comment, CommentToString}

/**
 * Created by IntelliJ IDEA.
 * User: rumi
 * Date: 28/02/11
 * Time: 02:29
 * To change this template use File | Settings | File Templates.
 */
  class TestCommentMapper extends Comment with CommentToString{
     def latestToString(qualName: String) = "testComment"
  }
}