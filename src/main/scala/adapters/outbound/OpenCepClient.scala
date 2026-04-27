package adapters.outbound

import domain.cep.{BrCep, BrCepDetailed}
import io.circe.Decoder
import ports.CepProvider
import sttp.client4.circe.asJson
import sttp.client4.{UriContext, WebSocketBackend, basicRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class OpenCepResponse(cep: String, logradouro: String, complemento: String, unidade: Option[String], bairro: String, localidade: String, uf: String, estado: Option[String], regiao: Option[String], ibge: String) derives Decoder

class OpenCepClient(backend: WebSocketBackend[Future]) extends CepProvider:
  override def search(cep: String): Future[BrCep] =
    basicRequest.get(uri"https://opencep.com/v1/$cep.json").response(asJson[OpenCepResponse]).send(backend).flatMap { response =>
      response.body match {
        case Right(d) =>
          val details = BrCepDetailed(
            d.logradouro, Option(d.complemento).filterNot(_.isBlank), d.unidade.filterNot(_.isBlank), d.bairro, d.localidade, d.uf, d.estado.filterNot(_.isBlank), d.regiao.filterNot(_.isBlank), Option(d.ibge).filterNot(_.isBlank)
          )

          Future.successful(
            BrCep(
              d.cep.filter(_.isDigit),
              d.logradouro, d.bairro, d.localidade, d.uf,
              details
            )
          )

        case Left(_) => Future.failed(new Exception("OpenCEP falhou"))
      }
    }
