package io.hhplus.concert.interfaces.api.waitingqueue;

import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueRequest.CreateQueue;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.CreateQueueToken;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.GetQueue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "대기열 API", description = "대기열 관련 API")
public interface WaitingQueueControllerDocs {

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "대기열 상태 조회", description = "polling을 통해 token에 해당하는 대기열 상태 및 순번 반환")
    ApiResponse<GetQueue> GetQueue(@Parameter(hidden = true) String token);

    @Operation(summary = "대기열 진입 및 토큰 발급", description = "memberId를 통해 해당 user를 대기열 진입 시키고, 대기열 토큰 반환")
    ApiResponse<CreateQueueToken> createQueue(CreateQueue request);
}
