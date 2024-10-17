package io.hhplus.concert.interfaces.api.waitingqueue;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueFacade;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueRequest.CreateQueue;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.CreateQueueToken;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.GetQueue;
import java.time.LocalDateTime;
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

    @GetMapping
    public ApiResult<GetQueue> GetQueue(@RequestHeader("X-QUEUE-TOKEN") String token) {
        WaitingQueueWithOrderInfo waitingQueueWithOrder =
            waitingQueueFacade.getWaitingQueueWithOrder(token);

        GetQueue response = GetQueue.from(waitingQueueWithOrder);
        return ApiResult.OK(response);
    }

    @PostMapping("/token")
    public ApiResult<CreateQueueToken> createQueue(@RequestBody CreateQueue request) {
        WaitingQueueInfo waitingQueueWithOrder =
            waitingQueueFacade.generateWaitingQueueToken();

        CreateQueueToken response = CreateQueueToken.from(waitingQueueWithOrder);
        return ApiResult.OK(response);
    }
}
