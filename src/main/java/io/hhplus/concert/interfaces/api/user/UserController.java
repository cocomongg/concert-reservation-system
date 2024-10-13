package io.hhplus.concert.interfaces.api.user;

import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.user.UserResponse.ChargeUserBalance;
import io.hhplus.concert.interfaces.api.user.UserResponse.GetUserBalance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/users")
@RestController
public class UserController implements UserControllerDocs {

    @GetMapping("/{userId}/balances")
    public ApiResponse<GetUserBalance> getUserBalance(@PathVariable Long userId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResponse.OK(new GetUserBalance(10_000));
    }

    @PostMapping("/{userId}/balances")
    public ApiResponse<ChargeUserBalance> chargeUserBalance(@PathVariable Long userId,
        @RequestBody UserRequest.ChargeUserBalance request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResponse.OK(new ChargeUserBalance(10_000 + request.getAmount()));
    }
}
