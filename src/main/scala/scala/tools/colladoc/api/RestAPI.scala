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
package scala.tools.colladoc {
package api {

import xml.NodeSeq
import net.liftweb.http.{AtomResponse, GetRequest, LiftResponse, LiftRules, NotFoundResponse, Req}
import net.liftweb.http.rest.XMLApiHelper
import model.mapper.Comment

/**
 * @author ignatov
 */
object RestAPI extends XMLApiHelper {
  def dispatch: LiftRules.DispatchPF = {
    case Req("atom" :: "comments" :: Nil, "", GetRequest) => () => showRecentCommentsAtom()
    case Req("atom" :: x :: Nil, "", _) => failure _
  }

  def failure(): LiftResponse = {
    NotFoundResponse()
  }

  def createTag(in: NodeSeq) = {
    <colladoc_api>in</colladoc_api>
  }

  def showRecentCommentsAtom() = AtomResponse(Comment.toAtomFeed(Comment.getLatestComments(20)))
}

}

}