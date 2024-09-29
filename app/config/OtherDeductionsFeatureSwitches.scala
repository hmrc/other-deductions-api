/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import play.api.Configuration
import shared.config.{FeatureSwitches, SharedAppConfig}

import javax.inject.{Inject, Singleton}

@Singleton
case class OtherDeductionsFeatureSwitches(featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: SharedAppConfig) = this(appConfig.featureSwitchConfig)

  override val supportingAgentsAccessControlEnabled: Boolean = isEnabled("supporting-agents-access-control")

  override def isEnabled(key: String): Boolean = isConfigTrue(key + ".enabled")

  private def isConfigTrue(key: String): Boolean = featureSwitchConfig.getOptional[Boolean](key).getOrElse(true)

  override def isReleasedInProduction(feature: String): Boolean = isConfigTrue(feature + ".released-in-production")

}

object OtherDeductionsFeatureSwitches {
  def apply(configuration: Configuration): FeatureSwitches = new OtherDeductionsFeatureSwitches(configuration)

  def apply(appConfig: SharedAppConfig): FeatureSwitches = new OtherDeductionsFeatureSwitches(appConfig)
}
