package com.harana.modules.airtable.models

case class Attachment(id: String,
                      url: String,
                      filename: String,
                      size: Float,
                     `type`: String)
                      //thumbnails: Map[String, Thumbnail])
