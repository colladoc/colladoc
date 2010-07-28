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

object DynamicModelFactory {

  /**
   * Creates the new member entity based upon existing entity `mbr` with given comment.
   * @param mbr The member entity to copy
   * @param cmt The new entity comment
   */
  def createMember(mbr: MemberEntity, cmt: Comment) = mbr match {
    case tpe: AbstractType => new AbstractTypeProxy(tpe, cmt)
    case tpe: AliasType => new AliasTypeProxy(tpe, cmt)
    case _def: Def => new DefProxy(_def, cmt)
    case ctr: Constructor => new ConstructorProxy(ctr, cmt)
    case mbr: NonTemplateMemberEntity => new NonTemplateMemberEntityProxy(mbr, cmt)
    case pkg: Package => new PackageProxy(pkg, cmt)
    case cls: Class => new ClassProxy(cls, cmt)
    case trt: Trait => new TraitProxy(trt, cmt)
    case tpl: DocTemplateEntity => new DocTemplateEntityProxy(tpl, cmt)
    case mbr: MemberEntity => new MemberEntityProxy(mbr, cmt)
  }

  class MemberEntityProxy(mbr: MemberEntity, cmt: Comment) extends MemberEntity {
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
  }

  class DocTemplateEntityProxy(tpl: DocTemplateEntity, cmt: Comment) extends MemberEntityProxy(tpl, cmt) with DocTemplateEntity {
    override def toRoot = tpl.toRoot
    def inSource = tpl.inSource
    def sourceUrl = tpl.sourceUrl
    def parentType = tpl.parentType
    def parentTemplates = tpl.parentTemplates
    def linearization = tpl.linearization
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

  class TraitProxy(trt: Trait, cmt: Comment) extends DocTemplateEntityProxy(trt, cmt) with Trait {
    def valueParams = trt.valueParams
    def typeParams = trt.typeParams
  }

  class ClassProxy(cls: Class, cmt: Comment) extends TraitProxy(cls, cmt) with Class {
    def primaryConstructor = cls.primaryConstructor
    def constructors = cls.constructors
    def isCaseClass = cls.isCaseClass
  }

  class PackageProxy(pkg: Package, cmt: Comment) extends DocTemplateEntityProxy(pkg, cmt) with Package {
    override def inTemplate = pkg.inTemplate
    override def toRoot = pkg.toRoot
    def packages = pkg.packages
  }

  class NonTemplateMemberEntityProxy(mbr: NonTemplateMemberEntity, cmt: Comment) extends MemberEntityProxy(mbr, cmt) with NonTemplateMemberEntity {
    def isUseCase = mbr.isUseCase
  }

  class DefProxy(_def: Def, cmt: Comment) extends NonTemplateMemberEntityProxy(_def, cmt) with Def {
    def valueParams = _def.valueParams
    def typeParams = _def.typeParams
  }

  class ConstructorProxy(ctr: Constructor, cmt: Comment) extends NonTemplateMemberEntityProxy(ctr, cmt) with Constructor {
    def isPrimary = ctr.isPrimary
    def valueParams = ctr.valueParams
  }

  class AbstractTypeProxy(tpe: AbstractType, cmt: Comment) extends NonTemplateMemberEntityProxy(tpe, cmt) with AbstractType {
    def lo = tpe.lo
    def hi = tpe.hi
    def typeParams = tpe.typeParams
  }

  class AliasTypeProxy(tpe: AliasType, cmt: Comment) extends NonTemplateMemberEntityProxy(tpe, cmt) with AliasType {
    def alias = tpe.alias
    def typeParams = tpe.typeParams
  }

}

}
}
}