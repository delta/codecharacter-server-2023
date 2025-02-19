/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.3.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package delta.codecharacter.core

import delta.codecharacter.dtos.AuthStatusResponseDto
import delta.codecharacter.dtos.ForgotPasswordRequestDto
import delta.codecharacter.dtos.GenericErrorDto
import delta.codecharacter.dtos.PasswordLoginRequestDto
import delta.codecharacter.dtos.PasswordLoginResponseDto
import delta.codecharacter.dtos.ResetPasswordRequestDto
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.beans.factory.annotation.Autowired

import jakarta.validation.constraints.*
import jakarta.validation.Valid

import kotlin.collections.List
import kotlin.collections.Map

@Validated
@RequestMapping("\${api.base-path:}")
interface AuthApi {

    @Operation(
        summary = "Forgot password",
        operationId = "forgotPassword",
        description = "Request password reset email to be sent when user forgot their password",
        responses = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(schema = Schema(implementation = GenericErrorDto::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @RequestMapping(
            method = [RequestMethod.POST],
            value = ["/auth/forgot-password"],
            produces = ["application/json"],
            consumes = ["application/json"]
    )
    fun forgotPassword(@Parameter(description = "", required = true) @Valid @RequestBody forgotPasswordRequestDto: ForgotPasswordRequestDto): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Get authentication status",
        operationId = "getAuthStatus",
        description = "Get authentication status: fully authenticated, activation pending and incomplete profile",
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = AuthStatusResponseDto::class))])
        ],
        security = [ SecurityRequirement(name = "http-bearer") ]
    )
    @RequestMapping(
            method = [RequestMethod.GET],
            value = ["/auth/status"],
            produces = ["application/json"]
    )
    fun getAuthStatus(): ResponseEntity<AuthStatusResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Password Login",
        operationId = "passwordLogin",
        description = "Login with email and password and get bearer token for authentication",
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = PasswordLoginResponseDto::class))]),
            ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(schema = Schema(implementation = GenericErrorDto::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = GenericErrorDto::class))])
        ]
    )
    @RequestMapping(
            method = [RequestMethod.POST],
            value = ["/auth/login/password"],
            produces = ["application/json"],
            consumes = ["application/json"]
    )
    fun passwordLogin(@Parameter(description = "", required = true) @Valid @RequestBody passwordLoginRequestDto: PasswordLoginRequestDto): ResponseEntity<PasswordLoginResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Reset password",
        operationId = "resetPassword",
        description = "Reset password using the token from password reset email",
        responses = [
            ApiResponse(responseCode = "204", description = "No Content"),
            ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(schema = Schema(implementation = GenericErrorDto::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @RequestMapping(
            method = [RequestMethod.POST],
            value = ["/auth/reset-password"],
            produces = ["application/json"],
            consumes = ["application/json"]
    )
    fun resetPassword(@Parameter(description = "", required = true) @Valid @RequestBody resetPasswordRequestDto: ResetPasswordRequestDto): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}
