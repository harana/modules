package com.harana.modules.shopify.models

case class Address(firstName: String,
                   lastName: String,
                   name: String,
                   company: Option[String],
                   address1: String,
                   address2: Option[String],
                   city: String,
                   zip: String,
                   province: String,
                   country: String,
                   provinceCode: String,
                   countryCode: String,
                   phone: Option[String],
                   latitude: Option[BigDecimal],
                   longitude: Option[BigDecimal])