package io.hhplus.concert.interfaces.api.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MemberResponse {

    @Getter
    @AllArgsConstructor
    public static class GetUserBalance {
        @Schema(description = "잔액")
        private final int balanceAmount;
    }

    @Getter
    @AllArgsConstructor
    public static class ChargeUserBalance {
        @Schema(description = "충전 후 잔액")
        private final int balanceAmount;
    }
}
