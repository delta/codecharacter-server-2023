package delta.codecharacter.server.match

import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MatchRepository : MongoRepository<MatchEntity, UUID> {
    fun findTop10ByOrderByTotalPointsDesc(): List<MatchEntity>
    fun findTop50ByPlayer1OrderByCreatedAtDesc(player1: PublicUserEntity): List<MatchEntity>
    fun findTop10ByPlayer2AndModeOrderByCreatedAtDesc(
        player2: PublicUserEntity,
        mode: MatchModeEnum
    ): List<MatchEntity>
    fun findByIdIn(matchIds: List<UUID>): List<MatchEntity>
}
