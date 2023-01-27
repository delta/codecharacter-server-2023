package delta.codecharacter.server.code.locked_code

import delta.codecharacter.dtos.CodeTypeDto
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/** Repository for [LockedCodeEntity] */
@Repository
interface LockedCodeRepository : MongoRepository<LockedCodeEntity, UUID> {
    fun findFirstByUserIdAndCodeType(userId: UUID, codeType: CodeTypeDto): Optional<LockedCodeEntity>
}
