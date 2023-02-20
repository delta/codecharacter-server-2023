package delta.codecharacter.server.game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.params.GameParameters
import java.util.UUID

data class GamePvPRequestEntity(
    @field:JsonProperty("game_id", required = true) val gameId: UUID,
    @field:JsonProperty("player1", required = true) val playerUser: GameCode,
    @field:JsonProperty("player2", required = true) val playerOpponent: GameCode,
    @field:JsonProperty("parameters", required = true) val parameters: GameParameters,
)
