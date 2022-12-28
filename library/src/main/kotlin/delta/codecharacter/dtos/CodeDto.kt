package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.dtos.LanguageDto
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
 * Code model
 * @param code
 * @param lastSavedAt
 * @param language
 */
data class CodeDto(

    @Schema(example = "#include <iostream>", required = true, description = "")
    @field:JsonProperty("code", required = true) val code: kotlin.String,

    @Schema(example = "2021-01-01T00:00Z", required = true, description = "")
    @field:JsonProperty("lastSavedAt", required = true) val lastSavedAt: java.time.Instant,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @field:JsonProperty("language", required = true) val language: LanguageDto
) {

}

