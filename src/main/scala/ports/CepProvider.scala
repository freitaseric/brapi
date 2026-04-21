package ports

import domain.cep.BrCep
import scala.concurrent.Future

trait CepProvider:
  def search(cep: String): Future[BrCep]
