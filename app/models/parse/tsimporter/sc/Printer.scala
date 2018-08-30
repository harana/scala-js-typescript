/* TypeScript importer for Scala.js
 * Copyright 2013-2014 LAMP/EPFL
 * @author  Sébastien Doeraene
 */

package org.scalajs.tools.tsimporter.sc

import java.io.PrintWriter
import org.scalajs.tools.tsimporter.Trees.Modifier

class Printer(private val output: PrintWriter, appName: String) {
  import Printer._

  private implicit val self = this

  private var currentJSNamespace = ""

  def printSymbol(sym: Symbol) {
    val name = sym.name
    sym match {
      case comment: CommentSymbol =>
        pln"/* ${comment.text} */"

      case sym: PackageSymbol =>
        val isRootPackage = name == Name.EMPTY

        val parentPackage :+ thisPackage =
          if (isRootPackage) "".split("\\.").toList.map(Name(_))
          else List(name)

        if (!parentPackage.isEmpty) {
          pln"package ${parentPackage.mkString(".")}"
        }

        if (isRootPackage) {
          val packageName = appName.replace("-","")
          pln"package com.harana.scalajs.slinky.$packageName"
          pln"";
          pln"import slinky.core.ExternalComponent"
          pln"import slinky.core.annotations.react"
          pln"import scala.scalajs.js"
          pln"import scala.scalajs.js.annotation.JSImport"
          pln"import scala.scalajs.js.|"
        }

        val oldJSNamespace = currentJSNamespace
        if (!isRootPackage)
          currentJSNamespace += name.name + "."

        if (!sym.members.isEmpty) {
          val (topLevels, packageObjectMembers) =
            sym.members.partition(canBeTopLevel)
//
//          pln"";
//          pln"package $thisPackage {"
//
          for (sym <- topLevels)
            printSymbol(sym)
        }

        currentJSNamespace = oldJSNamespace

      case sym: ClassSymbol =>

        if (name.name.endsWith("Props")) {
          val tpe = name.name.replace("Props", "")
          val quotedFile = s""""$appName""""
          pln"";
          pln"@react"
          pln"object $tpe extends ExternalComponent {"
          pln"";
          pln" case class Props("
          printMemberDecls(sym)
          pln" )"
          pln"";
          pln" override val component = ${tpe}DOM.$tpe"
          pln"}"
          pln"";
          pln"@js.native"
          pln"@JSImport($quotedFile, JSImport.Default)"
          pln"object ${tpe}DOM extends js.Object {"
          pln"  val ${tpe}: js.Object = js.native"
          pln"}"
        }else {

          val sealedKw = if (sym.isSealed) "sealed " else ""
          val abstractKw = if (sym.isAbstract) "abstract " else ""
          val kw = if (sym.isTrait) "trait" else "class"
          val constructorStr =
            if (sym.isTrait) ""
            else if (sym.members.exists(isParameterlessConstructor)) ""
            else " protected ()"
          val parents =
            if (sym.parents.isEmpty) List(TypeRef.Object)
            else sym.parents.toList

          pln"";
          pln"@js.native"
          if (!sym.isTrait) {
            if (currentJSNamespace.isEmpty)
              pln"@JSGlobal"
            else
              pln"""@JSGlobal("$currentJSNamespace${name.name}")"""
          }
          p"$sealedKw$abstractKw$kw $name"
          if (!sym.tparams.isEmpty)
            p"[${sym.tparams}]"

          {
            implicit val withSep = ListElemSeparator.WithKeyword
            pln"$constructorStr extends $parents {"
          }

          printMemberDecls(sym)
          pln"}"
        }

      case sym: ModuleSymbol =>
        pln"";
        if (sym.isGlobal) {
          pln"@js.native"
          if (currentJSNamespace.isEmpty)
            pln"@JSGlobal"
          else
            pln"""@JSGlobal("$currentJSNamespace${name.name}")"""
          pln"object $name extends js.Object {"
        } else {
          pln"object $name {"
        }
        printMemberDecls(sym)
        pln"}"

      case sym: TypeAliasSymbol =>
        p"  type $name"
        if (!sym.tparams.isEmpty)
          p"[${sym.tparams}]"
        pln" = ${sym.alias}"

      case sym: FieldSymbol =>
        sym.jsName foreach { jsName =>
          pln"""  @JSName("$jsName")"""
        }
        val access =
          if (sym.modifiers(Modifier.Protected)) "protected "
          else ""
        val decl =
          if (sym.modifiers(Modifier.Const)) "val"
          else if (sym.modifiers(Modifier.ReadOnly)) "def"
          else "var"
        p"  $access$decl $name: "
        if (sym.optional) {
          p"Option["
          p"${sym.tpe}"
          p"]"
        }else{
          p"${sym.tpe}"
        }
        if (!sym.modifiers(Modifier.Abstract))
          p" ${if (sym.optional) "= None" else ""}"
        pln""

      case sym: MethodSymbol =>
        val params = sym.params

        if (name == Name.CONSTRUCTOR) {
          if (!params.isEmpty)
            pln"  def this($params) = this()"
        } else {
          sym.jsName foreach { jsName =>
            pln"""  @JSName("$jsName")"""
          }
          if (sym.isBracketAccess)
            pln"""  @JSBracketAccess"""
          val modifiers =
            if (sym.needsOverride) "override " else ""
          p"  ${modifiers}def $name"
          if (!sym.tparams.isEmpty)
            p"[${sym.tparams}]"
          p"($params): ${sym.resultType}"
          if (!sym.modifiers(Modifier.Abstract))
            p" = js.native"
          pln""
        }

      case sym: ParamSymbol =>
        p"$name: ${sym.tpe}${if (sym.optional) " = ???" else ""}"

      case sym: TypeParamSymbol =>
        p"$name"
        sym.upperBound.foreach(bound => p" <: $bound")
    }
  }

  private def printMemberDecls(owner: ContainerSymbol) {
    val (constructors, others) =
      owner.members.toList.partition(_.name == Name.CONSTRUCTOR)
    for (sym <- constructors ++ others)
      printSymbol(sym)
  }

  private def canBeTopLevel(sym: Symbol): Boolean =
    sym.isInstanceOf[ContainerSymbol]

  private def isParameterlessConstructor(sym: Symbol): Boolean = {
    sym match {
      case sym: MethodSymbol =>
        sym.name == Name.CONSTRUCTOR && sym.params.isEmpty
      case _ =>
        false
    }
  }

  def printTypeRef(tpe: TypeRef) {
    tpe match {
      case TypeRef(typeName, Nil) =>
        p"$typeName"

      case TypeRef.Union(types) =>
        implicit val withPipe = ListElemSeparator.Pipe
        p"$types"

      case TypeRef.Intersection(types) =>
        implicit val withWith = ListElemSeparator.WithKeyword
        p"$types"

      case TypeRef.This =>
        p"this.type"

      case TypeRef.Singleton(termRef) =>
        p"$termRef.type"

      case TypeRef.Repeated(underlying) =>
        p"$underlying*"

      case TypeRef(typeName, targs) =>
        p"$typeName[$targs]"
    }
  }

  private def print(x: Any) {
    x match {
      case x: Symbol => printSymbol(x)
      case x: TypeRef => printTypeRef(x)
      case QualifiedName(Name.scala, Name.scalajs, Name.js, name) =>
        output.print("js.")
        output.print(name)
      case QualifiedName(Name.scala, name) => output.print(name)
      case QualifiedName(Name.java, Name.lang, name) => output.print(name)
      case _ => output.print(x)
    }
  }
}

object Printer {
  private class ListElemSeparator(val s: String) extends AnyVal

  private object ListElemSeparator {
    val Comma = new ListElemSeparator(", ")
    val Pipe = new ListElemSeparator(" | ")
    val WithKeyword = new ListElemSeparator(" with ")
  }

  private implicit class OutputHelper(val sc: StringContext) extends AnyVal {
    def p(args: Any*)(implicit printer: Printer,
        sep: ListElemSeparator = ListElemSeparator.Comma) {
      val strings = sc.parts.iterator
      val expressions = args.iterator

      val output = printer.output
      output.print(strings.next())
      while (strings.hasNext) {
        expressions.next() match {
          case seq: Seq[_] =>
            val iter = seq.iterator
            if (iter.hasNext) {
              printer.print(iter.next())
              while (iter.hasNext) {
                output.print(sep.s)
                printer.print(iter.next())
              }
            }

          case expr =>
            printer.print(expr)
        }
        output.print(strings.next())
      }
    }

    def pln(args: Any*)(implicit printer: Printer,
        sep: ListElemSeparator = ListElemSeparator.Comma) {
      p(args:_*)
      printer.output.println()
    }
  }
}
