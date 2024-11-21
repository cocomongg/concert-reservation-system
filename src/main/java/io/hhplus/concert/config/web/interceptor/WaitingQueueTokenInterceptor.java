package io.hhplus.concert.config.web.interceptor;

import io.hhplus.concert.app.waitingqueue.application.WaitingQueueFacade;
import io.hhplus.concert.app.common.error.CoreErrorType.WaitingQueue;
import io.hhplus.concert.app.common.error.CoreException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class WaitingQueueTokenInterceptor implements HandlerInterceptor {

    private final WaitingQueueFacade waitingQueueFacade;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {

        String token = request.getHeader("X-QUEUE-TOKEN");
        if(!StringUtils.hasText(token)) {
           throw new CoreException(WaitingQueue.QUEUE_TOKEN_MISSING);
        }

        waitingQueueFacade.checkTokenActivate(token, LocalDateTime.now());

        return true;
    }
}
