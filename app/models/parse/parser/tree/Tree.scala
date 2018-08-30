package models.parse.parser.tree

import scala.util.parsing.input.Positional

abstract sealed class Tree extends Positional

sealed trait DeclTree extends Tree
sealed trait TermTree extends Tree
sealed trait TypeTree extends Tree
sealed trait MemberTree extends Tree

// Identifiers and properties
sealed trait PropertyName extends TermTree {
  def name: String
}
object PropertyName {
  def apply(name: String): PropertyName = if (Ident.isValidIdentifier(name)) {
    Ident(name)
  } else {
    StringLiteral(name)
  }

  def unapply(tree: PropertyName): Some[String] = Some(tree.name)
}

case class Ident(name: String) extends Tree with PropertyName {
  Ident.requireValidIdent(name)
}
object Ident extends (String => Ident) {
  final def isValidIdentifier(name: String): Boolean = {
    val c = name.head
    (c == '$' || c == '_' || c.isUnicodeIdentifierStart) &&
      name.tail.forall(c => c == '$' || c.isUnicodeIdentifierPart)
  }

  @inline final def requireValidIdent(name: String) {
    require(isValidIdentifier(name), s"$name is not a valid identifier")
  }
}
case class QualifiedIdent(qualifier: List[Ident], name: Ident) extends Tree

// Declarations
case class ExportDecl(v: String) extends DeclTree
case class ImportDecl(v: String) extends DeclTree
case class ModuleDecl(name: PropertyName, members: List[DeclTree]) extends DeclTree
case class ValDecl(name: Ident, tpe: Option[TypeTree]) extends DeclTree
case class VarDecl(name: Ident, tpe: Option[TypeTree]) extends DeclTree
case class FunctionDecl(prot: Boolean, name: Ident, signature: FunSignature) extends DeclTree

// Function signature
case class FunSignature(tparams: List[TypeParam], params: List[FunParam], resultType: Option[TypeTree]) extends Tree
case class FunParam(name: Ident, optional: Boolean, tpe: Option[TypeTree]) extends Tree

// Type parameters
case class TypeParam(name: TypeName, upperBound: Option[TypeRefTree]) extends Tree

// Literals
sealed trait Literal extends TermTree
case class Undefined() extends Literal
case class Null() extends Literal
case class BooleanLiteral(value: Boolean) extends Literal
case class NumberLiteral(value: Double) extends Literal
case class StringLiteral(value: String) extends Literal with PropertyName {
  override def name = value
}

case class WhitespaceDecl(text: String) extends DeclTree

case class ImportCommentDecl(text: String) extends DeclTree
case class LineCommentDecl(text: String) extends DeclTree
case class MultilineCommentDecl(text: String) extends DeclTree

case class ImportCommentType(text: String) extends TypeTree
case class LineCommentType(text: String) extends TypeTree
case class MultilineCommentType(text: String) extends TypeTree

case class ImportCommentMember(text: String) extends MemberTree
case class LineCommentMember(text: String) extends MemberTree
case class MultilineCommentMember(text: String) extends MemberTree

// Type descriptions
case class TypeDecl(name: TypeName, tpe: TypeTree) extends DeclTree
case class EnumDecl(name: TypeName, members: List[Ident]) extends DeclTree
case class ClassDecl(
  abst: Boolean, name: TypeName, tparams: List[TypeParam], parent: Option[TypeRefTree], impls: List[TypeRefTree], members: List[MemberTree]) extends DeclTree
case class InterfaceDecl(name: TypeName, tparams: List[TypeParam], inheritance: List[TypeRefTree], members: List[MemberTree]) extends DeclTree
case class TypeAliasDecl(name: TypeName, tparams: List[TypeParam], alias: TypeTree) extends DeclTree
case class TypeRefTree(name: BaseTypeRef, tparams: List[TypeTree] = Nil) extends TypeTree

sealed abstract class BaseTypeRef extends Tree
case class CoreType(name: String) extends BaseTypeRef
case class TypeName(name: String) extends BaseTypeRef {
  Ident.requireValidIdent(name)
}
case class QualifiedTypeName(qualifier: List[Ident], name: TypeName) extends BaseTypeRef
case class ConstantType(literal: Literal) extends TypeTree
case class ObjectType(members: List[MemberTree]) extends TypeTree
case class FunctionType(signature: FunSignature) extends TypeTree
case class UnionType(left: TypeTree, right: TypeTree) extends TypeTree
case class TupleType(tparams: List[TypeTree]) extends TypeTree
case class TypeQuery(expr: QualifiedIdent) extends TypeTree
case class RepeatedType(underlying: TypeTree) extends TypeTree

// Type members
case class CallMember(signature: FunSignature) extends MemberTree
case class ConstructorMember(signature: FunSignature) extends MemberTree
case class IndexMember(indexName: Ident, indexType: TypeTree, valueType: TypeTree) extends MemberTree
case class PropertyMember(prot: Boolean, name: PropertyName, optional: Boolean, tpe: TypeTree, static: Boolean, readonly: Boolean) extends MemberTree
case class FunctionMember(prot: Boolean, name: PropertyName, optional: Boolean, signature: FunSignature, static: Boolean, readonly: Boolean) extends MemberTree
