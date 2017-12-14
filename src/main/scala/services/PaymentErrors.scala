package services

import akka.http.scaladsl.model.StatusCodes

object PaymentErrors {
  val clientErrors = List(
    StatusCodes.BadRequest,
    StatusCodes.Unauthorized,
    StatusCodes.PaymentRequired,
    StatusCodes.Forbidden,
    StatusCodes.NotFound,
    StatusCodes.MethodNotAllowed,
    StatusCodes.NotAcceptable,
    StatusCodes.ProxyAuthenticationRequired,
    StatusCodes.RequestTimeout,
    StatusCodes.Conflict,
    StatusCodes.Gone,
    StatusCodes.LengthRequired,
    StatusCodes.PreconditionFailed,
    StatusCodes.RequestEntityTooLarge,
    StatusCodes.RequestUriTooLong,
    StatusCodes.UnsupportedMediaType,
    StatusCodes.RequestedRangeNotSatisfiable,
    StatusCodes.ExpectationFailed,
    StatusCodes.UnprocessableEntity,
    StatusCodes.Locked,
    StatusCodes.FailedDependency,
    StatusCodes.UpgradeRequired,
    StatusCodes.PreconditionRequired,
    StatusCodes.TooManyRequests,
    StatusCodes.RequestHeaderFieldsTooLarge,
    StatusCodes.UnavailableForLegalReasons
  )

  val serverErrors = List(
    StatusCodes.InternalServerError,
    StatusCodes.NotImplemented,
    StatusCodes.BadGateway,
    StatusCodes.ServiceUnavailable,
    StatusCodes.GatewayTimeout,
    StatusCodes.HTTPVersionNotSupported,
    StatusCodes.VariantAlsoNegotiates,
    StatusCodes.InsufficientStorage,
    StatusCodes.NotExtended,
    StatusCodes.NetworkAuthenticationRequired
  )
}

