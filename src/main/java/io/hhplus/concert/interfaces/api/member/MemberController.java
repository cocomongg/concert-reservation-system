package io.hhplus.concert.interfaces.api.member;

import io.hhplus.concert.interfaces.api.common.response.ApiResponse;
import io.hhplus.concert.interfaces.api.member.MemberResponse.ChargeUserBalance;
import io.hhplus.concert.interfaces.api.member.MemberResponse.GetUserBalance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/members")
@RestController
public class MemberController implements MemberControllerDocs {

    @GetMapping("/{memberId}/balances")
    public ApiResponse<GetUserBalance> getMemberBalance(@PathVariable Long memberId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResponse.OK(new GetUserBalance(10_000));
    }

    @PostMapping("/{memberId}/balances")
    public ApiResponse<ChargeUserBalance> chargeMemberBalance(@PathVariable Long memberId,
        @RequestBody MemberRequest.ChargeUserBalance request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResponse.OK(new ChargeUserBalance(10_000 + request.getAmount()));
    }
}
