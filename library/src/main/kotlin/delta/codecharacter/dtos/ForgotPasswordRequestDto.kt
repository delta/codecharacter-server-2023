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
 * Forgot password request
 * @param email
 */
data class ForgotPasswordRequestDto(

    @get:Email
    @get:Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")
    @Schema(example = "test@test.com", required = true, description = "")
    @field:JsonProperty("email", required = true) val email: kotlin.String
) {

}

