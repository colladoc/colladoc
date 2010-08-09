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
package scala.tools.colladoc
package model

import net.liftweb.mapper._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._

import xml.NodeSeq

import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.common.{Full, Empty}

class Comment extends LongKeyedMapper[Comment] with IdPK {
  def getSingleton = Comment

  object qualifiedName extends MappedString(this, 255) {
    override def dbIndexed_? = true
  }
  object comment extends MappedString(this, 4000)

  object changeSet extends MappedDateTime(this)

  object user extends LongMappedMapper(this, User)
  object dateTime extends MappedDateTime(this)

  def userName: String = User.find(user.is) match {
    case Full(u) => u.userName
    case _ => ""
  }

  def dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy")

  def userNameDate: String =
    "%s by %s".format(dateFormat.format(dateTime.is), userName)
}

object Comment extends Comment with LongKeyedMetaMapper[Comment] {
  override def dbTableName = "comments"

  def select(qualifiedName: String, func: (String) => JsCmd) = {
    val fmt = new SimpleDateFormat("HH:mm:ss MM/dd/yy")
    def format(c: Comment) =
      "%s by %s".format(fmt.format(c.dateTime.is), User.find(c.user.is).open_!.userName)

    val opts = findAll(By(Comment.qualifiedName, qualifiedName), OrderBy(Comment.dateTime, Descending))
    if (!opts.isEmpty)
      SHtml.ajaxSelect(opts.map { c: Comment => (c.id.is.toString, format(c).toString) }, Empty, func, ("class", "select"))
    else
      NodeSeq.Empty
  }
}