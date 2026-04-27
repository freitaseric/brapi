package adapters.outbound

import domain.cep.{BrCep, BrCepDetailed}
import io.circe.Decoder
import ports.CepProvider
import sttp.client4.circe.asJson
import sttp.client4.{UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class ViaCepResponse(cep: String, logradouro: String, bairro: String, localidade: String, uf: String) derives Decoder

class ViaCepClient(backend: WebSocketBackend[Future]) extends CepProvider:
  override def search(cep: String): Future[BrCep] =
    basicRequest
      .get(uri"https://viacep.com.br/ws/$cep/json")
      .readTimeout(5.seconds)
      .response(asJson[ViaCepResponse])
      .send(backend)
      .flatMap { response =>
        response.body match {
          case Right(d) =>
            val details = BrCepDetailed(
              d.logradouro, None, None, d.bairro, d.localidade, d.uf, Some(d.uf), None, None
            )

            Future.successful(
              BrCep(
                d.cep.filter(_.isDigit),
                d.logradouro, d.bairro, d.localidade, d.uf,
                details
              )
            )
          case Left(_) => Future.failed(new Exception(s"ViaCEP não encontrou o CEP: $cep"))
        }
      }
