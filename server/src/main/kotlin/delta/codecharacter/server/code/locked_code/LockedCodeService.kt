package delta.codecharacter.server.code.locked_code

import delta.codecharacter.dtos.CodeTypeDto
import delta.codecharacter.dtos.UpdateLatestCodeRequestDto
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.config.DefaultCodeMapConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/** Service for locked code. */
@Service
class LockedCodeService(
    @Autowired private val lockedCodeRepository: LockedCodeRepository,
    @Autowired private val defaultCodeMapConfiguration: DefaultCodeMapConfiguration
) {

    fun getLockedCode(
        userId: UUID,
        codeType: CodeTypeDto = CodeTypeDto.NORMAL
    ): Pair<LanguageEnum, String> {
        val lockedCode =
            lockedCodeRepository
                .findFirstByUserIdAndCodeType(userId, codeType)
                .orElse(
                    LockedCodeEntity(
                        userId = userId,
                        code = defaultCodeMapConfiguration.defaultCode,
                        language = defaultCodeMapConfiguration.defaultLanguage,
                        codeType = codeType
                    )
                )
                .let { code -> Pair(code.language, code.code) }
        return lockedCode
    }

    fun updateLockedCode(userId: UUID, updateLatestCodeRequestDto: UpdateLatestCodeRequestDto) {
        lockedCodeRepository.save(
            LockedCodeEntity(
                code = updateLatestCodeRequestDto.code,
                language = LanguageEnum.valueOf(updateLatestCodeRequestDto.language.name),
                codeType = updateLatestCodeRequestDto.codeType ?: CodeTypeDto.NORMAL,
                userId = userId
            )
        )
    }
}
