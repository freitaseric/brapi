package domain.cep

import ports.CepProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CepServiceSuite extends munit.FunSuite {

  private val sampleCep = BrCep(
    "01310100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP",
    BrCepDetailed(
      "Avenida Paulista", Some("de 612 a 1510 - lado par"),
      Some(""), "Bela Vista", "São Paulo", "SP",
      Some("São Paulo"), Some("Sudeste"), Some("3550308")
    )
  )

  private def successProvider(result: BrCep): CepProvider = _ => Future.successful(result)

  private def failingProvider: CepProvider = _ => Future.failed(new Exception("provider down"))

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

  test("returns Left when provider list is empty") {
    val service = CepService(List.empty)
    service.doSearch("01310100").map { result =>
      assert(result.isLeft)
    }
  }

  test("returns Left when all providers fail") {
    val service = CepService(List(failingProvider, failingProvider))
    service.doSearch("01310100").map { result =>
      assertEquals(result, Left("Todos os provedores caíram!"))
    }
  }

  test("returns Right when at least one provider succeeds") {
    val service = CepService(List(failingProvider, successProvider(sampleCep)))
    service.doSearch("01310100").map { result =>
      assertEquals(result, Right(sampleCep))
    }
  }
}
