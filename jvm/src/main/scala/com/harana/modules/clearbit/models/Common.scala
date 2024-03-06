package com.harana.modules.clearbit.models

case class Facebook(handle: String)

case class LinkedIn(handle: String)

case class AngelList(handle: String,
                     bio: String,
                     blog: String,
                     site: String,
                     followers: Long,
                     avatar: String)

case class Crunchbase(handle: String)


case class Github(handle: String,
                  id: Long,
                  avatar: String,
                  company: String,
                  blog: String,
                  followers: Long,
                  following: Long)

case class Twitter(handle: String,
                   id: String,
                   bio: String,
                   followers: Long,
                   following: Long,
                   statuses: Long,
                   favorites: Long,
                   location: String,
                   site: String,
                   avatar: String)
