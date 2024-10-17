package io.hhplus.concert.interfaces.api.payment;

import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.payment.PaymentResponse.PaymentResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController implements PaymentControllerDocs{

    @PostMapping("")
    public ApiResult<PaymentResult> payment(@RequestBody PaymentRequest.Payment request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(
            PaymentResult.builder()
                .concertId(1L)
                .concertTitle("결제된 콘서트 이름")
                .seatNumber(17)
                .paymentId(1L)
                .paidAmount(10_000)
                .build()
        );
    }
}
