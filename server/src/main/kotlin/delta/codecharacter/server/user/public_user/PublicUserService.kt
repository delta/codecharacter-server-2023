package delta.codecharacter.server.user.public_user

import delta.codecharacter.dtos.CurrentUserProfileDto
import delta.codecharacter.dtos.DailyChallengeLeaderBoardResponseDto
import delta.codecharacter.dtos.LeaderboardEntryDto
import delta.codecharacter.dtos.PublicUserDto
import delta.codecharacter.dtos.UpdateCurrentUserProfileDto
import delta.codecharacter.dtos.UserStatsDto
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.leaderboard.LeaderboardTierEnum
import delta.codecharacter.server.match.MatchVerdictEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class PublicUserService(@Autowired private val publicUserRepository: PublicUserRepository) {

    fun create(
        userId: UUID,
        username: String,
        name: String,
        country: String,
        college: String,
        avatarId: Int
    ) {
        val publicUser =
            PublicUserEntity(
                userId = userId,
                username = username,
                name = name,
                country = country,
                college = college,
                avatarId = avatarId,
                rating = 1500.0,
                wins = 0,
                losses = 0,
                ties = 0,
<<<<<<< HEAD
                score = 0.0,
                challengesCompleted = null,
=======
>>>>>>> 5fcd9ea (fix: tier categories)
            )
        publicUserRepository.save(publicUser)
    }

    fun getLeaderboard(page: Int?, size: Int?): List<LeaderboardEntryDto> {
        val pageRequest = PageRequest.of(page ?: 0, size ?: 10, Sort.by(Sort.Direction.DESC, "rating"))
        return publicUserRepository.findAll(pageRequest).content.map {
            LeaderboardEntryDto(
                user =
                PublicUserDto(
                    username = it.username,
                    name = it.name,
                    country = it.country,
                    college = it.college,
                    avatarId = it.avatarId,
                ),
                stats =
                UserStatsDto(
                    rating = BigDecimal(it.rating),
                    wins = it.wins,
                    losses = it.losses,
                    ties = it.ties
                )
            )
        }
    }

    fun getDailyChallengeLeaderboard(
        page: Int?,
        size: Int?
    ): List<DailyChallengeLeaderBoardResponseDto> {
        val pageRequest = PageRequest.of(page ?: 0, size ?: 10, Sort.by(Sort.Direction.DESC, "score"))
        return publicUserRepository.findAll(pageRequest).content.map {
            DailyChallengeLeaderBoardResponseDto(
                userName = it.username, score = BigDecimal(it.score), avatarId = it.avatarId
            )
        }
    }
    fun getSortedLeaderBoard(): List<LeaderboardEntryDto> {
        val leaderboard =
            publicUserRepository.findAll().map {
                LeaderboardEntryDto(
                    user =
                    PublicUserDto(
                        username = it.username,
                        name = it.name,
                        country = it.country,
                        college = it.college,
                        avatarId = it.avatarId,
                    ),
                    stats =
                    UserStatsDto(
                        rating = BigDecimal(it.rating),
                        wins = it.wins,
                        losses = it.losses,
                        ties = it.ties,
                    )
                )
            }
        return leaderboard.sortedBy { it.stats.rating }.reversed()
    }

    fun getLeaderboardByTier(tier: LeaderboardTierEnum): List<LeaderboardEntryDto> {
        var leaderboardEntry = getSortedLeaderBoard()
        var leaderboardSize = leaderboardEntry.size
        if (tier == LeaderboardTierEnum.TIER1)
            return leaderboardEntry.subList(0, (0.1 * leaderboardSize).toInt() + 1)
        else {
            leaderboardEntry =
                leaderboardEntry.subList((0.1 * leaderboardSize).toInt() + 1, leaderboardSize)
            leaderboardSize = leaderboardEntry.size
            return if (tier == LeaderboardTierEnum.TIER2)
                leaderboardEntry.subList(0, (0.15 * leaderboardSize).toInt() + 1)
            else {
                leaderboardEntry =
                    leaderboardEntry.subList((0.15 * leaderboardSize).toInt() + 1, leaderboardSize)
                leaderboardSize = leaderboardEntry.size
                if (tier == LeaderboardTierEnum.TIER3)
                    leaderboardEntry.subList(0, (0.25 * leaderboardSize).toInt() + 1)
                else leaderboardEntry.subList((0.25 * leaderboardSize).toInt() + 1, leaderboardSize)
            }
        }
    }

    fun getMinRatingForTier(tier: LeaderboardTierEnum): Double {
        val leaderboardEntry = getLeaderboardByTier(tier)
        return leaderboardEntry[leaderboardEntry.size - 1].stats.rating.toDouble()
    }

    fun getUserTierByRating(rating: Double): LeaderboardTierEnum {
        return if (rating >= getMinRatingForTier(LeaderboardTierEnum.TIER1)) LeaderboardTierEnum.TIER1
        else if (rating < getMinRatingForTier(LeaderboardTierEnum.TIER1) &&
            rating >= getMinRatingForTier(LeaderboardTierEnum.TIER2)
        )
            LeaderboardTierEnum.TIER2
        else if (rating < getMinRatingForTier(LeaderboardTierEnum.TIER2) &&
            rating >= getMinRatingForTier(LeaderboardTierEnum.TIER3)
        )
            LeaderboardTierEnum.TIER3
        else LeaderboardTierEnum.TIER4
    }

    fun getUserProfile(userId: UUID, email: String): CurrentUserProfileDto {
        val user = publicUserRepository.findById(userId).get()
        return CurrentUserProfileDto(
            id = userId,
            username = user.username,
            email = email,
            name = user.name,
            country = user.country,
            college = user.college,
            avatarId = user.avatarId,
        )
    }

    fun updateUserProfile(userId: UUID, updateCurrentUserProfileDto: UpdateCurrentUserProfileDto) {
        val user = publicUserRepository.findById(userId).get()
        val updatedUser =
            user.copy(
                name = updateCurrentUserProfileDto.name ?: user.name,
                country = updateCurrentUserProfileDto.country ?: user.country,
                college = updateCurrentUserProfileDto.college ?: user.college,
                avatarId = updateCurrentUserProfileDto.avatarId ?: user.avatarId
            )
        publicUserRepository.save(updatedUser)
    }

    fun updatePublicRating(
        userId: UUID,
        isInitiator: Boolean,
        verdict: MatchVerdictEnum,
        newRating: Double
    ) {
        val user = publicUserRepository.findById(userId).get()
        val updatedUser =
            user.copy(
                rating = newRating,
                wins =
                if ((isInitiator && verdict == MatchVerdictEnum.PLAYER1) ||
                    (!isInitiator && verdict == MatchVerdictEnum.PLAYER2)
                )
                    user.wins + 1
                else user.wins,
                losses =
                if ((isInitiator && verdict == MatchVerdictEnum.PLAYER2) ||
                    (!isInitiator && verdict == MatchVerdictEnum.PLAYER1)
                )
                    user.losses + 1
                else user.losses,
                ties = if (verdict == MatchVerdictEnum.TIE) user.ties + 1 else user.ties
            )
        publicUserRepository.save(updatedUser)
    }

    fun getPublicUser(userId: UUID): PublicUserEntity {
        return publicUserRepository.findById(userId).get()
    }

    fun getPublicUserByUsername(username: String): PublicUserEntity {
        return publicUserRepository.findByUsername(username).orElseThrow {
            CustomException(HttpStatus.BAD_REQUEST, "Invalid username")
        }
    }

    fun isUsernameUnique(username: String): Boolean {
        return publicUserRepository.findByUsername(username).isEmpty
    }
}
