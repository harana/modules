package com.harana.modules.shopify.models

case class Metaobject(id: String,
                      `type`: String,
                      fields: List[Metafield])