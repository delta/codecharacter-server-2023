package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.dtos.GameMapTypeDto
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * GameMap revision model
 * @param id
 * @param map
 * @param mapType
 * @param mapImage
 * @param createdAt
 * @param message
 * @param parentRevision
 */
data class GameMapRevisionDto(

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", required = true, description = "")
    @field:JsonProperty("id", required = true) val id: java.util.UUID,

    @Schema(example = "0000\n0010\n0100\n1000\n", required = true, description = "")
    @field:JsonProperty("map", required = true) val map: kotlin.String,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @field:JsonProperty("mapType", required = true) val mapType: GameMapTypeDto = GameMapTypeDto.NORMAL,

    @Schema(example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII", required = true, description = "")
    @field:JsonProperty("mapImage", required = true) val mapImage: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @field:JsonProperty("createdAt", required = true) val createdAt: java.time.Instant,

    @Schema(example = "message", required = true, description = "")
    @field:JsonProperty("message", required = true) val message: kotlin.String,

    @Schema(example = "123e4567-e89b-12d3-a456-426614174111", description = "")
    @field:JsonProperty("parentRevision") val parentRevision: java.util.UUID? = null
) {

}

