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

package api.models.errors

import play.api.http.Status._

object ValueFormatError extends MtdError("FORMAT_VALUE", "The field should be between 0 and 99999999999.99", BAD_REQUEST)

object NameOfShipFormatError extends MtdError("FORMAT_NAME_OF_SHIP", "The provided name of ship is not valid", BAD_REQUEST)

object CustomerReferenceFormatError extends MtdError("FORMAT_CUSTOMER_REFERENCE", "The provided customer reference is not valid", BAD_REQUEST)

object DateFormatError extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD", BAD_REQUEST)

object RangeToDateBeforeFromDateError extends MtdError("RANGE_TO_DATE_BEFORE_FROM_DATE", "The To date is before the From date", BAD_REQUEST)

object NinoFormatError
    extends MtdError(
      code = "FORMAT_NINO",
      message = "The provided NINO is invalid",
      BAD_REQUEST
    )

object TaxYearFormatError
    extends MtdError(
      code = "FORMAT_TAX_YEAR",
      message = "The provided tax year is invalid",
      BAD_REQUEST
    )

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError
    extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "Tax year range invalid. A tax year range of one year is required", BAD_REQUEST)

//Standard Errors
object NotFoundError
    extends MtdError(
      code = "MATCHING_RESOURCE_NOT_FOUND",
      message = "Matching resource not found",
      NOT_FOUND
    )

object InternalError
    extends MtdError(
      code = "INTERNAL_SERVER_ERROR",
      message = "An internal server error occurred",
      INTERNAL_SERVER_ERROR
    )

object BadRequestError
    extends MtdError(
      code = "INVALID_REQUEST",
      message = "Invalid request",
      BAD_REQUEST
    )

object BVRError
    extends MtdError(
      code = "BUSINESS_ERROR",
      message = "Business validation error",
      BAD_REQUEST
    )

object ServiceUnavailableError
    extends MtdError(
      code = "SERVICE_UNAVAILABLE",
      message = "Internal server error",
      INTERNAL_SERVER_ERROR
    )

//Authorisation Errors
object ClientNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)

object ClientNotAuthenticatedError
    extends MtdError(
      code = "CLIENT_OR_AGENT_NOT_AUTHORISED",
      message = "The client and/or agent is not authorised",
      UNAUTHORIZED
    )

object InvalidBearerTokenError
    extends MtdError(
      code = "UNAUTHORIZED",
      message = "Bearer token is missing or not authorized",
      UNAUTHORIZED
    )

// Accept header Errors
object InvalidAcceptHeaderError
    extends MtdError(
      code = "ACCEPT_HEADER_INVALID",
      message = "The accept header is missing or invalid",
      NOT_ACCEPTABLE
    )

object UnsupportedVersionError
    extends MtdError(
      code = "NOT_FOUND",
      message = "The requested resource could not be found",
      NOT_FOUND
    )

object InvalidBodyTypeError
    extends MtdError(
      code = "INVALID_BODY_TYPE",
      message = "Expecting text/json or application/json body",
      UNSUPPORTED_MEDIA_TYPE
    )
