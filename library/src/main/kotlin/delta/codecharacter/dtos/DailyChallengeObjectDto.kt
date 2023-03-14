package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * The object describing the challenge for the day
 * @param cpp
 * @param java
 * @param python
 * @param image
 */
data class DailyChallengeObjectDto(

        @Schema(example = "null", description = "")
    @get:JsonProperty("cpp") var cpp: kotlin.String? = null,

        @Schema(example = "null", description = "")
    @get:JsonProperty("java") var java: kotlin.String? = null,

        @Schema(example = "null", description = "")
    @get:JsonProperty("python") var python: kotlin.String? = null,

        @Schema(example = "null", description = "")
    @get:JsonProperty("image") val image: kotlin.String? = null
) {

}

