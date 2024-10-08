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
import shared.utils.UnitSpec

class OtherDeductionsFeatureSwitchesSpec extends UnitSpec {

  "FeatureSwitches" should {
    val feature = "supporting-agents-access-control"
    "return true" when {
      "the feature switch is set to true" in {
        val config = Configuration(
          "supporting-agents-access-control.enabled"                -> true,
          "supporting-agents-access-control.enabled"                -> true,
          "supporting-agents-access-control.released-in-production" -> true
        )

        val featureSwitches = OtherDeductionsFeatureSwitches(config)

        featureSwitches.supportingAgentsAccessControlEnabled shouldBe true
        featureSwitches.isReleasedInProduction(feature) shouldBe true
        featureSwitches.isEnabled(feature) shouldBe true
      }

    }
    "return false" when {
      "the feature switch is set to false" in {
        val config = Configuration(
          "supporting-agents-access-control.enabled"                -> true,
          "supporting-agents-access-control.enabled"                -> false,
          "supporting-agents-access-control.released-in-production" -> false
        )

        val featureSwitches = OtherDeductionsFeatureSwitches(config)

        featureSwitches.supportingAgentsAccessControlEnabled shouldBe false
        featureSwitches.isReleasedInProduction(feature) shouldBe false
        featureSwitches.isEnabled(feature) shouldBe false
      }

    }

  }

}
