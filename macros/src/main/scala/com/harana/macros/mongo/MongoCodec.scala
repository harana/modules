package com.harana.macros.mongo

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class MongoCodec(anyRef: AnyRef) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro MongoCodecMacro.impl
}

object MongoCodecMacro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {

    import c.universe._

    val result = annottees map (_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }") :: _ =>

        q"""
              $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self =>
              ..$stats
						}
				"""
    }
    c.Expr[Any](result)
  }
}