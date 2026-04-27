package domain.cep

import ports.CepProvider

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class CepService(providers: List[CepProvider]) {
  def doSearch(cep: String): Future[Either[String, BrCep]] =
    if providers.isEmpty then
      Future.successful(Left("Nenhum provedor configurado"))
    else
      val attempts = providers.map(_.search(cep).map(Right(_)).recover { case _ => Left("") })
      Future.firstCompletedOf(attempts).flatMap {
        case r@Right(_) => Future.successful(r)
        case Left(_) =>
          Future.sequence(attempts).map { results =>
            results.collectFirst { case r@Right(_) => r }.getOrElse(Left("Todos os provedores caíram!"))
          }
      }
}
