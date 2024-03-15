package com.harana.modules.shopify.models

import java.time.Instant

case class Metafield(id: Long,
                     namespace: String,
                     key: String,
                     value: String,
                     description: Option[String],
                     owner_id: Long,
                     owner_resource: String,
                     created_at: Option[Instant],
                     updated_at: Option[Instant],
                     `type`: String,
                     admin_graphql_api_id: String)
