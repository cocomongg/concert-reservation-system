package io.hhplus.concert.domain.waitingqueue.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueErrorCode;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WaitingQueueQueryTest {
    
    @DisplayName("GetWaitingQueueCommonQuery 생성자 테스트")
    @Nested
    class GetWaitingQueueCommandQueryConstructorTest {
        @DisplayName("token 값이 유효하지 않다면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_InvalidTokenValue() {
            // given
            String invalidToken = "";
            
            // when, then
            assertThatThrownBy(() -> new GetWaitingQueueCommonQuery(invalidToken))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_RETRIEVAL_INPUT.getMessage());
        }

        @DisplayName("token 값이 유효하면 GetWaitingQueueCommonQuery 객체가 생성된다.")
        @Test
        void should_GenerateGetWaitingQueueCommonQuery_When_ValidValue() {
            // given
            String token = "token";

            // when
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            // then
            assertThat(query).isNotNull();
            assertThat(query.getToken()).isEqualTo(token);
        }
    }
}