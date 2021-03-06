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
package scala.tools.colladoc.boot

import net.liftweb.util.Props
import tools.colladoc.model.mapper.{Properties, User}
import net.liftweb.common.Empty

/**
 * Bootstrap class for Colladoc project.
 * @author Sergey Ignatov
 */
object Bootstrapper {
  def boot() {
    addDefaultAdmin()
    loadProperties()
  }

  /**
   * Add admin user on the first boot.
   */
  def addDefaultAdmin() {
    val userName = "colladoc"
    val password = "colladoc"
    val firstName = "Colladoc"
    val email = "colladoc@scala-webapps.epfl.ch"
    val isSuperUser = true

    if (User.count() == 0)
      User.create.
        userName(userName).
        password(password).
        firstName(firstName).
        email(email).
        superUser(isSuperUser).
        save
  }

  /**
   * Load properties to database from the file with properties.
   */
  def loadProperties() {
    Props.props.foreach { case (k, v) =>
      if (k.startsWith("-doc"))
        Properties.get(k) match {
          case Empty => Properties.set(k, v)
          case _ =>
        }
    }
  }
}