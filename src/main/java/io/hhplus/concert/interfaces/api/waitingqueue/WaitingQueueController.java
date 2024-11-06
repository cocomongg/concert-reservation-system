package io.hhplus.concert.interfaces.api.waitingqueue;

import io.hhplus.concert.application.waitingqueue.WaitingQueueFacade;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingTokenWithOrderInfo;
import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueRequest.CreateQueue;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.CreateQueueToken;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.GetQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/queues")
@RestController
public class WaitingQueueController implements WaitingQueueControllerDocs {

    private final WaitingQueueFacade waitingQueueFacade;

    @GetMapping("/tokens/order-info")
    public ApiResult<GetQueue> GetQueue(@RequestHeader("X-QUEUE-TOKEN") String token) {
        WaitingTokenWithOrderInfo waitingTokenWithOrderInfo =
            waitingQueueFacade.getWaitingTokenWithOrderInfo(token);

        GetQueue response = GetQueue.from(waitingTokenWithOrderInfo);
        return ApiResult.OK(response);
    }

    @PostMapping("/tokens")
    public ApiResult<CreateQueueToken> createQueue(@RequestBody CreateQueue request) {
        WaitingQueueTokenInfo tokenInfo = waitingQueueFacade.issueWaitingToken();

        CreateQueueToken response = CreateQueueToken.from(tokenInfo);
        return ApiResult.OK(response);
    }
}
