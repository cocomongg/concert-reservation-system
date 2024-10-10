package io.hhplus.concert.interfaces.api.user;

import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.user.UserResponse.ChargeUserBalance;
import io.hhplus.concert.interfaces.api.user.UserResponse.GetUserBalance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "유저 API", description = "유저 관련 API")
public interface UserControllerDocs {

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 조회", description = "userId에 해당하는 user의 잔액을 반환")
    ApiResponse<GetUserBalance> getUserBalance(Long userId, @Parameter(hidden = true) String token);

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 충전", description = "userId에 해당하는 user의 잔액을 입력한 금액만큼 충전")
    ApiResponse<ChargeUserBalance> chargeUserBalance(Long userId, UserRequest.ChargeUserBalance request,
        @Parameter(hidden = true) String token);
}
