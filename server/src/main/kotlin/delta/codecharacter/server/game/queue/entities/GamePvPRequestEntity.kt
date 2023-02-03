package delta.codecharacter.server.game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.code.Code
import delta.codecharacter.server.params.GameParameters
import java.util.UUID

data class GamePvPRequestEntity(
    @field:JsonProperty("game_id", required = true) val gameId: UUID,
    @field:JsonProperty("playerUser", required = true) val playerUser: Code,
    @field:JsonProperty("playerOpponent", required = true) val playerOpponent: Code,
    @field:JsonProperty("parameters", required = true) val parameters: GameParameters,
)
