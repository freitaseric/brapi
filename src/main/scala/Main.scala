import adapters.inbound.CepEndpoint
import adapters.outbound.ViaCepClient
import domain.cep.CepService
import org.slf4j.LoggerFactory
import sttp.client4.httpclient.HttpClientFutureBackend
import sttp.tapir.server.netty.NettyFutureServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

@main def bootstrap(): Unit = {
  val logger = LoggerFactory.getLogger("brApi")

  val backend = HttpClientFutureBackend()

  val viaCep = ViaCepClient(backend)

  val cepService = CepService(List(viaCep))

  val cepEndpoint = CepEndpoint(cepService)

  val swaggerEndpoints = SwaggerInterpreter().fromServerEndpoints[Future](List(cepEndpoint.route), "brApi", "0.1.0-SNAPSHOT")

  val port = sys.env.getOrElse("PORT", "8080").toInt
  Await.result(NettyFutureServer().port(port).host("0.0.0.0").addEndpoints(List(cepEndpoint.route) ++ swaggerEndpoints).start(), Duration.Inf)
  logger.info(s"🚀 brApi running at http://localhost:$port/api")

  Await.result(Promise[Unit]().future, Duration.Inf)
}