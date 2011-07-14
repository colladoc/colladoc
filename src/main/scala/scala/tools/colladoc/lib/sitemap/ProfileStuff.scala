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
package lib
package sitemap

import xml.Text
import net.liftweb.sitemap.Loc
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.NamedPF
import net.liftweb.common.Full
import net.liftweb.mapper.By
import model.mapper.User

/** Profile location parameter. */
case class ProfileLoc()

/**
 * Profile sitemap location.
 */
object ProfileStuff extends Loc[ProfileLoc] {
  /** The name of the page. */
  def name = "profile"

  /** The default parameters (used for generating the menu listing). */
  def defaultValue = Full(ProfileLoc())

  /** Parameters. */
  def params = List.empty

  /** Text of the link. */
  val text = new Loc.LinkText((loc: ProfileLoc) => Text("Profile"))

  /** Generate a link based on the current page. */
  val link = new Loc.Link[ProfileLoc](List("profile"))

  /** Rewrite location. */
  override val rewrite: LocRewrite = Full(NamedPF("Profile Rewrite") {
    case RewriteRequest(ParsePath("profile" :: username :: Nil, _, _, _), _, _)
      if (User.superUser_? && !User.find(By(User.userName, username)).isEmpty) =>
       (RewriteResponse("profile" :: Nil, Map("username" -> username)), ProfileLoc())
  })
}