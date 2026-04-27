package domain.cep

import io.circe.parser.decode
import io.circe.syntax.*

class BrCepSuite extends munit.FunSuite {

  private val sample = BrCep(
    "01310100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP",
    BrCepDetailed(
      "Avenida Paulista", Some("de 612 a 1510 - lado par"),
      Some(""), "Bela Vista", "São Paulo", "SP",
      Some("São Paulo"), Some("Sudeste"), Some("3550308")
    )
  )

  test("encodes to JSON with expected fields") {
    val json = sample.asJson
    assertEquals((json \\ "cep").headOption.flatMap(_.asString), Some("01310100"))
    assertEquals((json \\ "rua").headOption.flatMap(_.asString), Some("Avenida Paulista"))
    assertEquals((json \\ "bairro").headOption.flatMap(_.asString), Some("Bela Vista"))
    assertEquals((json \\ "cidade").headOption.flatMap(_.asString), Some("São Paulo"))
    assertEquals((json \\ "estado").headOption.flatMap(_.asString), Some("SP"))
  }

  test("decodes from valid JSON") {
    val json = """{"cep":"01310100","rua":"Avenida Paulista","bairro":"Bela Vista","cidade":"São Paulo","estado":"SP"}"""
    assertEquals(decode[BrCep](json), Right(sample))
  }

  test("roundtrip encode then decode") {
    val encoded = sample.asJson.noSpaces
    assertEquals(decode[BrCep](encoded), Right(sample))
  }

  test("fails to decode when required field is missing") {
    val json = """{"cep":"01310100","rua":"Avenida Paulista","bairro":"Bela Vista","cidade":"São Paulo"}"""
    assert(decode[BrCep](json).isLeft)
  }
}
