package io.hhplus.concert.interfaces.api.member;

import io.hhplus.concert.application.member.MemberFacade;
import io.hhplus.concert.application.member.MemberPointDto.MemberPointInfo;
import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.member.MemberResponse.ChargeMemberPoint;
import io.hhplus.concert.interfaces.api.member.MemberResponse.GetMemberPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController implements MemberControllerDocs {

    private final MemberFacade memberFacade;

    @GetMapping("/{memberId}/points")
    public ApiResult<GetMemberPoint> getMemberPoint(@PathVariable Long memberId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        MemberPointInfo memberPoint = memberFacade.getMemberPoint(memberId);

        return ApiResult.OK(new GetMemberPoint(memberPoint.getPointAmount()));
    }

    @PatchMapping("/{memberId}/points")
    public ApiResult<ChargeMemberPoint> chargeMemberPoint(@PathVariable Long memberId,
        @RequestBody MemberRequest.ChargeMemberPoint request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        MemberPointInfo memberPointInfo = memberFacade.chargeMemberPoint(memberId,
            request.getAmount());

        return ApiResult.OK(new ChargeMemberPoint(memberPointInfo.getPointAmount()));
    }
}
