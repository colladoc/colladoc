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

/**
 * Dynamic model factory extending [ModelFactory] with dynamic behavior.
 * @author Petr Hosek
 */
trait DynamicModelFactory extends ModelFactory { thisFactory: ModelFactory with DynamicCommentFactory with TreeFactory =>

  /**
   * Creates the new member entity based upon existing entity `mbr` with given comment.
   * @param mbr The member entity to copy
   * @param cmt The new entity comment
   * @return copied entity
   */
  def copyMember(mbr: MemberEntity, cmt: Comment)(tag: AnyRef = null): MemberEntity = mbr match {
    case tpe: AbstractType => new AbstractTypeProxy(tpe, cmt)(tag)
    case tpe: AliasType => new AliasTypeProxy(tpe, cmt)(tag)
    case _def: Def => new DefProxy(_def, cmt)(tag)
    case ctr: Constructor => new ConstructorProxy(ctr, cmt)(tag)
    case mbr: NonTemplateMemberEntity => new NonTemplateMemberEntityProxy(mbr, cmt)(tag)
    case pkg: Package => new PackageProxy(pkg, cmt)(tag)
    case cls: Class => new ClassProxy(cls, cmt)(tag)
    case trt: Trait => new TraitProxy(trt, cmt)(tag)
    case tpl: DocTemplateEntity => new DocTemplateEntityProxy(tpl, cmt)(tag)
    case mbr: MemberEntity => new MemberEntityProxy(mbr, cmt)(tag)
  }

  /**
   * Updated member entity proxy.
   */
  private class MemberEntityProxy(val mbr: MemberEntity, val cmt: Comment)(val tag: AnyRef = null) extends MemberEntity {
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
    override def equals(other: Any) = other match {
      case that: MemberEntityProxy => mbr.equals(that.mbr)
      case _ => mbr.equals(other)
    }
    override def hashCode = mbr.hashCode
  }

  /**
   * Updated documentable template entity proxy.
   */
  private class DocTemplateEntityProxy(tpl: DocTemplateEntity, cmt: Comment)(tag: AnyRef) extends MemberEntityProxy(tpl, cmt)(tag) with DocTemplateEntity {
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
    def isCaseClass = tpl.isCaseClass
    def selfType = tpl.selfType
  }

  /**
   * Updated trait entity proxy.
   */
  private class TraitProxy(trt: Trait, cmt: Comment)(tag: AnyRef) extends DocTemplateEntityProxy(trt, cmt)(tag) with Trait {
    def typeParams = trt.typeParams
  }

  /**
   * Updated class entity proxy.
   */
  private class ClassProxy(cls: Class, cmt: Comment)(tag: AnyRef) extends TraitProxy(cls, cmt)(tag) with Class {
    def primaryConstructor = cls.primaryConstructor
    def constructors = cls.constructors
    def valueParams = cls.valueParams
  }

  /**
   * Updated package entity proxy.
   */
  private class PackageProxy(pkg: Package, cmt: Comment)(tag: AnyRef) extends DocTemplateEntityProxy(pkg, cmt)(tag) with Package {
    override def inTemplate = pkg.inTemplate
    override def toRoot = pkg.toRoot
    def packages = pkg.packages
  }

  /**
   * Updated non template member entity proxy.
   */
  private class NonTemplateMemberEntityProxy(mbr: NonTemplateMemberEntity, cmt: Comment)(tag: AnyRef) extends MemberEntityProxy(mbr, cmt)(tag) with NonTemplateMemberEntity {
    def isUseCase = mbr.isUseCase
  }

  /**
   * Updated function entity proxy.
   */
  private class DefProxy(_def: Def, cmt: Comment)(tag: AnyRef) extends NonTemplateMemberEntityProxy(_def, cmt)(tag) with Def {
    def valueParams = _def.valueParams
    def typeParams = _def.typeParams
  }

  /**
   * Updated constructor entity proxy.
   */
  private class ConstructorProxy(ctr: Constructor, cmt: Comment)(tag: AnyRef) extends NonTemplateMemberEntityProxy(ctr, cmt)(tag) with Constructor {
    def isPrimary = ctr.isPrimary
    def valueParams = ctr.valueParams
  }

  /**
   * Updated abstract type entity proxy.
   */
  private class AbstractTypeProxy(tpe: AbstractType, cmt: Comment)(tag: AnyRef) extends NonTemplateMemberEntityProxy(tpe, cmt)(tag) with AbstractType {
    def lo = tpe.lo
    def hi = tpe.hi
    def typeParams = tpe.typeParams
  }

  /**
   * Updated alias type entity proxy.
   */
  private class AliasTypeProxy(tpe: AliasType, cmt: Comment)(tag: AnyRef) extends NonTemplateMemberEntityProxy(tpe, cmt)(tag) with AliasType {
    def alias = tpe.alias
    def typeParams = tpe.typeParams
  }

  /** Transforms member entity into extended entity providing dynamic behavior. */
  implicit def modifications(mbr: MemberEntity) = new MemberEntityExtensions(mbr)

  /**
   * Extended member entity class providing dynamic behavior.
   */
  class MemberEntityExtensions(mbr: MemberEntity) {

    /**
     * Get tag associated with member entity.
     * @return associated tag if there is any
     */
    def tag(): AnyRef = mbr match {
      case prx: MemberEntityProxy => prx.tag
      case _ => null
    }

  }

}

}
}
}