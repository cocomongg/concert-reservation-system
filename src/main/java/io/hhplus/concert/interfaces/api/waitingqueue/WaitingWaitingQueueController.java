package io.hhplus.concert.interfaces.api.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueRequest.CreateQueue;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.CreateQueueToken;
import io.hhplus.concert.interfaces.api.waitingqueue.WaitingQueueResponse.GetQueue;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/queues")
@RestController
public class WaitingWaitingQueueController implements WaitingQueueControllerDocs {

    @GetMapping
    public ApiResult<GetQueue> GetQueue(@RequestHeader("X-QUEUE-TOKEN") String token) {
        GetQueue mockData = GetQueue.builder()
            .order(77L)
            .remainingWaitingCount(50)
            .queueStatus(WaitingQueueStatus.WAITING.toString())
            .expiredAt(LocalDateTime.now().plusMinutes(30))
            .build();

        return ApiResult.OK(mockData);
    }

    @PostMapping("/token")
    public ApiResult<CreateQueueToken> createQueue(@RequestBody CreateQueue request) {

        CreateQueueToken mockData = CreateQueueToken.builder()
            .token("fc469731-7a49-4eba-b911-bfeec7e9b341")
            .order(10L)
            .queueStatus(WaitingQueueStatus.WAITING.toString())
            .expiredAt(LocalDateTime.now().plusMinutes(30))
            .build();

        return ApiResult.OK(mockData);
    }
}
