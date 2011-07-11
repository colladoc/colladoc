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
package snippet

import model.mapper.{Comment, User}
import lib.DependencyFactory._
import page.Profile
import xml.{NodeSeq, Text}
import net.liftweb.util.BindHelpers._
import net.liftweb.http.{SHtml, S, RequestVar}
import net.liftweb.mapper.{By, MaxRows}
import net.liftweb.common.{Box, Full}

/**
 * User profile snippet.
 * @author Sergey Ignatov
 */
class ProfileOps {
  object username extends RequestVar[String](S.param("username") openOr "")

  lazy val profile = new Profile(model.vend.rootPackage)

  def title(xhtml: NodeSeq): NodeSeq = Text(profile.title)

  def body(xhtml: NodeSeq): NodeSeq = {
    val maybeUser: Box[User] = User.find(By(User.userName, username))

    val fullname = maybeUser match {
      case Full(u) => u.userName.is
      case _ => ""
    }

    val comments = maybeUser match {
      case Full(u) =>
        val cmts = Comment.findAll(By(Comment.user, u), By(Comment.valid, true))
        if (cmts.length == 0)
          <span>No comments from this user.</span>
        else
          <ul>
            { cmts.map(c =>
                {
                  val abs = "/" + c.qualifiedName.is.replace(".", "/").replace("#", "$") + ".html"
                  <li>
                    <a href={abs}>{c.qualifiedName.is}</a>: {c.comment.is}
                  </li>
                })
            }
          </ul>
      case _ => <span></span>
    }


    bind("profile",
      profile.body,
      "fullname" -> Text(fullname),
      "comments" -> comments
    )
  }
}