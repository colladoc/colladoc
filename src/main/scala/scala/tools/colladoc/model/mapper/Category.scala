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

import net.liftweb.mapper._

/**
 * Category for discussion thread.
 * @author Sergey Ignatov
 */
class Category extends LongKeyedMapper[Category] with IdPK {
  def getSingleton = Category

  /** Category name. */
  object name extends MappedString(this, 255) {
    override def dbIndexed_? = true
  }

  /** Whether this category is still valid. */
  object valid extends MappedBoolean(this) {
    override def defaultValue = true
  }

  /** Is allowed to view this category for anonymous user. */
  object anonymousView extends MappedBoolean(this) {
    override def defaultValue = true
  }

  /** Is allowed to post into this category for anonymous user. */
  object anonymousPost extends MappedBoolean(this) {
    override def defaultValue = false
  }
}

object Category extends Category with LongKeyedMetaMapper[Category] {
  override def dbTableName = "categories"

  /** Get all categories with non empty names. */
  def all = findAll filter { c => c.name.is.trim.length > 0 }

  def get(d: Discussion) = Category.find(d.category).open_!
}