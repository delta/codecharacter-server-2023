package delta.codecharacter.server.match

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.CreateMatchRequestDto
import delta.codecharacter.dtos.DailyChallengeMatchRequestDto
import delta.codecharacter.dtos.GameDto
import delta.codecharacter.dtos.GameStatusDto
import delta.codecharacter.dtos.MatchDto
import delta.codecharacter.dtos.MatchModeDto
import delta.codecharacter.dtos.PublicUserDto
import delta.codecharacter.dtos.TierTypeDto
import delta.codecharacter.dtos.VerdictDto
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.code.code_revision.CodeRevisionService
import delta.codecharacter.server.code.latest_code.LatestCodeService
import delta.codecharacter.server.code.locked_code.LockedCodeService
import delta.codecharacter.server.daily_challenge.DailyChallengeService
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchEntity
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchRepository
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameService
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.game.queue.entities.GameCode
import delta.codecharacter.server.game_map.latest_map.LatestMapService
import delta.codecharacter.server.game_map.locked_map.LockedMapService
import delta.codecharacter.server.game_map.map_revision.MapRevisionService
import delta.codecharacter.server.logic.verdict.VerdictAlgorithm
import delta.codecharacter.server.notifications.NotificationService
import delta.codecharacter.server.user.public_user.PublicUserEntity
import delta.codecharacter.server.user.public_user.PublicUserService
import delta.codecharacter.server.user.rating_history.RatingHistoryService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class MatchService(
    @Autowired private val matchRepository: MatchRepository,
    @Autowired private val gameService: GameService,
    @Autowired private val latestCodeService: LatestCodeService,
    @Autowired private val codeRevisionService: CodeRevisionService,
    @Autowired private val lockedCodeService: LockedCodeService,
    @Autowired private val latestMapService: LatestMapService,
    @Autowired private val mapRevisionService: MapRevisionService,
    @Autowired private val lockedMapService: LockedMapService,
    @Autowired private val publicUserService: PublicUserService,
    @Autowired private val verdictAlgorithm: VerdictAlgorithm,
    @Autowired private val ratingHistoryService: RatingHistoryService,
    @Autowired private val notificationService: NotificationService,
    @Autowired private val dailyChallengeService: DailyChallengeService,
    @Autowired private val dailyChallengeMatchRepository: DailyChallengeMatchRepository,
    @Autowired private val jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder,
    @Autowired private val simpMessagingTemplate: SimpMessagingTemplate,
) {
    private var mapper: ObjectMapper = jackson2ObjectMapperBuilder.build()

    private fun createSelfMatch(userId: UUID, codeRevisionId: UUID?, mapRevisionId: UUID?) {
        val code: String
        val language: LanguageEnum
        if (codeRevisionId == null) {
            val latestCode = latestCodeService.getLatestCode(userId)
            code = latestCode.code
            language = LanguageEnum.valueOf(latestCode.language.name)
        } else {
            val codeRevision =
                codeRevisionService.getCodeRevisions(userId).find { it.id == codeRevisionId }
                    ?: throw CustomException(HttpStatus.BAD_REQUEST, "Invalid revision ID")
            code = codeRevision.code
            language = LanguageEnum.valueOf(codeRevision.language.name)
        }

        val map: String =
            if (mapRevisionId == null) {
                val latestMap = latestMapService.getLatestMap(userId)
                latestMap.map
            } else {
                val mapRevision =
                    mapRevisionService.getMapRevisions(userId).find { it.id == mapRevisionId }
                        ?: throw CustomException(HttpStatus.BAD_REQUEST, "Invalid revision ID")
                mapRevision.map
            }

        val matchId = UUID.randomUUID()
        val game = gameService.createGame(matchId)
        val publicUser = publicUserService.getPublicUser(userId)
        val match =
            MatchEntity(
                id = matchId,
                games = listOf(game),
                mode = MatchModeEnum.SELF,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicUser,
            )
        matchRepository.save(match)
        gameService.sendGameRequest(game, code, LanguageEnum.valueOf(language.name), map)
    }

    fun createPlayerVsMapMatch(publicUser: PublicUserEntity, publicOpponent: PublicUserEntity) {
        val userId = publicUser.userId
        val opponentId = publicOpponent.userId
        val (userLanguage, userCode) = lockedCodeService.getLockedCode(userId)
        val userMap = lockedMapService.getLockedMap(userId)

        val (opponentLanguage, opponentCode) = lockedCodeService.getLockedCode(opponentId)
        val opponentMap = lockedMapService.getLockedMap(opponentId)

        val matchId = UUID.randomUUID()

        val game1 = gameService.createGame(matchId)
        val game2 = gameService.createGame(matchId)
        val code =""
        val match =
            MatchEntity(
                id = matchId,
                games = listOf(game1, game2),
                mode = MatchModeEnum.MANUAL,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicOpponent,
            )
        matchRepository.save(match)

        gameService.sendGameRequest(game1, userCode, userLanguage, opponentMap)
        gameService.sendGameRequest(game2, opponentCode, opponentLanguage, userMap)
    }

    fun createPlayerVsPlayerMatch(publicUser: PublicUserEntity, publicOpponent: PublicUserEntity) {
        val userId = publicUser.userId
        val opponentId = publicOpponent.userId

        val (userLanguage, userCode) = lockedCodeService.getLockedCode(userId)
        val (opponentLanguage, opponentCode) = lockedCodeService.getLockedCode(opponentId)
        val u ="#include \"player_code.h\"\n" +
                "\n" +
                "// This initial code is well commented and serves as a small tutorial for game\n" +
                "// APIs, for more information you can refer to the documentation\n" +
                "\n" +
                "// This is the function player has to fill\n" +
                "// You can define any new functions here that you want\n" +
                "Game run(const PvPState &state) {\n" +
                "\n" +
                "  // Always start by instantiating a Game class object\n" +
                "  Game game;\n" +
                "\n" +
                "  size_t remaining_coins = state.get_coins_left();\n" +
                "\n" +
                "  game.logr() << \"TURN \" << state.get_turn_no() << \" LOGS:\";\n" +
                "\n" +
                "  // Get all the attackers and defenders in the game and store it\n" +
                "  const std::vector<Attacker> &attackers = state.get_attackers();\n" +
                "  const std::vector<Attacker> &opponent_attackers = state.get_opponent_attackers();\n" +
                "\n" +
                "  // The function get_all_valid_spawn_positions() is a helper which will give us\n" +
                "  // the list of valid spawn positions in map.\n" +
                "  // If the position  we're spawning is not one of these, the player will be\n" +
                "  // penalized by deducting the spawn cost but not spawning the attacker\n" +
                "  std::vector<Position> all_valid_spawn_positions =\n" +
                "      get_all_valid_spawn_positions();\n" +
                "\n" +
                "  // Lets say I want to spawn an attacker of each of the type in one turn\n" +
                "  // and I want to use the all_valid_spawn_positions list as well. In order to\n" +
                "  // keep traack of the last index in the list that we spawned at, we can use a\n" +
                "  // static variable in c++\n" +
                "\n" +
                "  static int last_spawned = 0;\n" +
                "  game.spawn_attacker(1, Position(0, 7));\n" +
                "  game.spawn_attacker(1, Position(0, 8));\n" +
                "  game.spawn_attacker(1, Position(0, 8));\n" +
                "   game.spawn_attacker(2, Position(0, 8))\n;"+
                "   game.spawn_attacker(3, Position(0, 9))\n;"+
                "   game.spawn_attacker(2, Position(0, 8))\n;"+

                "\n" +
                "  if (!opponent_attackers.empty()) {\n" +
                "    for (size_t type_id = 1; type_id <= Constants::NO_OF_ATTACKER_TYPES;\n" +
                "         type_id++) {\n" +
                "      // Spawn the attacker of type_id at position\n" +
                "      // all_valid_spawn_positions[last_spawned]\n" +
                "\n" +
                "      // There are two cases when you might be panalized\n" +
                "      //    - Spawning at invalid position\n" +
                "      //    - Spawning at position where you have already spawned one attacker\n" +
                "      //    in the same turn\n" +
                "      //\n" +
                "      // We have provided helpers to check just that\n" +
                "\n" +
                "      // game class will keep track of all your spawned positions for you and\n" +
                "      // provides a helper method called already_spawned_at_position(Position)\n" +
                "      // to check if you already spawned in the position\n" +
                "\n" +
                "      // Mostly a good practice to check with these two helpers before spawning,\n" +
                "      // to save up on accidental penalties\n" +
                "      if (is_valid_spawn_position(all_valid_spawn_positions[last_spawned]) &&\n" +
                "          !game.already_spawned_at_position(\n" +
                "              all_valid_spawn_positions[last_spawned])) {\n" +
                "        // If lets say you had run out of coins left, the game will just ignore\n" +
                "        // the spawn\n" +
                "        game.spawn_attacker(type_id, all_valid_spawn_positions[last_spawned]);\n" +
                "\n" +
                "        // This has the starting attributes for the attacker we are about to\n" +
                "        // spawn\n" +
                "        // For full information about the Attributes class refer the\n" +
                "        // documentation\n" +
                "        // This can be used for strategizing\n" +
                "        Attributes attackers_attributes =\n" +
                "            Constants::ATTACKER_TYPE_ATTRIBUTES.at(type_id);\n" +
                "\n" +
                "        // You can use the logger we provide to show log messages in the\n" +
                "        // rendered game\n" +
                "        game.logr() << \"To to be spawned at Position(\"\n" +
                "                    << all_valid_spawn_positions[last_spawned].get_x() << \",\"\n" +
                "                    << all_valid_spawn_positions[last_spawned].get_y() << \")\"\n" +
                "                    << '\\n';\n" +
                "        (last_spawned += 1) %= all_valid_spawn_positions.size();\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  // Now lets say you always want to set the target for the attackers[0] to\n" +
                "  // defenders[0]\n" +
                "  // To do that you do\n" +
                "  if (!attackers.empty() && !opponent_attackers.empty()) {\n" +
                "    // check if they are empty beforehand to be safe from unexpected errors\n" +
                "    game.set_target(attackers.front(), opponent_attackers.front());\n" +
                "  }\n" +
                "\n" +
                "  // Lets log all the spawned positions for this turn\n" +
                "  for (auto &[type_id, pos] : game.get_spawn_positions()) {\n" +
                "    // you can use logger macro as well, which is an alias for game.logr()\n" +
                "    logger << \"Type \" << type_id << \" at Position (\" << pos.get_x() << \",\"\n" +
                "           << pos.get_y() << \")\\n\";\n" +
                "  }\n" +
                "\n" +
                "  return game;\n" +
                "}\n"
        val userCodeBase = GameCode(code = u, language = userLanguage)
        val opponentCodeBase = GameCode(code = u, language = opponentLanguage)

        val matchId = UUID.randomUUID()

        val game = gameService.createGame(matchId)

        val match =
            MatchEntity(
                id = matchId,
                games = listOf(game),
                mode = MatchModeEnum.MANUAL,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicOpponent,
            )
        matchRepository.save(match)

        gameService.sendPlayerVsPlayerGameRequest(game, userCodeBase, opponentCodeBase)
    }
    fun createDualMatch(userId: UUID, opponentUsername: String, mode: MatchModeDto) {
        val publicUser = publicUserService.getPublicUser(userId)
        val publicOpponent = publicUserService.getPublicUserByUsername(opponentUsername)
        val opponentId = publicOpponent.userId
        if (publicUser.userId == opponentId) {
            throw CustomException(HttpStatus.BAD_REQUEST, "You cannot play against yourself")
        }
        when (mode) {
            MatchModeDto.MANUAL -> {
                createPlayerVsMapMatch(publicUser, publicOpponent)
            }
            MatchModeDto.PVP -> {
                createPlayerVsPlayerMatch(publicUser, publicOpponent)
            }
            else -> {
                throw CustomException(HttpStatus.BAD_REQUEST, "You selected wrong mode")
            }
        }
    }
    fun createDCMatch(userId: UUID, dailyChallengeMatchRequestDto: DailyChallengeMatchRequestDto) {
        val (_, chall, challType, _, completionStatus) = dailyChallengeService.getDailyChallengeByDate()
        val (value, _) = dailyChallengeMatchRequestDto
        if (completionStatus != null && completionStatus) {
            throw CustomException(HttpStatus.ACCEPTED, "You have already completed your daily Challenge")
        }
        val language: LanguageEnum
        val map: String
        val code: String
        when (challType) {
            ChallengeTypeDto.CODE -> { // code as question and map as answer
                code = chall
                language = LanguageEnum.CPP
                map = value
            }
            ChallengeTypeDto.MAP -> {
                map = chall
                language = LanguageEnum.valueOf(dailyChallengeMatchRequestDto.language.toString())
                code = value
            }
        }
        val matchId = UUID.randomUUID()
        val game = gameService.createGame(matchId)
        val user = publicUserService.getPublicUser(userId)
        val match =
            DailyChallengeMatchEntity(
                id = matchId,
                verdict = DailyChallengeMatchVerdictEnum.STARTED,
                createdAt = Instant.now(),
                user = user,
                game = game
            )
        dailyChallengeMatchRepository.save(match)
        gameService.sendGameRequest(game, code, language, map)
    }
    fun createMatch(userId: UUID, createMatchRequestDto: CreateMatchRequestDto) {
        when (createMatchRequestDto.mode) {
            MatchModeDto.SELF -> {
                val (_, _, mapRevisionId, codeRevisionId) = createMatchRequestDto
                createSelfMatch(userId, codeRevisionId, mapRevisionId)
            }
            MatchModeDto.MANUAL, MatchModeDto.AUTO, MatchModeDto.PVP -> {
                if (createMatchRequestDto.opponentUsername == null) {
                    throw CustomException(HttpStatus.BAD_REQUEST, "Opponent ID is required")
                }
                createDualMatch(
                    userId, createMatchRequestDto.opponentUsername!!, createMatchRequestDto.mode
                )
            }
        }
    }

    private fun mapMatchEntitiesToDtos(matchEntities: List<MatchEntity>): List<MatchDto> {
        return matchEntities.map { matchEntity ->
            MatchDto(
                id = matchEntity.id,
                matchMode = MatchModeDto.valueOf(matchEntity.mode.name),
                matchVerdict = VerdictDto.valueOf(matchEntity.verdict.name),
                createdAt = matchEntity.createdAt,
                games =
                matchEntity
                    .games
                    .map { gameEntity ->
                        GameDto(
                            id = gameEntity.id,
                            destruction = BigDecimal(gameEntity.destruction),
                            coinsUsed = gameEntity.coinsUsed,
                            status = GameStatusDto.valueOf(gameEntity.status.name),
                        )
                    }
                    .toSet(),
                user1 =
                PublicUserDto(
                    username = matchEntity.player1.username,
                    name = matchEntity.player1.name,
                    tier = TierTypeDto.valueOf(matchEntity.player1.tier.name),
                    country = matchEntity.player1.country,
                    college = matchEntity.player1.college,
                    avatarId = matchEntity.player1.avatarId,
                ),
                user2 =
                PublicUserDto(
                    username = matchEntity.player2.username,
                    name = matchEntity.player2.name,
                    tier = TierTypeDto.valueOf(matchEntity.player2.tier.name),
                    country = matchEntity.player2.country,
                    college = matchEntity.player2.college,
                    avatarId = matchEntity.player2.avatarId,
                ),
            )
        }
    }

    fun getTopMatches(): List<MatchDto> {
        val matches = matchRepository.findTop10ByOrderByTotalPointsDesc()
        return mapMatchEntitiesToDtos(matches)
    }

    fun getUserMatches(userId: UUID): List<MatchDto> {
        val publicUser = publicUserService.getPublicUser(userId)
        val matches = matchRepository.findByPlayer1OrderByCreatedAtDesc(publicUser)
        return mapMatchEntitiesToDtos(matches)
    }

    @RabbitListener(queues = ["gameStatusUpdateQueue"], ackMode = "AUTO")
    fun receiveGameResult(gameStatusUpdateJson: String) {
        val updatedGame = gameService.updateGameStatus(gameStatusUpdateJson)
        val matchId = updatedGame.matchId
        if (matchRepository.findById(matchId).isPresent) {
            val match = matchRepository.findById(updatedGame.matchId).get()
            if (match.mode != MatchModeEnum.AUTO && match.games.first().id == updatedGame.id) {
                simpMessagingTemplate.convertAndSend(
                    "/updates/${match.player1.userId}",
                    mapper.writeValueAsString(
                        GameDto(
                            id = updatedGame.id,
                            destruction = BigDecimal(updatedGame.destruction),
                            coinsUsed = updatedGame.coinsUsed,
                            status = GameStatusDto.valueOf(updatedGame.status.name),
                        )
                    )
                )
            }
            if (match.mode != MatchModeEnum.SELF &&
                match.games.all { game ->
                    game.status == GameStatusEnum.EXECUTED || game.status == GameStatusEnum.EXECUTE_ERROR
                }
            ) {
                val player1Game = match.games.first()
                val player2Game = match.games.last()
                val verdict =
                    verdictAlgorithm.getVerdict(
                        player1Game.status == GameStatusEnum.EXECUTE_ERROR,
                        player1Game.coinsUsed,
                        player1Game.destruction,
                        player2Game.status == GameStatusEnum.EXECUTE_ERROR,
                        player2Game.coinsUsed,
                        player2Game.destruction
                    )
                val finishedMatch = match.copy(verdict = verdict)
                val (newUserRating, newOpponentRating) =
                    ratingHistoryService.updateRating(match.player1.userId, match.player2.userId, verdict)

                publicUserService.updatePublicRating(
                    userId = match.player1.userId,
                    isInitiator = true,
                    verdict = verdict,
                    newRating = newUserRating
                )
                publicUserService.updatePublicRating(
                    userId = match.player2.userId,
                    isInitiator = false,
                    verdict = verdict,
                    newRating = newOpponentRating
                )

                if (match.mode == MatchModeEnum.MANUAL) {
                    notificationService.sendNotification(
                        match.player1.userId,
                        "Match Result",
                        "${
                        when (verdict) {
                            MatchVerdictEnum.PLAYER1 -> "Won"
                            MatchVerdictEnum.PLAYER2 -> "Lost"
                            MatchVerdictEnum.TIE -> "Tied"
                        }
                        } against ${match.player2.username}",
                    )
                }

                matchRepository.save(finishedMatch)
            }
        } else if (dailyChallengeMatchRepository.findById(matchId).isPresent) {
            println(updatedGame.destruction)
            println(updatedGame.matchId)
      /*
       * Get the DCMatch with help of the matchId
       * pass the game parameters into the daily-challenge-verdict-algorithm
       * Game-Parameters :- GameStatus, destruction,coinsUsed
       * Store the verdict back in verdict in daily-challenge entity
       * websocket for sending game status /updates/{userId} -> userId from dcMatchEntity
       * store the finsished DC Match
       * If verdict-> success make isDailyChallengeComplete-true
       */
        }
    }
}
