package com.harana.modules.clearbit.models

import io.circe.generic.JsonCodec

@JsonCodec
case class RiskResponse(id: String,
                        live: Boolean,
                        fingerprint: Boolean,
                        email: Email,
                        address: Address,
                        ip: IP,
                        risk: Risk)

@JsonCodec
case class Email(valid: Option[Boolean],
                 socialMatch: Option[Boolean],
                 companyMatch: Option[Boolean],
                 nameMatch: Option[Boolean],
                 disposable: Option[Boolean],
                 freeProvider: Option[Boolean],
                 blacklisted: Option[Boolean])

@JsonCodec
case class Address(geoMatch: Option[String])

@JsonCodec
case class IP(proxy: Option[Boolean],
              geoMatch: Option[Boolean],
              blacklisted: Boolean,
              rateLimited: Option[Boolean])

@JsonCodec
case class Risk(level: String,
                score: Int,
                reasons: List[String])