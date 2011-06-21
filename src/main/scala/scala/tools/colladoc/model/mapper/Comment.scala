/*
 * Copyright (c) 2010, Petr Hosek. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scala.tools.colladoc {
package model {
package mapper {
import lib.util.Helpers._
import lib.util.NameUtils._

import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import tools.nsc.doc.model.MemberEntity

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Mapper for comment table storing documentation changes.
 * @author Petr Hosek
 */
class Comment extends LongKeyedMapper[Comment] with IdPK {
  def getSingleton = Comment

  /** Qualified name of symbol this comment belongs to. */
  object qualifiedName extends MappedString(this, 255) {
    override def dbIndexed_? = true
  }
  /** Changed comment content. */
  object comment extends MappedString(this, 4000)

  /** Changeset this comment belongs to. */
  object changeSet extends MappedDateTime(this)

  /** Changed comment author. */
  object user extends LongMappedMapper(this, User)
  /** Change date and time. */
  object dateTime extends MappedDateTime(this)

  /** Whether this change is still valid. */
  object valid extends MappedBoolean(this) {
    override def defaultValue = true
  }

  /** Get change author's username. */
  def userName: String = User.find(user.is) match {
    case Full(u) => u.userName
    case _ => ""
  }

  def dateFormatter(d: Date) = new SimpleDateFormat("HH:mm:ss dd MMMM yyyy").format(d)

  def atomDateFormatter(d: Date) = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d)

  def iso8601Formatter(d: Date) = new SimpleDateFormat("yyyy-MM-dd").format(d)

  /** Get change author's username and date. */
  def userNameDate: String =
    "%s by %s".format(dateFormatter(dateTime.is), userName)

  /**
   * Comment representation for Atom.
   * @return Atom entry for comment
   */
  def toAtomEntry =
    <entry>
      <title>{qualifiedName.is}</title>
      <id>tag:colladoc,{iso8601Formatter(dateTime.is)}:/atom/comments/{id.is}</id>
      <updated>{atomDateFormatter(dateTime.is)}</updated>
      <content>
        {"New comment for " + qualifiedName.is + ":" + comment.is + " by " + userName + " at " + dateFormatter(changeSet.is)}
      </content>
    </entry>
}

/**
 * Mapper for comment table storing documentation changes.
 * @author Petr Hosek
 */
object Comment extends Comment with LongKeyedMetaMapper[Comment]
                               with CommentToString{
  override def dbTableName = "comments"

  /**
   * Find latest change for given symbol qualified name.
   * @param qualName symbol qualified name
   * @return latest change if exists, none otherwise
   */
  def latest(qualName: String) = {
    Comment.findAll(By(Comment.qualifiedName, qualName),
      By(Comment.valid, true),
      OrderBy(Comment.dateTime, Descending), MaxRows(1)) match {
      case List(c: Comment, _*) => Some(c)
      case _ => None
    }
  }

  def latestToString(member: MemberEntity) : Option[String] = {
      latest(member.uniqueName) match { case Some(str) => Some(str.comment.is);
                                        case _ => None}
                                        //member.comment match { case Some(str) => str.body.toString; case _ => ""}}
    }

  /**
   * Get all revisions for given symbol qualified name.
   * @param qualName symbol qualified name
   * @return tuples of revisions' identifiers and date
   */
  def revisions(qualName: String, valid: Boolean = true) = {
    val cmts = changeSets(findAll(By(qualifiedName, qualName), By(Comment.valid, valid), OrderBy(Comment.dateTime, Descending)))
    cmts.map{ c => (c.id.is.toString, c.userNameDate) }
  }

  /**
   * Group changes to changesets.
   * @param cmts list of comment changes
   * @return grouped comment changes
   */
  def changeSets(cmts: List[Comment]) =
    if (cmts.nonEmpty)
      cmts.groupBy(c => c.qualifiedName.is + c.user.is).values.flatMap { cs =>
        cs.head :: ((cs zip cs.tail) collect {
          case (c1, c2) if c1.dateTime.is - c2.dateTime.is > minutes(30) => c2
        })
      }.toList sortWith(_.dateTime.is.getTime > _.dateTime.is.getTime)
    else
      cmts

  /**
   * Atom representation for list of messages.
   * @param cmts list of comment changes
   * @return Atom feed
   */
  def toAtomFeed(cmts: List[Comment]) = {
    val entries = cmts.map(_.toAtomEntry)
    val updatedDate = if (cmts isEmpty) new Date() else cmts.head.dateTime.is
    <feed xmlns="http://www.w3.org/2005/Atom">
      <author>
        <name>Colladoc</name>
      </author>
      <id>tag:coladoc,2011:/atom</id>
      <title>Colladoc recent chaneged comments</title>
      <updated>{atomDateFormatter(updatedDate)}</updated>
      {entries}
    </feed>
  }

  /**
   * Get latest comments.
   * @param count amount of entries
   * @return list of comments
   */
  def getLatestComments(count: Int) = Comment.findAll(By(Comment.valid, true), OrderBy(Comment.dateTime, Descending), MaxRows(count))
}

trait CommentToString{
  def latestToString(member: MemberEntity) : Option[String]
}
}
}
}
