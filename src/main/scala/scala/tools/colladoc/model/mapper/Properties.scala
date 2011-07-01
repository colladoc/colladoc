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
package scala.tools.colladoc.model.mapper

import net.liftweb.mapper._
import net.liftweb.common.{Full, Box}

/**
 * Key/value record, container for application property.
 * @author Sergey Ignatov
 */
class Property extends LongKeyedMapper[Property] {
  def getSingleton = Properties

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object key extends MappedString(this, 255) {
     override def dbIndexed_? = true
  }
  object value extends MappedText(this)
}

/** Mapper for application properties. */
object Properties extends Property with LongKeyedMetaMapper[Property] {
  override def dbTableName = "properties"

  override def dbIndexes = UniqueIndex(key) :: super.dbIndexes

  /** Get map with all properties. */
  def props: Map[String, String] = findAll() map {p => (p.key.is, p.value.is)} toMap

  /** Get property value by key. */
  def get(key: String): Box[String] = Box(props.get(key))

  /** Set property value. */
  def set(key: String, value: String) = (
          find(key) match {
            case Full(p) => p
            case _ => create.key(key)
          }).value(value).save
}