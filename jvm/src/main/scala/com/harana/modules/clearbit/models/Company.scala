package com.harana.modules.clearbit.models

case class Company(id: String,
                   name: String,
                   legalName: String,
                   domain: String,
                   domainAliases: List[String],
                   logo: String,
                   site: Site,
                   tags: List[String],
                   category: Category,
                   description: String,
                   foundedYear: Integer,
                   location: String,
                   timeZone: String,
                   utcOffset: Long,
                   geo: CompanyGeo,
                   metrics: Metrics,
                   facebook: Facebook,
                   linkedin: LinkedIn,
                   twitter: Twitter,
                   crunchbase: Crunchbase,
                   emailProvider: Boolean,
                   `type`: String,
                   ticker: String,
                   phone: String,
                   indexedAt: String,
                   tech: List[String],
                   parent: Parent)

case class Site(title: String,
                h1: String,
                metaDescription: String,
                phoneNumbers: List[String],
                emailAddresses: List[String])

case class Category(sector: String,
                    industryGroup: String,
                    industry: String,
                    subIndustry: String,
                    sicCode: String,
                    naicsCode: String)

case class CompanyGeo(streetNumber: String,
                      streetName: String,
                      subPremise: String,
                      city: String,
                      state: String,
                      stateCode: String,
                      postalCode: String,
                      country: String,
                      countryCode: String,
                      lat: Double,
                      lng: Double)

case class Metrics(alexaUsRank: Long,
                   alexaGlobalRank: Long,
                   employees: Long,
                   employeesRange: String,
                   marketCap: Long,
                   raised: Long,
                   annualRevenue: Long,
                   fiscalYearEnd: Long,
                   estimatedAnnualRevenue: String)

case class Parent(domain: String)
