package delta.codecharacter.server.game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.code.LanguageEnum

data class GameCode(
   @field:JsonProperty("source_code", required = true) val code: String,
   @field:JsonProperty("language", required = true) val language: LanguageEnum)
