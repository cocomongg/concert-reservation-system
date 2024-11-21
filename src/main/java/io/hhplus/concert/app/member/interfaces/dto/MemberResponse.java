package io.hhplus.concert.app.member.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MemberResponse {

    @Getter
    @AllArgsConstructor
    public static class GetMemberPoint {
        @Schema(description = "잔액")
        private final int pointAmount;
    }

    @Getter
    @AllArgsConstructor
    public static class ChargeMemberPoint {
        @Schema(description = "충전 후 잔액")
        private final int pointAmount;
    }
}
