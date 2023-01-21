package delta.codecharacter.server.daily_challenge.match

import delta.codecharacter.server.game.GameEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import java.time.Instant
import java.util.UUID

@Document(collation = "daily_challenge_match")

data class DailyChallengeMatchEntity(
    @Id val id : UUID,
    @DocumentReference(lazy = true) val game : GameEntity,
    val verdict: DailyChallengeMatchVerdictEnum,
    val createdAt:Instant
)
