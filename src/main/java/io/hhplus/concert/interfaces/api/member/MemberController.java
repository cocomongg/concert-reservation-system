package io.hhplus.concert.interfaces.api.member;

import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.member.MemberResponse.ChargeMemberPoint;
import io.hhplus.concert.interfaces.api.member.MemberResponse.GetMemberPoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/members")
@RestController
public class MemberController implements MemberControllerDocs {

    @GetMapping("/{memberId}/points")
    public ApiResult<GetMemberPoint> getMemberPoint(@PathVariable Long memberId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(new GetMemberPoint(10_000));
    }

    @PatchMapping("/{memberId}/points")
    public ApiResult<ChargeMemberPoint> chargeMemberPoint(@PathVariable Long memberId,
        @RequestBody MemberRequest.ChargeMemberPoint request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(new ChargeMemberPoint(10_000 + request.getAmount()));
    }
}
