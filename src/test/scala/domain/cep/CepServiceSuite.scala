package domain.cep

import ports.CepProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CepServiceSuite extends munit.FunSuite {

  private def successProvider(result: BrCep): CepProvider = _ => Future.successful(result)
  private def failingProvider: CepProvider = _ => Future.failed(new Exception("provider down"))

  private val sampleCep = BrCep("01310100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP")

  test("returns Right with address when single provider succeeds") {
    val service = CepService(List(successProvider(sampleCep)))
    service.doSearch("01310100").map { result =>
      assertEquals(result, Right(sampleCep))
    }
  }

  test("returns Left when single provider fails") {
    val service = CepService(List(failingProvider))
    service.doSearch("00000000").map { result =>
      assertEquals(result, Left("Todos os provedores caíram!"))
    }
  }

  test("returns Right when first of two providers succeeds") {
    val service = CepService(List(successProvider(sampleCep), failingProvider))
    service.doSearch("01310100").map { result =>
      assertEquals(result, Right(sampleCep))
    }
  }

  test("returns Right when second provider succeeds and first fails") {
    val service = CepService(List(failingProvider, successProvider(sampleCep)))
    service.doSearch("01310100").map { result =>
      assertEquals(result, Right(sampleCep))
    }
  }

  test("returns Left when all providers fail") {
    val service = CepService(List(failingProvider, failingProvider))
    service.doSearch("00000000").map { result =>
      assertEquals(result, Left("Todos os provedores caíram!"))
    }
  }
}
