package delta.codecharacter.server.schedulers

import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulingService(@Autowired private val publicUserService: PublicUserService) {

    @Scheduled(cron = "\${environment.registration-time}", zone = "GMT")
    fun updateTempLeaderboard() {
        publicUserService.resetRatingsAfterPracticePhase()
        publicUserService.updateLeaderboardAfterPracticePhase()
    }

    @Scheduled(cron = "\${environment.game-start-time}", zone = "GMT")
    fun updateLeaderboard() {
        publicUserService.updateTierForUser()
    }

    @Scheduled(cron = "\${environment.update-time}", zone = "GMT")
    fun promoteAndDemoteUserTiers() {
        publicUserService.promoteTiers()
    }
}
