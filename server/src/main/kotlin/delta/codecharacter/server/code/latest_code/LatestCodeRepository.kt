package delta.codecharacter.server.code.latest_code

import delta.codecharacter.dtos.CodeTypeDto
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/** Repository for [LatestCodeEntity] */
@Repository
interface LatestCodeRepository : MongoRepository<LatestCodeEntity, UUID> {
    fun findFirstByUserIdAndCodeType(userId: UUID, codeType: CodeTypeDto): Optional<LatestCodeEntity>
}
