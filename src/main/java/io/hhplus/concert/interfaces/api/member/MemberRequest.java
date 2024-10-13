package io.hhplus.concert.interfaces.api.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeUserBalance {
        @Schema(description = "충전할 금액")
        private int amount;
    }
}
