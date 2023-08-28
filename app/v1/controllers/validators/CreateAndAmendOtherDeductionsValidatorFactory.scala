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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveJsonObject, ResolveNino, ResolveParsedNumber, ResolveTaxYear}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.JsValue
import v1.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequestData, Seafarers}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

class CreateAndAmendOtherDeductionsValidatorFactory {

  private val resolveJson = new ResolveJsonObject[CreateAndAmendOtherDeductionsBody]()

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendOtherDeductionsRequestData] =
    new Validator[CreateAndAmendOtherDeductionsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendOtherDeductionsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(taxYear),
          resolveJson(body)
        ).mapN(CreateAndAmendOtherDeductionsRequestData) andThen validateBody

      private def validateBody(
          parsed: CreateAndAmendOtherDeductionsRequestData): Validated[Seq[MtdError], CreateAndAmendOtherDeductionsRequestData] = {
        import parsed.body._
        List(
          validateBodyFieldFormat(seafarers),
          validateBodyDateRange(seafarers)
        ).traverse(identity)
          .map(_ => parsed)
      }

    }

  private def validateBodyFieldFormat(seafarers: Option[Seq[Seafarers]]): Validated[Seq[MtdError], Unit] = {
    val validationResult = seafarers
      .getOrElse(Seq.empty[Seafarers])
      .zipWithIndex
      .traverse_ { case (item, index) => validateSeafarers(item, index) }

    validationResult
  }

  private def validateBodyDateRange(seafarers: Option[Seq[Seafarers]]): Validated[Seq[MtdError], Unit] = {
    val validationResult = seafarers
      .getOrElse(Seq.empty[Seafarers])
      .zipWithIndex
      .traverse_ { case (item, index) => validateToDateBeforeFromDate(item, index) }

    validationResult
  }

  private def validateSeafarers(seafarers: Seafarers, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    (
      ValidateCustomerReference.validateOptional(field = seafarers.customerReference, path = s"/seafarers/$arrayIndex/customerReference"),
      validateAmountDeducted(value = seafarers.amountDeducted, path = s"/seafarers/$arrayIndex/amountDeducted"),
      validateNameOfShip(
        field = seafarers.nameOfShip,
        path = s"/seafarers/$arrayIndex/nameOfShip"
      ),
      ValidateDate.validateDateFormat(
        field = seafarers.fromDate,
        path = s"/seafarers/$arrayIndex/fromDate"
      ),
      ValidateDate.validateDateFormat(
        field = seafarers.toDate,
        path = s"/seafarers/$arrayIndex/toDate"
      )
    ).tupled
      .andThen { case (_, _, _, _, _) => Validated.Valid(()) }
  }

  object ValidateCustomerReference {
    val customerRefRegex: String = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$"

    def validateOptional(field: Option[String], path: String): Validated[Seq[MtdError], Unit] = {

      field match {
        case None        => Valid(())
        case Some(value) => validate(value, path)
      }
    }

    private def validate(customerRef: String, path: String): Validated[Seq[MtdError], Unit] = {
      if (customerRef.matches(customerRefRegex)) Valid(()) else Invalid(List(CustomerReferenceFormatError.copy(paths = Some(Seq(path)))))
    }

  }

  private val resolveAmountDeducted = ResolveParsedNumber()

  private def validateAmountDeducted(value: BigDecimal, path: String): Validated[Seq[MtdError], Unit] = {
    resolveAmountDeducted(value, path = Some(path)).map(_ => ())
  }

  private def validateNameOfShip(field: String, path: String): Validated[Seq[MtdError], Unit] = {
    if (field.length <= 105) Valid(()) else Invalid(List(NameOfShipFormatError.copy(paths = Some(Seq(path)))))
  }

  object ValidateDate {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def validateDateFormat(field: String, path: String): Validated[Seq[MtdError], Unit] = Try {
      LocalDate.parse(field, dateFormat)
    } match {
      case Success(_) => Valid(())
      case Failure(_) => Invalid(List(DateFormatError.copy(paths = Some(Seq(path)))))
    }

    def validateDateRange(from: String, to: String, fromPath: String, toPath: String): Validated[Seq[MtdError], Unit] = {
      val fromDate = LocalDate.parse(from, dateFormat)
      val toDate   = LocalDate.parse(to, dateFormat)

      if (toDate.isBefore(fromDate)) Invalid(List(RangeToDateBeforeFromDateError.copy(paths = Some(Seq(fromPath, toPath))))) else Valid(())
    }

  }

  private def validateToDateBeforeFromDate(seafarers: Seafarers, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    val validation = ValidateDate
      .validateDateRange(
        from = seafarers.fromDate,
        to = seafarers.toDate,
        fromPath = s"/seafarers/$arrayIndex/fromDate",
        toPath = s"/seafarers/$arrayIndex/toDate"
      )

    validation.fold(
      errors => Validated.invalid(errors.toList),
      _ => Validated.valid(())
    )
  }

}
