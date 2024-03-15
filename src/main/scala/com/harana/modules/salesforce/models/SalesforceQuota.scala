package com.harana.modules.salesforce.models

case class SalesforceQuota(used: Int,
                           remaining: Int,
                           percentage: Int)