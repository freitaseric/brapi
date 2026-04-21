package adapters.inbound

import domain.cep.{BrCep, CepService}
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

class CepEndpoint(service: CepService):
  private val endpointDefinition = endpoint.name("Buscar CEP").get.in("api" / "cep" / path[String]("cep")).out(jsonBody[BrCep]).errorOut(stringBody)
  val route: ServerEndpoint[Any, Future] = endpointDefinition.serverLogic[Future](cep => service.doSearch(cep))
