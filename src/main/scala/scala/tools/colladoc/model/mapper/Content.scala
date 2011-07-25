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
 * Comment of content thread.
 * @author Sergey Ignatov
 */
class Content extends LongKeyedMapper[Content] with IdPK {
  def getSingleton = Content

  /** Qualified name of symbol this comment belongs to. */
  object qualifiedName extends MappedString(this, 255) {
    override def dbIndexed_? = true
  }

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

  /** Get change author's username and date. */
  def userNameDate: String = "%s by %s".format(dateFormatter(dateTime.is), userName)

  def atomDateTime: String = atomDateFormatter(dateTime.is).toString

  def humanDateTime: String = dateFormatter(dateTime.is).toString
}

object Content extends Content with LongKeyedMetaMapper[Content] {
  override def dbTableName = "content"
}