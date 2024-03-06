package com.harana.modules.clearbit.models

case class Person(id: String,
                  name: Name,
                  email: String,
                  gender: String,
                  location: String,
                  timeZone: String,
                  utcOffset: Long,
                  geo: PersonGeo,
                  bio: String,
                  site: String,
                  avatar: String,
                  employment: Employment,
                  facebook: Facebook,
                  github: Github,
                  twitter: Twitter,
                  linkedin: LinkedIn,
                  aboutme: AboutMe,
                  gravatar: Gravatar,
                  fuzzy: Boolean,
                  emailProvider: Boolean,
                  indexedAt: String)

case class Name(fullName: String,
                givenName: String,
                familyName: String)

case class PersonGeo(city: String,
                     state: String,
                     stateCode: String,
                     country: String,
                     countryCode: String,
                     lat: Double,
                     lng: Double)

case class Employment(name: String,
                      title: String,
                      domain: String,
                      role: String,
                      subRole: String,
                      seniority: String)

case class AboutMe(handle: String,
                   bio: String,
                   avatar: String)

case class Gravatar(handle: String,
                    urls: List[Url],
                    avatar: String,
                    avatars: List[Avatar])

case class Url(value: String, title: String)

case class Avatar(url: String, `type`: String)