package delta.codecharacter.server.game_map.locked_map

import delta.codecharacter.dtos.GameMapTypeDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

/**
 * Locked map entity.
 *
 * @param userId
 * @param map
 */
@Document(collection = "locked_map")
data class LockedMapEntity(
    @Id val userId: UUID,
    val map: String,
    val mapImage: String,
    val mapType: GameMapTypeDto
)
