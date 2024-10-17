package io.hhplus.concert.application.member;

import io.hhplus.concert.domain.member.model.MemberPoint;
import lombok.Getter;

public class MemberPointDto {

    @Getter
    public static class MemberPointInfo {
        private final Long memberId;
        private final int pointAmount;

        public MemberPointInfo(MemberPoint memberPoint) {
            this.memberId = memberPoint.getMemberId();
            this.pointAmount = memberPoint.getPointAmount();
        }
    }
}
