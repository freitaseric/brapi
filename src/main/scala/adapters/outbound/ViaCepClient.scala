package adapters.outbound

import domain.cep.BrCep
import io.circe.Decoder
import ports.CepProvider
import sttp.client4.circe.asJson
import sttp.client4.{UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ViaCepResponse(cep: String, logradouro: String, bairro: String, localidade: String, uf: String) derives Decoder

class ViaCepClient(backend: WebSocketBackend[Future]) extends CepProvider:
  override def search(cep: String): Future[BrCep] = {
    basicRequest.get(uri"https://viacep.com.br/ws/$cep/json").response(asJson[ViaCepResponse]).send(backend).flatMap { response =>
      response.body match {
        case Right(d) => Future.successful(BrCep(d.cep.filter(_.isDigit), d.logradouro, d.bairro, d.localidade, d.uf))
        case Left(_) => Future.failed(new Exception("ViaCEP falhou"))
      }
    }
  }
