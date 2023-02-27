package delta.codecharacter.server.leaderboard

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.dtos.LeaderboardEntryDto
import delta.codecharacter.server.TestAttributes
import delta.codecharacter.server.WithMockCustomUser
import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
@SpringBootTest
internal class LeaderboardControllerIntegrationTest(@Autowired val mockMvc: MockMvc) {

    @Autowired private lateinit var jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder
    private lateinit var mapper: ObjectMapper

    @Autowired private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setUp() {
        mapper = jackson2ObjectMapperBuilder.build()
        mongoTemplate.save<PublicUserEntity>(TestAttributes.publicUser)
    }

    @Test
    @WithMockCustomUser
    fun `should return empty list when leaderboard not found`() {
        mockMvc.get("/leaderboard") { contentType = MediaType.APPLICATION_JSON }.andExpect {
            status { is2xxSuccessful() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { listOf<LeaderboardEntryDto>() }
        }
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection<PublicUserEntity>()
    }
}
