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
package scala.tools.colladoc {
package model {
package comment {

import tools.nsc.doc.model.MemberEntity
import tools.nsc.doc.model._

import tools.nsc.doc.model.comment.Comment
import java.util.Date

object DynamicModelFactory {

  /**
   * Creates the new member entity based upon existing entity `mbr` with given comment.
   * @param mbr The member entity to copy
   * @param cmt The new entity comment
   */
  def createMember(mbr: MemberEntity, cmt: Comment, dte: Date = new Date) = mbr match {
    case tpe: AbstractType => new AbstractTypeProxy(tpe, cmt, dte)
    case tpe: AliasType => new AliasTypeProxy(tpe, cmt, dte)
    case _def: Def => new DefProxy(_def, cmt, dte)
    case ctr: Constructor => new ConstructorProxy(ctr, cmt, dte)
    case mbr: NonTemplateMemberEntity => new NonTemplateMemberEntityProxy(mbr, cmt, dte)
    case pkg: Package => new PackageProxy(pkg, cmt, dte)
    case cls: Class => new ClassProxy(cls, cmt, dte)
    case trt: Trait => new TraitProxy(trt, cmt, dte)
    case tpl: DocTemplateEntity => new DocTemplateEntityProxy(tpl, cmt, dte)
    case mbr: MemberEntity => new MemberEntityProxy(mbr, cmt, dte)
  }

  class MemberEntityProxy(mbr: MemberEntity, cmt: Comment, val date: Date) extends MemberEntity {
    def name = mbr.name
    def qualifiedName = mbr.qualifiedName
    def comment = Some(cmt)
    def inTemplate = mbr.inTemplate
    def toRoot = mbr.toRoot
    def inDefinitionTemplates = mbr.inDefinitionTemplates
    def definitionName = mbr.definitionName
    def visibility = mbr.visibility
    def flags = mbr.flags
    def deprecation = mbr.deprecation
    def inheritedFrom = mbr.inheritedFrom
    def resultType = mbr.resultType
    def isDef = mbr.isDef
    def isVal = mbr.isVal
    def isLazyVal = mbr.isLazyVal
    def isVar = mbr.isVar
    def isImplicit = mbr.isImplicit
    def isAbstract = mbr.isAbstract
    def isConstructor = mbr.isConstructor
    def isAliasType = mbr.isAliasType
    def isAbstractType = mbr.isAbstractType
    def isTemplate = mbr.isTemplate
    def universe = mbr.universe
  }

  class DocTemplateEntityProxy(tpl: DocTemplateEntity, cmt: Comment, dte: Date) extends MemberEntityProxy(tpl, cmt, dte) with DocTemplateEntity {
    override def toRoot = tpl.toRoot
    def inSource = tpl.inSource
    def sourceUrl = tpl.sourceUrl
    def parentType = tpl.parentType
    def linearization = tpl.linearization
    def linearizationTemplates = tpl.linearizationTemplates
    def linearizationTypes = tpl.linearizationTypes
    def subClasses = tpl.subClasses
    def members = tpl.members
    def templates = tpl.templates
    def methods = tpl.methods
    def values = tpl.values
    def abstractTypes = tpl.abstractTypes
    def aliasTypes = tpl.aliasTypes
    def companion = tpl.companion
    def isPackage = tpl.isPackage
    def isRootPackage = tpl.isRootPackage
    def isTrait = tpl.isTrait
    def isClass = tpl.isClass
    def isObject = tpl.isObject
    def isDocTemplate = tpl.isDocTemplate
    def selfType = tpl.selfType
  }

  class TraitProxy(trt: Trait, cmt: Comment, dte: Date) extends DocTemplateEntityProxy(trt, cmt, dte) with Trait {
    def valueParams = trt.valueParams
    def typeParams = trt.typeParams
  }

  class ClassProxy(cls: Class, cmt: Comment, dte: Date) extends TraitProxy(cls, cmt, dte) with Class {
    def primaryConstructor = cls.primaryConstructor
    def constructors = cls.constructors
    def isCaseClass = cls.isCaseClass
  }

  class PackageProxy(pkg: Package, cmt: Comment, dte: Date) extends DocTemplateEntityProxy(pkg, cmt, dte) with Package {
    override def inTemplate = pkg.inTemplate
    override def toRoot = pkg.toRoot
    def packages = pkg.packages
  }

  class NonTemplateMemberEntityProxy(mbr: NonTemplateMemberEntity, cmt: Comment, dte: Date) extends MemberEntityProxy(mbr, cmt, dte) with NonTemplateMemberEntity {
    def isUseCase = mbr.isUseCase
  }

  class DefProxy(_def: Def, cmt: Comment, dte: Date) extends NonTemplateMemberEntityProxy(_def, cmt, dte) with Def {
    def valueParams = _def.valueParams
    def typeParams = _def.typeParams
  }

  class ConstructorProxy(ctr: Constructor, cmt: Comment, dte: Date) extends NonTemplateMemberEntityProxy(ctr, cmt, dte) with Constructor {
    def isPrimary = ctr.isPrimary
    def valueParams = ctr.valueParams
  }

  class AbstractTypeProxy(tpe: AbstractType, cmt: Comment, dte: Date) extends NonTemplateMemberEntityProxy(tpe, cmt, dte) with AbstractType {
    def lo = tpe.lo
    def hi = tpe.hi
    def typeParams = tpe.typeParams
  }

  class AliasTypeProxy(tpe: AliasType, cmt: Comment, dte: Date) extends NonTemplateMemberEntityProxy(tpe, cmt, dte) with AliasType {
    def alias = tpe.alias
    def typeParams = tpe.typeParams
  }

  implicit def modifications(mbr: MemberEntity) = new {
    def date(): Option[Date] = mbr match {
      case mbrp: MemberEntityProxy => Some(mbrp.date)
      case _ => None
    }
  }

}

}
}
}