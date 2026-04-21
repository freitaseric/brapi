package domain.cep

import io.circe.{Decoder, Encoder}

case class BrCep(cep: String, rua: String, bairro: String, cidade: String, estado: String) derives Encoder, Decoder
