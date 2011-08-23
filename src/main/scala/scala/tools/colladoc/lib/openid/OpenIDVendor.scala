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
package openid

import org.openid4java.consumer.VerificationResult
import org.openid4java.message.AuthRequest
import org.openid4java.discovery.{DiscoveryInformation, Identifier}
import net.liftweb.openid._
import net.liftweb.openid.WellKnownAttributes._
import net.liftweb.common.{Logger, Full, Box}
import model.mapper.User
import net.liftweb.http.S

/**
 * Manages OpenID logins - creating a new entry in the database on login for a user who hasn't been seen before.
 * @author Sergey Ignatov
 */
object OpenIDVendor extends SimpleOpenIDVendor with Logger {
  override def createAConsumer = new AnyRef with OpenIDConsumer[UserType] {

    def addParams(di: DiscoveryInformation, authReq: AuthRequest) {
      WellKnownEndpoints.findEndpoint(di) map {
        ep =>
          ep.makeAttributeExtension(List(Email, FullName, FirstName, LastName)) foreach {
            ex => authReq.addExtension(ex)
          }
      }
    }

    beforeAuth = Box(addParams _)
  }

  override def postLogin(identifier: Box[Identifier], res: VerificationResult) {
    identifier match {
      case Full(id) =>
        val user = User.createIfNew(id.getIdentifier)

        if (user.deleted_?) {
          S.error("Invalid user credentials or user deleted")
        } else {
          val attrs = WellKnownAttributes.attributeValues(res.getAuthResponse)
          attrs.get(Email) map { e => user.email(trace("Extracted email", e)) }
          attrs.get(FirstName) map { n => user.firstName(trace("Extracted name", n)) }
          attrs.get(LastName) map { n => user.lastName(trace("Extracted name", n)) }

          if (user.userName.is.trim.isEmpty)
            user.userName(user.firstName + " " + user.lastName)

          if (user.userName.is.trim.isEmpty)
            user.userName(id.getIdentifier)

          user.save()

          User.logUserIn(user)
        }
      case _ =>
        // TODO: response with error
    }
  }
}