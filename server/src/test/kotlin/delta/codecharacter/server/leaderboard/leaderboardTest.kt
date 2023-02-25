package delta.codecharacter.server.leaderboard

import delta.codecharacter.dtos.TierTypeDto
import delta.codecharacter.server.user.public_user.PublicUserEntity
import delta.codecharacter.server.user.public_user.PublicUserRepository
import delta.codecharacter.server.user.public_user.PublicUserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class leaderboardTest {

    private lateinit var publicUserRepository: PublicUserRepository
    private lateinit var publicUserService: PublicUserService
    private lateinit var publicUserEntity: PublicUserEntity

    @BeforeEach
    fun setUp() {
        publicUserRepository = mockk(relaxed = true)
        publicUserService = PublicUserService(publicUserRepository)
        publicUserEntity = mockk(relaxed = true)
    }
    @Test
    fun `should get leaderboard by tiers`() {
        val user1 =
            PublicUserEntity(
                userId = UUID.randomUUID(),
                username = "testUser",
                name = "test user",
                country = "In",
                college = "college",
                avatarId = 1,
                tier = TierTypeDto.TIER1,
                tutorialLevel = 1,
                rating = 2000.0,
                wins = 0,
                losses = 0,
                ties = 0,
                isActivated = true,
                score = 0.0,
                isDailyChallengeCompleted = false
            )
        val user2 =
            PublicUserEntity(
                userId = UUID.randomUUID(),
                username = "testUser",
                name = "test user",
                country = "In",
                college = "college",
                avatarId = 1,
                tier = TierTypeDto.TIER1,
                tutorialLevel = 1,
                rating = 1800.0,
                wins = 0,
                losses = 0,
                ties = 0,
                isActivated = true,
                score = 0.0,
                isDailyChallengeCompleted = false
            )
        val user3 =
            PublicUserEntity(
                userId = UUID.randomUUID(),
                username = "testUser",
                name = "test user",
                country = "In",
                college = "college",
                avatarId = 1,
                tier = TierTypeDto.TIER2,
                tutorialLevel = 1,
                rating = 1500.0,
                wins = 0,
                losses = 0,
                ties = 0,
                isActivated = true,
                score = 0.0,
                isDailyChallengeCompleted = false
            )
        val user4 =
            PublicUserEntity(
                userId = UUID.randomUUID(),
                username = "testUser",
                name = "test user",
                country = "In",
                college = "college",
                avatarId = 1,
                tier = TierTypeDto.TIER2,
                tutorialLevel = 1,
                rating = 1500.0,
                wins = 0,
                losses = 0,
                ties = 0,
                isActivated = true,
                score = 0.0,
                isDailyChallengeCompleted = false
            )
        every { publicUserRepository.findAllByTier(TierTypeDto.TIER1, any()) } returns
            listOf(user1, user2)
        every { publicUserRepository.findAllByTier(TierTypeDto.TIER2, any()) } returns
            listOf(user3, user4)

        val tier1Entries = publicUserService.getLeaderboard(0, 10, TierTypeDto.TIER1)
        val tier2Entries = publicUserService.getLeaderboard(0, 10, TierTypeDto.TIER2)
        tier1Entries.forEach { user -> assert(user.user.tier == TierTypeDto.TIER1) }
        tier2Entries.forEach { user -> assert(user.user.tier == TierTypeDto.TIER2) }
    }
}
