package domain.cep

import ports.CepProvider

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class CepService(providers: List[CepProvider]) {
  def doSearch(cep: String): Future[Either[String, BrCep]] = {
    val result = Promise[Either[String, BrCep]]()
    val failureCount = AtomicInteger(0)

    providers.foreach { provider =>
      provider.search(cep).onComplete {
        case Success(value) => result.trySuccess(Right(value))
        case Failure(_) =>
          if (failureCount.incrementAndGet() == providers.size)
            result.trySuccess(Left("Todos os provedores caíram!"))
      }
    }

    result.future
  }
}
