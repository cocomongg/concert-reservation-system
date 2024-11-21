package io.hhplus.concert.app.payment.interfaces.controller;

import io.hhplus.concert.app.payment.application.PaymentDto.PaymentInfo;
import io.hhplus.concert.app.payment.application.PaymentFacade;
import io.hhplus.concert.app.common.api.response.ApiResult;
import io.hhplus.concert.app.payment.interfaces.dto.PaymentRequest.Payment;
import io.hhplus.concert.app.payment.interfaces.dto.PaymentResponse.PaymentResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController implements PaymentControllerDocs{

    private final PaymentFacade paymentFacade;

    @PostMapping("")
    public ApiResult<PaymentResult> payment(@RequestBody Payment request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        PaymentInfo paymentInfo = paymentFacade.payment(request.getReservationId(), token,
            LocalDateTime.now());

        PaymentResult response = PaymentResult.from(paymentInfo);
        return ApiResult.OK(response);
    }
}
