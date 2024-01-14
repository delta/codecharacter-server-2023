package delta.codecharacter.server.game_map.map_revision

import delta.codecharacter.dtos.CreateMapRevisionRequestDto
import delta.codecharacter.dtos.GameMapRevisionDto
import delta.codecharacter.dtos.GameMapTypeDto
import delta.codecharacter.dtos.MapCommitByCommitIdResponseDto
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.logic.validation.MapValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/** Service for map revision. */
@Service
class MapRevisionService(
    @Autowired private val mapRevisionRepository: MapRevisionRepository,
    @Autowired private val mapValidator: MapValidator,
) {
    @Value("\${environment.is-event-open}") private val isEventOpen = true
    fun createMapRevision(userId: UUID, createMapRevisionRequestDto: CreateMapRevisionRequestDto) {
        if (!isEventOpen) {
            throw CustomException(HttpStatus.BAD_REQUEST, "Match phase has ended")
        }
        val (map, _, message, mapType) = createMapRevisionRequestDto
        mapValidator.validateMap(map)
        val parentCodeRevision =
            mapRevisionRepository
                .findFirstByUserIdAndMapTypeOrderByCreatedAtDesc(
                    userId, mapType ?: GameMapTypeDto.NORMAL
                )
                .orElse(null)
        mapRevisionRepository.save(
            MapRevisionEntity(
                id = UUID.randomUUID(),
                map = map,
                mapType = mapType ?: GameMapTypeDto.NORMAL,
                mapImage = createMapRevisionRequestDto.mapImage,
                message = message,
                userId = userId,
                parentRevision = parentCodeRevision,
                createdAt = Instant.now()
            )
        )
    }

    fun resetMapRevisionAfterPracticePhase() {
        mapRevisionRepository.deleteAll()
    }

    fun getMapRevisions(
        userId: UUID,
        mapType: GameMapTypeDto? = GameMapTypeDto.NORMAL
    ): List<GameMapRevisionDto> {
        return mapRevisionRepository.findAllByUserIdAndMapTypeOrderByCreatedAtDesc(
            userId, mapType ?: GameMapTypeDto.NORMAL
        )
            .map {
                GameMapRevisionDto(
                    id = it.id,
                    map = it.map,
                    message = it.message,
                    parentRevision = it.parentRevision?.id,
                    createdAt = it.createdAt
                )
            }
    }

    fun getMapRevisionByCommitId(userId: UUID, commitId: UUID): MapCommitByCommitIdResponseDto {
        val map: MapCommitByCommitIdResponseDto =
            mapRevisionRepository
                .findByUserIdAndId(userId, commitId)
                .orElseThrow { throw CustomException(HttpStatus.BAD_REQUEST, "User not found") }
                .let { mapRevisionEntity ->
                    MapCommitByCommitIdResponseDto(
                        map = mapRevisionEntity.map, mapImage = mapRevisionEntity.mapImage
                    )
                }
        return map
    }
}
