package delta.codecharacter.server.logic.daily_challenge_score

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.server.config.GameConfiguration
import delta.codecharacter.server.daily_challenge.DailyChallengeEntity
import org.springframework.beans.factory.annotation.Value
import java.time.Duration
import java.time.Instant
import kotlin.math.exp

class DailyChallengeScoreAlgorithm : ScoreAlgorithm {

    @Value("\${environment.event-start-date}") val tempDate: String = ""

    override fun getHoursSinceDailyChallengeLaunched(): Double {
        val givenDateTime = Instant.parse(tempDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        // we need hours in decimal format so convert to seconds then take into hours

        return (period.toSeconds().toDouble().rem(86400)) / 3600.00
    }

    override fun getPlayerBaseScore(
        coinsLeftPercent: Double,
        destructionPercent: Double,
        perfectBaseScore: Double,
        challType: ChallengeTypeDto
    ): Double {
        if (challType == ChallengeTypeDto.CODE)
            return ((100.0 - coinsLeftPercent) + (2 * (100 - destructionPercent)) + perfectBaseScore)
        return (coinsLeftPercent + (2 * destructionPercent) + perfectBaseScore)
    }

    override fun getTimeScore(perfectTimeScore: Double): Double {
        val hours = getHoursSinceDailyChallengeLaunched()
        return perfectTimeScore * exp((-1) * (hours / 15))
    }

    override fun getDailyChallengeScore(
        playerCoinsUsed: Int,
        playerDestruction: Double,
        dailyChallenge: DailyChallengeEntity
    ): Double {
        val gameConfiguration = GameConfiguration()
        val totalCoins = gameConfiguration.gameParameters().numberOfCoins
        val (_, _, _, challType, _, _, perfectScore, numberOfCompletions) = dailyChallenge
        val perfectBasePart = 0.7 * perfectScore * exp(((-1) * (numberOfCompletions.toDouble() / 150)))
        val perfectTimePart = 0.3 * perfectScore
        val coinsLeftPercentage = ((totalCoins - playerCoinsUsed.toDouble()) / totalCoins) * 100
        return (
            (
                getPlayerBaseScore(
                    coinsLeftPercentage, playerDestruction, perfectBasePart, challType
                ) +
                    getTimeScore(perfectTimePart)
                ) * 100.0
            )
            .toInt() / 100.0
    }
}
