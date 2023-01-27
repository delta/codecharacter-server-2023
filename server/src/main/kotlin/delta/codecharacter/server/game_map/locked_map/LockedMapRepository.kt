package delta.codecharacter.server.game_map.locked_map

import delta.codecharacter.dtos.GameMapTypeDto
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/** Repository for [LockedMapEntity] */
@Repository
interface LockedMapRepository : MongoRepository<LockedMapEntity, UUID> {
    fun findByUserIdAndMapType(userId: UUID, mapType: GameMapTypeDto): Optional<LockedMapEntity>
}
