package io.hhplus.concert.app.payment.interfaces.controller;

import io.hhplus.concert.app.common.api.response.ApiErrorResponse;
import io.hhplus.concert.app.common.api.response.ApiResult;
import io.hhplus.concert.app.payment.interfaces.dto.PaymentRequest.Payment;
import io.hhplus.concert.app.payment.interfaces.dto.PaymentResponse.PaymentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "결제 API", description = "결제 관련 API")
public interface PaymentControllerDocs {
    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "결제", description = "콘서트 예약 Id를 통해 해당 예약에 대한 결제 처리")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PaymentResult.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    ApiResult<PaymentResult> payment(Payment request,
        @Parameter(hidden = true) String token);
}
