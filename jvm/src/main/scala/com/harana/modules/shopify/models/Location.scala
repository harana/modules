package com.harana.modules.shopify.models

case class Location(id: Long,
                    name: String,
                    address1: String,
                    address2: String,
                    city: String,
                    zip: String,
                    country: String,
                    phone: String,
                    province: String,
                    countryCode: String,
                    countryName: String,
                    provinceCode: String)