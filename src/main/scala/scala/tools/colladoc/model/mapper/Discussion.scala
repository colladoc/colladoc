/*
 * Copyright (c) 2011, Sergey Ignatov. All rights reserved.
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
package scala.tools.colladoc
package model.mapper

import xml.NodeSeq
import net.liftweb.mapper._
import net.liftweb.common.Full
import lib.util.DateUtils.{atomDateFormatter, dateFormatter}

/**
 * Comment of discussion thread.
 * @author Sergey Ignatov
 */
class Discussion extends LongKeyedMapper[Discussion] with IdPK {
  def getSingleton = Discussion

  /** Category of this discussion. */
  object category extends LongMappedMapper(this, Category)

  /** Qualified name of symbol this comment belongs to. */
  object qualifiedName extends MappedString(this, 255) {
    override def dbIndexed_? = true
  }

  /** Parent in the hierarchy. */
  object parent extends LongMappedMapper(this, Discussion)

  /** Changed comment content. */
  object comment extends MappedString(this, 4000)

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

  /** Link to user's profile. */
  def authorProfileHyperlink = User.find(user.is) match {
    case Full(u) => u.profileHyperlink
    case _ => NodeSeq.Empty
  }

  /** Author's email. */
  def authorEmail = User.find(user.is) match {
    case Full(u) => u.email.is
    case _ => ""
  }

  /** Get change author's username and date. */
  def userNameDate: String = "%s by %s".format(dateFormatter(dateTime.is), userName)

  def atomDateTime: String = atomDateFormatter(dateTime.is).toString

  def humanDateTime: String = dateFormatter(dateTime.is).toString
}

object Discussion extends Discussion with LongKeyedMetaMapper[Discussion] {
  override def dbTableName = "discussions"

  /** Get discussion top level comments for template. */
  def topLevelComments(category: Category, qualifiedName: String) = findAll(
    By(Discussion.category, category),
    By(Discussion.qualifiedName, qualifiedName),
    NullRef(Discussion.parent),
    OrderBy(Discussion.dateTime, Ascending))

  /** Get discussion comments count for template. */
  def count(category: Category, qualifiedName: String): Long = count(
    By(Discussion.category, category),
    By(Discussion.qualifiedName, qualifiedName),
    By(Discussion.valid, true))

  /** Get replies for discussion current comment. */
  def replies(category: Category, d: Discussion) = Discussion.findAll(
    By(Discussion.category, category),
    By(Discussion.qualifiedName, d.qualifiedName.is),
    NotNullRef(Discussion.parent),
    By(Discussion.parent, d),
    OrderBy(Discussion.dateTime, Ascending))
}