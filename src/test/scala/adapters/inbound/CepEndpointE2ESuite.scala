package adapters.inbound

import adapters.outbound.ViaCepClient
import domain.cep.{BrCep, CepService}
import io.circe.parser.decode
import sttp.client4.httpclient.HttpClientFutureBackend
import sttp.client4.{UriContext, basicRequest}
import sttp.tapir.server.netty.NettyFutureServerBinding

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import sttp.tapir.server.netty.NettyFutureServer

class CepEndpointE2ESuite extends munit.FunSuite {

  private val httpClient = HttpClientFutureBackend()
  private var binding: Option[NettyFutureServerBinding] = None

  override def beforeAll(): Unit = {
    val sttpBackend = HttpClientFutureBackend()
    val service     = CepService(List(ViaCepClient(sttpBackend)))
    val endpoint    = CepEndpoint(service)
    binding = Some(Await.result(
      NettyFutureServer().port(0).host("127.0.0.1").addEndpoints(List(endpoint.route)).start(),
      Duration(10, "seconds")
    ))
  }

  override def afterAll(): Unit = {
    Await.result(httpClient.close(), Duration(10, "seconds"))
    binding.foreach(b => Await.result(b.stop(), Duration(30, "seconds")))
  }

  private def baseUrl = s"http://127.0.0.1:${binding.get.port}"

  test("GET /api/cep/:cep returns 200 with normalized address for a valid CEP") {
    basicRequest.get(uri"$baseUrl/api/cep/01310100").send(httpClient).map { response =>
      assertEquals(response.code.code, 200)
      val json = response.body.getOrElse(fail("empty body"))
      val cep  = decode[BrCep](json).getOrElse(fail(s"invalid JSON: $json"))
      assertEquals(cep.cep, "01310100")
      assertEquals(cep.estado, "SP")
      assert(cep.rua.nonEmpty)
      assert(cep.cidade.nonEmpty)
    }
  }

  test("GET /api/cep/:cep returns non-200 for an unknown CEP") {
    basicRequest.get(uri"$baseUrl/api/cep/00000000").send(httpClient).map { response =>
      assertNotEquals(response.code.code, 200)
    }
  }

  test("GET /api/cep/:cep returns non-200 for invalid CEP format") {
    val invalidCeps = List("123", "abc12345", "0131010")
    Future.sequence(
      invalidCeps.map { cep =>
        basicRequest.get(uri"$baseUrl/api/cep/$cep").send(httpClient).map { response =>
          assertNotEquals(response.code.code, 200, s"CEP '$cep' deveria retornar erro")
        }
      }
    ).map(_ => ())
  }
}
