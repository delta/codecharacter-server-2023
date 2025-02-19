package delta.codecharacter.server.daily_challenge

import delta.codecharacter.core.DailyChallengesApi
import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.dtos.DailyChallengeLeaderBoardResponseDto
import delta.codecharacter.dtos.DailyChallengeMatchRequestDto
import delta.codecharacter.server.match.MatchService
import delta.codecharacter.server.user.UserEntity
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class DailyChallengeController(
    @Autowired private val dailyChallengeService: DailyChallengeService,
    @Autowired private val matchService: MatchService,
    @Autowired private val publicUserService: PublicUserService
) : DailyChallengesApi {
    @Secured(value = ["ROLE_USER"])
    override fun getDailyChallenge(): ResponseEntity<DailyChallengeGetRequestDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(dailyChallengeService.getDailyChallengeByDateForUser(user.id, false))
    }
    override fun getDailyChallengeLeaderBoard(
        page: Int?,
        size: Int?
    ): ResponseEntity<List<DailyChallengeLeaderBoardResponseDto>> {
        return ResponseEntity.ok(publicUserService.getDailyChallengeLeaderboard(page, size))
    }

    @Secured(value = ["ROLE_USER"])
    override fun createDailyChallengeMatch(
        dailyChallengeMatchRequestDto: DailyChallengeMatchRequestDto
    ): ResponseEntity<Unit> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(matchService.createDCMatch(user.id, dailyChallengeMatchRequestDto))
    }
}
