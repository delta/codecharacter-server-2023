package delta.codecharacter.server.code.latest_code

import delta.codecharacter.dtos.CodeDto
import delta.codecharacter.dtos.CodeTypeDto
import delta.codecharacter.dtos.LanguageDto
import delta.codecharacter.dtos.UpdateLatestCodeRequestDto
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.config.DefaultCodeMapConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/** Service for handling the latest code. */
@Service
class LatestCodeService(
    @Autowired private val latestCodeRepository: LatestCodeRepository,
    @Autowired private val defaultCodeMapConfiguration: DefaultCodeMapConfiguration
) {

    fun getLatestCode(userId: UUID, codeType: CodeTypeDto = CodeTypeDto.NORMAL): CodeDto {
        val latestCode: CodeDto =
            latestCodeRepository
                .findFirstByUserIdAndCodeType(userId, codeType)
                .orElse(
                    LatestCodeEntity(
                        userId = userId,
                        code = defaultCodeMapConfiguration.defaultCode,
                        language = defaultCodeMapConfiguration.defaultLanguage,
                        codeType = codeType,
                        lastSavedAt = Instant.MIN
                    )
                )
                .let { code ->
                    CodeDto(
                        code = code.code,
                        language = LanguageDto.valueOf(code.language.name),
                        codeType = code.codeType,
                        lastSavedAt = code.lastSavedAt,
                    )
                }

        return latestCode
    }

    fun updateLatestCode(userId: UUID, updateLatestCodeRequestDto: UpdateLatestCodeRequestDto) {
        latestCodeRepository.save(
            LatestCodeEntity(
                code = updateLatestCodeRequestDto.code,
                codeType = updateLatestCodeRequestDto.codeType ?: CodeTypeDto.NORMAL,
                language = LanguageEnum.valueOf(updateLatestCodeRequestDto.language.name),
                userId = userId,
                lastSavedAt = Instant.now()
            )
        )
    }
}
