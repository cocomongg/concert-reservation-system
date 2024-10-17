package io.hhplus.concert.interfaces.api.member;

import io.hhplus.concert.interfaces.api.common.response.ApiErrorResponse;
import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertItem;
import io.hhplus.concert.interfaces.api.member.MemberResponse.ChargeMemberPoint;
import io.hhplus.concert.interfaces.api.member.MemberResponse.GetMemberPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "유저 API", description = "유저 관련 API")
public interface MemberControllerDocs {

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 조회", description = "memberId에 해당하는 user의 잔액을 반환")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "유저 잔액 반환 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GetMemberPoint.class))),
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
    ApiResult<GetMemberPoint> getMemberPoint(Long memberId, @Parameter(hidden = true) String token);

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "유저 잔액 충전", description = "memberId에 해당하는 user의 잔액을 입력한 금액만큼 충전")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "유저 잔액 충전 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ChargeMemberPoint.class))),
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
    ApiResult<ChargeMemberPoint> chargeMemberPoint(Long memberId, MemberRequest.ChargeMemberPoint request,
        @Parameter(hidden = true) String token);
}
