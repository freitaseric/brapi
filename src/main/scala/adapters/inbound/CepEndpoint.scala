package adapters.inbound

import domain.cep.{BrCep, CepService}
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

class CepEndpoint(service: CepService):
  private val endpointDefinition = endpoint.name("Buscar CEP").description("Busca, concorrentemente, em diversos provedores de CEP e padroniza a saída em formato JSON.").get.in("api" / "cep" / path[String]("cep")).out(jsonBody[BrCep]).errorOut(stringBody)
  val route: ServerEndpoint.Full[Unit, Unit, String, String, BrCep, Any, Future] = endpointDefinition.serverLogic[Future](cep =>
    validate(cep) match
      case Right(valid) => service.doSearch(valid)
      case Left(err) => Future.successful(Left(err))
  )

  private def validate(cep: String): Either[String, String] =
    if cep.matches("^\\d{8}$") then return Right(cep)
    else return Left("CEP inválido: informe exatamente 8 dígitos numéricos")
