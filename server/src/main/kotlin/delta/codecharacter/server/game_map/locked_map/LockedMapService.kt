package delta.codecharacter.server.game_map.locked_map

import delta.codecharacter.dtos.GameMapTypeDto
import delta.codecharacter.dtos.UpdateLatestMapRequestDto
import delta.codecharacter.server.config.DefaultCodeMapConfiguration
import delta.codecharacter.server.logic.validation.MapValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/** Service for locked map. */
@Service
class LockedMapService(
    @Autowired private val lockedMapRepository: LockedMapRepository,
    @Autowired private val defaultCodeMapConfiguration: DefaultCodeMapConfiguration,
    @Autowired private val mapValidator: MapValidator,
) {

    fun getLockedMap(userId: UUID, mapType: GameMapTypeDto? = GameMapTypeDto.NORMAL): String {
        val lockedMap =
            lockedMapRepository
                .findByUserIdAndMapType(userId, mapType = mapType ?: GameMapTypeDto.NORMAL)
                .orElse(
                    LockedMapEntity(
                        userId, defaultCodeMapConfiguration.defaultMap, "", GameMapTypeDto.NORMAL
                    )
                )
        return lockedMap.map
    }

    fun updateLockedMap(userId: UUID, updateLatestMapRequestDto: UpdateLatestMapRequestDto) {
        mapValidator.validateMap(updateLatestMapRequestDto.map)
        lockedMapRepository.save(
            LockedMapEntity(
                map = updateLatestMapRequestDto.map,
                userId = userId,
                mapImage = updateLatestMapRequestDto.mapImage,
                mapType = updateLatestMapRequestDto.mapType ?: GameMapTypeDto.NORMAL
            )
        )
    }
}
