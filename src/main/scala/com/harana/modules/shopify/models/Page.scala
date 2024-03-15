package com.harana.modules.shopify.models

case class Page[T](previousUrl: Option[String],
                   nextUrl: Option[String],
                   items: List[T])