package delta.codecharacter.server.schedulers

import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class LeaderboardScheduling(@Autowired private val publicUserService: PublicUserService) {
    @Value("\${environment.registration-time}") val registrationDate: String = ""
    @Value("\${environment.game-start-time}") val eventDate: String = ""

    @Scheduled(cron = "\${environment.registration-time}")
    fun updateTempLeaderboard() {
        publicUserService.updateTempLeaderboardByTier()
    }

    @Scheduled(cron = "\${environment.game-start-time}")
    fun updateLeaderboard() {
        publicUserService.updateLeaderboard()
    }
}
