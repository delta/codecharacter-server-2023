package delta.codecharacter.server.daily_challenge.verdict

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.config.GameConfiguration
import delta.codecharacter.server.daily_challenge.DailyChallengeEntity
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.math.exp

@Service
class DailyChallengeScoreAlgorithm (
        @Autowired private val publicUserService: PublicUserService
) : DailyChallengeVerdictAlgorithm{
    @Value("\${environment.event-start-date}") val tempDate: String = ""

    override fun getHoursSinceDailyChallengeLaunched(): Double {
        val givenDateTime = Instant.parse(tempDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        return period.toHours().toDouble().rem(24)
    }

    override fun getPlayerBaseScore(coinsLeftPercent: Double, destructionPercent: Double, perfectBaseScore: Double, challType: ChallengeTypeDto): Double {
        if(challType==ChallengeTypeDto.CODE) return ((100.0-coinsLeftPercent) + (2*(100-destructionPercent)) + perfectBaseScore )
        return (coinsLeftPercent + (2*destructionPercent) + perfectBaseScore )
    }

    override fun getTimeScore(perfectTimeScore: Double): Double {
        val hours = getHoursSinceDailyChallengeLaunched()
        return perfectTimeScore* exp((-1)*(hours/15))
    }

    override fun getDailyChallengeVerdict(userId : UUID,playerCoinsUsed: Int, playerDestruction: Double,dailyChallenge: DailyChallengeEntity): DailyChallengeMatchVerdictEnum {
        val gameConfiguration = GameConfiguration()
        val totalCoins = gameConfiguration.gameParameters().numberOfCoins
        val (_,_,_,challType,_,_,perfectScore,numberOfCompletions) = dailyChallenge
        val perfectBasePart = 0.7*perfectScore*exp(((-1)*(numberOfCompletions.toDouble()/150)))
        val perfectTimePart = 0.3*perfectScore
        val coinsLeftPercentage = ((totalCoins - playerCoinsUsed.toDouble()) / totalCoins) * 100
        val playerScore : Double = ((getPlayerBaseScore(coinsLeftPercentage, playerDestruction, perfectBasePart, challType) + getTimeScore(perfectTimePart))*100.0).toInt()/100.0
        publicUserService.updateDailyChallengeScore(userId,playerScore)
        return DailyChallengeMatchVerdictEnum.SUCCESS
    }
}
