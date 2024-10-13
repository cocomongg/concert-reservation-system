package io.hhplus.concert.interfaces.api.member;

import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.member.MemberResponse.ChargeUserBalance;
import io.hhplus.concert.interfaces.api.member.MemberResponse.GetUserBalance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "유저 API", description = "유저 관련 API")
public interface MemberControllerDocs {

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 조회", description = "memberId에 해당하는 user의 잔액을 반환")
    ApiResponse<GetUserBalance> getMemberBalance(Long memberId, @Parameter(hidden = true) String token);

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 충전", description = "memberId에 해당하는 user의 잔액을 입력한 금액만큼 충전")
    ApiResponse<ChargeUserBalance> chargeMemberBalance(Long memberId, MemberRequest.ChargeUserBalance request,
        @Parameter(hidden = true) String token);
}
