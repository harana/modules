package com.harana.modules.handlebars

import com.github.jknack.handlebars.context.MapValueResolver
import com.github.jknack.handlebars.helper.EachHelper
import com.github.jknack.handlebars.{Helper, Options, ValueResolver}

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.{universe => ru}
import scala.util.Try

object ScalaResolver extends ValueResolver {

  private val rootMirror = ru.runtimeMirror(getClass.getClassLoader)

  private def methodMirrorFor(context: AnyRef, name: String): Option[ru.MethodMirror] = {
    val meta = rootMirror.reflect(context)
    val optAccessor = meta.symbol.info.decls find { m =>
      m.isMethod && m.isPublic && m.name.toString == name
    }
    optAccessor.map(a => meta.reflectMethod(a.asMethod))
  }

  override def resolve(context: AnyRef, name: String): AnyRef = context match {
    case m: collection.Map[_,_] => MapValueResolver.INSTANCE.resolve(m.asJava, name)
    case _ =>
      val optMM = methodMirrorFor(context, name)
      val ret = optMM.fold(ValueResolver.UNRESOLVED)(m => resolve(m.apply())): AnyRef
      println(s"...returning ${ret.toString}")
      ret
  }

  override def resolve(context: scala.Any): AnyRef = context match {
    case m: collection.Map[_,_] => MapValueResolver.INSTANCE.resolve(m.asJava)
    case Some(x: AnyRef) => x
    case None => null
    case x: AnyRef => x
  }

  override def propertySet(context: scala.Any): java.util.Set[java.util.Map.Entry[String, AnyRef]] = context match {
    case m: collection.Map[_,_] =>
      MapValueResolver.INSTANCE.propertySet(m.asJava)
    case _ =>
      println(s"ScalaMemberResolver.propertySet in context: [${context.getClass.getName}]")
      val meta = rootMirror.reflect(context)
      val accessors = meta.symbol.info.decls.filter(m => m.isMethod && m.isPublic).toSeq
      val results = for {
        a <- accessors
        v <- Try(meta.reflectMethod(a.asMethod).apply()).toOption
      } yield a.name.toString -> v.asInstanceOf[AnyRef]
      results.toMap.asJava.entrySet
  }
}

object ScalaEachHelper extends Helper[AnyRef] {
  override def apply(context: scala.AnyRef, options: Options): AnyRef = context match {
    case iter: Iterable[_] => EachHelper.INSTANCE.apply(iter.asJava, options)
    case _ => EachHelper.INSTANCE.apply(context, options)
  }
}
