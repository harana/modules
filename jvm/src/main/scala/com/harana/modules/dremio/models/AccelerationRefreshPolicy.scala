package com.harana.modules.dremio.models

import io.circe.generic.JsonCodec

@JsonCodec
case class AccelerationRefreshPolicy(refreshPeriodMs: Long,
                                     gracePeriodMs: Long,
                                     method: AccelerationRefreshPolicyMethod,
                                     refreshField: String)