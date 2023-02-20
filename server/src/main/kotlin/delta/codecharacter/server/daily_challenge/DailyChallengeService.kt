package delta.codecharacter.server.daily_challenge

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.logic.daily_challenge_score.DailyChallengeScoreAlgorithm
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class DailyChallengeService(
    @Autowired private val dailyChallengeRepository: DailyChallengeRepository,
    @Autowired private val publicUserService: PublicUserService,
    @Autowired private val dailyChallengeScoreAlgorithm: DailyChallengeScoreAlgorithm
) {

    @Value("\${environment.event-start-date}") private lateinit var startDate: String

    fun findNumberOfDays(): Int {
        val givenDateTime = Instant.parse(startDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        return period.toDays().toInt()
    }

    fun getDailyChallengeByDate(): DailyChallengeEntity {
        val dc =
            dailyChallengeRepository.findByDay(findNumberOfDays()).orElseThrow {
                throw CustomException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        return dc
    }

    fun getDailyChallengeByDateForUser(userId: UUID): DailyChallengeGetRequestDto {
        val user = publicUserService.getPublicUser(userId)
        val dc = getDailyChallengeByDate()
        return DailyChallengeGetRequestDto(
            challName = dc.challName,
            chall = dc.chall,
            challType = dc.challType,
            description = dc.description,
            completionStatus = user.dailyChallengeHistory.containsKey(dc.day)
        )
    }

    fun completeDailyChallenge(gameEntity: GameEntity, userId: UUID): DailyChallengeMatchVerdictEnum {
        val (_, coinsUsed, destruction, _, _) = gameEntity
        if (gameEntity.status == GameStatusEnum.EXECUTE_ERROR)
            return DailyChallengeMatchVerdictEnum.FAILIURE
        val dc = getDailyChallengeByDate()
        if ((dc.challType == ChallengeTypeDto.MAP && destruction > dc.toleratedDestruction) ||
            (dc.challType == ChallengeTypeDto.CODE && destruction < dc.toleratedDestruction)
        ) {
            val score =
                dailyChallengeScoreAlgorithm.getDailyChallengeScore(
                    playerCoinsUsed = coinsUsed,
                    playerDestruction = destruction,
                    dailyChallenge = dc,
                )
            val updatedDc = dc.copy(numberOfCompletions = dc.numberOfCompletions + 1)
            dailyChallengeRepository.save(updatedDc)
            publicUserService.updateDailyChallengeScore(userId, score, dc)
            return DailyChallengeMatchVerdictEnum.SUCCESS
        }
        return DailyChallengeMatchVerdictEnum.FAILIURE
    }
}
