package com.harana.modules.clearbit.models

case class WebhookResponse(`type`: ModelType,
                           body: Object,
                           status: Int,
                           id: String)