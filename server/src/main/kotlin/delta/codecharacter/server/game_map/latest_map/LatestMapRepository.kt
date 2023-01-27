package delta.codecharacter.server.game_map.latest_map

import delta.codecharacter.dtos.GameMapTypeDto
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/** Repository for [LatestMapEntity] */
@Repository
interface LatestMapRepository : MongoRepository<LatestMapEntity, UUID> {
    fun findByUserIdAndMapType(userId: UUID, mapType: GameMapTypeDto): Optional<LatestMapEntity>
}
