package delta.codecharacter.server.schedulers

import delta.codecharacter.server.code.code_revision.CodeRevisionService
import delta.codecharacter.server.code.latest_code.LatestCodeService
import delta.codecharacter.server.code.locked_code.LockedCodeService
import delta.codecharacter.server.game_map.latest_map.LatestMapService
import delta.codecharacter.server.game_map.locked_map.LockedMapService
import delta.codecharacter.server.game_map.map_revision.MapRevisionService
import delta.codecharacter.server.match.MatchService
import delta.codecharacter.server.user.public_user.PublicUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulingService(
    @Autowired private val publicUserService: PublicUserService,
    @Autowired private val matchService: MatchService,
    @Autowired private val codeRevisionService: CodeRevisionService,
    @Autowired private val latestCodeService: LatestCodeService,
    @Autowired private val lockedCodeService: LockedCodeService,
    @Autowired private val latestMapService: LatestMapService,
    @Autowired private val lockedMapService: LockedMapService,
    @Autowired private val mapRevisionService: MapRevisionService
) {
    private val logger: Logger = LoggerFactory.getLogger(SchedulingService::class.java)

    @Scheduled(cron = "\${environment.registration-time}", zone = "GMT+5:30")
    fun updateTempLeaderboard() {
        logger.info("Practice phase ended!!")
        publicUserService.resetRatingsAfterPracticePhase()
        codeRevisionService.resetCodeRevisionAfterPracticePhase()
        latestCodeService.resetLatestCodeAfterPracticePhase()
        lockedCodeService.resetLockedCodeAfterPracticePhase()
        latestMapService.resetLatestMapAfterPracticePhase()
        lockedMapService.resetLockedMapAfterPracticePhase()
        mapRevisionService.resetMapRevisionAfterPracticePhase()
        publicUserService.updateLeaderboardAfterPracticePhase()
    }

    @Scheduled(cron = "\${environment.promote-demote-time}", zone = "GMT+5:30")
    fun createAutoMatch() {
        matchService.createAutoMatch()
    }
}
