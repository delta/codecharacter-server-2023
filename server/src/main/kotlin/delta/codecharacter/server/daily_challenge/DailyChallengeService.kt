package delta.codecharacter.server.daily_challenge

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.dtos.DailyChallengeObjectDto
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.logic.daily_challenge_score.DailyChallengeScoreAlgorithm
import delta.codecharacter.server.user.public_user.PublicUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
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
    @Value("\${environment.is-event-open}") private lateinit var isEventOpen: String
    private val logger: Logger = LoggerFactory.getLogger(PublicUserService::class.java)

    fun findNumberOfDays(): Int {
        val givenDateTime = Instant.parse(startDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        return period.toDays().toInt()
    }

    fun getDailyChallengeByDate(): DailyChallengeEntity {
        if (!isEventOpen.toBoolean()) {
            throw CustomException(HttpStatus.BAD_REQUEST, "Match phase has ended")
        }
        val currentDailyChallenge =
            dailyChallengeRepository.findByDay(findNumberOfDays()).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        return currentDailyChallenge
    }

    fun getDailyChallengeByDateForUser(
        userId: UUID,
        isFromMatch: Boolean
    ): DailyChallengeGetRequestDto {
        val user = publicUserService.getPublicUser(userId)
        val currentDailyChallenge = getDailyChallengeByDate()
        val codeChallString =
            "This is a blind code challenge, you have to build a map to counter this attack. Godspeed, commander.\n"
        val chall =
            if (currentDailyChallenge.challType == ChallengeTypeDto.CODE && !isFromMatch) {
                DailyChallengeObjectDto(
                    cpp = codeChallString, python = codeChallString, java = codeChallString
                )
            } else {
                currentDailyChallenge.chall
            }
        return DailyChallengeGetRequestDto(
            challName = currentDailyChallenge.challName,
            chall = chall,
            challType = currentDailyChallenge.challType,
            description = currentDailyChallenge.description,
            completionStatus = user.dailyChallengeHistory.containsKey(currentDailyChallenge.day)
        )
    }

    fun completeDailyChallenge(gameEntity: GameEntity, userId: UUID): DailyChallengeMatchVerdictEnum {
        val (_, coinsUsed, destruction, _, _) = gameEntity
        if (gameEntity.status == GameStatusEnum.EXECUTE_ERROR)
            return DailyChallengeMatchVerdictEnum.FAILURE
        val currentDailyChallenge = getDailyChallengeByDate()
        if ((
            currentDailyChallenge.challType == ChallengeTypeDto.MAP &&
                destruction > currentDailyChallenge.toleratedDestruction
            ) ||
            (
                currentDailyChallenge.challType == ChallengeTypeDto.CODE &&
                    destruction < currentDailyChallenge.toleratedDestruction
                )
        ) {
            val user = publicUserService.getPublicUser(userId)
            if (user.dailyChallengeHistory.containsKey(currentDailyChallenge.day)) {
                return DailyChallengeMatchVerdictEnum.FAILURE
            }
            val score =
                dailyChallengeScoreAlgorithm.getDailyChallengeScore(
                    playerCoinsUsed = coinsUsed,
                    playerDestruction = destruction,
                    dailyChallenge = currentDailyChallenge,
                )
            val updatedDailyChallenge =
                currentDailyChallenge.copy(
                    numberOfCompletions = currentDailyChallenge.numberOfCompletions + 1
                )
            dailyChallengeRepository.save(updatedDailyChallenge)
            publicUserService.updateDailyChallengeScore(userId, score, currentDailyChallenge)
            return DailyChallengeMatchVerdictEnum.SUCCESS
        }
        return DailyChallengeMatchVerdictEnum.FAILURE
    }
}
