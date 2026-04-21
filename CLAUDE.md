# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
sbt compile          # compile
sbt run              # run the server (defaults to port 8080)
sbt test             # run all tests
sbt "testOnly *Suite" # run a single test suite
sbt assembly         # build fat JAR → target/scala-3.x/brapi-0.1.0-SNAPSHOT.jar
```

Port is configurable via `PORT` env var: `PORT=9090 sbt run`.

## Architecture

This is a **Ports and Adapters (Hexagonal)** REST API in Scala 3, serving Brazilian CEP (postal code) lookups.

```
src/main/scala/
  Main.scala                         # entry point — wires dependencies, starts Netty/Tapir server
  ports/
    CepProvider.scala                # inbound port trait: search(cep) => Future[BrCep]
  domain/cep/
    BrCep.scala                      # domain model (normalized address, Circe codecs derived)
    CepService.scala                 # domain logic: scatter-gather across providers
  adapters/
    inbound/CepEndpoint.scala        # Tapir HTTP endpoint (GET /api/cep/:cep)
    outbound/ViaCepClient.scala      # ViaCEP external API adapter implementing CepProvider
```

**Data flow:** `CepEndpoint` → `CepService` → `List[CepProvider]` (currently only `ViaCepClient`).

**Scatter-gather pattern in `CepService`:** fires all providers concurrently, resolves with the first success. If all providers fail, returns `Left("Todos os provedores caíram!")`. This is the foundation for adding multiple CEP providers.

## Key Libraries

| Library | Purpose |
|---|---|
| Tapir 1.x | HTTP endpoint definition + Netty server + Swagger UI |
| sttp client4 | HTTP client for outbound calls |
| Circe | JSON encoding/decoding (derives via `derives Encoder, Decoder`) |
| munit | Test framework |
| sbt-assembly | Fat JAR packaging |

## Adding a New CEP Provider

1. Create a class in `adapters/outbound/` that extends `ports.CepProvider`
2. Inject it into `CepService(List(...))` in `Main.scala`

The scatter-gather logic in `CepService` picks it up automatically.

## Roadmap Notes

- Concurrent multi-provider scatter-gather is partially implemented (the `CepService` logic exists; only one provider is wired).
- Future: migrate `Future`-based concurrency to ZIO.
