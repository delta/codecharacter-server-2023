package delta.codecharacter.server.daily_challenge.verdict

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.server.daily_challenge.DailyChallengeEntity
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import java.util.UUID

interface DailyChallengeVerdictAlgorithm {
    fun getDailyChallengeVerdict (
        userId : UUID,
        playerCoinsUsed : Int,
        playerDestruction : Double,
        dailyChallenge : DailyChallengeEntity
    ): DailyChallengeMatchVerdictEnum

    fun getPlayerBaseScore(
        coinsLeftPercent : Double,
        destructionPercent : Double,
        perfectBaseScore : Double,
        challType : ChallengeTypeDto
    ):Double

    fun getHoursSinceDailyChallengeLaunched():Double

    fun getTimeScore(
        perfectTimeScore : Double,
    ):Double
}
