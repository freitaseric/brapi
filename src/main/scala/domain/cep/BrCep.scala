package domain.cep

import io.circe.{Decoder, Encoder}

case class BrCepDetailed(logradouro: String, complemento: Option[String], unidade: Option[String], bairro: String, cidade: String, uf: String, estado: Option[String], regiao: Option[String], ibge: Option[String]) derives Encoder, Decoder

case class BrCep(cep: String, rua: String, bairro: String, cidade: String, estado: String, detalhes: BrCepDetailed, provedor: String) derives Encoder, Decoder
