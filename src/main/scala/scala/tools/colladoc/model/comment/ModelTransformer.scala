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
package comment

import tools.nsc.doc.model.{DocTemplateEntity, MemberEntity}
import collection.mutable.ArrayBuffer

class ModelTransformer extends Function1[MemberEntity, MemberEntity] {
  /** Check whether the entity has changed. */
  protected def unchanged(mbr: MemberEntity, ents: Seq[MemberEntity]) =
    ents.length == 1 && (ents.head == mbr)

  /** Call transform(MemberEntity) for each entity in iterator, append results to buffer. */
  def transform(it: Iterator[MemberEntity], buf: ArrayBuffer[MemberEntity]): Seq[MemberEntity] =
    it.foldLeft(buf)(_ ++= transform(_)).toSeq

  /** Call transform(MemberEntity) to each entity in ents, yield ents if nothing changes, otherwise a new sequence of concatenated results. */
  def transform(ents: Seq[MemberEntity]): Seq[MemberEntity] = {
    val (ents1, ents2) = ents span (mbr => unchanged(mbr, transform(mbr)))

    if (ents2.isEmpty) ents
    else ents1 ++ transform(ents2.head) ++ transform(ents2.tail)
  }

  /** Transform the entity. */
  def transform(mbr: MemberEntity): Seq[MemberEntity] = {
    mbr match {
      case tpl: DocTemplateEntity  =>
        val mbrs = tpl.members
        val nmbrs = transform(mbrs)

        if (mbrs eq nmbrs) List(tpl)
        else null // TODO: use DynamicModelFactory to create DocTemplateEntity copy
      case mbr => List(mbr)
    }
  }

  def apply(mbr: MemberEntity): MemberEntity = {
    val seq = transform(mbr)
    if (seq.length > 1)
      throw new UnsupportedOperationException("transform must return single entity for root");
    else seq.head
  }
}

class RuleTransformer(rules: RewriteRule*) extends ModelTransformer {
  override def transform(mbr: MemberEntity): Seq[MemberEntity] =
    rules.foldLeft(super.transform(mbr)) { (res, rule) => rule transform res }
}

abstract class RewriteRule extends ModelTransformer {
  val name = this.toString()

  override def transform(ents: Seq[MemberEntity]): Seq[MemberEntity] = super.transform(ents)
  override def transform(mbr: MemberEntity): Seq[MemberEntity] = List(mbr)
}