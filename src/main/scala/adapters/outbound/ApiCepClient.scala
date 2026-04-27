package adapters.outbound

import domain.cep.{BrCep, BrCepDetailed}
import io.circe.Decoder
import ports.CepProvider
import sttp.client4.circe.asJson
import sttp.client4.{UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class ApiCepResponse(code: String, state: String, city: String, district: String, address: String) derives Decoder

class ApiCepClient(backend: WebSocketBackend[Future]) extends CepProvider {
  override def search(cep: String): Future[BrCep] =
    basicRequest
      .get(uri"https://cdn.apicep.com/file/apicep/$cep.json")
      .readTimeout(5.seconds)
      .response(asJson[ApiCepResponse])
      .send(backend)
      .flatMap { response =>
        response.body match {
          case Right(d) =>
            val details = BrCepDetailed(
              d.address, None, None, d.district, d.city, d.state, Some(d.state), None, None
            )

            Future.successful(
              BrCep(
                d.code.filter(_.isDigit),
                d.address, d.district, d.city, d.state,
                details, "ApiCEP"
              )
            )
          case Left(_) => Future.failed(new Exception(s"ApiCep não encontrou o CEP: $cep"))
        }
      }
}
