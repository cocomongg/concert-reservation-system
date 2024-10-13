package io.hhplus.concert.interfaces.api.queue;

import io.hhplus.concert.domain.queue.model.WaitingQueueStatus;
import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.queue.QueueRequest.CreateQueue;
import io.hhplus.concert.interfaces.api.queue.QueueResponse.CreateQueueToken;
import io.hhplus.concert.interfaces.api.queue.QueueResponse.GetQueue;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/queues")
@RestController
public class QueueController implements QueueControllerDocs{

    @GetMapping
    public ApiResponse<GetQueue> GetQueue(@RequestHeader("X-QUEUE-TOKEN") String token) {
        GetQueue mockData = GetQueue.builder()
            .order(77L)
            .remainingWaitingCount(50)
            .queueStatus(WaitingQueueStatus.WAITED.toString())
            .expiredAt(LocalDateTime.now().plusMinutes(30))
            .build();

        return ApiResponse.OK(mockData);
    }

    @PostMapping("/token")
    public ApiResponse<CreateQueueToken> createQueue(@RequestBody CreateQueue request) {

        CreateQueueToken mockData = CreateQueueToken.builder()
            .token("fc469731-7a49-4eba-b911-bfeec7e9b341")
            .order(10L)
            .queueStatus(WaitingQueueStatus.WAITED.toString())
            .expiredAt(LocalDateTime.now().plusMinutes(30))
            .build();

        return ApiResponse.OK(mockData);
    }
}
