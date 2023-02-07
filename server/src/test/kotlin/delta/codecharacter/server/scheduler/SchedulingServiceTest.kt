package delta.codecharacter.server.scheduler

import delta.codecharacter.server.schedulers.SchedulingService
import delta.codecharacter.server.user.public_user.PublicUserRepository
import delta.codecharacter.server.user.public_user.PublicUserService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

internal class SchedulingServiceTest {

    private lateinit var publicUserService: PublicUserService
    private lateinit var schedulingService: SchedulingService
    private lateinit var publicUserRepository: PublicUserRepository
    @BeforeEach
    fun setUp() {
        publicUserRepository = mockk(relaxed = true)
        publicUserService = PublicUserService(publicUserRepository)
        schedulingService = SchedulingService(publicUserService)
    }

    @Test
    fun `should promote user tiers`() {
        schedulingService.promoteAndDemoteUserTiers()
        await.atMost(Duration.ofSeconds(1)).untilAsserted {
            verify(atLeast = 1) { publicUserService.promoteTiers() }
        }
    }

    @Test
    fun `should update user tiers based on ratings`() {
        every { publicUserService.updateTierForUser() } just runs
        schedulingService.updateLeaderboard()
        await.atMost(Duration.ofSeconds(1)).untilAsserted {
            verify { publicUserService.updateTierForUser() }
        }
    }

    @Test
    fun `should reset ratings and update tiers after register phase`() {
        every { publicUserService.resetRatingsAfterPracticePhase() } just runs
        every { publicUserService.updateLeaderboardAfterPracticePhase() } just runs
        schedulingService.updateLeaderboardAfterPracticePhase()
        await.atMost(Duration.ofSeconds(1)).untilAsserted {
            verify { publicUserService.resetRatingsAfterPracticePhase() }
            verify { publicUserService.updateLeaderboardAfterPracticePhase() }
        }
    }
}
