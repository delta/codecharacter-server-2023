package delta.codecharacter.server.code.code_revision

import delta.codecharacter.dtos.CodeRevisionDto
import delta.codecharacter.dtos.CodeTypeDto
import delta.codecharacter.dtos.CreateCodeRevisionRequestDto
import delta.codecharacter.dtos.LanguageDto
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/** Service for code revision. */
@Service
class CodeRevisionService(@Autowired private val codeRevisionRepository: CodeRevisionRepository) {
    @Value("\${environment.is-event-open}") private val isEventOpen = false

    fun createCodeRevision(userId: UUID, createCodeRevisionRequestDto: CreateCodeRevisionRequestDto) {
        if (!isEventOpen) {
            throw CustomException(HttpStatus.BAD_REQUEST, "Match phase has ended")
        }

        val (code, message, language) = createCodeRevisionRequestDto
        val parentCodeRevision =
            codeRevisionRepository
                .findFirstByUserIdAndCodeTypeOrderByCreatedAtDesc(
                    userId, createCodeRevisionRequestDto.codeType ?: CodeTypeDto.NORMAL
                )
                .orElse(null)
        codeRevisionRepository.save(
            CodeRevisionEntity(
                id = UUID.randomUUID(),
                code = code,
                codeType = createCodeRevisionRequestDto.codeType ?: CodeTypeDto.NORMAL,
                message = message,
                language = LanguageEnum.valueOf(language.name),
                userId = userId,
                parentRevision = parentCodeRevision,
                createdAt = Instant.now()
            )
        )
    }

    fun resetCodeRevisionAfterPracticePhase() {
        codeRevisionRepository.deleteAll()
    }

    fun getCodeRevisions(
        userId: UUID,
        codeTypeDto: CodeTypeDto = CodeTypeDto.NORMAL
    ): List<CodeRevisionDto> {
        return codeRevisionRepository.findAllByUserIdAndCodeTypeOrderByCreatedAtDesc(
            userId, codeTypeDto
        )
            .map {
                CodeRevisionDto(
                    id = it.id,
                    code = it.code,
                    message = it.message,
                    language = LanguageDto.valueOf(it.language.name),
                    createdAt = it.createdAt,
                    parentRevision = it.parentRevision?.id
                )
            }
    }
}
