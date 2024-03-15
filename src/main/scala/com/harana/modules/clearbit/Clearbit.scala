package com.harana.modules.clearbit

import com.harana.modules.clearbit.models.RiskResponse
import zio.Task
import zio.macros.accessible

@accessible
trait Clearbit {

  def calculateRisk(emailAddress: String, ipAddress: String, firstName: String, lastName: String): Task[RiskResponse]

}