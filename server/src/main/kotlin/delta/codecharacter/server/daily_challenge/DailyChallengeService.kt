package delta.codecharacter.server.daily_challenge

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.daily_challenge.verdict.DailyChallengeScoreAlgorithm
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
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

    @Value("\${environment.event-start-date}") val tempDate: String = ""

    fun findNumberOfDays(): Int {
        val givenDateTime = Instant.parse(tempDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        return period.toDays().toInt()+1
    }

    fun getDailyChallengeByDate(): DailyChallengeEntity {
        val dc =
                dailyChallengeRepository.findByDay(findNumberOfDays()).orElseThrow {
                    throw CustomException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid Request")
                }
        return dc
    }

    fun getDailyChallengeByDateForUser(userId : UUID): DailyChallengeGetRequestDto {
        val user = publicUserService.getPublicUser(userId)
        val dc = getDailyChallengeByDate()
        return DailyChallengeGetRequestDto(
            challName = dc.challName,
            chall = dc.chall,
            challType = dc.challType,
            description = dc.description,
            completionStatus = user.isDailyChallengeCompleted
        )
    }

    fun completeDailyChallenge(gameEntity: GameEntity, userId:UUID) : DailyChallengeMatchVerdictEnum {
        val (_, coinsUsed, destruction,_,_) = gameEntity
            val dc = dailyChallengeRepository.findByDay(findNumberOfDays()).get()
            if(gameEntity.status==GameStatusEnum.EXECUTE_ERROR) return DailyChallengeMatchVerdictEnum.FAILURE
            if((dc.challType==ChallengeTypeDto.MAP && destruction>dc.toleratedDestruction) ||
                    (dc.challType==ChallengeTypeDto.CODE && destruction<dc.toleratedDestruction)  ) {
                val verdict = dailyChallengeScoreAlgorithm.getDailyChallengeVerdict(
                        userId,
                        playerCoinsUsed = coinsUsed,
                        playerDestruction = destruction,
                        dailyChallenge = dc,
                )
                if(verdict==DailyChallengeMatchVerdictEnum.SUCCESS){
                    println("Initially")
                    println(dc.numberOfCompletions)
                    val updatedDc = dc.copy(numberOfCompletions = dc.numberOfCompletions+1)
                    println("Updated")
                    println(updatedDc.numberOfCompletions)
                    dailyChallengeRepository.save(updatedDc)
                }
                return verdict
            }
            return DailyChallengeMatchVerdictEnum.FAILURE


    /*
     * if destruction >= 60 --> completed --> usr can not play
     * else not-completed user can play
     *
     * score = base-score + (value depending on coinsUsed and destruction %)
     * base-score will get reduced as player starts solving challenge
     * base-score = base-score - rv (rv-> reducing value) (like CTF style)
     *
     * Corresponding LeaderBoard updates
     *
     * Scheduling for isDailyChallengeComplete -> false by 24hrs
     */
    }
}
