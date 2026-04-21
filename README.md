# brApi

API REST para consulta de CEPs brasileiros, construída em Scala 3 com arquitetura Ports and Adapters (Hexagonal).

## Funcionalidades

- Busca de endereço por CEP com resposta normalizada
- Padrão Scatter-Gather: consulta múltiplos provedores em paralelo e retorna o primeiro que responder com sucesso
- Documentação interativa via Swagger UI em `/docs`
- Pronto para containerização e deploy no Render

## Tecnologias

| Biblioteca   | Função                                       |
|--------------|----------------------------------------------|
| Scala 3      | Linguagem principal                          |
| Tapir 1.x    | Definição de endpoints HTTP + servidor Netty |
| sttp client4 | Cliente HTTP para chamadas externas          |
| Circe        | Serialização/deserialização JSON             |
| munit        | Framework de testes                          |
| sbt-assembly | Empacotamento em fat JAR                     |

## Executando localmente

**Pré-requisitos:** JDK 21+ e sbt instalados.

```bash
# compilar
sbt compile

# rodar o servidor (porta padrão: 8080)
sbt run

# rodar em outra porta
PORT=9090 sbt run

# rodar os testes
sbt test

# gerar fat JAR
sbt assembly
```

A API ficará disponível em `http://localhost:8080` e a documentação Swagger em `http://localhost:8080/docs`.

## Executando com Docker

```bash
docker build -t brapi .
docker run -p 8080:8080 brapi
```

## Endpoints

### `GET /api/cep/:cep`

Busca um endereço pelo CEP informado.

**Exemplo de requisição:**

```
GET /api/cep/01310100
```

**Exemplo de resposta (`200 OK`):**

```json
{
  "cep": "01310100",
  "rua": "Avenida Paulista",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "estado": "SP"
}
```

**Resposta de erro (`400` / `500`):**

```
Todos os provedores caíram!
```

## Arquitetura

```
src/main/scala/
  Main.scala                             # ponto de entrada — wiring e inicialização do servidor
  ports/
    CepProvider.scala                    # porta de saída: contrato dos provedores de CEP
  domain/cep/
    BrCep.scala                          # modelo de domínio com codecs Circe derivados
    CepService.scala                     # lógica de domínio: scatter-gather entre provedores
  adapters/
    inbound/CepEndpoint.scala            # endpoint Tapir (GET /api/cep/:cep)
    outbound/ViaCepClient.scala          # adaptador do provedor ViaCEP
```

**Fluxo de dados:** `CepEndpoint` → `CepService` → `List[CepProvider]` (atualmente apenas `ViaCepClient`).

### Adicionando um novo provedor de CEP

1. Crie uma classe em `adapters/outbound/` que implemente `ports.CepProvider`
2. Injete-a na lista em `Main.scala`: `CepService(List(viaCep, novoProvedor))`

O scatter-gather do `CepService` passa a usá-la automaticamente.

## Roadmap

Veja [ROADMAP.md](ROADMAP.md) para os próximos passos planejados.
