package io.hhplus.concert.interfaces.api.payment;

import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.payment.PaymentResponse.PaymentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "결제 API", description = "결제 관련 API")
public interface PaymentControllerDocs {

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "결제", description = "콘서트 예약 Id를 통해 해당 예약에 대한 결제 처리")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 성공"),
    })
    ApiResult<PaymentResult> payment(PaymentRequest.Payment request,
        @Parameter(hidden = true) String token);
}
