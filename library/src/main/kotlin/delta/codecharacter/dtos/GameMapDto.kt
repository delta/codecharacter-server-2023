package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
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
 * GameMap model
 * @param map
 * @param mapImage
 * @param lastSavedAt
 */
data class GameMapDto(

    @Schema(example = "0000\n0010\n0100\n1000\n", required = true, description = "")
    @field:JsonProperty("map", required = true) val map: kotlin.String,

    @Schema(example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII", required = true, description = "")
    @field:JsonProperty("mapImage", required = true) val mapImage: kotlin.String,

    @Schema(example = "2021-01-01T00:00Z", required = true, description = "")
    @field:JsonProperty("lastSavedAt", required = true) val lastSavedAt: java.time.Instant
) {

}

